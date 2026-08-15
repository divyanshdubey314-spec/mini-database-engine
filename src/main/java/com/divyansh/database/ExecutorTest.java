package com.divyansh.database;

import java.io.IOException;

public class ExecutorTest {
    public static void main(String[] args) throws IOException {
        String filePath = "executor_test.db";

        try (PageStore store = new PageStore(filePath)) {
            PageAllocator allocator = new PageAllocator(store);
            BPlusTree index = new BPlusTree(store, allocator);
            Table table = new Table(store, allocator, index);
            QueryExecutor executor = new QueryExecutor(table);

            System.out.println(executor.execute("INSERT INTO users VALUES(1, Divyansh)"));
            System.out.println(executor.execute("INSERT INTO users VALUES(2, Alice)"));
            System.out.println(executor.execute("INSERT INTO users VALUES(3, Bob)"));

            System.out.println(executor.execute("SELECT * FROM users WHERE id = 2"));
            System.out.println(executor.execute("SELECT * FROM users WHERE id = 99"));

            System.out.println(executor.execute("SELECT * FROM users"));
        }
    }
}