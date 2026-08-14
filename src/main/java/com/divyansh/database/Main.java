package com.divyansh.database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException {
        String filePath = "test.db";

        // Step 1: Write a page with some text in it
        try (PageStore store = new PageStore(filePath)) {
            Page page = new Page();
            byte[] message = "Hello from page 0!".getBytes(StandardCharsets.UTF_8);
            page.getBuffer().put(message); // write bytes into the page

            store.writePage(0, page);
            System.out.println("Wrote page 0. Total pages: " + store.getPageCount());
        }

        // Step 2: Reopen the file and read it back
        try (PageStore store = new PageStore(filePath)) {
            Page page = store.readPage(0);
            byte[] data = page.toBytes();

            // Only print the first 30 bytes as text (rest is empty/zeroed space)
            String text = new String(data, 0, 30, StandardCharsets.UTF_8).trim();
            System.out.println("Read back: " + text);
        }
    }
}