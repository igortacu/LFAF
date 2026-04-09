package org.example.lab6.ast;

import java.util.List;

/** Function call — {@code save(args)}, {@code invest(args)}, {@code budget(args)}. */
public class CallNode implements ASTNode {
    private final String        callee;
    private final List<ASTNode> args;

    public CallNode(String callee, List<ASTNode> args) {
        this.callee = callee;
        this.args   = args;
    }

    public String        getCallee() { return callee; }
    public List<ASTNode> getArgs()   { return args; }

    @Override
    public String describe(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Call[" + callee + "]\n");
        for (ASTNode arg : args) {
            sb.append(arg.describe(indent + "  "));
        }
        return sb.toString();
    }
}
