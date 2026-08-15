package com.divyansh.database;

import java.io.IOException;

public class TreeTest {
    public static void main(String[] args) throws IOException {
        String filePath = "tree_test.db";

        try (PageStore store = new PageStore(filePath)) {
            PageAllocator allocator = new PageAllocator(store);
            BPlusTree tree = new BPlusTree(store, allocator);

            tree.insert(10, 0, 0);
            tree.insert(5, 0, 1);
            tree.insert(20, 0, 2);
            tree.insert(15, 1, 0);

            System.out.println("Search 15: " + java.util.Arrays.toString(tree.search(15)));
            System.out.println("Search 5: " + java.util.Arrays.toString(tree.search(5)));
            System.out.println("Search 99: " + tree.search(99));
        }
    }
}