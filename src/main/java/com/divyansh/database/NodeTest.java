package com.divyansh.database;

public class NodeTest {
    public static void main(String[] args) {

        // Build a leaf node in memory
        BPlusTreeNode leaf = new BPlusTreeNode(true);
        leaf.keys.add(5);
        leaf.recordPageNumbers.add(0L);
        leaf.slotIndexes.add(0);

        leaf.keys.add(8);
        leaf.recordPageNumbers.add(0L);
        leaf.slotIndexes.add(1);

        leaf.nextLeafPageNumber = 7; // pretend the next leaf lives at page 7

        // Serialize it to a Page (like it's about to be written to disk)
        Page page = leaf.toPage();

        // Deserialize it back (like we just read it back from disk)
        BPlusTreeNode reloaded = BPlusTreeNode.fromPage(page);

        // Check everything survived the round trip
        System.out.println("isLeaf: " + reloaded.isLeaf);
        System.out.println("keys: " + reloaded.keys);
        System.out.println("recordPageNumbers: " + reloaded.recordPageNumbers);
        System.out.println("slotIndexes: " + reloaded.slotIndexes);
        System.out.println("nextLeafPageNumber: " + reloaded.nextLeafPageNumber);
    }
}