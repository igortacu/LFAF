package org.example.lab4;

import org.example.lab4.node.*;

import java.util.ArrayList;
import java.util.List;

public class RegexParser {
    private final String input;
    private int pos;

    public RegexParser(String input) {
        this.input = input;
        this.pos = 0;
    }

    public RegexNode parse() {
        RegexNode result = parseExpr();
        if (pos < input.length()) {
            throw new IllegalArgumentException(
                "Unexpected character '" + input.charAt(pos) + "' at position " + pos);
        }
        return result;
    }

    // Lowest precedence: handles A|B alternation
    private RegexNode parseExpr() {
        List<RegexNode> alternatives = new ArrayList<>();
        alternatives.add(parseConcat());
        while (pos < input.length() && input.charAt(pos) == '|') {
            pos++; // consume '|'
            alternatives.add(parseConcat());
        }
        return alternatives.size() == 1 ? alternatives.get(0) : new AlternationNode(alternatives);
    }

    // Middle precedence: handles AB concatenation
    private RegexNode parseConcat() {
        List<RegexNode> parts = new ArrayList<>();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == ')' || c == '|') break;
            parts.add(parseTerm());
        }
        return parts.size() == 1 ? parts.get(0) : new ConcatNode(parts);
    }

    // High precedence: atom + optional quantifier
    private RegexNode parseTerm() {
        RegexNode atom = parseAtom();
        if (pos >= input.length()) return atom;

        char c = input.charAt(pos);
        if (c == '*') {
            pos++;
            return new RepeatNode(atom, 0, 5);
        } else if (c == '+') {
            pos++;
            return new RepeatNode(atom, 1, 5);
        } else if (c == '?') {
            pos++;
            return new RepeatNode(atom, 0, 1);
        } else if (c == '^') {
            pos++; // consume '^'
            int start = pos;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                pos++;
            }
            if (pos == start) {
                throw new IllegalArgumentException("Expected digits after '^' at position " + (pos - 1));
            }
            int n = Integer.parseInt(input.substring(start, pos));
            return new RepeatNode(atom, n, n);
        }
        return atom;
    }

    // Highest precedence: literal or (...)group
    private RegexNode parseAtom() {
        skipSpaces();
        if (pos >= input.length()) {
            throw new IllegalArgumentException("Unexpected end of input at position " + pos);
        }
        char c = input.charAt(pos);
        if (c == '(') {
            pos++; // consume '('
            RegexNode inner = parseExpr();
            if (pos >= input.length() || input.charAt(pos) != ')') {
                throw new IllegalArgumentException("Expected ')' at position " + pos);
            }
            pos++; // consume ')'
            return inner;
        }
        pos++;
        return new LiteralNode(c);
    }

    private void skipSpaces() {
        while (pos < input.length() && input.charAt(pos) == ' ') {
            pos++;
        }
    }
}
