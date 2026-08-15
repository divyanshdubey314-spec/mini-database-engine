package com.divyansh.database;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Record {
    private final int id;
    private final String name;

    public Record(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Converts this record into bytes: [4 bytes: id][4 bytes: name length][name bytes]
    public byte[] serialize() {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + nameBytes.length);
        buffer.putInt(id);
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        return buffer.array();
    }

    // Reads a Record back out of a ByteBuffer positioned at the start of a record
    public static Record deserialize(ByteBuffer buffer) {
        int id = buffer.getInt();
        int nameLength = buffer.getInt();
        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        return new Record(id, name);
    }

    // Total size in bytes this record takes up when serialized
    public int size() {
        return 4 + 4 + name.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public String toString() {
        return "Record{id=" + id + ", name='" + name + "'}";
    }
}