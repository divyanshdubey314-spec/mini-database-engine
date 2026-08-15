package com.divyansh.database;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BPlusTreeNode {

    public boolean isLeaf;
    public List<Integer> keys = new ArrayList<>();

    // Leaf-only fields
    public List<Long> recordPageNumbers = new ArrayList<>();
    public List<Integer> slotIndexes = new ArrayList<>();
    public long nextLeafPageNumber = -1; // -1 means "no next leaf"

    // Internal-only field: one more child than keys
    public List<Long> childPageNumbers = new ArrayList<>();

    public BPlusTreeNode(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    // Turns this node into a Page that can be written to disk
    public Page toPage() {
        Page page = new Page();
        ByteBuffer buffer = page.getBuffer();
        buffer.clear();

        buffer.put((byte) (isLeaf ? 1 : 0));
        buffer.putInt(keys.size());
        buffer.putLong(nextLeafPageNumber);

        if (isLeaf) {
            for (int i = 0; i < keys.size(); i++) {
                buffer.putInt(keys.get(i));
                buffer.putLong(recordPageNumbers.get(i));
                buffer.putInt(slotIndexes.get(i));
            }
        } else {
            for (int i = 0; i < keys.size(); i++) {
                buffer.putInt(keys.get(i));
                buffer.putLong(childPageNumbers.get(i));
            }
            // one extra trailing child pointer
            buffer.putLong(childPageNumbers.get(keys.size()));
        }

        return page;
    }

    // Reads a node back out of a Page loaded from disk
    public static BPlusTreeNode fromPage(Page page) {
        ByteBuffer buffer = page.getBuffer();
        buffer.rewind();

        boolean isLeaf = buffer.get() == 1;
        int keyCount = buffer.getInt();
        long nextLeaf = buffer.getLong();

        BPlusTreeNode node = new BPlusTreeNode(isLeaf);
        node.nextLeafPageNumber = nextLeaf;

        if (isLeaf) {
            for (int i = 0; i < keyCount; i++) {
                node.keys.add(buffer.getInt());
                node.recordPageNumbers.add(buffer.getLong());
                node.slotIndexes.add(buffer.getInt());
            }
        } else {
            for (int i = 0; i < keyCount; i++) {
                node.keys.add(buffer.getInt());
                node.childPageNumbers.add(buffer.getLong());
            }
            node.childPageNumbers.add(buffer.getLong()); // trailing child
        }

        return node;
    }
}