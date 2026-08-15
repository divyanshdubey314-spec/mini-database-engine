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
    private static final int MIN_KEYS = BPlusTreeNode.MAX_KEYS / 2;

    public void delete(int key) throws IOException {
        if (rootPageNumber == -1) return;

        deleteRecursive(rootPageNumber, key);

        BPlusTreeNode root = BPlusTreeNode.fromPage(store.readPage((int) rootPageNumber));
        if (!root.isLeaf && root.keys.isEmpty()) {
            // Root became empty after a merge - its one remaining child becomes the new root
            rootPageNumber = root.childPageNumbers.get(0);
        }
    }

    // Returns true if this node underflowed (fewer than MIN_KEYS) after the delete
    private boolean deleteRecursive(long pageNumber, int key) throws IOException {
        BPlusTreeNode node = BPlusTreeNode.fromPage(store.readPage((int) pageNumber));

        if (node.isLeaf) {
            int idx = node.keys.indexOf(key);
            if (idx == -1) return false; // key not found
            node.keys.remove(idx);
            node.recordPageNumbers.remove(idx);
            node.slotIndexes.remove(idx);
            store.writePage((int) pageNumber, node.toPage());
            return node.keys.size() < MIN_KEYS && pageNumber != rootPageNumber;
        }

        int i = 0;
        while (i < node.keys.size() && key >= node.keys.get(i)) i++;
        long childPageNumber = node.childPageNumbers.get(i);

        boolean childUnderflow = deleteRecursive(childPageNumber, key);
        if (!childUnderflow) return false;

        fixUnderflow(node, i);
        store.writePage((int) pageNumber, node.toPage());
        return node.keys.size() < MIN_KEYS && pageNumber != rootPageNumber;
    }

    private void fixUnderflow(BPlusTreeNode parent, int childIndex) throws IOException {
        long childPageNum = parent.childPageNumbers.get(childIndex);
        BPlusTreeNode child = BPlusTreeNode.fromPage(store.readPage((int) childPageNum));

        // Try borrowing from left sibling
        if (childIndex > 0) {
            long leftNum = parent.childPageNumbers.get(childIndex - 1);
            BPlusTreeNode left = BPlusTreeNode.fromPage(store.readPage((int) leftNum));
            if (left.keys.size() > MIN_KEYS) {
                borrowFromLeft(parent, childIndex, left, child, leftNum, childPageNum);
                return;
            }
        }
        // Try borrowing from right sibling
        if (childIndex < parent.childPageNumbers.size() - 1) {
            long rightNum = parent.childPageNumbers.get(childIndex + 1);
            BPlusTreeNode right = BPlusTreeNode.fromPage(store.readPage((int) rightNum));
            if (right.keys.size() > MIN_KEYS) {
                borrowFromRight(parent, childIndex, child, right, childPageNum, rightNum);
                return;
            }
        }
        // Can't borrow from either sibling - must merge
        if (childIndex > 0) {
            long leftNum = parent.childPageNumbers.get(childIndex - 1);
            BPlusTreeNode left = BPlusTreeNode.fromPage(store.readPage((int) leftNum));
            mergeNodes(parent, childIndex - 1, left, child, leftNum, childPageNum);
        } else {
            long rightNum = parent.childPageNumbers.get(childIndex + 1);
            BPlusTreeNode right = BPlusTreeNode.fromPage(store.readPage((int) rightNum));
            mergeNodes(parent, childIndex, child, right, childPageNum, rightNum);
        }
    }

    private void borrowFromLeft(BPlusTreeNode parent, int childIndex, BPlusTreeNode left, BPlusTreeNode child, long leftNum, long childNum) throws IOException {
        if (child.isLeaf) {
            int lastIdx = left.keys.size() - 1;
            child.keys.add(0, left.keys.remove(lastIdx));
            child.recordPageNumbers.add(0, left.recordPageNumbers.remove(lastIdx));
            child.slotIndexes.add(0, left.slotIndexes.remove(lastIdx));
            parent.keys.set(childIndex - 1, child.keys.get(0));
        } else {
            int lastKeyIdx = left.keys.size() - 1;
            child.keys.add(0, parent.keys.get(childIndex - 1));
            parent.keys.set(childIndex - 1, left.keys.remove(lastKeyIdx));
            child.childPageNumbers.add(0, left.childPageNumbers.remove(left.childPageNumbers.size() - 1));
        }
        store.writePage((int) leftNum, left.toPage());
        store.writePage((int) childNum, child.toPage());
    }

    private void borrowFromRight(BPlusTreeNode parent, int childIndex, BPlusTreeNode child, BPlusTreeNode right, long childNum, long rightNum) throws IOException {
        if (child.isLeaf) {
            child.keys.add(right.keys.remove(0));
            child.recordPageNumbers.add(right.recordPageNumbers.remove(0));
            child.slotIndexes.add(right.slotIndexes.remove(0));
            parent.keys.set(childIndex, right.keys.get(0));
        } else {
            child.keys.add(parent.keys.get(childIndex));
            parent.keys.set(childIndex, right.keys.remove(0));
            child.childPageNumbers.add(right.childPageNumbers.remove(0));
        }
        store.writePage((int) childNum, child.toPage());
        store.writePage((int) rightNum, right.toPage());
    }

    // Merges 'right' INTO 'left', removing the separator key from parent
    private void mergeNodes(BPlusTreeNode parent, int leftKeyIndex, BPlusTreeNode left, BPlusTreeNode right, long leftNum, long rightNum) throws IOException {
        if (left.isLeaf) {
            left.keys.addAll(right.keys);
            left.recordPageNumbers.addAll(right.recordPageNumbers);
            left.slotIndexes.addAll(right.slotIndexes);
            left.nextLeafPageNumber = right.nextLeafPageNumber;
        } else {
            left.keys.add(parent.keys.get(leftKeyIndex));
            left.keys.addAll(right.keys);
            left.childPageNumbers.addAll(right.childPageNumbers);
        }
        parent.keys.remove(leftKeyIndex);
        parent.childPageNumbers.remove(leftKeyIndex + 1);

        store.writePage((int) leftNum, left.toPage());
        // rightNum's page is now orphaned/unused - a real DB would free it via a free-list
    }
}