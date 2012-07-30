package com.sk89q.mntfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Acts as a file system mount.
 * 
 * @author sk89q
 */
public class WritableMount extends ReadableMount {
    
    /**
     * Create a new mount that can be written to and read from.
     * @param supervisor object to supervise access with
     * @param baseDir base directory to read from
     */
    public WritableMount(ResourceSupervisor supervisor, File baseDir) {
        super(supervisor, baseDir);
    }

    @Override
    public boolean isWritable(Path path) {
        return true;
    }

    @Override
    public void mkdirs(Path path) throws IOException {
        ResourceLock lock = supervisor.allocate();
        
        try {
            File resolved = resolve(path, false);
            if (resolved.isDirectory()) {
                return;
            }
            if (!resolved.mkdirs()) {
                throw new IOException("Failed to make all the directories");
            }
        } finally {
            lock.release();
        }
    }

    @Override
    public OutputStream getOutputStream(Path path, boolean append) throws IOException {
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        File file = resolve(path, true);
        return new SupervisedFileOutputStream(getSupervisor(), file, append);
    }

    @Override
    public void delete(Path path) throws IOException {
        ResourceLock lock = supervisor.allocate();
        
        try {
            delete(resolve(path, false));
        } finally {
            lock.release();
        }
    }
    
    /**
     * Delete a file, or a directory and its contents.
     * @param file
     * @throws IOException
     */
    private void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
            
            if (!file.delete()) {
                throw new IOException("Could delete directory " + file.getName());
            }
        } else if (file.isFile()) {
            if (!file.delete()) {
                throw new IOException("Could delete file " + file.getName());
            }
        } else {
            throw new FileNotFoundException("Path element does not exist");
        }
    }

    @Override
    public void move(Path fromPath, Path toPath) throws IOException {
        ResourceLock lock = supervisor.allocate();
        
        try {
            File from = resolve(fromPath, false);
            File to = resolve(toPath, false);
            
            if (!from.renameTo(to)) {
                if (!from.exists()) {
                    throw new FileNotFoundException("Source file did not exist");
                }
                throw new IOException("Move failed");
            }
        } finally {
            lock.release();
        }
    }

    @Override
    public void copy(Path fromPath, Path toPath) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = getInputStream(fromPath);
            out = getOutputStream(toPath, false);

            byte[] buf = new byte[1024 * 4];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            IOUtil.close(in);
            IOUtil.close(out);
        }
    }
}
