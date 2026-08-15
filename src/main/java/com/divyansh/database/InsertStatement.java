package com.divyansh.database;

public class InsertStatement {
    public String tableName;
    public int id;
    public String name;

    @Override
    public String toString() {
        return "INSERT INTO " + tableName + " VALUES(" + id + ", " + name + ")";
    }
}