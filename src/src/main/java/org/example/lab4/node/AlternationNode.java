package org.example.lab4.node;

import org.example.lab4.ProcessingTracer;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AlternationNode implements RegexNode {
    private final List<RegexNode> children;
    private static final Random RANDOM = new Random();

    public AlternationNode(List<RegexNode> children) {
        this.children = children;
    }

    @Override
    public String generate(ProcessingTracer tracer) {
        int index = RANDOM.nextInt(children.size());
        RegexNode chosen = children.get(index);
        String result = chosen.generate(tracer);
        String options = children.stream().map(RegexNode::describe).collect(Collectors.joining(", "));
        tracer.log("ALTERNATION — chose '" + result + "' from [" + options + "]");
        return result;
    }

    @Override
    public String describe() {
        return children.get(0).describe();
    }
}
