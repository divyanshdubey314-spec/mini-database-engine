package com.divyansh.database;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String filePath = "test.db";

        // Step 1: Write multiple records into one page
        try (PageStore store = new PageStore(filePath)) {
            PageAllocator allocator = new PageAllocator(store);
            long pageNum = allocator.allocatePage();

            Page page = new Page();
            page.addRecord(new Record(1, "Divyansh"));
            page.addRecord(new Record(2, "Alice"));
            page.addRecord(new Record(3, "Bob"));

            store.writePage((int) pageNum, page);
            System.out.println("Wrote page " + pageNum + " with 3 records. Total pages: " + store.getPageCount());
        }

        // Step 2: Reopen the file and read all records back
        try (PageStore store = new PageStore(filePath)) {
            Page page = store.readPage(0);
            for (Record record : page.getRecords()) {
                System.out.println("Read back: " + record);
            }
        }
    }
}