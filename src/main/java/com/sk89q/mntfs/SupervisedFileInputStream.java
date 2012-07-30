package com.sk89q.mntfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SupervisedFileInputStream extends InputStream {
    
    private final ResourceLock pointer;
    private final FileInputStream in;

    public SupervisedFileInputStream(ResourceSupervisor supervisor, File file) throws IOException {
        pointer = supervisor.allocate();
        this.in = new FileInputStream(file);
    }

    @Override
    public int read() throws IOException {
        try {
            return in.read();
        } catch (IOException e) {
            pointer.release();
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            in.close();
        } finally {
            pointer.release();
        }
    }

}
