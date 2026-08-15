package com.divyansh.database;

import java.io.IOException;

public class SplitTest {
    public static void main(String[] args) throws IOException {
        String filePath = "split_test.db";

        try (PageStore store = new PageStore(filePath)) {
            PageAllocator allocator = new PageAllocator(store);
            BPlusTree tree = new BPlusTree(store, allocator);

            // Insert 500 keys - MAX_KEYS is 100, so this forces multiple leaf splits
            // AND at least one internal node split (multi-level tree)
            for (int i = 1; i <= 500; i++) {
                tree.insert(i, 0, i); // fake page 0, slot = i, just for testing
            }

            System.out.println("Inserted 500 keys.");

            // Search across a wide spread - early, middle, late keys
            System.out.println("Search 1: " + java.util.Arrays.toString(tree.search(1)));
            System.out.println("Search 150: " + java.util.Arrays.toString(tree.search(150)));
            System.out.println("Search 300: " + java.util.Arrays.toString(tree.search(300)));
            System.out.println("Search 500: " + java.util.Arrays.toString(tree.search(500)));
            System.out.println("Search 999 (should be null): " + tree.search(999));
        }
    }
}