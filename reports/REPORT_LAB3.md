# Laboratory Work 3: Lexer & Scanner

### Course: Formal Languages & Finite Automata
### Author: Tacu Igor

----

## Theory

A **lexer** (also known as a scanner or tokenizer) is the first phase of a compiler or interpreter. Its responsibility is to read the raw source code as a stream of characters and group them into meaningful sequences called **tokens**. Each token represents a logical unit of the language, such as a keyword, identifier, number, operator, or delimiter.

The lexer operates based on a set of rules (often derived from regular expressions or finite automata) that define how characters combine into valid tokens. Key concepts include:

- **Lexeme**: The actual character sequence in the source code that matches a token pattern (e.g., `"income"`, `"42.5"`, `">="`).
- **Token Type**: A category that classifies the lexeme (e.g., KEYWORD, INTEGER, OPERATOR).
- **Lookahead**: The lexer may need to peek at the next character(s) to disambiguate tokens (e.g., distinguishing `=` from `==`, or `>` from `>=`).

Lexical analysis is closely related to regular languages (Type 3 in the Chomsky hierarchy), as most token patterns can be described by regular expressions and recognized by finite automata.

## Objectives:

1. Understand what lexical analysis is
2. Learn how tokens are identified and categorized
3. Implement a lexer that can tokenize a simple domain-specific language
4. Handle keywords, identifiers, numbers (integers and floats), operators, and delimiters
5. Report unknown or malformed tokens gracefully

## Implementation description

### Domain: Financial/Budget DSL

The lexer is designed for a simple financial domain-specific language with keywords such as `income`, `expense`, `tax`, `profit`, `loss`, `save`, `invest`, `budget`, along with control-flow keywords `if`, `then`, `else`. The language supports arithmetic expressions, comparisons, variable assignments, and percentage literals.

### 1. Token Types (TokenType.java:1-44)

The `TokenType` enum defines all recognized token categories, organized into four groups:

```java
public enum TokenType {
    // Keywords
    INCOME, EXPENSE, TAX, PROFIT, LOSS, SAVE, INVEST, BUDGET,
    IF, THEN, ELSE,

    // General types
    IDENTIFIER, INTEGER, FLOAT, PERCENT,

    // Operators
    ASSIGN,         // =
    PLUS,           // +
    MINUS,          // -
    MULTIPLY,       // *
    DIVIDE,         // /
    GREATER,        // >
    LESS,           // <
    GREATER_EQUAL,  // >=
    LESS_EQUAL,     // <=
    EQUAL_EQUAL,    // ==
    NOT_EQUAL,      // !=

    // Symbols
    LPAREN,         // (
    RPAREN,         // )
    COMMA,          // ,
    SEMICOLON,      // ;

    EOF,
    UNKNOWN
}
```

This provides 28 distinct token types covering keywords (11), literals (3 — identifier, integer, float), a percent modifier, operators (11), delimiters (4), and special tokens (EOF, UNKNOWN).

### 2. Token Representation (Token.java:1-30)

The `Token` class encapsulates a token's type, lexeme, and source line number for debugging and error reporting:

```java
public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;

    @Override
    public String toString() {
        return String.format("%-15s -> %-15s [line %d]", type, lexeme, line);
    }
}
```

The `toString()` method formats tokens in a tabular style that makes the output easy to read and verify.

### 3. Lexer Core (Lexer.java:34-51)

The main `tokenize()` method drives the lexer. It processes the input character by character, dispatching to specialized methods based on the character class:

```java
public List<Token> tokenize() {
    while (!isAtEnd()) {
        char current = peek();

        if (Character.isWhitespace(current)) {
            handleWhitespace();
        } else if (Character.isLetter(current) || current == '_') {
            tokenizeIdentifierOrKeyword();
        } else if (Character.isDigit(current)) {
            tokenizeNumber();
        } else {
            tokenizeSymbolOrOperator();
        }
    }

    tokens.add(new Token(TokenType.EOF, "EOF", line));
    return tokens;
}
```

The dispatch logic categorizes each character into one of four paths: whitespace handling, identifier/keyword recognition, number parsing, or symbol/operator matching.

### 4. Keyword vs. Identifier Resolution (Lexer.java:60-69)

Keywords and identifiers share the same lexical pattern (letter/underscore followed by letters, digits, or underscores). The lexer first reads the full word, then checks it against a keyword map:

```java
private static final Map<String, TokenType> keywords = new HashMap<>();
static {
    keywords.put("income", TokenType.INCOME);
    keywords.put("expense", TokenType.EXPENSE);
    keywords.put("tax", TokenType.TAX);
    // ... other keywords
}

private void tokenizeIdentifierOrKeyword() {
    int start = position;
    while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
        advance();
    }
    String lexeme = input.substring(start, position);
    TokenType type = keywords.getOrDefault(lexeme, TokenType.IDENTIFIER);
    tokens.add(new Token(type, lexeme, line));
}
```

This approach is efficient — the keyword lookup is O(1) via the HashMap, and identifiers are simply the fallback case.

### 5. Number Tokenization (Lexer.java:72-103)

The number tokenizer handles both integers and floating-point literals, and also recognizes a trailing `%` as a separate PERCENT token:

```java
private void tokenizeNumber() {
    int start = position;
    boolean hasDot = false;

    while (!isAtEnd() && Character.isDigit(peek())) {
        advance();
    }

    if (!isAtEnd() && peek() == '.') {
        hasDot = true;
        advance();
        if (!isAtEnd() && Character.isDigit(peek())) {
            while (!isAtEnd() && Character.isDigit(peek())) {
                advance();
            }
        } else {
            // 12. without digits after dot -> mark as UNKNOWN
            String badLexeme = input.substring(start, position);
            tokens.add(new Token(TokenType.UNKNOWN, badLexeme, line));
            return;
        }
    }

    String lexeme = input.substring(start, position);
    tokens.add(new Token(hasDot ? TokenType.FLOAT : TokenType.INTEGER, lexeme, line));

    if (!isAtEnd() && peek() == '%') {
        advance();
        tokens.add(new Token(TokenType.PERCENT, "%", line));
    }
}
```

Notable design decisions:
- A number like `12.` (dot without trailing digits) is flagged as UNKNOWN rather than silently accepted
- The `%` suffix is emitted as a separate token, keeping the numeric literal clean

### 6. Operator and Symbol Tokenization (Lexer.java:105-177)

The operator tokenizer uses a `switch` statement with single-character lookahead via the `match()` helper to handle multi-character operators:

```java
private void tokenizeSymbolOrOperator() {
    char current = advance();
    switch (current) {
        case '=':
            if (match('=')) {
                tokens.add(new Token(TokenType.EQUAL_EQUAL, "==", line));
            } else {
                tokens.add(new Token(TokenType.ASSIGN, "=", line));
            }
            break;
        case '>':
            if (match('=')) {
                tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", line));
            } else {
                tokens.add(new Token(TokenType.GREATER, ">", line));
            }
            break;
        // ... other cases
        default:
            tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(current), line));
            break;
    }
}
```

The `match()` method implements a conditional advance — it consumes the next character only if it matches the expected value:

```java
private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (input.charAt(position) != expected) return false;
    position++;
    return true;
}
```

This enables clean disambiguation of `=` vs `==`, `>` vs `>=`, `<` vs `<=`, and `!` vs `!=`. An unmatched `!` is classified as UNKNOWN.

## Conclusions / Screenshots / Results

The lexer implementation successfully demonstrates the core principles of lexical analysis:

1. **Character-by-character scanning**: The lexer processes input sequentially using `peek()` and `advance()` primitives, mirroring how a finite automaton consumes symbols.

2. **Token classification**: All 28 token types are correctly recognized. Keywords are distinguished from identifiers via a constant-time hash map lookup.

3. **Multi-character token handling**: The lookahead mechanism (`match()`) correctly resolves ambiguous single-character prefixes into multi-character operators (e.g., `=` → `ASSIGN` vs. `==` → `EQUAL_EQUAL`).

4. **Error handling**: Malformed tokens (e.g., `12.` without trailing digits, standalone `!`) are reported as UNKNOWN rather than causing a crash, allowing the lexer to continue processing.

5. **Line tracking**: The lexer tracks newlines for accurate source location reporting, which is essential for meaningful error messages.

Example tokenization of `income = 5000; tax = income * 20%;`:

```
INCOME          -> income          [line 1]
ASSIGN          -> =               [line 1]
INTEGER         -> 5000            [line 1]
SEMICOLON       -> ;               [line 1]
TAX             -> tax             [line 1]
ASSIGN          -> =               [line 1]
INCOME          -> income          [line 1]
MULTIPLY        -> *               [line 1]
INTEGER         -> 20              [line 1]
PERCENT         -> %               [line 1]
SEMICOLON       -> ;               [line 1]
EOF             -> EOF             [line 1]
```

The lexer correctly identifies domain-specific keywords (`income`, `tax`), operators (`=`, `*`), literals (`5000`, `20`), the percent modifier (`%`), and delimiters (`;`). This demonstrates a working foundation for the first phase of a compiler pipeline for a financial DSL.

## References

* Course materials by Cretu Dumitru, with kudos to Vasile Drumea and Irina Cojuhari
* Aho, A. V., Lam, M. S., Sethi, R., & Ullman, J. D. (2006). Compilers: Principles, Techniques, and Tools (2nd ed.)
* Sipser, M. (2012). Introduction to the Theory of Computation
