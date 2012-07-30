package com.sk89q.mntfs;

import java.io.IOException;

public class LockException extends IOException {
    
    private static final long serialVersionUID = -3715328163335551498L;

    public LockException() {
        super();
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(String message) {
        super(message);
    }

    public LockException(Throwable cause) {
        super(cause);
    }
    
}