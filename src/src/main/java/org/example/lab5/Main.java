package org.example.lab5;

public class Main {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("  Laboratory Work 5: Chomsky Normal Form");
        System.out.println("  Variant 25");
        System.out.println("=".repeat(60));
        System.out.println();

        CFGrammar original = CFGrammar.variant25();
        original.print("Original Grammar (Variant 25)");

        CNFConverter converter = new CNFConverter(original);
        converter.convert();

        // --- Verify CNF property on the result ---
        System.out.println("=".repeat(60));
        System.out.println("  CNF Verification");
        System.out.println("=".repeat(60));
        CNFConverter verifier = new CNFConverter(original);
        CFGrammar cnf = verifier.convert();

        boolean valid = true;
        for (var entry : cnf.getProductions().entrySet()) {
            for (var rhs : entry.getValue()) {
                boolean ok = (rhs.size() == 1 && cnf.getTerminals().contains(rhs.get(0)))
                          || (rhs.size() == 2 && cnf.getNonTerminals().containsAll(rhs));
                if (!ok) {
                    System.out.println("  NOT CNF: " + entry.getKey() + " → " + rhs);
                    valid = false;
                }
            }
        }
        if (valid) System.out.println("  All productions are in CNF. ✓");
    }
}
