package org.example;

import org.example.FiniteAutomaton;
import org.example.Grammar;

import java.util.*;


public class Main {
    public static void main(String[] args) {
        Grammar g = Grammar.variant25();

        System.out.println("Start symbol: " + g.getStartSymbol());
        System.out.println("productions:");
        for (Map.Entry<String, List<Grammar.Production>> e : g.getP().entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }

        System.out.println("\n 5 generated strings:");
        Set<String> sentences = new HashSet<>();
        while(sentences.size() < 5){
            String s = g.generateSentence();
            if(s != null) sentences.add(s);
        }
        for(String s : sentences){
            System.out.println(" " + s);
        }

        FiniteAutomaton fa = g.toAutomaton();
        System.out.println("\n Finite Automaton (from grammar)");
        fa.printTransitions();

        System.out.println("\n Membership tests:");
        String[] tests = {
                "db",      // true
                "bdaab",   // true
                "ddca",    // true
                "ca",      // false
                "da",      // false
                "b",       // false
                "bddca",   // true (S->bS->dA->dB->cB->a)
                "bdca"     // false
        };
        
        for(String t : tests){
            boolean ok = fa.stringBelongToLanguage(t);
            System.out.println(" " + t + " -> " + ok);
        }
    }
}