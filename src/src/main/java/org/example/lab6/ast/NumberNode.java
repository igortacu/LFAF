package org.example.lab6.ast;

/** Numeric literal — integer or float, optionally followed by {@code %}. */
public class NumberNode implements ASTNode {
    private final String  value;
    private final boolean isPercent;

    public NumberNode(String value, boolean isPercent) {
        this.value     = value;
        this.isPercent = isPercent;
    }

    public String  getValue()     { return value; }
    public boolean isPercent()    { return isPercent; }

    @Override
    public String describe(String indent) {
        return indent + "Number[" + value + (isPercent ? "%" : "") + "]\n";
    }
}
