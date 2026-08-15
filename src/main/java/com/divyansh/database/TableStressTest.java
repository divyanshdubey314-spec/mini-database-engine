package com.divyansh.database;

import java.io.IOException;

public class TableStressTest {
    public static void main(String[] args) throws IOException {
        String filePath = "stress_test.db";

        try (PageStore store = new PageStore(filePath)) {
            PageAllocator allocator = new PageAllocator(store);
            BPlusTree index = new BPlusTree(store, allocator);
            Table table = new Table(store, allocator, index);

            // Insert 300 records - enough to force multiple data pages AND tree splits
            for (int i = 1; i <= 300; i++) {
                table.insert(new Record(i, "User" + i));
            }
            System.out.println("Inserted 300 records.");

            // Spot-check across the range: first, early, middle, late, last, and a miss
            int[] checkIds = {1, 50, 150, 250, 300, 999};
            for (int id : checkIds) {
                Record r = table.find(id);
                System.out.println("Find " + id + ": " + r);
            }
        }
    }
}