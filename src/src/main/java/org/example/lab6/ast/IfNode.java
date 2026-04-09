package org.example.lab6.ast;

/** Conditional statement: {@code if condition then expr [else expr] ;} */
public class IfNode implements ASTNode {
    private final ASTNode condition;
    private final ASTNode thenBranch;
    private final ASTNode elseBranch; // nullable

    public IfNode(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch) {
        this.condition  = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public ASTNode getCondition()  { return condition; }
    public ASTNode getThenBranch() { return thenBranch; }
    public ASTNode getElseBranch() { return elseBranch; }

    @Override
    public String describe(String indent) {
        StringBuilder sb = new StringBuilder(indent + "If\n");
        sb.append(indent).append("  [condition]\n")
          .append(condition.describe(indent + "    "));
        sb.append(indent).append("  [then]\n")
          .append(thenBranch.describe(indent + "    "));
        if (elseBranch != null) {
            sb.append(indent).append("  [else]\n")
              .append(elseBranch.describe(indent + "    "));
        }
        return sb.toString();
    }
}
