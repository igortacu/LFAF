package org.example.lab4;

import java.util.ArrayList;
import java.util.List;

public class ProcessingTracer {
    private final List<String> steps = new ArrayList<>();

    public void log(String message) {
        steps.add(message);
    }

    public void printTrace(String result) {
        for (int i = 0; i < steps.size(); i++) {
            System.out.println("Step " + (i + 1) + ": " + steps.get(i));
        }
        System.out.println("Result: " + result);
    }

    public void clear() {
        steps.clear();
    }
}
