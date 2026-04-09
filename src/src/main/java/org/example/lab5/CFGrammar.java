package org.example.lab5;

import java.util.*;

/**
 * Context-free grammar representation for CNF conversion.
 * Productions are stored as: NT -> list of RHS alternatives,
 * where each RHS is a list of symbols (terminals or non-terminals).
 * An empty RHS list represents an epsilon production.
 */
public class CFGrammar {
    private final Set<String> nonTerminals;
    private final Set<String> terminals;
    private final Map<String, List<List<String>>> productions;
    private final String startSymbol;

    public CFGrammar(Set<String> nonTerminals, Set<String> terminals,
                     Map<String, List<List<String>>> productions, String startSymbol) {
        this.nonTerminals = new LinkedHashSet<>(nonTerminals);
        this.terminals = new LinkedHashSet<>(terminals);
        this.productions = new LinkedHashMap<>();
        for (Map.Entry<String, List<List<String>>> e : productions.entrySet()) {
            List<List<String>> alts = new ArrayList<>();
            for (List<String> rhs : e.getValue()) {
                alts.add(new ArrayList<>(rhs));
            }
            this.productions.put(e.getKey(), alts);
        }
        this.startSymbol = startSymbol;
    }

    public Set<String> getNonTerminals() { return Collections.unmodifiableSet(nonTerminals); }
    public Set<String> getTerminals()    { return Collections.unmodifiableSet(terminals); }
    public Map<String, List<List<String>>> getProductions() { return Collections.unmodifiableMap(productions); }
    public String getStartSymbol()       { return startSymbol; }

    public void print(String title) {
        System.out.println("=== " + title + " ===");
        System.out.println("VN = " + nonTerminals);
        System.out.println("VT = " + terminals);
        System.out.println("S  = " + startSymbol);
        System.out.println("Productions:");

        // Sort for deterministic output
        List<String> lhsList = new ArrayList<>(productions.keySet());
        // start symbol first
        lhsList.remove(startSymbol);
        lhsList.add(0, startSymbol);

        for (String lhs : lhsList) {
            List<List<String>> alts = productions.get(lhs);
            if (alts == null || alts.isEmpty()) continue;
            for (List<String> rhs : alts) {
                String rhsStr = rhs.isEmpty() ? "ε" : String.join("", rhs);
                System.out.println("  " + lhs + " → " + rhsStr);
            }
        }
        System.out.println();
    }

    /**
     * Variant 25:
     * VN={S,A,B,C,D}, VT={a,b}, S=S
     * 1. S→bA    2. S→BC    3. A→a    4. A→aS    5. A→bCaCa
     * 6. B→A     7. B→bS    8. B→bCAa 9. C→ε     10. C→AB   11. D→AB
     */
    public static CFGrammar variant25() {
        Set<String> vn = new LinkedHashSet<>(Arrays.asList("S", "A", "B", "C", "D"));
        Set<String> vt = new LinkedHashSet<>(Arrays.asList("a", "b"));

        Map<String, List<List<String>>> p = new LinkedHashMap<>();
        p.put("S", Arrays.asList(
            Arrays.asList("b", "A"),               // S → bA
            Arrays.asList("B", "C")                // S → BC
        ));
        p.put("A", Arrays.asList(
            Collections.singletonList("a"),        // A → a
            Arrays.asList("a", "S"),               // A → aS
            Arrays.asList("b", "C", "a", "C", "a") // A → bCaCa
        ));
        p.put("B", Arrays.asList(
            Collections.singletonList("A"),        // B → A
            Arrays.asList("b", "S"),               // B → bS
            Arrays.asList("b", "C", "A", "a")      // B → bCAa
        ));
        p.put("C", Arrays.asList(
            Collections.emptyList(),               // C → ε
            Arrays.asList("A", "B")                // C → AB
        ));
        p.put("D", Collections.singletonList(
            Arrays.asList("A", "B")                // D → AB
        ));

        return new CFGrammar(vn, vt, p, "S");
    }
}
