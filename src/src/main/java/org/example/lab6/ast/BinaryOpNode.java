package org.example.lab6.ast;

/** Binary infix operation: left op right (arithmetic or comparison). */
public class BinaryOpNode implements ASTNode {
    private final String  op;
    private final ASTNode left;
    private final ASTNode right;

    public BinaryOpNode(String op, ASTNode left, ASTNode right) {
        this.op    = op;
        this.left  = left;
        this.right = right;
    }

    public String  getOp()    { return op; }
    public ASTNode getLeft()  { return left; }
    public ASTNode getRight() { return right; }

    @Override
    public String describe(String indent) {
        return indent + "BinaryOp[" + op + "]\n"
             + left.describe(indent  + "  ")
             + right.describe(indent + "  ");
    }
}
