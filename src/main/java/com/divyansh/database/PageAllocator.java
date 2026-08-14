package com.divyansh.database;

import java.io.IOException;

public class PageAllocator {

    private final PageStore store;
    private long nextPageNumber;

    public PageAllocator(PageStore store) throws IOException {
        this.store = store;
        // Start allocating right after however many pages already exist
        this.nextPageNumber = store.getPageCount();
    }

    // Hands out the next free page number and reserves it
    public long allocatePage() {
        long allocated = nextPageNumber;
        nextPageNumber++;
        return allocated;
    }

    public long getNextPageNumber() {
        return nextPageNumber;
    }
}