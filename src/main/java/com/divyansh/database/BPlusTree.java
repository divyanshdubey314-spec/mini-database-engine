package com.divyansh.database;

import java.io.IOException;

public class BPlusTree {

    private final PageStore store;
    private final PageAllocator allocator;
    private long rootPageNumber = -1;

    public BPlusTree(PageStore store, PageAllocator allocator) {
        this.store = store;
        this.allocator = allocator;
    }

    // Represents "a split happened, here's the new key and new right-side page"
    private static class SplitResult {
        int splitKey;
        long newRightPageNumber;
        SplitResult(int splitKey, long newRightPageNumber) {
            this.splitKey = splitKey;
            this.newRightPageNumber = newRightPageNumber;
        }
    }

    public void insert(int key, long recordPageNumber, int slotIndex) throws IOException {
        if (rootPageNumber == -1) {
            BPlusTreeNode root = new BPlusTreeNode(true);
            root.keys.add(key);
            root.recordPageNumbers.add(recordPageNumber);
            root.slotIndexes.add(slotIndex);

            long newPageNum = allocator.allocatePage();
            store.writePage((int) newPageNum, root.toPage());
            rootPageNumber = newPageNum;
            return;
        }

        SplitResult result = insertIntoNode(rootPageNumber, key, recordPageNumber, slotIndex);

        if (result != null) {
            // The root itself split - create a brand new root above it
            BPlusTreeNode newRoot = new BPlusTreeNode(false);
            newRoot.keys.add(result.splitKey);
            newRoot.childPageNumbers.add(rootPageNumber);
            newRoot.childPageNumbers.add(result.newRightPageNumber);

            long newRootPageNum = allocator.allocatePage();
            store.writePage((int) newRootPageNum, newRoot.toPage());
            rootPageNumber = newRootPageNum;
        }
    }

    // Inserts into the subtree rooted at pageNumber. Returns a SplitResult if THIS node split, else null.
    private SplitResult insertIntoNode(long pageNumber, int key, long recordPageNumber, int slotIndex) throws IOException {
        BPlusTreeNode node = BPlusTreeNode.fromPage(store.readPage((int) pageNumber));

        if (node.isLeaf) {
            int pos = 0;
            while (pos < node.keys.size() && node.keys.get(pos) < key) pos++;
            node.keys.add(pos, key);
            node.recordPageNumbers.add(pos, recordPageNumber);
            node.slotIndexes.add(pos, slotIndex);

            if (node.keys.size() <= BPlusTreeNode.MAX_KEYS) {
                store.writePage((int) pageNumber, node.toPage());
                return null; // no split needed
            }
            return splitLeaf(pageNumber, node);

        } else {
            // Internal node: find which child to descend into
            int i = 0;
            while (i < node.keys.size() && key >= node.keys.get(i)) i++;
            long childPageNumber = node.childPageNumbers.get(i);

            SplitResult childSplit = insertIntoNode(childPageNumber, key, recordPageNumber, slotIndex);
            if (childSplit == null) {
                return null; // child didn't split, nothing to do here
            }

            // Child DID split - absorb the new key/pointer into THIS node
            node.keys.add(i, childSplit.splitKey);
            node.childPageNumbers.add(i + 1, childSplit.newRightPageNumber);

            if (node.keys.size() <= BPlusTreeNode.MAX_KEYS) {
                store.writePage((int) pageNumber, node.toPage());
                return null;
            }
            return splitInternal(pageNumber, node);
        }
    }

    private SplitResult splitLeaf(long pageNumber, BPlusTreeNode leaf) throws IOException {
        int mid = leaf.keys.size() / 2;

        BPlusTreeNode rightLeaf = new BPlusTreeNode(true);
        rightLeaf.keys.addAll(leaf.keys.subList(mid, leaf.keys.size()));
        rightLeaf.recordPageNumbers.addAll(leaf.recordPageNumbers.subList(mid, leaf.keys.size()));
        rightLeaf.slotIndexes.addAll(leaf.slotIndexes.subList(mid, leaf.keys.size()));

        leaf.keys.subList(mid, leaf.keys.size()).clear();
        leaf.recordPageNumbers.subList(mid, leaf.recordPageNumbers.size()).clear();
        leaf.slotIndexes.subList(mid, leaf.slotIndexes.size()).clear();

        long rightPageNum = allocator.allocatePage();
        rightLeaf.nextLeafPageNumber = leaf.nextLeafPageNumber;
        leaf.nextLeafPageNumber = rightPageNum;

        store.writePage((int) pageNumber, leaf.toPage());
        store.writePage((int) rightPageNum, rightLeaf.toPage());

        return new SplitResult(rightLeaf.keys.get(0), rightPageNum);
    }

    private SplitResult splitInternal(long pageNumber, BPlusTreeNode node) throws IOException {
        int mid = node.keys.size() / 2;
        int upKey = node.keys.get(mid); // this key moves UP to the parent, doesn't stay in either side

        BPlusTreeNode right = new BPlusTreeNode(false);
        right.keys.addAll(node.keys.subList(mid + 1, node.keys.size()));
        right.childPageNumbers.addAll(node.childPageNumbers.subList(mid + 1, node.childPageNumbers.size()));

        node.keys.subList(mid, node.keys.size()).clear();
        node.childPageNumbers.subList(mid + 1, node.childPageNumbers.size()).clear();

        long rightPageNum = allocator.allocatePage();
        store.writePage((int) pageNumber, node.toPage());
        store.writePage((int) rightPageNum, right.toPage());

        return new SplitResult(upKey, rightPageNum);
    }

    public long[] search(int key) throws IOException {
        if (rootPageNumber == -1) return null;

        BPlusTreeNode node = BPlusTreeNode.fromPage(store.readPage((int) rootPageNumber));
        while (!node.isLeaf) {
            int i = 0;
            while (i < node.keys.size() && key >= node.keys.get(i)) i++;
            long childPageNumber = node.childPageNumbers.get(i);
            node = BPlusTreeNode.fromPage(store.readPage((int) childPageNumber));
        }

        for (int i = 0; i < node.keys.size(); i++) {
            if (node.keys.get(i) == key) {
                return new long[]{node.recordPageNumbers.get(i), node.slotIndexes.get(i)};
            }
        }
        return null;
    }
}