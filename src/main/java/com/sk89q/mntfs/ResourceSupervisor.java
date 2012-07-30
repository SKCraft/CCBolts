package com.sk89q.mntfs;

import java.util.concurrent.Semaphore;

/**
 * Supervises the amount of resource usage. Multiple supervisors can be
 * chained together, where all parent supervisors have to have space before
 * the child supervisor can allocate space.
 * 
 * @author sk89q
 */
public class ResourceSupervisor {
    
    private final ResourceSupervisor parent;
    private final Semaphore maxOpen;
    private int maxFileSize = 1024;
    
    /**
     * Construct a new instance.
     * @param maxOpen maximum number of concurrently open resources
     */
    public ResourceSupervisor(int maxOpen) {
        this.parent = null;
        this.maxOpen = new Semaphore(maxOpen);
    }
    
    /**
     * Construct a new instance with a parent supervisor.
     * @param parent parent supervisor
     * @param maxOpen maximum number of concurrently open resources
     */
    public ResourceSupervisor(ResourceSupervisor parent, int maxOpen) {
        this.parent = parent;
        this.maxOpen = new Semaphore(maxOpen);
    }
    
    /**
     * Allocate space for one resource.
     * @return lock object to release with
     * @throws LockException if there a lock cannot be opened
     */
    public ResourceLock allocate() throws LockException {
        if (parent != null) {
            parent.allocate();
        }
        
        if (maxOpen.tryAcquire()) {
            return new ResourceLock(this);
        } else {
            parent.release();
            throw new LockException();
        }
    }

    /**
     * Release a lock.
     */
    void release() {
        maxOpen.release();
        
        if (parent != null) {
            parent.release();
        }
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

}
