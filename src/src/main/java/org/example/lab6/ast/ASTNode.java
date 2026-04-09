package org.example.lab6.ast;

/** Base interface for every node in the Abstract Syntax Tree. */
public interface ASTNode {
    /**
     * Returns a human-readable, indented representation of this node
     * and all its children.
     *
     * @param indent prefix whitespace for this node's line
     */
    String describe(String indent);
}
