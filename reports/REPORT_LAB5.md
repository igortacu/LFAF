# Laboratory Work 5: Chomsky Normal Form

### Course: Formal Languages & Finite Automata
### Author: Tacu Igor

----

## Theory

**Chomsky Normal Form (CNF)** is a restricted way of writing a context-free grammar in which every production rule has one of exactly two forms:

- **A → BC** — the right-hand side is exactly two non-terminals
- **A → a** — the right-hand side is exactly one terminal

Any context-free language (except the empty language or languages containing only ε) can be expressed by a grammar in CNF. Converting to CNF is useful because it enables efficient parsing (e.g., the CYK algorithm runs in O(n³) on CNF grammars) and simplifies theoretical proofs.

The standard conversion procedure consists of five cleanup steps applied in sequence:

1. **Eliminate ε-productions** — remove all rules of the form A → ε by propagating "nullability" into every rule that references A.
2. **Eliminate unit productions (renaming)** — remove rules of the form A → B (where B is a single non-terminal) by replacing them with all the non-unit productions reachable from B.
3. **Eliminate inaccessible symbols** — remove any non-terminal (and its rules) that cannot be reached from the start symbol.
4. **Eliminate non-productive symbols** — remove any non-terminal that can never derive a string of terminals.
5. **Obtain CNF** — replace terminals in long rules with dedicated non-terminals (T_t → t) and binarize any rule with three or more symbols by introducing new intermediate non-terminals.

## Objectives

1. Understand the theory behind Chomsky Normal Form.
2. Implement each of the five normalization steps as a separate method.
3. The implementation must accept any context-free grammar, not only variant 25.
4. Execute and verify the result.

## Implementation description

Two classes are introduced in the `org.example.lab5` package:

### 1. CFGrammar (CFGrammar.java)

A general context-free grammar representation:

```java
public class CFGrammar {
    private final Set<String> nonTerminals;
    private final Set<String> terminals;
    private final Map<String, List<List<String>>> productions; // NT → list of RHS alternatives
    private final String startSymbol;
    ...
}
```

Each production's right-hand side is a `List<String>` (a list of symbol names). An empty list represents an ε-production. The class also provides a static `variant25()` factory that encodes the grammar from the assignment:

```
G = ({S, A, B, C, D}, {a, b}, P, S)
P = {
  1. S → bA      2. S → BC      3. A → a       4. A → aS
  5. A → bCaCa   6. B → A       7. B → bS      8. B → bCAa
  9. C → ε      10. C → AB     11. D → AB
}
```

### 2. CNFConverter (CNFConverter.java)

The converter holds a mutable copy of the grammar and exposes one method per normalization step, plus a `convert()` pipeline that runs all five steps and prints intermediate states.

#### Step 1 — Eliminate ε-productions

```java
public CFGrammar eliminateEpsilon() {
    // Find nullable set via fixed-point iteration
    Set<String> nullable = new HashSet<>();
    boolean changed = true;
    while (changed) {
        changed = false;
        for (var e : productions.entrySet()) {
            for (var rhs : e.getValue()) {
                if (rhs.isEmpty() || nullable.containsAll(rhs)) {
                    nullable.add(e.getKey()); changed = true; break;
                }
            }
        }
    }
    // For each production, add all 2^k non-empty subsets (omitting nullable positions)
    ...
}
```

The nullable set is computed with a fixed-point loop. For variant 25, only **C** is nullable (`C → ε`). Every production referencing C receives new alternatives where C is optionally removed. The original ε-rule is dropped.

**Result:** `S → B` (from `S → BC` with C removed) is added; `C → ε` is removed; several extra A and B alternatives are generated.

#### Step 2 — Eliminate unit productions

```java
public CFGrammar eliminateUnitProductions() {
    // Build unit-reachability via transitive closure
    Map<String, Set<String>> reach = ...; // each NT → set of NTs reachable by unit steps
    // For A: add all non-unit productions of every reachable NT
    ...
}
```

Unit pairs found: `(S, B)` and `(B, A)` — transitively `(S, A)` as well. After this step, `S` and `B` inherit all concrete (non-unit) productions of both `A` and `B`; the unit rules `S → B` and `B → A` are eliminated.

#### Step 3 — Eliminate inaccessible symbols

A BFS from the start symbol S finds all reachable non-terminals. **D** is unreachable (it only appears as a LHS, never in any RHS reachable from S), so it is removed along with its production `D → AB`.

```java
public CFGrammar eliminateInaccessible() {
    Set<String> reachable = new LinkedHashSet<>();
    Queue<String> queue = new LinkedList<>();
    reachable.add(startSymbol); queue.add(startSymbol);
    while (!queue.isEmpty()) {
        String nt = queue.poll();
        for (var rhs : productions.get(nt))
            for (String sym : rhs)
                if (nonTerminals.contains(sym) && reachable.add(sym)) queue.add(sym);
    }
    nonTerminals.retainAll(reachable);
    productions.keySet().retainAll(reachable);
    ...
}
```

#### Step 4 — Eliminate non-productive symbols

A symbol is productive if it can derive at least one string of terminals. The fixed-point starts with all NTs whose rules consist entirely of terminals, then propagates. After steps 1–3, all remaining symbols (S, A, B, C) are already productive, so this step makes no changes.

#### Step 5 — Obtain CNF

Two sub-phases:

**5a — Terminal wrapping.** Any terminal appearing in a rule with two or more symbols is replaced by a new non-terminal. Two wrappers are created:

| New NT | Production |
|--------|------------|
| `TB`   | `TB → b`   |
| `TA`   | `TA → a`   |

**5b — Binarization.** Any rule with three or more symbols is split right-recursively:

```
A → X1 X2 X3 … Xn
     becomes
A → X1 Xnew,  Xnew → X2 X3 … Xn
```

This repeats (with fresh NT names X1, X2, …) until every rule has at most two symbols on the right. For example:

```
S → TB C A TA          (length 4)
  → S → TB X1,  X1 → C X17,  X17 → A TA
```

The final grammar has 30 helper non-terminals (X1–X30) plus TB and TA.

### Verification

`Main.java` checks every production of the final grammar:

```java
boolean ok = (rhs.size() == 1 && cnf.getTerminals().contains(rhs.get(0)))
          || (rhs.size() == 2 && cnf.getNonTerminals().containsAll(rhs));
```

Output: **All productions are in CNF. ✓**

## Conclusions / Screenshots / Results

Running `org.example.lab5.Main` produces the following intermediate grammars:

**After Step 1 (ε-elimination):**
- C → ε is removed; `S → B` is added; A gains `baCa`, `bCaa`, `baa`; B gains `bAa`.

**After Step 2 (unit elimination):**
- `S → B` and `B → A` removed; S and B inherit all non-unit productions of A and B.

**After Step 3 (inaccessible removal):**
- D (and `D → AB`) removed. VN shrinks from {S,A,B,C,D} to {S,A,B,C}.

**After Step 4 (non-productive removal):**
- No changes — all remaining symbols are productive.

**After Step 5 (CNF):**
- Two terminal non-terminals (TB, TA) and 30 helper non-terminals (X1–X30) introduced.
- Every production is either `A → a` or `A → BC`. Verified programmatically.

The five-step algorithm correctly transforms an arbitrary CFG into CNF while preserving the generated language. The implementation is general — passing any CFG to `CNFConverter` will produce a correct CNF equivalent.

## References

- Cretu Dumitru, Vasile Drumea, Irina Cojuhari — Course materials, LFAF
- [Chomsky Normal Form — Wikipedia](https://en.wikipedia.org/wiki/Chomsky_normal_form)
- Sipser, M. (2012). *Introduction to the Theory of Computation*, 3rd ed.
- Hopcroft, J., Motwani, R., Ullman, J. (2006). *Introduction to Automata Theory, Languages, and Computation*, 3rd ed.
