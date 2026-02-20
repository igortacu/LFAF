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
        List<String> sortedStates = new ArrayList<>(delta.keySet());
        Collections.sort(sortedStates);
        for (String from : sortedStates) {
            Map<Character, Set<String>> trans = delta.get(from);
            List<Character> sortedSymbols = new ArrayList<>(trans.keySet());
            Collections.sort(sortedSymbols);
            for (Character sym : sortedSymbols) {
                List<String> toList = new ArrayList<>(trans.get(sym));
                Collections.sort(toList);
                for (String to : toList) {
                    System.out.println("  " + from + " --" + sym + "--> " + to);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Task 3a: Convert FA to Regular Grammar (right-linear)
    // Each transition  q --a--> p  becomes production  q -> a p
    // Transitions into a final state  q --a--> f  also get  q -> a  (terminal-only)
    // -------------------------------------------------------------------------
    public Grammar toRegularGrammar() {
        Set<String> Vn = new HashSet<>(Q);
        Set<Character> Vt = new HashSet<>(Sigma);
        Map<String, List<Grammar.Production>> P = new HashMap<>();

        for (String state : Q) {
            P.put(state, new ArrayList<>());
        }

        for (String from : delta.keySet()) {
            for (Map.Entry<Character, Set<String>> e : delta.get(from).entrySet()) {
                char symbol = e.getKey();
                for (String to : e.getValue()) {
                    // q -> a p
                    P.get(from).add(new Grammar.Production(symbol, to));
                    // if destination is a final state, also add q -> a  (accepts here)
                    if (F.contains(to)) {
                        P.get(from).add(new Grammar.Production(symbol, null));
                    }
                }
            }
        }

        return new Grammar(Vn, Vt, P, q0);
    }

    // -------------------------------------------------------------------------
    // Task 3b: Determine whether the FA is deterministic
    // An FA is a DFA iff every (state, symbol) pair has AT MOST ONE successor.
    // -------------------------------------------------------------------------
    public boolean isDeterministic() {
        for (String state : delta.keySet()) {
            for (Map.Entry<Character, Set<String>> e : delta.get(state).entrySet()) {
                if (e.getValue().size() > 1) return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Task 3c: Convert NDFA -> DFA using the subset-construction algorithm
    // -------------------------------------------------------------------------
    public FiniteAutomaton toDFA() {
        // Each DFA state represents a *set* of NFA states; we encode it as a
        // sorted, comma-joined string, e.g.  "{A,B,C}"
        Map<Set<String>, String> stateNames = new LinkedHashMap<>();
        Map<String, Map<Character, Set<String>>> dfaDelta = new LinkedHashMap<>();
        Set<String> dfaFinal = new HashSet<>();

        Set<String> startSet = new HashSet<>();
        startSet.add(q0);
        String startName = encode(startSet);
        stateNames.put(startSet, startName);

        Queue<Set<String>> worklist = new ArrayDeque<>();
        worklist.add(startSet);

        while (!worklist.isEmpty()) {
            Set<String> current = worklist.poll();
            String currentName = stateNames.get(current);
            dfaDelta.put(currentName, new HashMap<>());

            // Check if this DFA state contains any NFA final state
            for (String nfaState : current) {
                if (F.contains(nfaState)) {
                    dfaFinal.add(currentName);
                    break;
                }
            }

            for (char symbol : Sigma) {
                Set<String> reachable = new HashSet<>();
                for (String nfaState : current) {
                    Map<Character, Set<String>> trans = delta.get(nfaState);
                    if (trans != null && trans.containsKey(symbol)) {
                        reachable.addAll(trans.get(symbol));
                    }
                }
                if (reachable.isEmpty()) continue;

                if (!stateNames.containsKey(reachable)) {
                    String newName = encode(reachable);
                    stateNames.put(reachable, newName);
                    worklist.add(reachable);
                }
                String targetName = stateNames.get(reachable);
                dfaDelta.get(currentName)
                        .computeIfAbsent(symbol, k -> new HashSet<>())
                        .add(targetName);
            }
        }

        Set<String> dfaStates = new HashSet<>(stateNames.values());
        return new FiniteAutomaton(dfaStates, new HashSet<>(Sigma), dfaDelta, startName, dfaFinal);
    }

    /** Encodes a set of NFA states as a readable DFA state name, e.g. "{A,B}". */
    private static String encode(Set<String> states) {
        List<String> sorted = new ArrayList<>(states);
        Collections.sort(sorted);
        return "{" + String.join(",", sorted) + "}";
    }
}
