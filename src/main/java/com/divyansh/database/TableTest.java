package com.divyansh.database;

import java.io.IOException;

public class TableTest {
    public static void main(String[] args) throws IOException {
        String filePath = "table_test.db";

        try (PageStore store = new PageStore(filePath)) {
            PageAllocator allocator = new PageAllocator(store);
            BPlusTree index = new BPlusTree(store, allocator);
            Table table = new Table(store, allocator, index);

            table.insert(new Record(1, "Divyansh"));
            table.insert(new Record(2, "Alice"));
            table.insert(new Record(3, "Bob"));

            System.out.println("Find 1: " + table.find(1));
            System.out.println("Find 2: " + table.find(2));
            System.out.println("Find 3: " + table.find(3));
            System.out.println("Find 99: " + table.find(99));
        }
    }
}