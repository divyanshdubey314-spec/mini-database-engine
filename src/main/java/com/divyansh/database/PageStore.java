package com.divyansh.database;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;

public class PageStore implements AutoCloseable {

    private final RandomAccessFile file;
    private final FileChannel channel;

    public PageStore(String filePath) throws IOException {
        this.file = new RandomAccessFile(filePath, "rw");
        this.channel = file.getChannel();
    }

    // Writes a page at the given page number (0-indexed)
    public void writePage(int pageNumber, Page page) throws IOException {
        long offset = (long) pageNumber * Page.PAGE_SIZE;
        ByteBuffer buffer = page.getBuffer();
        buffer.rewind(); // make sure we read from the start
        channel.write(buffer, offset);
    }

    // Reads a page at the given page number (0-indexed)
    public Page readPage(int pageNumber) throws IOException {
        long offset = (long) pageNumber * Page.PAGE_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(Page.PAGE_SIZE);
        channel.read(buffer, offset);
        return new Page(buffer.array());
    }

    // Total number of pages currently in the file
    public long getPageCount() throws IOException {
        return file.length() / Page.PAGE_SIZE;
    }

    @Override
    public void close() throws IOException {
        channel.close();
        file.close();
    }
}