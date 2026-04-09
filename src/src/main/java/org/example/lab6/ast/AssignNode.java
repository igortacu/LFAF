package org.example.lab6.ast;

/** Assignment statement: {@code name = expression ;} */
public class AssignNode implements ASTNode {
    private final String name;   // the keyword or identifier on the left-hand side
    private final ASTNode value; // the right-hand side expression

    public AssignNode(String name, ASTNode value) {
        this.name  = name;
        this.value = value;
    }

    public String getName()   { return name; }
    public ASTNode getValue() { return value; }

    @Override
    public String describe(String indent) {
        return indent + "Assign[" + name + "]\n"
             + value.describe(indent + "  ");
    }
}
