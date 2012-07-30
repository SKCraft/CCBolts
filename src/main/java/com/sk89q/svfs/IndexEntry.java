package com.sk89q.svfs;

public class IndexEntry {
    
    private String name;
    private int block;
    private long size;
    private long lastModified;
    
    public IndexEntry(String name, int block, long size, long lastModified) {
        super();
        this.name = name;
        this.block = block;
        this.size = size;
        this.lastModified = lastModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return block == 0;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

}
