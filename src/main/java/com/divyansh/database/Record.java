package com.divyansh.database;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Record {
    private final int id;
    private final String name;
    private boolean deleted;

    public Record(int id, String name) {
        this.id = id;
        this.name = name;
        this.deleted = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void markDeleted() {
        this.deleted = true;
    }

    // [1 byte: deleted flag][4 bytes: id][4 bytes: name length][name bytes]
    public byte[] serialize() {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + 4 + nameBytes.length);
        buffer.put((byte) (deleted ? 1 : 0));
        buffer.putInt(id);
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        return buffer.array();
    }

    public static Record deserialize(ByteBuffer buffer) {
        boolean deleted = buffer.get() == 1;
        int id = buffer.getInt();
        int nameLength = buffer.getInt();
        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        String name = new String(nameBytes, StandardCharsets.UTF_8);
        Record record = new Record(id, name);
        record.deleted = deleted;
        return record;
    }

    public int size() {
        return 1 + 4 + 4 + name.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public String toString() {
        return "Record{id=" + id + ", name='" + name + "'" + (deleted ? ", DELETED" : "") + "}";
    }
}