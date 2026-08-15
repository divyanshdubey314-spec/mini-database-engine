package com.divyansh.database;

import java.util.List;

public class TokenizerTest {
    public static void main(String[] args) {
        String sql = "SELECT * FROM users WHERE id = 5";
        List<String> tokens = Tokenizer.tokenize(sql);
        System.out.println(tokens);

        String sql2 = "SELECT * FROM users WHERE id=5"; // no spaces around =
        System.out.println(Tokenizer.tokenize(sql2));
    }
}