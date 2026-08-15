package com.divyansh.database;

public class DeleteStatement {
    public String tableName;
    public String whereColumn;
    public int whereValue;

    @Override
    public String toString() {
        return "DELETE FROM " + tableName + " WHERE " + whereColumn + " = " + whereValue;
    }
}