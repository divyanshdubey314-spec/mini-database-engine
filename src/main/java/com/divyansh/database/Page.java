package com.divyansh.database;

import java.nio.ByteBuffer;

public class Page {
    public static final int PAGE_SIZE = 4096; // 4KB, standard page size

    private final ByteBuffer buffer;

    public Page() {
        this.buffer = ByteBuffer.allocate(PAGE_SIZE);
    }

    public Page(byte[] data) {
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Page data must be exactly " + PAGE_SIZE + " bytes");
        }
        this.buffer = ByteBuffer.wrap(data);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public byte[] toBytes() {
        return buffer.array();
    }
}