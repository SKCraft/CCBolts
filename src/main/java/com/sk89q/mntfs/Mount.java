package com.sk89q.mntfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a file mount with accessible files and directories referenced
 * by {@link Path}s.
 * 
 * @author sk89q
 */
public interface Mount {
    
    /**
     * Returns whether the mount is writable.
     * @return true if writable
     */
    boolean isWritable(Path path);

    /**
     * List all the files below a path.
     * @param path path
     * @return list of paths
     * @throws FileNotFoundException if the path does not exist, or consists of files instead of directories
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOException thrown on some I/O error
     */
    Path[] list(Path path) throws IOException;

    /**
     * Returns whether a given path exists.
     * @param path path
     * @return true if it exists
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOException thrown on other I/O error
     */
    boolean exists(Path path) throws IOException;
    
    /**
     * Returns whether a given path is a directory.
     * @param path path
     * @return true if a directory
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOException thrown on other I/O error
     */
    boolean isDirectory(Path path) throws IOException;
    
    /**
     * Returns the fil esize of a given path.
     * @param path path
     * @return file size in bytes    
     * @throws FileNotFoundException if the path does not exist
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOExceptionthrown on other I/O error
     */
    long getSize(Path path) throws IOException;

    /**
     * Open a file for reading.
     * @param path path
     * @return input stream
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOExceptionthrown on other I/O error
     */
    InputStream getInputStream(Path path) throws IOException;
    
    /**
     * Open a file for writing.
     * @param path path
     * @return input stream
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOExceptionthrown on other I/O error
     */
    OutputStream getOutputStream(Path path, boolean append) throws IOException;
    
    /**
     * Make the given directories.
     * @param path path
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOException thrown on error when creating a directory
     */
    void mkdirs(Path path) throws IOException;
    
    /**
     * Deletes a path.
     * @param path path to delete
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOException thrown on error when deleting
     */
    void delete(Path path) throws IOException;
    
    /**
     * Moves an object from one path to another.
     * @param from from path
     * @param to to path
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOException thrown if the move failed
     */
    void move(Path from, Path to) throws IOException;
    
    /**
     * Moves an object from one path to another.
     * @param from from path
     * @param to to path
     * @throws LockException thrown if the resource cannot be accessed
     * @throws IOException thrown if the copy failed
     */
    void copy(Path from, Path to) throws IOException;

}