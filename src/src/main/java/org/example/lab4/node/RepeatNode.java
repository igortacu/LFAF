package org.example.lab4.node;

import org.example.lab4.ProcessingTracer;

import java.util.Random;

public class RepeatNode implements RegexNode {
    private final RegexNode child;
    private final int min;
    private final int max;
    private static final Random RANDOM = new Random();

    public RepeatNode(RegexNode child, int min, int max) {
        this.child = child;
        this.min = min;
        this.max = max;
    }

    @Override
    public String generate(ProcessingTracer tracer) {
        int count = (min == max) ? min : min + RANDOM.nextInt(max - min + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(child.generate(tracer));
        }
        String result = sb.toString();
        String quantifierLabel = quantifierLabel();
        tracer.log("REPEAT(" + quantifierLabel + ", " + min + ".." + max + ") — count=" + count + " → \"" + result + "\"");
        return result;
    }

    private String quantifierLabel() {
        if (min == 0 && max == 1) return "?";
        if (min == 0) return "*";
        if (min == 1) return "+";
        return "^" + min;
    }

    @Override
    public String describe() {
        return child.describe();
    }
}
