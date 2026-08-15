package com.divyansh.database;

import java.io.IOException;
import java.util.List;

public class Table {

    private final PageStore store;
    private final PageAllocator allocator;
    private final BPlusTree index;

    private long currentDataPage = -1;
    private final List<Long> allDataPages = new java.util.ArrayList<>();

    public Table(PageStore store, PageAllocator allocator, BPlusTree index) {
        this.store = store;
        this.allocator = allocator;
        this.index = index;
    }

    public void insert(Record record) throws IOException {
        Page page;

        if (currentDataPage == -1) {
            currentDataPage = allocator.allocatePage();
            allDataPages.add(currentDataPage);
            page = new Page();
        } else {
            page = store.readPage((int) currentDataPage);
        }

        boolean fit = page.addRecord(record);

        if (!fit) {
            currentDataPage = allocator.allocatePage();
            allDataPages.add(currentDataPage);
            page = new Page();
            fit = page.addRecord(record);
            if (!fit) {
                throw new IllegalStateException("Record too large to fit in an empty page");
            }
        }

        int slotIndex = page.getRecords().size() - 1;

        store.writePage((int) currentDataPage, page);
        index.insert(record.getId(), currentDataPage, slotIndex);
    }

    public Record find(int id) throws IOException {
        long[] location = index.search(id);
        if (location == null) {
            return null;
        }

        long pageNumber = location[0];
        int slotIndex = (int) location[1];

        Page page = store.readPage((int) pageNumber);
        return page.getRecords().get(slotIndex);
    }

    public List<Record> findAll() throws IOException {
        List<Record> all = new java.util.ArrayList<>();
        for (long pageNum : allDataPages) {
            Page page = store.readPage((int) pageNum);
            for (Record r : page.getRecords()) {
                if (!r.isDeleted()) {
                    all.add(r);
                }
            }
        }
        return all;
    }
    public boolean delete(int id) throws IOException {
        long[] location = index.search(id);
        if (location == null) return false;

        long pageNumber = location[0];
        int slotIndex = (int) location[1];

        Page page = store.readPage((int) pageNumber);
        java.util.List<Record> records = page.getRecords();
        records.get(slotIndex).markDeleted();

        // Rewrite the whole page with the updated (now-deleted) record
        Page newPage = new Page();
        for (Record r : records) {
            newPage.addRecord(r);
        }
        store.writePage((int) pageNumber, newPage);

        index.delete(id);
        return true;
    }
}