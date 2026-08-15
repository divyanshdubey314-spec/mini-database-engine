package com.divyansh.database;

import java.io.IOException;

public class Table {

    private final PageStore store;
    private final PageAllocator allocator;
    private final BPlusTree index;

    // Tracks the current "active" page we're appending records into
    private long currentDataPage = -1;

    public Table(PageStore store, PageAllocator allocator, BPlusTree index) {
        this.store = store;
        this.allocator = allocator;
        this.index = index;
    }

    // Inserts a record, indexes it by id, returns nothing - the tree now knows where it lives
    public void insert(Record record) throws IOException {
        Page page;
        boolean isNewPage = false;

        if (currentDataPage == -1) {
            // No active page yet - allocate the first one
            currentDataPage = allocator.allocatePage();
            page = new Page();
            isNewPage = true;
        } else {
            page = store.readPage((int) currentDataPage);
        }

        boolean fit = page.addRecord(record);

        if (!fit) {
            // Current page is full - start a fresh one
            currentDataPage = allocator.allocatePage();
            page = new Page();
            fit = page.addRecord(record);
            isNewPage = true;
            if (!fit) {
                throw new IllegalStateException("Record too large to fit in an empty page");
            }
        }

        // The slot index is "how many records were in the page before this one"
        int slotIndex = page.getRecords().size() - 1;

        store.writePage((int) currentDataPage, page);
        index.insert(record.getId(), currentDataPage, slotIndex);
    }

    // Looks up a record by id using the B+ Tree index
    public Record find(int id) throws IOException {
        long[] location = index.search(id);
        if (location == null) {
            return null; // not found
        }

        long pageNumber = location[0];
        int slotIndex = (int) location[1];

        Page page = store.readPage((int) pageNumber);
        return page.getRecords().get(slotIndex);
    }
}