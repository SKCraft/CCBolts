package com.sk89q.svfs;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

class EntryInputStream extends InputStream {
    
    private final SimpleVFS vfs;
    private final RandomAccessFile stream;
    private long pos = 0;
    private long len = 0;
    private int blockLenLeft;
    private int nextBlock;
    
    EntryInputStream(SimpleVFS vfs, int startBlock) throws IOException {
        this.vfs = vfs;
        this.stream = vfs.getStream();
        
        stream.seek(startBlock);
        readBlockHeader();
    }
    
    private void readBlockHeader() throws IOException {
        this.len = stream.readInt() & 0xFFFFFFFFL;
        this.nextBlock = stream.readUnsignedShort();
        this.blockLenLeft = vfs.getBlockSize() - SimpleVFS.FILE_HEADER_SIZE;
    }

    @Override
    public int read() throws IOException {
        if (pos >= len) {
            return -1;
        }
        
        // Jump to the next block
        if (blockLenLeft < 1) {
            if (this.nextBlock <= 0) {
                throw new EOFException("No more blocks to read, but length of file not reached.");
            } else {
                stream.seek(vfs.getOffset(nextBlock));
                readBlockHeader();
            }
        }
        
        pos++;
        blockLenLeft--;
        return stream.read();
    }

}
