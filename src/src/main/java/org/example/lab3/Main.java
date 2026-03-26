package org.example.lab3;

import java.util.List;
import java.util.Scanner;

public class Main {
    static void main() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter a string: ");
        String input = sc.nextLine();
        Lexer lexer = new Lexer(input);
        lexer.tokenize().forEach(System.out::println);
    }
}
