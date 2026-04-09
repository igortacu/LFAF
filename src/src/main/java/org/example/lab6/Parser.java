package org.example.lab6;

import org.example.lab3.Token;
import org.example.lab3.TokenType;
import org.example.lab6.ast.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Recursive-descent parser for the financial DSL defined in Lab 3.
 *
 * Grammar (simplified BNF):
 * <pre>
 *   program        → statement* EOF
 *   statement      → ifStmt
 *                  | assignment SEMICOLON
 *                  | expression SEMICOLON
 *   assignment     → lhsName ASSIGN expression
 *   lhsName        → any keyword | IDENTIFIER
 *   ifStmt         → IF expression THEN expression (ELSE expression)? SEMICOLON
 *   expression     → comparison
 *   comparison     → addition (compOp addition)?
 *   addition       → multiplication ((PLUS | MINUS) multiplication)*
 *   multiplication → unary ((MULTIPLY | DIVIDE) unary)*
 *   unary          → MINUS unary | primary
 *   primary        → LPAREN expression RPAREN
 *                  | callExpr
 *                  | number
 *                  | IDENTIFIER
 *                  | keyword (used as a value)
 *   callExpr       → (SAVE | INVEST | BUDGET) LPAREN argList? RPAREN
 *   argList        → expression (COMMA expression)*
 *   number         → (INTEGER | FLOAT) PERCENT?
 *   compOp         → GREATER | LESS | GREATER_EQUAL | LESS_EQUAL | EQUAL_EQUAL | NOT_EQUAL
 * </pre>
 */
public class Parser {

    // Token types that represent domain keywords usable as LHS of an assignment
    private static final Set<TokenType> ANY_KEYWORD = EnumSet.of(
        TokenType.INCOME, TokenType.EXPENSE, TokenType.TAX,
        TokenType.PROFIT, TokenType.LOSS,
        TokenType.SAVE, TokenType.INVEST, TokenType.BUDGET
    );

    // Keywords that double as callable functions when followed by '('
    private static final Set<TokenType> CALL_KEYWORD = EnumSet.of(
        TokenType.SAVE, TokenType.INVEST, TokenType.BUDGET
    );

    // Comparison operator token types
    private static final Set<TokenType> COMP_OPS = EnumSet.of(
        TokenType.GREATER, TokenType.LESS,
        TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL,
        TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL
    );

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // -----------------------------------------------------------------------
    // Public entry point
    // -----------------------------------------------------------------------

    public ProgramNode parse() {
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            stmts.add(parseStatement());
        }
        return new ProgramNode(stmts);
    }

    // -----------------------------------------------------------------------
    // Statements
    // -----------------------------------------------------------------------

    private ASTNode parseStatement() {
        // if-then-else
        if (check(TokenType.IF)) {
            return parseIf();
        }

        // Assignment: (keyword | IDENTIFIER) followed immediately by '='
        if ((ANY_KEYWORD.contains(peek().getType()) || check(TokenType.IDENTIFIER))
                && peekAhead(1).getType() == TokenType.ASSIGN) {
            return parseAssignment();
        }

        // Expression statement
        ASTNode expr = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression");
        return expr;
    }

    private ASTNode parseAssignment() {
        Token lhs = advance(); // keyword or IDENTIFIER
        consume(TokenType.ASSIGN, "Expected '=' after '" + lhs.getLexeme() + "'");
        ASTNode value = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after assignment");
        return new AssignNode(lhs.getLexeme(), value);
    }

    /**
     * ifStmt → IF expression THEN expression (ELSE expression)? SEMICOLON
     * The branches are expressions (which can include calls); the trailing
     * semicolon belongs to the whole if-statement.
     */
    private ASTNode parseIf() {
        consume(TokenType.IF,   "Expected 'if'");
        ASTNode condition  = parseExpression();
        consume(TokenType.THEN, "Expected 'then'");
        ASTNode thenBranch = parseExpression();
        ASTNode elseBranch = null;
        if (check(TokenType.ELSE)) {
            advance();
            elseBranch = parseExpression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after if-statement");
        return new IfNode(condition, thenBranch, elseBranch);
    }

    // -----------------------------------------------------------------------
    // Expressions — precedence levels (low to high)
    // -----------------------------------------------------------------------

    private ASTNode parseExpression() {
        return parseComparison();
    }

    /** comparison → addition (compOp addition)? */
    private ASTNode parseComparison() {
        ASTNode left = parseAddition();
        if (COMP_OPS.contains(peek().getType())) {
            Token op    = advance();
            ASTNode right = parseAddition();
            return new BinaryOpNode(op.getLexeme(), left, right);
        }
        return left;
    }

    /** addition → multiplication ((PLUS | MINUS) multiplication)* */
    private ASTNode parseAddition() {
        ASTNode left = parseMultiplication();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            Token op    = advance();
            ASTNode right = parseMultiplication();
            left = new BinaryOpNode(op.getLexeme(), left, right);
        }
        return left;
    }

    /** multiplication → unary ((MULTIPLY | DIVIDE) unary)* */
    private ASTNode parseMultiplication() {
        ASTNode left = parseUnary();
        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE)) {
            Token op    = advance();
            ASTNode right = parseUnary();
            left = new BinaryOpNode(op.getLexeme(), left, right);
        }
        return left;
    }

    /** unary → MINUS unary | primary */
    private ASTNode parseUnary() {
        if (check(TokenType.MINUS)) {
            Token op = advance();
            return new UnaryOpNode(op.getLexeme(), parseUnary());
        }
        return parsePrimary();
    }

    // -----------------------------------------------------------------------
    // Primaries
    // -----------------------------------------------------------------------

    private ASTNode parsePrimary() {
        Token t = peek();

        // Grouped expression
        if (t.getType() == TokenType.LPAREN) {
            advance();
            ASTNode expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')'");
            return expr;
        }

        // Function call: save/invest/budget followed by '('
        if (CALL_KEYWORD.contains(t.getType())
                && peekAhead(1).getType() == TokenType.LPAREN) {
            return parseCall();
        }

        // Numeric literal (integer or float), optionally followed by %
        if (t.getType() == TokenType.INTEGER || t.getType() == TokenType.FLOAT) {
            return parseNumber();
        }

        // Domain keyword used as a value reference (income, expense, tax, etc.)
        if (ANY_KEYWORD.contains(t.getType())) {
            return new KeywordNode(advance().getLexeme());
        }

        // Plain identifier
        if (t.getType() == TokenType.IDENTIFIER) {
            return new IdentifierNode(advance().getLexeme());
        }

        throw new ParseException("Unexpected token: '" + t.getLexeme()
                + "' (" + t.getType() + ") at line " + t.getLine());
    }

    /** callExpr → callKeyword LPAREN argList? RPAREN */
    private ASTNode parseCall() {
        Token callee = advance(); // SAVE | INVEST | BUDGET
        consume(TokenType.LPAREN, "Expected '(' after '" + callee.getLexeme() + "'");
        List<ASTNode> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            args.add(parseExpression());
            while (check(TokenType.COMMA)) {
                advance();
                args.add(parseExpression());
            }
        }
        consume(TokenType.RPAREN, "Expected ')'");
        return new CallNode(callee.getLexeme(), args);
    }

    /** number → (INTEGER | FLOAT) PERCENT? */
    private ASTNode parseNumber() {
        Token num = advance();
        boolean isPercent = false;
        if (check(TokenType.PERCENT)) {
            advance();
            isPercent = true;
        }
        return new NumberNode(num.getLexeme(), isPercent);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Token peek() {
        return tokens.get(pos);
    }

    private Token peekAhead(int offset) {
        int idx = pos + offset;
        return idx < tokens.size() ? tokens.get(idx) : tokens.get(tokens.size() - 1);
    }

    private Token advance() {
        if (!check(TokenType.EOF)) pos++;
        return tokens.get(pos - 1);
    }

    private boolean check(TokenType type) {
        return peek().getType() == type;
    }

    private Token consume(TokenType type, String message) {
        if (!check(type)) {
            throw new ParseException(message + " — got '"
                    + peek().getLexeme() + "' at line " + peek().getLine());
        }
        return advance();
    }

    // -----------------------------------------------------------------------

    /** Thrown when the input does not conform to the grammar. */
    public static class ParseException extends RuntimeException {
        public ParseException(String message) { super(message); }
    }
}
