package com.sk89q.custombolts.cc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.luaj.vm2.LuaError;

import dan200.computer.core.Computer;

public class AbstractAPI {
    
    private final Computer computer;
    private Method tryAbort;
    
    public AbstractAPI(Computer computer) {
        this.computer = computer;
        findCalls();
    }
    
    public Computer getComputer() {
        return computer;
    }
    
    private void findCalls() {
        try {
            tryAbort = Computer.class.getDeclaredMethod("tryAbort", new Class<?>[0]);
            tryAbort.setAccessible(true);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    public void tryAbort() throws LuaError {
        try {
            tryAbort.invoke(computer);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof LuaError) {
                throw (LuaError) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }
    
}
