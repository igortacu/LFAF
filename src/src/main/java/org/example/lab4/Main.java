package org.example.lab4;

import org.example.lab4.node.RegexNode;

public class Main {
    private static final String[] REGEXES = {
        "(a|b)(c|d)E+G?",
        "P(Q|R|S)T(UV|W|X)*Z+",
        "1(0|1)*2(3|4)^5 36"
    };

    public static void main(String[] args) {
        RegexGenerator generator = new RegexGenerator();

        for (int i = 0; i < REGEXES.length; i++) {
            String regex = REGEXES[i];
            System.out.println("=".repeat(60));
            System.out.println("Regex " + (i + 1) + ": " + regex);
            System.out.println("=".repeat(60));

            RegexNode ast = new RegexParser(regex).parse();

            for (int sample = 1; sample <= 5; sample++) {
                System.out.println("\n--- Sample " + sample + " ---");
                ProcessingTracer tracer = new ProcessingTracer();
                String result = generator.generate(ast, tracer);
                tracer.printTrace(result);
            }
            System.out.println();
        }
    }
}
