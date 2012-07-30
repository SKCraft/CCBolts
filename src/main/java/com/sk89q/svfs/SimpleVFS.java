package com.sk89q.svfs;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Implements a very simple virtual file system backed by one single random
 * accessible file. This FS has various restrictions on the sizes and number of
 * files that can be placed into it.
 * 
 * @author sk89q
 */
public class SimpleVFS {
    
    private static final Semaphore totalPointers = new Semaphore(50);
    public static final short DEFAULT_BLOCK_SIZE = 1024;
    public static final int HEADER_SIZE = 5 + 2 + 2 + 2;
    public static final int FILE_HEADER_SIZE = 1 + 4;
    public static final int VERSION = 1;
    
    private final File baseFile;
    private final RandomAccessFile stream;
    private final int maxFilesystemSize;
    private final int maxFileSize;
    private final int maxEntryCount;

    private boolean loaded = false;
    private final Semaphore allocatedPointers;
    private int blockSize = DEFAULT_BLOCK_SIZE;
    private int indexLoc = 0;
    private Map<String, IndexEntry> entries;
    private byte[] freeSpaceMap;
    
    /**
     * Create a new instance of the virtual file system.
     * @param baseFile base file to back the VFS
     * @param maxFilesystemSize maximum size of the real backed file (in bytes)
     * @param maxFileSize maximum size of individual file entries in the VFS (in bytes)
     * @param maxEntryCount maximum size of the directory index
     * @param maxFilePointers maximum number of "open" virtual file pointers at once
     * @throws IOException thrown if the backed file can be accessed
     */
    public SimpleVFS(File baseFile, int maxFilesystemSize, int maxFileSize,
            int maxEntryCount, int maxFilePointers) throws IOException {
        super();
        this.baseFile = baseFile;
        this.stream = new RandomAccessFile(baseFile, "rw");
        this.maxFilesystemSize = maxFilesystemSize;
        this.maxFileSize = maxFileSize;
        this.maxEntryCount = maxEntryCount;
        
        allocatedPointers = new Semaphore(maxFilePointers);
    }

    /**
     * Get the file that backs this VFS.
     * @return file
     */
    public File getBaseFile() {
        return baseFile;
    }

    /**
     * Get the maximum size of the entire file system.
     * @return size in bytes
     */
    public int getMaxFilesystemSize() {
        return maxFilesystemSize;
    }

    /**
     * Get the maximum size of any file.
     * @return size in bytes
     */
    public int getMaxFilesize() {
        return maxFileSize;
    }

    /**
     * Get the maximum number of entries.
     * @return maximum entry count
     */
    public int getMaxEntryCount() {
        return maxEntryCount;
    }
    
    /**
     * Get the block size in bytes.
     * @return block size in bytes
     */
    public int getBlockSize() {
        return blockSize;
    }
    
    /**
     * Get the offset in the VFS file for the given block, considering the
     * size of the VFS header and free block map.
     * @param block a block number between 0 and 65534, inclusive
     * @return offset in bytes
     * @throws IOException if past the end of the VFS size maximum
     */
    public int getOffset(int block) throws IOException {
        if (block < 1) {
            throw new IllegalArgumentException("Invalid block number below 1");
        }
        if (block >= 65535) {
            throw new IllegalArgumentException("Invalid block number above 65534");
        }
        int offset = HEADER_SIZE + block * blockSize;
        if (offset + blockSize - 1 > maxFilesystemSize) {
            throw new IOException("Out of space");
        }
        return offset;
    }
    
    /**
     * Reads and verifies the magic bytes of the VFS file.
     * Seeking must be done beforehand.
     * @throws IOException if invalid magic bytes are found
     */
    private void readMagicBytes() throws IOException {
        if (stream.readByte() != 0) throw new IOException("Not a SVFS file");
        if (stream.readChar() != 'S') throw new IOException("Not a SVFS file");
        if (stream.readChar() != 'V') throw new IOException("Not a SVFS file");
        if (stream.readChar() != 'F') throw new IOException("Not a SVFS file");
        if (stream.readChar() != 'S') throw new IOException("Not a SVFS file");
    }
    
    /**
     * Writes the magic bytes of the VFS file. Seeking must be done beforehand.
     * @throws IOException on write exception
     */
    private void writeMagicBytes() throws IOException {
        stream.write(0);
        stream.write('S');
        stream.write('V');
        stream.write('F');
        stream.write('S');
    }
    
    /**
     * Try to parse an existing VFS file and load the file/directory index
     * into memory. If this method fails, the state of this VFS object will
     * be undefined. See {@link #initializeNew()} to build a brand new VFS
     * file system from scratch.
     * @throws IOException thrown on an error while verifying/reading
     */
    private void initializeFromFile() throws IOException {
        stream.seek(0);
        
        readMagicBytes();
        
        int version = stream.readUnsignedShort();
        if (version != VERSION) {
            throw new IOException("Invalid filesystem version");
        }
        
        blockSize = stream.readUnsignedShort();
        if (blockSize < 64 || (blockSize % 8) != 0) {
            throw new IOException("Invalid block size");
        }
        
        indexLoc = stream.readUnsignedShort();
        readIndex();
        
        freeSpaceMap = new byte[blockSize];
        stream.read(freeSpaceMap, 0, blockSize);
    }
    
    /**
     * Attempt to read the VFS index into memory, based on the value of
     * the current index location (as read possibly from
     * {@link #initializeFromFile()}).
     * @throws IOException on read error
     */
    private void readIndex() throws IOException {
        entries = new LinkedHashMap<String, IndexEntry>();
        
        if (indexLoc != 0) {
            DataInputStream in = null;
            
            try {
                in = new DataInputStream(getEntryInputStream(indexLoc));
                int numEntries = in.readUnsignedShort();
                for (int i = 0; i < numEntries; i++) {
                    String path = stream.readUTF();
                    int block = stream.readUnsignedShort();
                    long size = stream.readInt() & 0xFFFFFFFFL;
                    long lastModified = stream.readLong();
                    entries.put(path, new IndexEntry(path, block, size, lastModified));
                }
            } finally {
                close(in);
            }
        }
    }
    
    /**
     * Initializes a new VFS file system.
     * @throws IOException on write error
     */
    private void initializeNew() throws IOException {
        stream.seek(0);
        
        writeMagicBytes();

        // Version
        stream.writeShort(VERSION);
        
        // Block size
        blockSize = DEFAULT_BLOCK_SIZE;
        stream.writeShort(blockSize);
        
        // File/directory index location
        indexLoc = 1;
        stream.writeShort(indexLoc);
        
        // Read the free space map
        freeSpaceMap = new byte[blockSize];
        stream.write(freeSpaceMap);
        
        entries = new LinkedHashMap<String, IndexEntry>();
        stream.writeInt(0); // Length of this block
        stream.writeInt(0); // 0 = no more sectors
    }
    
    /**
     * Checks whether the full directory tree exists above the given path.
     * @param path path to check
     * @return true if it exists
     */
    private boolean directoryExistsFor(Path path) {
        Path leaf = path;
        while (!(leaf = path.getParent()).isRoot()) {
            IndexEntry entry = entries.get(leaf.toString());
            if (entry != null && entry.isDirectory()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets a {@link IndexEntry} given a path.
     * @param path path
     * @return entry or null if the entry does not exist
     */
    private IndexEntry getEntry(Path path) {
        return entries.get(path.toString());
    }
    
    /**
     * Get the input stream for an entry, starting at the given start block.
     * @param startBlock block start at
     * @return input stream
     * @throws IOException on I/O error
     */
    protected EntryInputStream getEntryInputStream(int startBlock) throws IOException {
        return new EntryInputStream(this, startBlock);
    }

    /**
     * Get the output stream for an entry, starting at the given start block.
     * @param startBlock block start at
     * @return input stream
     * @throws IOException on I/O error
     */
    protected EntryOutputStream getEntryOutputStream(int startBlock) throws IOException {
        return new EntryOutputStream(this, startBlock);
    }
    
    /**
     * Load up the existing VFS file system or initialize a new one. This is
     * only called if the VFS is used.
     * @throws IOException on I/O error
     */
    private synchronized void prepare() throws IOException {
        if (loaded) return;
    
        try {
            initializeFromFile();
        } catch (IOException e) {
            initializeNew();
        }
    }

    /**
     * Write data to a given path.
     * @param path path
     * @param out output stream
     * @throws IOException on write exception
     */
    public synchronized void write(Path path, ByteArrayOutputStream out) throws IOException {
        prepare();
        
        if (!directoryExistsFor(path)) {
            throw new IOException("Parent directory doesn't exist");
        }
        
        IndexEntry entry = getEntry(path);
        
        if (entry != null) { // May have to resize
            int newLen = out.size();
            int existingBlockCount = (int) Math.ceil((FILE_HEADER_SIZE + entry.getSize()) / (double) blockSize);
            int neededBlockCount = (int) Math.ceil((FILE_HEADER_SIZE + newLen) / (double) blockSize);
            
            if (neededBlockCount <= existingBlockCount) {
                stream.seek(entry.getBlock());
                stream.writeInt(newLen);
                stream.write(out.toByteArray());
                
                for (int i = 0; i < existingBlockCount - neededBlockCount; i++) {
                    
                }
            }
        } else {
        }
    }

    /**
     * Reads the file stored at the path.
     * @param path
     * @return
     * @throws IOException
     */
    public byte[] read(Path path) throws IOException {
        IndexEntry entry = getEntry(path);
        
        if (entry == null) {
            throw new FileNotFoundException("File not found for path " + path.toString());
        }
        
        if (entry.isDirectory()) {
            throw new IOException("Given path is a directory, not a file");
        }
        
        byte[] buf = new byte[(int) entry.getSize()];
        
        EntryInputStream in = null;
        try {
            in = getEntryInputStream(indexLoc);
            in.read(buf, 0, buf.length);
        } finally {
            close(in);
        }
        
        return buf;
    }
    
    /**
     * Get an output stream for a file. The file's contents will be stored
     * in memory until the stream is closed, and then the fill will be
     * written to the VFS.
     * @param path path to write to
     * @return output stream
     * @throws IOException on I/O exception
     */
    public OutputStream getOutputStream(String path) throws IOException {
        return new VirtualFileOutputStream(this, Path.parse(path), maxFileSize);
    }
    
    public InputStream getInputStream(String path) throws IOException {
        return new VirtualFileInputStream(this, Path.parse(path));
    }
    
    public void allocatePointer() throws IOException {
        if (!allocatedPointers.tryAcquire()) {
            throw new IOException("Out of file pointers");
        }
        
        if (!totalPointers.tryAcquire()) {
            throw new IOException("Out of global file pointers");
        }
    }

    public void releasePointer() {
        allocatedPointers.release();
        totalPointers.release();
    }
    
    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }

    RandomAccessFile getStream() {
        return stream;
    }

}
