package org.example.lab3;

public enum TokenType {
    // Keywords
    INCOME,
    EXPENSE,
    TAX,
    PROFIT,
    LOSS,
    SAVE,
    INVEST,
    BUDGET,
    IF,
    THEN,
    ELSE,

    // General types
    IDENTIFIER,
    INTEGER,
    FLOAT,
    PERCENT,

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