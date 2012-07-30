package com.sk89q.svfs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VirtualFileInputStream extends InputStream {
    
    private final SimpleVFS vfs;
    private final ByteArrayInputStream in;
    
    public VirtualFileInputStream(SimpleVFS vfs, Path path) throws IOException {
        vfs.allocatePointer();
        this.vfs = vfs;
        byte[] buf = vfs.read(path);
        in = new ByteArrayInputStream(buf);
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public void close() throws IOException {
        try {
            in.close();
        } finally {
            vfs.releasePointer();
        }
    }

}
