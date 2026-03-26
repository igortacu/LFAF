# Finite Automata Graphs - Variant 25

This directory contains the visual representations of the NDFA and DFA for Lab 2.

## Files

- **ndfa_variant25.dot** - Graphviz DOT source for the Non-Deterministic Finite Automaton
- **ndfa_variant25.png** - Visual graph of the NDFA
- **dfa_variant25.dot** - Graphviz DOT source for the Deterministic Finite Automaton
- **dfa_variant25.png** - Visual graph of the DFA
- **generate_images.py** - Python script to generate PNG images from DOT files

## NDFA (Non-Deterministic Finite Automaton)

The NDFA for Variant 25 has:
- **States**: {q0, q1, q2, q3}
- **Alphabet**: {a, b}
- **Start state**: q0
- **Final state**: q2 (shown with double circle)
- **Non-deterministic transition**: δ(q0,a) = {q0, q1} - from q0 on 'a', can go to both q0 and q1

## DFA (Deterministic Finite Automaton)

The DFA was generated using subset construction algorithm:
- **States**: {q0}, {q0,q1}, {q0,q1,q2}, {q0,q1,q2,q3}, {q1}, {q2}, {q3}
- **Alphabet**: {a, b}
- **Start state**: {q0}
- **Final states**: {q2}, {q0,q1,q2}, {q0,q1,q2,q3} (any state containing q2)
- **Fully deterministic**: Each state-symbol pair has exactly one transition

## Regenerating Images

To regenerate the PNG images from the DOT files:

```bash
# Using Python script
python3 generate_images.py

# Or using Graphviz directly
dot -Tpng ndfa_variant25.dot -o ndfa_variant25.png
dot -Tpng dfa_variant25.dot -o dfa_variant25.png
```

## Key Observations

1. **Non-determinism**: The NDFA has a non-deterministic choice at q0 when reading 'a'
2. **Subset Construction**: The DFA states represent sets of NDFA states (e.g., {q0,q1} means "could be in q0 or q1")
3. **Equivalence**: Both automata accept exactly the same language
4. **State Explosion**: The DFA has 7 states compared to the NDFA's 4 states, demonstrating the typical state space increase
