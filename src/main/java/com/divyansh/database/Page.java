package com.divyansh.database;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Page {
    public static final int PAGE_SIZE = 4096;

    private final ByteBuffer buffer;

    public Page() {
        this.buffer = ByteBuffer.allocate(PAGE_SIZE);
        buffer.putInt(0); // first 4 bytes = record count, starts at 0
    }

    public Page(byte[] data) {
        if (data.length != PAGE_SIZE) {
            throw new IllegalArgumentException("Page data must be exactly " + PAGE_SIZE + " bytes");
        }
        this.buffer = ByteBuffer.wrap(data);
    }

    // Appends a record to this page. Returns false if it doesn't fit.
    public boolean addRecord(Record record) {
        int recordCount = buffer.getInt(0);
        byte[] recordBytes = record.serialize();

        int writeOffset = 4;
        buffer.position(4);
        for (int i = 0; i < recordCount; i++) {
            Record.deserialize(buffer); // just walks past it correctly, whatever the format is
            writeOffset = buffer.position();
        }

        if (writeOffset + recordBytes.length > PAGE_SIZE) {
            return false;
        }

        buffer.position(writeOffset);
        buffer.put(recordBytes);
        buffer.putInt(0, recordCount + 1);
        return true;
    }

    // Reads back all records stored in this page
    public List<Record> getRecords() {
        List<Record> records = new ArrayList<>();
        int recordCount = buffer.getInt(0);
        buffer.position(4);
        for (int i = 0; i < recordCount; i++) {
            records.add(Record.deserialize(buffer));
        }
        return records;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public byte[] toBytes() {
        return buffer.array();
    }
}