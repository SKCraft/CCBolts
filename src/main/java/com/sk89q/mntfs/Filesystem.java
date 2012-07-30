package com.sk89q.mntfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implements a file system interface with virtual roots and virtual mounts.
 * 
 * @author sk89q
 */
public class Filesystem implements Mount {
    
    private final Map<Path, Mount> mounts = new LinkedHashMap<Path, Mount>();
    private Mount userMount;
    
    public Filesystem() {
    }
    
    /**
     * Get the currently set user mount.
     * @return user mount, nor null
     */
    public Mount getUserMount() {
        return userMount;
    }

    /**
     * Set the user mount.
     * @param userMount user mount
     */
    public void setUserMount(Mount userMount) {
        this.userMount = userMount;
    }

    /**
     * Attach a mount.
     * @param at root path to mount at
     * @param mount mount to attach
     */
    public void attach(Path at, Mount mount) {
        mounts.put(at, mount);
    }
    
    /**
     * Resolve a path into its mount and resolved path.
     * @param path path
     * @return mapped path
     */
    public MappedPath resolveMount(Path path) {
        if (userMount != null && path.size() > 0 && path.get(0).startsWith("~")) {
            return new MappedPath(userMount, path);
        }
        
        for (Map.Entry<Path, Mount> entry : mounts.entrySet()) {
            if (entry.getKey().contains(path)) {
                Path subpath = entry.getKey().relative(path);
                return new MappedPath(entry.getValue(), subpath);
            }
        }
        
        return null;
    }
    
    /**
     * Resolve a path into its mount and resolved path.
     * @param path path
     * @param throwException true to throw an exception if no mount is found
     * @return mapped path
     * @throws FileNotFoundException if no mount is found, and exceptions are set to be thrown
     */
    public MappedPath resolveMount(Path path, boolean throwException) throws FileNotFoundException {
        MappedPath mapped = resolveMount(path);
        if (mapped == null && throwException) {
            throw new FileNotFoundException("Directory does not exist to list");
        }
        return mapped;
    }

    @Override
    public boolean isWritable(Path path) {
        MappedPath mapped = resolveMount(path);
        if (mapped == null) {
            return false;
        }
        return mapped.getMount().isWritable(mapped.getPath());
    }

    @Override
    public Path[] list(Path path) throws IOException {
        MappedPath mapped = resolveMount(path, true);
        return mapped.getMount().list(mapped.getPath());
    }

    @Override
    public boolean exists(Path path) throws IOException {
        MappedPath mapped = resolveMount(path);
        if (mapped == null) {
            return false;
        }
        return mapped.getMount().exists(mapped.getPath());
    }

    @Override
    public boolean isDirectory(Path path) throws IOException {
        MappedPath mapped = resolveMount(path);
        if (mapped == null) {
            return false;
        }
        return mapped.getMount().exists(mapped.getPath());
    }

    @Override
    public long getSize(Path path) throws IOException {
        MappedPath mapped = resolveMount(path, true);
        return mapped.getMount().getSize(mapped.getPath());
    }

    @Override
    public InputStream getInputStream(Path path) throws IOException {
        MappedPath mapped = resolveMount(path, true);
        return mapped.getMount().getInputStream(mapped.getPath());
    }

    @Override
    public OutputStream getOutputStream(Path path, boolean append)
            throws IOException {
        MappedPath mapped = resolveMount(path, true);
        return mapped.getMount().getOutputStream(mapped.getPath(), append);
    }

    @Override
    public void mkdirs(Path path) throws IOException {
        MappedPath mapped = resolveMount(path, true);
        mapped.getMount().mkdirs(mapped.getPath());
    }

    @Override
    public void delete(Path path) throws IOException {
        MappedPath mapped = resolveMount(path, true);
        mapped.getMount().delete(mapped.getPath());
    }

    @Override
    public void move(Path from, Path to) throws IOException {
        copy(from, to);
        delete(from);
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
    
    public static class MappedPath {
        
        private final Mount mount;
        private final Path path;
        
        private MappedPath(Mount mount, Path path) {
            this.mount = mount;
            this.path = path;
        }
        
        public Mount getMount() {
            return mount;
        }
        
        public Path getPath() {
            return path;
        }
        
    }

}
