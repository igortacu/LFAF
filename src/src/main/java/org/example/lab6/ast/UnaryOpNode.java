package org.example.lab6.ast;

/** Unary prefix operation, e.g. negation: {@code -expr}. */
public class UnaryOpNode implements ASTNode {
    private final String  op;
    private final ASTNode operand;

    public UnaryOpNode(String op, ASTNode operand) {
        this.op      = op;
        this.operand = operand;
    }

    public String  getOp()      { return op; }
    public ASTNode getOperand() { return operand; }

    @Override
    public String describe(String indent) {
        return indent + "UnaryOp[" + op + "]\n"
             + operand.describe(indent + "  ");
    }
}
