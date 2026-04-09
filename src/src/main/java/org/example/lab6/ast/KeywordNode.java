package org.example.lab6.ast;

/**
 * A domain keyword used as a value expression — e.g. {@code income},
 * {@code expense}, {@code tax}, {@code profit}, {@code loss},
 * {@code save}, {@code invest}, {@code budget}.
 */
public class KeywordNode implements ASTNode {
    private final String keyword;

    public KeywordNode(String keyword) { this.keyword = keyword; }

    public String getKeyword() { return keyword; }

    @Override
    public String describe(String indent) {
        return indent + "Keyword[" + keyword + "]\n";
    }
}
