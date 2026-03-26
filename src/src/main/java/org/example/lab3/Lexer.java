package org.example.lab3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String input;
    private final List<Token> tokens = new ArrayList<>();
    private int position = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("income", TokenType.INCOME);
        keywords.put("expense", TokenType.EXPENSE);
        keywords.put("tax", TokenType.TAX);
        keywords.put("profit", TokenType.PROFIT);
        keywords.put("loss", TokenType.LOSS);
        keywords.put("save", TokenType.SAVE);
        keywords.put("invest", TokenType.INVEST);
        keywords.put("budget", TokenType.BUDGET);
        keywords.put("if", TokenType.IF);
        keywords.put("then", TokenType.THEN);
        keywords.put("else", TokenType.ELSE);
    }

    public Lexer(String input) {
        this.input = input;
    }

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

    private void handleWhitespace() {
        char current = advance();
        if (current == '\n') {
            line++;
        }
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
                // 12. without digits after dot -> mark as UNKNOWN or accept as float
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

            case '+':
                tokens.add(new Token(TokenType.PLUS, "+", line));
                break;

            case '-':
                tokens.add(new Token(TokenType.MINUS, "-", line));
                break;

            case '*':
                tokens.add(new Token(TokenType.MULTIPLY, "*", line));
                break;

            case '/':
                tokens.add(new Token(TokenType.DIVIDE, "/", line));
                break;

            case '>':
                if (match('=')) {
                    tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", line));
                } else {
                    tokens.add(new Token(TokenType.GREATER, ">", line));
                }
                break;

            case '<':
                if (match('=')) {
                    tokens.add(new Token(TokenType.LESS_EQUAL, "<=", line));
                } else {
                    tokens.add(new Token(TokenType.LESS, "<", line));
                }
                break;

            case '!':
                if (match('=')) {
                    tokens.add(new Token(TokenType.NOT_EQUAL, "!=", line));
                } else {
                    tokens.add(new Token(TokenType.UNKNOWN, "!", line));
                }
                break;

            case '(':
                tokens.add(new Token(TokenType.LPAREN, "(", line));
                break;

            case ')':
                tokens.add(new Token(TokenType.RPAREN, ")", line));
                break;

            case ',':
                tokens.add(new Token(TokenType.COMMA, ",", line));
                break;

            case ';':
                tokens.add(new Token(TokenType.SEMICOLON, ";", line));
                break;

            default:
                tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(current), line));
                break;
        }
    }

    private boolean isAtEnd() {
        return position >= input.length();
    }

    private char peek() {
        return input.charAt(position);
    }

    private char advance() {
        return input.charAt(position++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (input.charAt(position) != expected) return false;
        position++;
        return true;
    }
}