package com.sk89q.svfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VirtualFileOutputStream extends OutputStream {
    
    private final SimpleVFS vfs;
    private final Path path;
    private final int maxFileSize;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    public VirtualFileOutputStream(SimpleVFS vfs, Path path, int maxFileSize) throws IOException {
        vfs.allocatePointer();
        this.vfs = vfs;
        this.path = path;
        this.maxFileSize = maxFileSize;
    }

    @Override
    public void write(int b) throws IOException {
        if (out.size() > maxFileSize) {
            throw new IOException("File too big, can't write more bytes");
        }
        out.write(b);
    }

    @Override
    public void close() throws IOException {
        try {
            vfs.write(path, out);
        } finally {
            vfs.releasePointer();
        }
    }

}
