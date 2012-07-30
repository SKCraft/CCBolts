package com.sk89q.mntfs;

public class ResourceLock {
    
    private final ResourceSupervisor supervisor;
    private boolean released = false;
    
    public ResourceLock(ResourceSupervisor supervisor) {
        this.supervisor = supervisor;
    }
    
    public synchronized void release() {
        if (!released) {
            supervisor.release();
            released = true;
        }
    }

}
