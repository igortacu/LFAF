package org.example.lab4.node;

import org.example.lab4.ProcessingTracer;

public interface RegexNode {
    String generate(ProcessingTracer tracer);

    /** Human-readable description of this node for trace output (no side effects). */
    String describe();
}
