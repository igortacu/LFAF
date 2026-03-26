package org.example.lab4.node;

import org.example.lab4.ProcessingTracer;

import java.util.List;
import java.util.stream.Collectors;

public class ConcatNode implements RegexNode {
    private final List<RegexNode> children;

    public ConcatNode(List<RegexNode> children) {
        this.children = children;
    }

    @Override
    public String generate(ProcessingTracer tracer) {
        tracer.log("CONCAT — " + children.size() + " parts");
        StringBuilder sb = new StringBuilder();
        for (RegexNode child : children) {
            sb.append(child.generate(tracer));
        }
        return sb.toString();
    }

    @Override
    public String describe() {
        return children.stream().map(RegexNode::describe).collect(Collectors.joining());
    }
}
