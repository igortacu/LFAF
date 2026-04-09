package org.example.lab5;

import java.util.*;

/**
 * Converts a Context-Free Grammar to Chomsky Normal Form (CNF).
 *
 * Steps (per the assignment):
 *   1. Eliminate ε-productions
 *   2. Eliminate unit productions (renaming)
 *   3. Eliminate inaccessible symbols
 *   4. Eliminate non-productive symbols
 *   5. Convert to proper CNF (A→BC or A→a)
 */
public class CNFConverter {

    private Set<String> nonTerminals;
    private Set<String> terminals;
    private Map<String, List<List<String>>> productions;
    private String startSymbol;
    private int freshCounter = 0;

    public CNFConverter(CFGrammar grammar) {
        this.nonTerminals = new LinkedHashSet<>(grammar.getNonTerminals());
        this.terminals    = new LinkedHashSet<>(grammar.getTerminals());
        this.productions  = deepCopy(grammar.getProductions());
        this.startSymbol  = grammar.getStartSymbol();
    }

    // -----------------------------------------------------------------------
    // Step 1: Eliminate ε-productions
    // -----------------------------------------------------------------------
    public CFGrammar eliminateEpsilon() {
        // 1a. Find all nullable non-terminals
        Set<String> nullable = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, List<List<String>>> e : productions.entrySet()) {
                if (nullable.contains(e.getKey())) continue;
                for (List<String> rhs : e.getValue()) {
                    if (rhs.isEmpty() || nullable.containsAll(rhs)) {
                        nullable.add(e.getKey());
                        changed = true;
                        break;
                    }
                }
            }
        }

        // 1b. For every production, add all non-empty subsets obtained by removing nullable symbols
        Map<String, List<List<String>>> newProductions = new LinkedHashMap<>();
        for (Map.Entry<String, List<List<String>>> e : productions.entrySet()) {
            String lhs = e.getKey();
            Set<List<String>> newAlts = new LinkedHashSet<>();

            for (List<String> rhs : e.getValue()) {
                if (rhs.isEmpty()) continue; // drop ε-productions; they'll be recreated via combinations

                // Positions in rhs that are nullable
                List<Integer> nullPos = new ArrayList<>();
                for (int i = 0; i < rhs.size(); i++) {
                    if (nullable.contains(rhs.get(i))) nullPos.add(i);
                }

                // Iterate over all 2^|nullPos| subsets: bit=1 means "omit this position"
                int combos = 1 << nullPos.size();
                for (int mask = 0; mask < combos; mask++) {
                    List<String> newRhs = new ArrayList<>();
                    for (int i = 0; i < rhs.size(); i++) {
                        int idx = nullPos.indexOf(i);
                        boolean omit = idx >= 0 && ((mask >> idx & 1) == 1);
                        if (!omit) newRhs.add(rhs.get(i));
                    }
                    if (!newRhs.isEmpty()) newAlts.add(newRhs); // never add new ε-production
                }
            }
            newProductions.put(lhs, new ArrayList<>(newAlts));
        }

        productions = newProductions;
        return snapshot();
    }

    // -----------------------------------------------------------------------
    // Step 2: Eliminate unit productions  (A → B where B ∈ VN)
    // -----------------------------------------------------------------------
    public CFGrammar eliminateUnitProductions() {
        // Compute the set of NTs reachable from each NT via unit steps only
        Map<String, Set<String>> reach = new LinkedHashMap<>();
        for (String nt : nonTerminals) {
            reach.put(nt, new LinkedHashSet<>(Collections.singleton(nt)));
        }

        // Direct unit edges
        for (Map.Entry<String, List<List<String>>> e : productions.entrySet()) {
            for (List<String> rhs : e.getValue()) {
                if (rhs.size() == 1 && nonTerminals.contains(rhs.get(0))) {
                    reach.get(e.getKey()).add(rhs.get(0));
                }
            }
        }

        // Transitive closure
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String a : nonTerminals) {
                Set<String> ra = reach.get(a);
                Set<String> toAdd = new LinkedHashSet<>();
                for (String b : new ArrayList<>(ra)) {
                    toAdd.addAll(reach.get(b));
                }
                if (ra.addAll(toAdd)) changed = true;
            }
        }

        // Build new productions: for each A, collect all non-unit prods of every NT reachable from A
        Map<String, List<List<String>>> newProductions = new LinkedHashMap<>();
        for (String nt : nonTerminals) {
            Set<List<String>> newAlts = new LinkedHashSet<>();
            for (String reachable : reach.get(nt)) {
                List<List<String>> rhsList = productions.get(reachable);
                if (rhsList == null) continue;
                for (List<String> rhs : rhsList) {
                    boolean isUnit = rhs.size() == 1 && nonTerminals.contains(rhs.get(0));
                    if (!isUnit) newAlts.add(new ArrayList<>(rhs));
                }
            }
            newProductions.put(nt, new ArrayList<>(newAlts));
        }

        productions = newProductions;
        return snapshot();
    }

    // -----------------------------------------------------------------------
    // Step 3: Eliminate inaccessible symbols
    // -----------------------------------------------------------------------
    public CFGrammar eliminateInaccessible() {
        Set<String> reachable = new LinkedHashSet<>();
        Queue<String> queue = new LinkedList<>();
        reachable.add(startSymbol);
        queue.add(startSymbol);

        while (!queue.isEmpty()) {
            String nt = queue.poll();
            List<List<String>> alts = productions.get(nt);
            if (alts == null) continue;
            for (List<String> rhs : alts) {
                for (String sym : rhs) {
                    if (nonTerminals.contains(sym) && reachable.add(sym)) {
                        queue.add(sym);
                    }
                }
            }
        }

        nonTerminals.retainAll(reachable);
        productions.keySet().retainAll(reachable);

        return snapshot();
    }

    // -----------------------------------------------------------------------
    // Step 4: Eliminate non-productive symbols
    // -----------------------------------------------------------------------
    public CFGrammar eliminateNonProductive() {
        // A symbol is productive if it can derive a string of terminals
        Set<String> productive = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, List<List<String>>> e : productions.entrySet()) {
                if (productive.contains(e.getKey())) continue;
                for (List<String> rhs : e.getValue()) {
                    boolean allProd = rhs.stream()
                        .allMatch(sym -> terminals.contains(sym) || productive.contains(sym));
                    if (allProd) {
                        productive.add(e.getKey());
                        changed = true;
                        break;
                    }
                }
            }
        }

        Set<String> nonProd = new HashSet<>(nonTerminals);
        nonProd.removeAll(productive);
        nonTerminals.removeAll(nonProd);
        nonProd.forEach(productions::remove);

        // Remove productions that reference non-productive symbols
        for (List<List<String>> alts : productions.values()) {
            alts.removeIf(rhs -> rhs.stream().anyMatch(nonProd::contains));
        }

        return snapshot();
    }

    // -----------------------------------------------------------------------
    // Step 5: Convert to proper CNF  (A → BC  or  A → a)
    // -----------------------------------------------------------------------
    public CFGrammar toCNF() {
        // Phase 5a: For multi-symbol RHS, replace each terminal t with a new NT T_t → t
        Map<String, String> termToNT = new LinkedHashMap<>();

        // Scan (snapshot values to avoid CME if productions is modified)
        for (List<List<String>> alts : new ArrayList<>(productions.values())) {
            for (List<String> rhs : alts) {
                if (rhs.size() < 2) continue;
                for (String sym : rhs) {
                    if (terminals.contains(sym) && !termToNT.containsKey(sym)) {
                        String ntName = "T" + sym.toUpperCase();
                        while (nonTerminals.contains(ntName)) ntName += "_";
                        nonTerminals.add(ntName);
                        termToNT.put(sym, ntName);
                    }
                }
            }
        }

        // Register T_t → t productions (after the scan, so no CME)
        for (Map.Entry<String, String> e : termToNT.entrySet()) {
            productions.put(e.getValue(), new ArrayList<>(
                Collections.singletonList(Collections.singletonList(e.getKey()))
            ));
        }

        // Replace terminals in multi-symbol RHS (snapshot keySet to avoid CME)
        for (String lhs : new ArrayList<>(productions.keySet())) {
            List<List<String>> alts = productions.get(lhs);
            List<List<String>> newAlts = new ArrayList<>();
            for (List<String> rhs : alts) {
                if (rhs.size() < 2) { newAlts.add(rhs); continue; }
                List<String> newRhs = new ArrayList<>();
                for (String sym : rhs) newRhs.add(termToNT.getOrDefault(sym, sym));
                newAlts.add(newRhs);
            }
            productions.put(lhs, newAlts);
        }

        // Phase 5b: Binarize — reduce any RHS of 3+ symbols to binary rules.
        // freshNT modifies `productions`, so we snapshot keySet each iteration.
        boolean anyLong = true;
        while (anyLong) {
            anyLong = false;
            for (String lhs : new ArrayList<>(productions.keySet())) {
                List<List<String>> alts = productions.get(lhs);
                if (alts == null) continue;
                List<List<String>> newAlts = new ArrayList<>();
                for (List<String> rhs : alts) {
                    if (rhs.size() <= 2) {
                        newAlts.add(rhs);
                    } else {
                        anyLong = true;
                        // A → X1 X2 … Xn  →  A → X1 Xnew,  Xnew → X2 … Xn
                        String fresh = freshNT("X"); // puts fresh→[] into productions
                        List<String> rest = new ArrayList<>(rhs.subList(1, rhs.size()));
                        productions.get(fresh).add(rest);
                        newAlts.add(Arrays.asList(rhs.get(0), fresh));
                    }
                }
                productions.put(lhs, newAlts);
            }
        }

        return snapshot();
    }

    // -----------------------------------------------------------------------
    // Full conversion pipeline
    // -----------------------------------------------------------------------
    public CFGrammar convert() {
        System.out.println("Step 1: Eliminating ε-productions");
        eliminateEpsilon().print("After Step 1 — Eliminate ε-productions");

        System.out.println("Step 2: Eliminating unit productions (renaming)");
        eliminateUnitProductions().print("After Step 2 — Eliminate unit productions");

        System.out.println("Step 3: Eliminating inaccessible symbols");
        eliminateInaccessible().print("After Step 3 — Eliminate inaccessible symbols");

        System.out.println("Step 4: Eliminating non-productive symbols");
        eliminateNonProductive().print("After Step 4 — Eliminate non-productive symbols");

        System.out.println("Step 5: Converting to Chomsky Normal Form");
        CFGrammar result = toCNF();
        result.print("Final — Chomsky Normal Form");

        return result;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private String freshNT(String prefix) {
        String name;
        do {
            name = prefix + (++freshCounter);
        } while (nonTerminals.contains(name) || terminals.contains(name));
        nonTerminals.add(name);
        productions.put(name, new ArrayList<>());
        return name;
    }

    private CFGrammar snapshot() {
        return new CFGrammar(nonTerminals, terminals, productions, startSymbol);
    }

    private static Map<String, List<List<String>>> deepCopy(Map<String, List<List<String>>> orig) {
        Map<String, List<List<String>>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<List<String>>> e : orig.entrySet()) {
            List<List<String>> alts = new ArrayList<>();
            for (List<String> rhs : e.getValue()) {
                alts.add(new ArrayList<>(rhs));
            }
            copy.put(e.getKey(), alts);
        }
        return copy;
    }
}
