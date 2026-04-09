package org.example.lab6.ast;

import java.util.List;

/** Root node — holds an ordered list of top-level statements. */
public class ProgramNode implements ASTNode {
    private final List<ASTNode> statements;

    public ProgramNode(List<ASTNode> statements) {
        this.statements = statements;
    }

    public List<ASTNode> getStatements() { return statements; }

    @Override
    public String describe(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Program\n");
        for (ASTNode stmt : statements) {
            sb.append(stmt.describe(indent + "  "));
        }
        return sb.toString();
    }
}
