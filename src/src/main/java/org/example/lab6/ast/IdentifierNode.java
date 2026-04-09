package org.example.lab6.ast;

/** A plain user-defined identifier (not a reserved keyword). */
public class IdentifierNode implements ASTNode {
    private final String name;

    public IdentifierNode(String name) { this.name = name; }

    public String getName() { return name; }

    @Override
    public String describe(String indent) {
        return indent + "Identifier[" + name + "]\n";
    }
}
