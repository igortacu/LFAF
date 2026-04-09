# Laboratory Work 6: Parser & Building an Abstract Syntax Tree

### Course: Formal Languages & Finite Automata
### Author: Tacu Igor

----

## Theory

**Parsing** (syntactic analysis) is the second phase of a compiler pipeline, following lexical analysis. The parser takes a flat stream of tokens produced by the lexer and verifies that they form grammatically valid sentences, building a hierarchical data structure in the process.

An **Abstract Syntax Tree (AST)** is that hierarchical structure. Unlike a concrete parse tree (which retains every token, including punctuation and grouping delimiters), an AST strips away redundant syntactic detail and keeps only the semantically meaningful nodes. Each node represents a language construct — an assignment, a binary operation, a conditional — and its children represent the sub-constructs that compose it.

A **recursive-descent parser** implements the grammar directly in code: each grammar rule becomes a method, and rules that reference other rules simply call those methods. This produces a clean, readable correspondence between the grammar specification and the implementation.

## Objectives

1. Build AST data structures suitable for the financial DSL tokenised in Lab 3.
2. Implement a recursive-descent parser that converts a token stream into an AST.
3. Demonstrate the full pipeline: source text → tokens (lexer) → AST (parser).

## Implementation description

The implementation lives in the `org.example.lab6` package. It reuses the `Lexer`, `Token`, and `TokenType` classes from `org.example.lab3` without any modifications.

### 1. Grammar

The parser is driven by the following grammar:

```
program        → statement* EOF

statement      → ifStmt
               | assignment SEMICOLON
               | expression SEMICOLON

assignment     → lhsName ASSIGN expression
lhsName        → INCOME | EXPENSE | TAX | PROFIT | LOSS
               | SAVE | INVEST | BUDGET | IDENTIFIER

ifStmt         → IF expression THEN expression
                 (ELSE expression)? SEMICOLON

expression     → comparison
comparison     → addition (compOp addition)?
addition       → multiplication ((PLUS | MINUS) multiplication)*
multiplication → unary ((MULTIPLY | DIVIDE) unary)*
unary          → MINUS unary | primary
primary        → LPAREN expression RPAREN
               | callExpr
               | number
               | IDENTIFIER
               | keyword (as a value)

callExpr       → (SAVE | INVEST | BUDGET) LPAREN argList? RPAREN
argList        → expression (COMMA expression)*
number         → (INTEGER | FLOAT) PERCENT?
compOp         → GREATER | LESS | GREATER_EQUAL | LESS_EQUAL
               | EQUAL_EQUAL | NOT_EQUAL
```

Key design decisions:
- All domain keywords (`income`, `expense`, …) can appear on the LHS of an assignment.
- `save`, `invest`, `budget` are function calls **only** when immediately followed by `(`; otherwise they are value references.
- An if-statement's branches are parsed as expressions, so the single trailing `;` terminates the whole conditional.
- Operator precedence is encoded by the call hierarchy: comparison < addition < multiplication < unary < primary.

### 2. AST Node Hierarchy (`ast/`)

All nodes implement the `ASTNode` interface:

```java
public interface ASTNode {
    String describe(String indent);
}
```

| Node class      | Represents                                        |
|-----------------|---------------------------------------------------|
| `ProgramNode`   | Root — a list of top-level statements             |
| `AssignNode`    | `name = expression`                               |
| `IfNode`        | `if cond then expr [else expr]`                   |
| `BinaryOpNode`  | Infix arithmetic or comparison (`+`, `>`, …)      |
| `UnaryOpNode`   | Prefix negation (`-expr`)                         |
| `NumberNode`    | Integer or float literal, optionally with `%`     |
| `IdentifierNode`| User-defined name                                 |
| `KeywordNode`   | Domain keyword used as a value (`income`, …)      |
| `CallNode`      | Function call with argument list (`save(expr, …)`) |

Each node's `describe(String indent)` method returns an indented textual representation that forms a readable tree when printed.

### 3. Parser (`Parser.java`)

The parser is a standard recursive-descent implementation:

```java
public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public ProgramNode parse() {
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.EOF)) stmts.add(parseStatement());
        return new ProgramNode(stmts);
    }
    ...
}
```

**Statement dispatch** uses one token of look-ahead to distinguish the three statement forms:

```java
private ASTNode parseStatement() {
    if (check(TokenType.IF)) return parseIf();

    // (keyword | IDENTIFIER) followed by '=' → assignment
    if ((ANY_KEYWORD.contains(peek().getType()) || check(TokenType.IDENTIFIER))
            && peekAhead(1).getType() == TokenType.ASSIGN) {
        return parseAssignment();
    }
    // Otherwise: expression statement
    ASTNode expr = parseExpression();
    consume(TokenType.SEMICOLON, "Expected ';'");
    return expr;
}
```

**If-statement** parsing keeps the grammar simple: branches are plain expressions, the semicolon belongs to the whole construct:

```java
private ASTNode parseIf() {
    consume(TokenType.IF,   "Expected 'if'");
    ASTNode condition  = parseExpression();
    consume(TokenType.THEN, "Expected 'then'");
    ASTNode thenBranch = parseExpression();
    ASTNode elseBranch = null;
    if (check(TokenType.ELSE)) { advance(); elseBranch = parseExpression(); }
    consume(TokenType.SEMICOLON, "Expected ';'");
    return new IfNode(condition, thenBranch, elseBranch);
}
```

**Expression hierarchy** mirrors operator precedence directly in the call chain:

```java
private ASTNode parseAddition() {
    ASTNode left = parseMultiplication();
    while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
        Token op  = advance();
        left = new BinaryOpNode(op.getLexeme(), left, parseMultiplication());
    }
    return left;
}
```

**Primary parsing** handles the keyword-vs-call ambiguity:

```java
private ASTNode parsePrimary() {
    // Function call when a callable keyword precedes '('
    if (CALL_KEYWORD.contains(peek().getType())
            && peekAhead(1).getType() == TokenType.LPAREN) {
        return parseCall();
    }
    // Same keyword used as a plain value reference
    if (ANY_KEYWORD.contains(peek().getType())) {
        return new KeywordNode(advance().getLexeme());
    }
    ...
}
```

**Percentage literals** are collapsed into `NumberNode` at parse time:

```java
private ASTNode parseNumber() {
    Token num = advance();
    boolean pct = false;
    if (check(TokenType.PERCENT)) { advance(); pct = true; }
    return new NumberNode(num.getLexeme(), pct);
}
```

### 4. Demo program (`Main.java`)

The demo runs the complete pipeline on an 8-statement financial program:

```
income  = 5000;
expense = 2000;
tax     = income * 15%;
profit  = income - expense - tax;
if profit > 1000 then save(profit) else invest(profit);
budget  = income - expense;
loss    = expense - income;
if loss > 0 then invest(loss * 50%);
```

## Conclusions / Screenshots / Results

Running `org.example.lab6.Main` produces:

```
── Abstract Syntax Tree ──
Program
  Assign[income]
    Number[5000]
  Assign[expense]
    Number[2000]
  Assign[tax]
    BinaryOp[*]
      Keyword[income]
      Number[15%]
  Assign[profit]
    BinaryOp[-]
      BinaryOp[-]
        Keyword[income]
        Keyword[expense]
      Keyword[tax]
  If
    [condition]
      BinaryOp[>]
        Keyword[profit]
        Number[1000]
    [then]
      Call[save]
        Keyword[profit]
    [else]
      Call[invest]
        Keyword[profit]
  Assign[budget]
    BinaryOp[-]
      Keyword[income]
      Keyword[expense]
  Assign[loss]
    BinaryOp[-]
      Keyword[expense]
      Keyword[income]
  If
    [condition]
      BinaryOp[>]
        Keyword[loss]
        Number[0]
    [then]
      Call[invest]
        BinaryOp[*]
          Keyword[loss]
          Number[50%]

Parsed 8 top-level statement(s). ✓
```

Key observations:

1. **Operator precedence is preserved.** `income - expense - tax` becomes a left-associative `BinaryOp[-]` tree (subtraction of `(income - expense)` and then `tax`), matching the expected evaluation order.

2. **Percentage literals are folded.** `15%` and `50%` appear as `Number[15%]` and `Number[50%]` — a single AST node — rather than two separate nodes.

3. **Call vs. value disambiguation.** `save` and `invest` appear as `Call[…]` nodes inside the if-statement branches (because they're followed by `(`), while `profit` and `loss` appear as `Keyword[…]` value nodes.

4. **The full pipeline is end-to-end.** The Lab 3 `Lexer` is reused unchanged; the `Parser` sits above it consuming the token list; the AST nodes sit above the parser. Each layer has a single, well-defined responsibility.

## References

- Cretu Dumitru, Vasile Drumea, Irina Cojuhari — Course materials, LFAF
- [Parsing — Wikipedia](https://en.wikipedia.org/wiki/Parsing)
- [Abstract Syntax Tree — Wikipedia](https://en.wikipedia.org/wiki/Abstract_syntax_tree)
- Aho, A. V., Lam, M. S., Sethi, R., & Ullman, J. D. (2006). *Compilers: Principles, Techniques, and Tools* (2nd ed.)
