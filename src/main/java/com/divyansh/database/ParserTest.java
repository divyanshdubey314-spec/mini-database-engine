package com.divyansh.database;

import java.util.List;

public class ParserTest {
    public static void main(String[] args) {
        String sql1 = "SELECT * FROM users";
        List<String> tokens1 = Tokenizer.tokenize(sql1);
        SelectStatement stmt1 = new SqlParser(tokens1).parseSelect();
        System.out.println(stmt1);

        String sql2 = "SELECT * FROM users WHERE id = 5";
        List<String> tokens2 = Tokenizer.tokenize(sql2);
        SelectStatement stmt2 = new SqlParser(tokens2).parseSelect();
        System.out.println(stmt2);
        String sql3 = "INSERT INTO users VALUES(4, Charlie)";
        List<String> tokens3 = Tokenizer.tokenize(sql3);
        InsertStatement stmt3 = new SqlParser(tokens3).parseInsert();
        System.out.println(stmt3);

        String sql4 = "DELETE FROM users WHERE id = 3";
        List<String> tokens4 = Tokenizer.tokenize(sql4);
        DeleteStatement stmt4 = new SqlParser(tokens4).parseDelete();
        System.out.println(stmt4);
    }
}