package com.divyansh.database;

public class SelectStatement {
    public String tableName;
    public String whereColumn; // null if no WHERE clause
    public int whereValue;
    public boolean hasWhere;

    @Override
    public String toString() {
        if (hasWhere) {
            return "SELECT * FROM " + tableName + " WHERE " + whereColumn + " = " + whereValue;
        }
        return "SELECT * FROM " + tableName;
    }
}