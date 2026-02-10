package org.example;

import java.util.*;

 // Finite Automaton: FA = (Q, Sigma, delta, q0, F)
public class FiniteAutomaton {

    private final Set<String> Q; // states
    private final Set<Character> Sigma; // alphabet
    private final Map<String, Map<Character, Set<String>>> delta; // transitions
    private final String q0; // start state
    private final Set<String> F; // final states

    public FiniteAutomaton(Set<String> Q,
                           Set<Character> Sigma,
                           Map<String, Map<Character, Set<String>>> delta,
                           String q0,
                           Set<String> F) {
        this.Q = new HashSet<>(Q);
        this.Sigma = new HashSet<>(Sigma);

        // deep copy transitions
        this.delta = new HashMap<>();
        for (String s : delta.keySet()) {
            Map<Character, Set<String>> inner = new HashMap<>();
            for (Map.Entry<Character, Set<String>> e : delta.get(s).entrySet()) {
                inner.put(e.getKey(), new HashSet<>(e.getValue()));
            }
            this.delta.put(s, inner);
        }

        this.q0 = q0;
        this.F = new HashSet<>(F);
    }

    // return true if accepts the string
    public boolean stringBelongToLanguage(final String inputString) {
        Set<String> current = new HashSet<>();
        current.add(q0);

        for (int i = 0; i < inputString.length(); i++) {
            char ch = inputString.charAt(i);

            // If ch is not in alphabet -> reject early
            if (!Sigma.contains(ch)) return false;

            Set<String> next = new HashSet<>();
            for (String st : current) {
                Map<Character, Set<String>> trans = delta.get(st);
                if (trans == null) continue;

                Set<String> toStates = trans.get(ch);
                if (toStates != null) next.addAll(toStates);
            }

            if (next.isEmpty()) return false;
            current = next;
        }

        // accept if any current state is final
        for (String st : current) {
            if (F.contains(st)) return true;
        }
        return false;
    }

    public void printTransitions() {
        System.out.println("Transitions:");
        for (String from : delta.keySet()) {
            Map<Character, Set<String>> trans = delta.get(from);
            for (Map.Entry<Character, Set<String>> e : trans.entrySet()) {
                char sym = e.getKey();
                for (String to : e.getValue()) {
                    System.out.println("  " + from + " --" + sym + "--> " + to);
                }
            }
        }
    }
}
