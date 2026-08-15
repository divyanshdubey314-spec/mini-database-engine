package com.divyansh.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueryExecutor {

    private final Table table;

    public QueryExecutor(Table table) {
        this.table = table;
    }

    // Runs a raw SQL string end to end: tokenize -> parse -> execute
    public Object execute(String sql) throws IOException {
        List<String> tokens = Tokenizer.tokenize(sql);
        String command = tokens.get(0).toUpperCase();

        SqlParser parser = new SqlParser(tokens);

        switch (command) {
            case "SELECT":
                SelectStatement select = parser.parseSelect();
                return executeSelect(select);

            case "INSERT":
                InsertStatement insert = parser.parseInsert();
                table.insert(new Record(insert.id, insert.name));
                return "Inserted 1 row.";

            case "DELETE":
                DeleteStatement del = parser.parseDelete();
                boolean deleted = table.delete(del.whereValue);
                return deleted ? "Deleted 1 row." : "No matching row.";

            default:
                throw new IllegalArgumentException("Unsupported command: " + command);
        }
    }

    private List<Record> executeSelect(SelectStatement stmt) throws IOException {
        if (stmt.hasWhere) {
            List<Record> results = new ArrayList<>();
            Record found = table.find(stmt.whereValue);
            if (found != null) {
                results.add(found);
            }
            return results;
        } else {
            return table.findAll(); // full table scan
        }
    }
}