package org.example.lab2;

import org.example.FiniteAutomaton;
import org.example.Grammar;

import java.util.*;

/**
 * Lab 2 – Determinism in Finite Automata. Conversion from NDFA to DFA. Chomsky Hierarchy.
 *
 * Variant 25 FA (from variant.txt):
 *   Q  = {q0, q1, q2, q3}
 *   Σ  = {a, b}
 *   F  = {q2}
 *   q0 = q0
 *   δ(q0,a) = q0
 *   δ(q0,a) = q1   <- non-deterministic
 *   δ(q1,a) = q2
 *   δ(q1,b) = q1
 *   δ(q2,a) = q3
 *   δ(q3,a) = q1
 */
public class Main {

    /** Build the exact Variant 25 NDFA from variant.txt */
    private static FiniteAutomaton buildVariant25NDFA() {
        Set<String> Q     = new HashSet<>(Arrays.asList("q0", "q1", "q2", "q3"));
        Set<Character> Sigma = new HashSet<>(Arrays.asList('a', 'b'));
        Map<String, Map<Character, Set<String>>> delta = new HashMap<>();

        // q0 --a--> q0  AND  q0 --a--> q1  (NDFA!)
        delta.put("q0", new HashMap<>());
        delta.get("q0").put('a', new HashSet<>(Arrays.asList("q0", "q1")));

        // q1 --a--> q2,  q1 --b--> q1
        delta.put("q1", new HashMap<>());
        delta.get("q1").put('a', new HashSet<>(Collections.singletonList("q2")));
        delta.get("q1").put('b', new HashSet<>(Collections.singletonList("q1")));

        // q2 --a--> q3
        delta.put("q2", new HashMap<>());
        delta.get("q2").put('a', new HashSet<>(Collections.singletonList("q3")));

        // q3 --a--> q1
        delta.put("q3", new HashMap<>());
        delta.get("q3").put('a', new HashSet<>(Collections.singletonList("q1")));

        Set<String> F = new HashSet<>(Collections.singletonList("q2"));
        return new FiniteAutomaton(Q, Sigma, delta, "q0", F);
    }

    public static void main(String[] args) {

        // =====================================================================
        // 1. Chomsky Hierarchy classification of the Variant 25 grammar (Lab 1)
        // =====================================================================
        System.out.println("========================================");
        System.out.println(" CHOMSKY HIERARCHY CLASSIFICATION");
        System.out.println("========================================");

        Grammar g = Grammar.variant25();
        String chomsky = g.classifyChomskyHierarchy();
        System.out.println("Variant 25 grammar is: " + chomsky);

        // =====================================================================
        // 2. The Variant 25 NDFA (from variant.txt)
        // =====================================================================
        System.out.println("\n========================================");
        System.out.println(" VARIANT 25 NDFA");
        System.out.println("========================================");

        FiniteAutomaton ndfa = buildVariant25NDFA();
        ndfa.printTransitions();

        // =====================================================================
        // 3b. Determinism check
        // =====================================================================
        System.out.println("\n========================================");
        System.out.println(" 3b. DETERMINISM CHECK");
        System.out.println("========================================");

        System.out.println("Is deterministic: " + ndfa.isDeterministic());
        System.out.println("Reason: δ(q0,a) = {q0, q1} — one input leads to two states.");

        // =====================================================================
        // 3a. Convert NDFA -> Regular Grammar
        // =====================================================================
        System.out.println("\n========================================");
        System.out.println(" 3a. NDFA -> REGULAR GRAMMAR");
        System.out.println("========================================");

        Grammar regGrammar = ndfa.toRegularGrammar();
        System.out.println("Start symbol: " + regGrammar.getStartSymbol());
        System.out.println("Productions:");
        // Collect and sort for readable output
        List<String> productions = new ArrayList<>();
        for (Map.Entry<String, List<Grammar.Production>> e : regGrammar.getP().entrySet()) {
            for (Grammar.Production p : e.getValue()) {
                String rhs = (p.nonterminal == null)
                        ? String.valueOf(p.terminal)
                        : String.valueOf(p.terminal) + p.nonterminal;
                productions.add("  " + e.getKey() + " -> " + rhs);
            }
        }
        Collections.sort(productions);
        productions.forEach(System.out::println);

        // =====================================================================
        // 3c. NDFA -> DFA (subset construction)
        // =====================================================================
        System.out.println("\n========================================");
        System.out.println(" 3c. NDFA -> DFA  (subset construction)");
        System.out.println("========================================");

        FiniteAutomaton dfa = ndfa.toDFA();
        System.out.println("Resulting DFA transitions:");
        dfa.printTransitions();
        System.out.println("Is deterministic: " + dfa.isDeterministic());

        // Verify equivalence: NDFA and DFA must accept the same strings
        System.out.println("\nEquivalence check (NDFA vs DFA):");
        String[] tests = {"aa", "aaa", "aba", "abaa", "a", "b", "bb", "aab", "aaba", "aaaa"};
        System.out.printf("  %-10s %-8s %-8s%n", "String", "NDFA", "DFA");
        for (String t : tests) {
            boolean ndfa_res = ndfa.stringBelongToLanguage(t);
            boolean dfa_res  = dfa.stringBelongToLanguage(t);
            System.out.printf("  %-10s %-8s %-8s %s%n",
                    t, ndfa_res, dfa_res,
                    ndfa_res == dfa_res ? "OK" : "MISMATCH");
        }
    }
}

