package com.sk89q.mntfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReadableMount implements Mount {

    protected final ResourceSupervisor supervisor;
    protected final File baseDir;

    public ReadableMount(ResourceSupervisor supervisor, File baseDir) {
        this.supervisor = supervisor;
        this.baseDir = baseDir;
    }
    
    /**
     * Get the resource supervisor.
     * @return the supervisor
     */
    public ResourceSupervisor getSupervisor() {
        return supervisor;
    }

    @Override
    public boolean isWritable(Path path) {
        return false;
    }

    @Override
    public Path[] list(Path path) throws IOException {
        ResourceLock lock = supervisor.allocate();
        
        try {
            File leaf = baseDir;
            
            for (String part : path.parts()) {
                leaf = new File(leaf, part);
                
                if (!leaf.exists()) {
                    throw new FileNotFoundException("A directory on the path did not exist");
                }
                
                if (!leaf.isDirectory()) {
                    throw new FileNotFoundException(
                            "An element on the path was not a directory, so it cannot be entered");
                }
            }
            
            if (!leaf.exists()) {
                return new Path[0]; // This is only the case if it's the base directory
            }
            
            if (!leaf.isDirectory()) {
                throw new FileNotFoundException("A directory on the path did not exist");
            }
            
            File[] files = leaf.listFiles();
            Path[] paths = new Path[files.length];
            
            for (int i = 0; i < files.length; i++) {
                paths[i] = path.combine(files[i].getName());
            }
            
            return paths;
        } finally {
            lock.release();
        }
    }

    protected File resolve(Path path, boolean checkParents) throws IOException {
        File leaf = baseDir;
        
        String[] parts = path.parts();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            
            leaf = new File(leaf, part);
            
            if (i != parts.length - 1) {
                if (checkParents && !leaf.exists()) {
                    throw new IOException("A directory on the path did not exist");
                }
                
                if (checkParents && !leaf.isDirectory()) {
                    throw new IOException(
                            "An element on the path was not a directory, so it cannot be entered");
                }
            }
        }
        
        return leaf;
    }

    @Override
    public InputStream getInputStream(Path path) throws IOException {
        File file = resolve(path, true);
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist");
        }
        return new FileInputStream(file);
    }

    @Override
    public void mkdirs(Path path) throws IOException {
        throw new IOException("Access denied");
    }

    @Override
    public OutputStream getOutputStream(Path path, boolean append)
            throws IOException {
        throw new IOException("Access denied");
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new IOException("Access denied");
    }

    @Override
    public boolean exists(Path path) throws IOException {
        ResourceLock lock = supervisor.allocate();
        
        try {
            return resolve(path, false).exists();
        } finally {
            lock.release();
        }
    }

    @Override
    public boolean isDirectory(Path path) throws IOException {
        ResourceLock lock = supervisor.allocate();
        
        try {
            return resolve(path, false).isDirectory();
        } finally {
            lock.release();
        }
    }

    @Override
    public long getSize(Path path) throws IOException {
        ResourceLock lock = supervisor.allocate();
        
        try {
            long length = resolve(path, false).length();
            if (length == 0) {
                throw new FileNotFoundException("File does not exist");
            }
            return length;
        } finally {
            lock.release();
        }
    }

    @Override
    public void move(Path from, Path to) throws IOException {
        throw new IOException("Access denied");
    }

    @Override
    public void copy(Path from, Path to) throws IOException {
        throw new IOException("Access denied");
    }

}