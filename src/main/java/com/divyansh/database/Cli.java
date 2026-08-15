package com.divyansh.database;

import java.io.IOException;
import java.util.Scanner;

public class Cli {
    public static void main(String[] args) throws IOException {
        String filePath = "mydb.db";

        try (PageStore store = new PageStore(filePath)) {
            PageAllocator allocator = new PageAllocator(store);
            BPlusTree index = new BPlusTree(store, allocator);
            Table table = new Table(store, allocator, index);
            QueryExecutor executor = new QueryExecutor(table);

            Scanner scanner = new Scanner(System.in);
            System.out.println("Mini Database CLI. Type SQL, or 'exit' to quit.");

            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine();

                if (line.equalsIgnoreCase("exit")) {
                    break;
                }
                if (line.isBlank()) {
                    continue;
                }

                try {
                    Object result = executor.execute(line);
                    System.out.println(result);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }

            System.out.println("Goodbye.");
        }
    }
}