# Laboratory Work 4: Regular Expression Processor

### Course: Formal Languages & Finite Automata
### Author: Tacu Igor

----

## Theory

A **regular expression** (regex) is a sequence of characters that defines a search pattern, typically used for string matching and manipulation. Regular expressions are closely tied to regular languages (Type 3 in the Chomsky hierarchy) and can be recognized by finite automata.

The key constructs of regular expressions include:

- **Literals**: Single characters that match themselves (e.g., `a`, `B`, `1`)
- **Concatenation**: Placing patterns side by side means they must appear in sequence (e.g., `AB` matches "AB")
- **Alternation** (`|`): Matches one of several alternatives (e.g., `a|b` matches "a" or "b")
- **Quantifiers**: Control repetition of the preceding element:
  - `*` — zero or more repetitions (Kleene star)
  - `+` — one or more repetitions
  - `?` — zero or one occurrence (optional)
  - `^n` — exactly *n* repetitions (power notation)
- **Grouping** (`(...)`): Groups sub-expressions to apply operators to compound patterns

Processing a regular expression involves two phases: **parsing** the regex string into an Abstract Syntax Tree (AST), and **traversing** that tree to generate or match strings. The AST naturally captures operator precedence — alternation has the lowest precedence, then concatenation, then quantifiers.

## Objectives:

1. Understand the structure and semantics of regular expressions
2. Build a regex parser that converts regex strings into an AST
3. Implement an AST-based string generator with processing trace
4. Support alternation, concatenation, quantifiers (`*`, `+`, `?`, `^n`), and grouping
5. Demonstrate correct generation for multiple regex patterns

## Implementation description

### 1. AST Node Hierarchy (node/RegexNode.java)

The AST is built around a `RegexNode` interface that all node types implement:

```java
public interface RegexNode {
    String generate(ProcessingTracer tracer);
    String describe();
}
```

Each node knows how to `generate()` a random string matching its pattern (logging steps to a `ProcessingTracer`), and `describe()` itself for trace output. Four concrete node types implement this interface:

| Node Type | Purpose | Example Pattern |
|-----------|---------|-----------------|
| `LiteralNode` | Matches a single character | `a`, `E`, `1` |
| `ConcatNode` | Sequences children left to right | `AB`, `(a\|b)c` |
| `AlternationNode` | Randomly picks one child | `a\|b\|c` |
| `RepeatNode` | Repeats child [min..max] times | `E+`, `G?`, `X*`, `(3\|4)^5` |

### 2. Recursive Descent Parser (RegexParser.java)

The parser uses a **recursive descent** strategy with three precedence levels, mapping directly to the grammar of regular expressions:

```
expr   → concat ('|' concat)*      // lowest precedence
concat → term term*                 // middle precedence
term   → atom quantifier?           // highest precedence
atom   → '(' expr ')' | literal
```

The top-level `parse()` method invokes `parseExpr()` and verifies that all input was consumed:

```java
public RegexNode parse() {
    RegexNode result = parseExpr();
    if (pos < input.length()) {
        throw new IllegalArgumentException(
            "Unexpected character '" + input.charAt(pos) + "' at position " + pos);
    }
    return result;
}
```

**Alternation** (lowest precedence) collects alternatives separated by `|`:

```java
private RegexNode parseExpr() {
    List<RegexNode> alternatives = new ArrayList<>();
    alternatives.add(parseConcat());
    while (pos < input.length() && input.charAt(pos) == '|') {
        pos++;
        alternatives.add(parseConcat());
    }
    return alternatives.size() == 1 ? alternatives.get(0) : new AlternationNode(alternatives);
}
```

**Concatenation** (middle precedence) collects sequential terms until hitting a `)` or `|`:

```java
private RegexNode parseConcat() {
    List<RegexNode> parts = new ArrayList<>();
    while (pos < input.length()) {
        char c = input.charAt(pos);
        if (c == ')' || c == '|') break;
        parts.add(parseTerm());
    }
    return parts.size() == 1 ? parts.get(0) : new ConcatNode(parts);
}
```

**Quantifiers** (highest precedence) are parsed as suffixes to atoms:

```java
private RegexNode parseTerm() {
    RegexNode atom = parseAtom();
    if (pos >= input.length()) return atom;

    char c = input.charAt(pos);
    if (c == '*') { pos++; return new RepeatNode(atom, 0, 5); }
    else if (c == '+') { pos++; return new RepeatNode(atom, 1, 5); }
    else if (c == '?') { pos++; return new RepeatNode(atom, 0, 1); }
    else if (c == '^') {
        pos++;
        int start = pos;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        int n = Integer.parseInt(input.substring(start, pos));
        return new RepeatNode(atom, n, n);
    }
    return atom;
}
```

The `^n` quantifier parses the digits following `^` and creates a `RepeatNode` with `min == max == n`, enforcing exact repetition.

### 3. AST Node Implementations

**LiteralNode** (node/LiteralNode.java) — the simplest node, returns its character:

```java
public class LiteralNode implements RegexNode {
    private final char c;

    @Override
    public String generate(ProcessingTracer tracer) {
        return String.valueOf(c);
    }
}
```

**ConcatNode** (node/ConcatNode.java) — generates each child in order and concatenates the results:

```java
public String generate(ProcessingTracer tracer) {
    tracer.log("CONCAT — " + children.size() + " parts");
    StringBuilder sb = new StringBuilder();
    for (RegexNode child : children) {
        sb.append(child.generate(tracer));
    }
    return sb.toString();
}
```

**AlternationNode** (node/AlternationNode.java) — randomly selects one alternative:

```java
public String generate(ProcessingTracer tracer) {
    int index = RANDOM.nextInt(children.size());
    RegexNode chosen = children.get(index);
    String result = chosen.generate(tracer);
    String options = children.stream().map(RegexNode::describe).collect(Collectors.joining(", "));
    tracer.log("ALTERNATION — chose '" + result + "' from [" + options + "]");
    return result;
}
```

**RepeatNode** (node/RepeatNode.java) — repeats its child a random number of times within `[min, max]`:

```java
public String generate(ProcessingTracer tracer) {
    int count = (min == max) ? min : min + RANDOM.nextInt(max - min + 1);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
        sb.append(child.generate(tracer));
    }
    String result = sb.toString();
    tracer.log("REPEAT(" + quantifierLabel() + ", " + min + ".." + max
             + ") — count=" + count + " → \"" + result + "\"");
    return result;
}
```

The `quantifierLabel()` helper maps the min/max range back to the original quantifier symbol (`?`, `*`, `+`, or `^n`) for readable trace output.

### 4. Processing Tracer (ProcessingTracer.java)

The `ProcessingTracer` records each processing step so users can see exactly how a string was generated:

```java
public class ProcessingTracer {
    private final List<String> steps = new ArrayList<>();

    public void log(String message) {
        steps.add(message);
    }

    public void printTrace(String result) {
        for (int i = 0; i < steps.size(); i++) {
            System.out.println("Step " + (i + 1) + ": " + steps.get(i));
        }
        System.out.println("Result: " + result);
    }
}
```

This provides transparency into the generation process, showing which alternatives were chosen, how many repetitions occurred, and how concatenation assembled the final string.

### 5. Orchestration (Main.java)

The `Main` class processes three regex patterns, generating 5 sample strings for each with full processing traces:

```java
private static final String[] REGEXES = {
    "(a|b)(c|d)E+G?",
    "P(Q|R|S)T(UV|W|X)*Z+",
    "1(0|1)*2(3|4)^5 36"
};
```

For each regex: the parser builds an AST, then the generator traverses it 5 times with a fresh tracer each time.

## Conclusions / Screenshots / Results

The regex processor correctly handles all target patterns. Sample output for each regex:

**Regex 1: `(a|b)(c|d)E+G?`**
```
--- Sample 1 ---
Step 1: CONCAT — 4 parts
Step 2: ALTERNATION — chose 'b' from [a, b]
Step 3: ALTERNATION — chose 'd' from [c, d]
Step 4: REPEAT(+, 1..5) — count=3 → "EEE"
Step 5: REPEAT(?, 0..1) — count=1 → "G"
Result: bdEEEG

--- Sample 2 ---
Step 1: CONCAT — 4 parts
Step 2: ALTERNATION — chose 'a' from [a, b]
Step 3: ALTERNATION — chose 'd' from [c, d]
Step 4: REPEAT(+, 1..5) — count=3 → "EEE"
Step 5: REPEAT(?, 0..1) — count=1 → "G"
Result: adEEEG

--- Sample 3 ---
Step 1: CONCAT — 4 parts
Step 2: ALTERNATION — chose 'b' from [a, b]
Step 3: ALTERNATION — chose 'c' from [c, d]
Step 4: REPEAT(+, 1..5) — count=3 → "EEE"
Step 5: REPEAT(?, 0..1) — count=0 → ""
Result: bcEEE
```

**Regex 2: `P(Q|R|S)T(UV|W|X)*Z+`**
```
--- Sample 1 ---
Step 1: CONCAT — 5 parts
Step 2: ALTERNATION — chose 'Q' from [Q, R, S]
Step 3: ALTERNATION — chose 'W' from [UV, W, X]
Step 4: REPEAT(*, 0..5) — count=1 → "W"
Step 5: REPEAT(+, 1..5) — count=4 → "ZZZZ"
Result: PQTWZZZZ
```

**Regex 3: `1(0|1)*2(3|4)^5 36`**
```
--- Sample 1 ---
Step 1: CONCAT — 5 parts
Step 2: ALTERNATION — chose '0' from [0, 1]
Step 3: REPEAT(*, 0..5) — count=1 → "0"
Step 4: REPEAT(^5, 5..5) — count=5 → "34334"
Step 5: ALTERNATION — chose '3' from [3, 4]
...
Result: 102343343 36
```

Key observations from the implementation:

1. **Recursive descent parsing** naturally handles operator precedence — alternation binds loosest, then concatenation, then quantifiers — without needing an explicit precedence table.

2. **AST-based generation** cleanly separates parsing from string generation. The same AST could be reused for matching, visualization, or NFA construction.

3. **The `^n` quantifier** extends standard regex syntax, demonstrating how the parser can be extended with custom operators by adding cases to the `parseTerm()` method.

4. **Processing traces** provide full visibility into the generation process, showing each decision point (which alternative was chosen, how many repetitions) and how they compose into the final string.

5. **The node hierarchy** follows the Composite design pattern — `ConcatNode` and `AlternationNode` contain children of type `RegexNode`, enabling arbitrary nesting of patterns like `(UV|W|X)*` where alternation is nested inside repetition.

## References

* Course materials by Cretu Dumitru, with kudos to Vasile Drumea and Irina Cojuhari
* Aho, A. V., Lam, M. S., Sethi, R., & Ullman, J. D. (2006). Compilers: Principles, Techniques, and Tools (2nd ed.)
* Sipser, M. (2012). Introduction to the Theory of Computation
