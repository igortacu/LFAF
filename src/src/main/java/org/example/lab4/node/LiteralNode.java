package org.example.lab4.node;

import org.example.lab4.ProcessingTracer;

public class LiteralNode implements RegexNode {
    private final char c;

    public LiteralNode(char c) {
        this.c = c;
    }

    @Override
    public String generate(ProcessingTracer tracer) {
        return String.valueOf(c);
    }

    @Override
    public String describe() {
        return String.valueOf(c);
    }
}
