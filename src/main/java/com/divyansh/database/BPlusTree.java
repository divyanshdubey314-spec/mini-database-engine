package com.divyansh.database;

import java.io.IOException;
import java.util.List;

public class BPlusTree {

    private final PageStore store;
    private final PageAllocator allocator;
    private long rootPageNumber = -1; // -1 means "tree is empty, no root yet"

    public BPlusTree(PageStore store, PageAllocator allocator) {
        this.store = store;
        this.allocator = allocator;
    }

    // Inserts a key pointing to (recordPageNumber, slotIndex). No splitting yet.
    public void insert(int key, long recordPageNumber, int slotIndex) throws IOException {
        if (rootPageNumber == -1) {
            // Tree is empty: create a brand new leaf and make it the root
            BPlusTreeNode root = new BPlusTreeNode(true);
            root.keys.add(key);
            root.recordPageNumbers.add(recordPageNumber);
            root.slotIndexes.add(slotIndex);

            long newPageNum = allocator.allocatePage();
            store.writePage((int) newPageNum, root.toPage());
            rootPageNumber = newPageNum;
            return;
        }

        // Tree already has a root leaf: load it, insert in sorted position
        BPlusTreeNode leaf = BPlusTreeNode.fromPage(store.readPage((int) rootPageNumber));

        int insertPos = 0;
        while (insertPos < leaf.keys.size() && leaf.keys.get(insertPos) < key) {
            insertPos++;
        }
        leaf.keys.add(insertPos, key);
        leaf.recordPageNumbers.add(insertPos, recordPageNumber);
        leaf.slotIndexes.add(insertPos, slotIndex);

        store.writePage((int) rootPageNumber, leaf.toPage());
        // NOTE: no overflow/split check yet - that's next
    }

    // Searches for a key, returns [recordPageNumber, slotIndex] or null if not found
    public long[] search(int key) throws IOException {
        if (rootPageNumber == -1) {
            return null; // empty tree
        }

        BPlusTreeNode leaf = BPlusTreeNode.fromPage(store.readPage((int) rootPageNumber));

        for (int i = 0; i < leaf.keys.size(); i++) {
            if (leaf.keys.get(i) == key) {
                return new long[]{leaf.recordPageNumbers.get(i), leaf.slotIndexes.get(i)};
            }
        }
        return null; // not found
    }
}