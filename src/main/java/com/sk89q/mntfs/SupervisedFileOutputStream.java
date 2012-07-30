package com.sk89q.mntfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SupervisedFileOutputStream extends OutputStream {
    
    private final ResourceLock pointer;
    private final OutputStream out;
    private final int maxFileSize;
    private long written;

    public SupervisedFileOutputStream(ResourceSupervisor supervisor, File file, boolean append) throws IOException {
        pointer = supervisor.allocate();
        this.out = new FileOutputStream(file, append);
        this.maxFileSize = supervisor.getMaxFileSize();
        this.written = (append && file.exists()) ? file.length() : 0;
    }

    @Override
    public void write(int b) throws IOException {
        if (written >= maxFileSize) {
            throw new IOException("File too big, can't write more bytes");
        }
        written++;
        try {
            out.write(b);
        } catch (IOException e) {
            pointer.release();
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            out.close();
        } finally {
            pointer.release();
        }
    }

}
