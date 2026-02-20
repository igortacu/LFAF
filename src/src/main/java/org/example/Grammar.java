package org.example;

import java.util.*;

// 25 variant
// G = [Vn, Vt, P, S]
public class Grammar {
    public static class Production {
        public  final char terminal;
        public final String nonterminal;
        public Production(char terminal, String nonterminal) {
            this.terminal = terminal;
            this.nonterminal = nonterminal;
        }
        @Override
        public String toString() {
            return nonterminal == null ? String.valueOf(terminal) : ("" + terminal + "->" + nonterminal);
        }

    }
    private final Set<String> Vn;
    private final Set<Character> Vt;
    private final Map<String, List<Production>> P;
    private final String startSymbol;

    private final Random random = new Random();

    public Grammar(Set<String> Vn, Set<Character> Vt, Map<String, List<Production>> P, String startSymbol){
        this.Vn = Vn;
        this.Vt = Vt;
        this.P = P;
        for (Map.Entry<String, List<Production>> e : P.entrySet()) {
            this.P.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
        this.startSymbol = startSymbol;
    }


    public static Grammar variant25(){
        Set<String> Vn = new HashSet<>(Arrays.asList("S", "A", "B"));
        Set<Character> Vt = new HashSet<>(Arrays.asList('a', 'b', 'c', 'd'));

        Map<String, List<Production>> P = new HashMap<>();
        P.put("S", Arrays.asList(
                new Production('b', "S"),
                new Production('d', "A"))
        );
        P.put("A", Arrays.asList(
                new Production('a', "A"),
                new Production('d', "B"),
                new Production('b', null)
        ));
        P.put("B", Arrays.asList(
                new Production('c', "B"),
                new Production('a', null)
        ));
        return new Grammar(Vn, Vt, P, "S");
    }

    public  String getStartSymbol() {
        return startSymbol;
    }

    public Map<String, List<Production>> getP() {
        return Collections.unmodifiableMap(P);
    }

    public String generateSentence(){
        final int MAX_STEPS = 60;
        String current = startSymbol;
        StringBuilder sb = new StringBuilder();

        int steps = 0;
        while(current != null){
            steps++;
            if(steps > MAX_STEPS) return null;
            List<Production> options = P.get(current);
            if (options == null || options.isEmpty()) {
                // No production rules -> invalid grammar state
                return null;
            }

            Production chosen = chooseWithBias(current, options, steps);

            sb.append(chosen.terminal);
            current = chosen.nonterminal;

        }

        return sb.toString();
    }

    // prefer productions that terminate or move forward

    private Production chooseWithBias(String nonterminal, List<Production> options, int steps) {
        // copy ot avoid mutating
        List<Production> copy = new ArrayList<>(options);
        boolean preferEnd = steps > 20;

        if(preferEnd){
            // try to find an ending production first, so nonTerminal is null
           List<Production> endings = copy.stream().filter(p -> p.nonterminal == null).toList();
           if(endings.isEmpty() && random.nextDouble() < 0.65){
               return endings.get(random.nextInt(endings.size()));
           }

        }
        return copy.get(random.nextInt(options.size()));
    }

    // Classify the grammar according to Chomsky Hierarchy:
    // Type 0 – Unrestricted
    // Type 1 – Context-Sensitive  (|lhs| <= |rhs|, except S -> ε when S not in any rhs)
    // Type 2 – Context-Free       (all lhs are single non-terminals)
    // Type 3 – Regular            (all rhs are of the form tA or t, i.e. right-linear)
    public String classifyChomskyHierarchy() {
        boolean isType3 = true;
        boolean isType2 = true;
        boolean isType1 = true;

        for (Map.Entry<String, List<Production>> entry : P.entrySet()) {
            String lhs = entry.getKey();

            // Type 2 / Type 3 require single non-terminal on the left
            if (lhs.length() != 1 || !Vn.contains(lhs)) {
                isType2 = false;
                isType3 = false;
            }

            for (Production prod : entry.getValue()) {
                // Build the full right-hand side string for length checks
                String rhs = prod.nonterminal == null
                        ? String.valueOf(prod.terminal)
                        : String.valueOf(prod.terminal) + prod.nonterminal;

                // Type 1: |lhs| <= |rhs| (epsilon allowed only for start symbol)
                boolean isEpsilon = rhs.isEmpty();
                if (isEpsilon && !lhs.equals(startSymbol)) {
                    isType1 = false;
                } else if (!isEpsilon && lhs.length() > rhs.length()) {
                    isType1 = false;
                }

                // Type 3 (right-linear): rhs is tA or just t
                // Our Production already enforces exactly this shape, so this holds
                // unless lhs is not a single non-terminal (checked above)
            }
        }

        if (isType3 && isType2) return "Type 3 - Regular Grammar";
        if (isType2)            return "Type 2 - Context-Free Grammar";
        if (isType1)            return "Type 1 - Context-Sensitive Grammar";
        return                         "Type 0 - Unrestricted Grammar";
    }

    // convert regular expression to finite automaton
    // mapping goes as follows: x -> tY becomes transition x-> t -> Y
    // x -> t   becomes transition x - t-> X (Final state)

    public FiniteAutomaton toAutomaton(){
        Set<String> states = new HashSet<>(Vn);
        String finalState = "X";
        states.add(finalState);

        Set<Character> alphabet = new HashSet<>(Vt);
        Map<String, Map<Character, Set<String>>> delta = new HashMap<>();
        for(String state : states){
            delta.put(state, new HashMap<>());
        }
        for (Map.Entry<String, List<Production>> e : P.entrySet()) {
            String from = e.getKey();
            for(Production prod : e.getValue()){
                char symbol = prod.terminal;
                String to = (prod.nonterminal == null) ? finalState : prod.nonterminal;
                delta.get(from).computeIfAbsent(symbol, k -> new HashSet<>()).add(to);
            }
        }

        Set<String> accepting = new HashSet<>();
        accepting.add(finalState);

        return new FiniteAutomaton(states, alphabet, delta, startSymbol, accepting);
    }
}
