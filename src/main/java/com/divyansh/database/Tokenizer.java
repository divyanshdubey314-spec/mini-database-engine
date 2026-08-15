package com.divyansh.database;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    // Turns a raw SQL string into a list of tokens
    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c)) {
                // End of current token, if any
                flush(current, tokens);

            } else if (c == '*' || c == '=' || c == '(' || c == ')' || c == ',') {
                // Symbols are always their own token, even if jammed against a word
                flush(current, tokens);
                tokens.add(String.valueOf(c));

            } else {
                // Regular character - part of a word/number
                current.append(c);
            }
        }

        flush(current, tokens); // catch anything left at the end
        return tokens;
    }

    // Helper: if there's a token being built, add it to the list and reset
    private static void flush(StringBuilder current, List<String> tokens) {
        if (current.length() > 0) {
            tokens.add(current.toString());
            current.setLength(0);
        }
    }
}