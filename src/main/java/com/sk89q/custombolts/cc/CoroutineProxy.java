package com.sk89q.custombolts.cc;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import dan200.computer.core.Computer;

public class CoroutineProxy extends AbstractAPI {
    
    private static final int MAX_ALIVE_COROUTINES = 3;
    
    private final Object lock = new Object();
    private final LuaTable backend;
    private final LuaThread[] allocation = new LuaThread[MAX_ALIVE_COROUTINES];
    
    public CoroutineProxy(Computer computer, LuaTable backend) {
        super(computer);
        this.backend = backend;
    }
    
    private void checkAllocation() {
        synchronized (lock) {
            int alive = 0;
            
            for (int i = 0; i < allocation.length; i++) {
                if (allocation[i] == null) {
                    continue;
                }
                
                if (!allocation[i].getStatus().equals("dead")) {
                    alive++;
                } else {
                    allocation[i] = null;
                }
            }
            
            if (alive >= MAX_ALIVE_COROUTINES) {
                throw new LuaError("Too many coroutines; yield or kill some before making more");
            }
        }
    }
    
    private void allocate(LuaThread thread) {
        synchronized (lock) {
            for (int i = 0; i < allocation.length; i++) {
                if (allocation[i] == null) {
                    allocation[i] = thread;
                    break;
                }
            }
        }
    }
    
    private LuaThread createThread(Varargs args) {
        synchronized (lock) {
            checkAllocation();
            LuaThread thread = (LuaThread) backend.get("create").invoke(args);
            if (thread != null) {
                allocate(thread);
            }
            return thread;
        }
    }
    
    @ApiMethod("create")
    public class CreateMethod extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            synchronized (lock) {
                return createThread(args);
            }
        }
    }
    
    @ApiMethod("wrap")
    public class WrapMethod extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            synchronized (lock) {
                throw new LuaError("Not supported yet");
            }
        }
    }
    
    @ApiMethod("resume")
    public class ResumeMethod extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            return backend.get("resume").invoke(args);
        }
    }
    
    @ApiMethod("yield")
    public class YieldMethod extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            return backend.get("yield").invoke(args);
        }
    }
    
    @ApiMethod("running")
    public class RunningMethod extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            return backend.get("running").invoke(args);
        }
    }
    
    @ApiMethod("status")
    public class StatusMethod extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            return backend.get("status").invoke(args);
        }
    }
    

}
