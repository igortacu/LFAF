# Laboratory Work 1: Introduction to Formal Languages, Regular Grammars, and Finite Automata

### Course: Formal Languages & Finite Automata
### Author: Tacu Igor

----

## Theory

A formal language is a set of strings formed from an alphabet according to specific rules defined by a grammar. A **grammar** consists of:
- **Vn** (non-terminal symbols): Variables that can be expanded
- **Vt** (terminal symbols): The actual characters in the output
- **P** (production rules): Rules for transforming non-terminals into terminals/non-terminals
- **S** (start symbol): The initial non-terminal

A **Finite Automaton** is a mathematical model of computation consisting of:
- **Q** (states): A finite set of states
- **Σ** (alphabet): Input symbols
- **δ** (transition function): Rules for moving between states
- **q0** (initial state): Starting state
- **F** (final states): Accepting states

Regular grammars can be converted to finite automata, as both describe regular languages.

## Objectives:

* Understand formal languages and grammars
* Implement a Grammar class that can generate valid strings from variant 25
* Generate 5 valid strings from the grammar
* Convert the Grammar to a Finite Automaton
* Implement string membership testing for the Finite Automaton

## Implementation description

### Grammar Class (Grammar.java:30-38)
The Grammar class stores the four components of a formal grammar: non-terminals (Vn), terminals (Vt), production rules (P), and the start symbol. The variant 25 grammar has productions: S→bS|dA, A→aA|dB|b, B→cB|a.

```java
public Grammar(Set<String> Vn, Set<Character> Vt, Map<String, List<Production>> P, String startSymbol){
    this.Vn = Vn;
    this.Vt = Vt;
    this.P = P;
    for (Map.Entry<String, List<Production>> e : P.entrySet()) {
        this.P.put(e.getKey(), new ArrayList<>(e.getValue()));
    }
    this.startSymbol = startSymbol;
}
```

### String Generation (Grammar.java:70-93)
The `generateSentence()` method uses recursive production rule application to generate valid strings. It starts from the start symbol and randomly selects production rules until reaching a terminal production (where nonterminal is null). A step limit prevents infinite loops, and bias is applied to prefer terminating productions after 20 steps.

```java
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
            return null;
        }

        Production chosen = chooseWithBias(current, options, steps);

        sb.append(chosen.terminal);
        current = chosen.nonterminal;
    }

    return sb.toString();
}
```

### Grammar to Finite Automaton Conversion (Grammar.java:117-140)
The conversion maps each non-terminal to a state. Productions of form X→tY become transitions from state X to Y on symbol t. Productions X→t (terminal only) transition to a final state X. This creates an NFA that accepts the same language as the grammar.

```java
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
```

### String Membership Testing (FiniteAutomaton.java:37-65)
The `stringBelongToLanguage()` method simulates the NFA by maintaining a set of possible current states. For each input character, it computes all reachable next states from all current states. The string is accepted if any final state is reached after processing all characters.

```java
public boolean stringBelongToLanguage(final String inputString) {
    Set<String> current = new HashSet<>();
    current.add(q0);

    for (int i = 0; i < inputString.length(); i++) {
        char ch = inputString.charAt(i);

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

    for (String st : current) {
        if (F.contains(st)) return true;
    }
    return false;
}
```

## Conclusions / Screenshots / Results

The implementation successfully demonstrates the relationship between regular grammars and finite automata. The Grammar class generates valid strings following variant 25's production rules, producing strings like "db", "bdaab", "ddca", and "bddca".

The conversion to a Finite Automaton preserves the language's structure - the automaton correctly accepts strings generated by the grammar and rejects invalid strings. Testing confirmed that "db", "bdaab", "ddca", and "bddca" are accepted, while "ca", "da", "b", and "bdca" are correctly rejected.

This demonstrates that regular grammars and finite automata are equivalent in their expressive power for regular languages.

## References

* Course materials by Cretu Dumitru, with kudos to Vasile Drumea and Irina Cojuhari
* Formal Languages and Automata Theory concepts