package org.example.lab4;

import org.example.lab4.node.RegexNode;

public class RegexGenerator {
    public String generate(RegexNode root, ProcessingTracer tracer) {
        return root.generate(tracer);
    }
}
