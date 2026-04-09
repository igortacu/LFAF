package org.example.lab6;

import org.example.lab3.Lexer;
import org.example.lab3.Token;
import org.example.lab6.ast.ProgramNode;

import java.util.List;

public class Main {

    // A multi-statement financial program used as the demo input.
    private static final String SAMPLE = """
            income = 5000;
            expense = 2000;
            tax = income * 15%;
            profit = income - expense - tax;
            if profit > 1000 then save(profit) else invest(profit);
            budget = income - expense;
            loss = expense - income;
            if loss > 0 then invest(loss * 50%);
            """;

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("  Laboratory Work 6: Parser & AST");
        System.out.println("=".repeat(60));
        System.out.println();

        // ── Step 1: Lex ──────────────────────────────────────────────
        System.out.println("── Input program ──");
        System.out.println(SAMPLE);

        Lexer lexer = new Lexer(SAMPLE);
        List<Token> tokens = lexer.tokenize();

        System.out.println("── Token stream ──");
        tokens.forEach(System.out::println);
        System.out.println();

        // ── Step 2: Parse ─────────────────────────────────────────────
        Parser parser = new Parser(tokens);
        ProgramNode ast = parser.parse();

        // ── Step 3: Print AST ─────────────────────────────────────────
        System.out.println("── Abstract Syntax Tree ──");
        System.out.print(ast.describe(""));

        System.out.println();
        System.out.println("Parsed " + ast.getStatements().size() + " top-level statement(s). ✓");
    }
}
