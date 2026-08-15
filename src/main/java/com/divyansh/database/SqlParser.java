package com.divyansh.database;

import java.util.List;

public class SqlParser {

    private final List<String> tokens;
    private int pos = 0;

    public SqlParser(List<String> tokens) {
        this.tokens = tokens;
    }

    // Entry point: parse a full SELECT statement
    public SelectStatement parseSelect() {
        expect("SELECT");
        expect("*");
        expect("FROM");

        SelectStatement stmt = new SelectStatement();
        stmt.tableName = consume(); // the table name itself, whatever it is

        if (pos < tokens.size() && peek().equals("WHERE")) {
            consume(); // eat "WHERE"
            stmt.whereColumn = consume();
            expect("=");
            stmt.whereValue = Integer.parseInt(consume());
            stmt.hasWhere = true;
        }

        return stmt;
    }
    public InsertStatement parseInsert() {
        expect("INSERT");
        expect("INTO");

        InsertStatement stmt = new InsertStatement();
        stmt.tableName = consume();

        expect("VALUES");
        expect("(");
        stmt.id = Integer.parseInt(consume());
        expect(",");
        stmt.name = consume();
        expect(")");

        return stmt;
    }

    public DeleteStatement parseDelete() {
        expect("DELETE");
        expect("FROM");

        DeleteStatement stmt = new DeleteStatement();
        stmt.tableName = consume();

        expect("WHERE");
        stmt.whereColumn = consume();
        expect("=");
        stmt.whereValue = Integer.parseInt(consume());

        return stmt;
    }

    // Checks the next token matches what we expect, then consumes it. Throws if wrong.
    private void expect(String expected) {
        String actual = consume();
        if (!actual.equalsIgnoreCase(expected)) {
            throw new IllegalArgumentException("Expected '" + expected + "' but got '" + actual + "'");
        }
    }

    // Returns the current token and advances position
    private String consume() {
        if (pos >= tokens.size()) {
            throw new IllegalArgumentException("Unexpected end of input");
        }
        return tokens.get(pos++);
    }

    // Looks at the current token WITHOUT advancing position
    private String peek() {
        return tokens.get(pos);
    }
}