package com.sk89q.custombolts.cc;

import java.util.ArrayList;
import java.util.List;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public abstract class PeripheralAdapter<T> implements IPeripheral {
    
    private final T base;
    private final ArrayList<String> methodNames = new ArrayList<String>();
    private final List<ApiCallable<Object[]>> methodImpl = new ArrayList<ApiCallable<Object[]>>();
    
    public PeripheralAdapter(T base) {
        this.base = base;
    }
    
    public T getBase() {
        return base;
    }
    
    public void register(String name, ApiCallable<Object[]> func) {
        methodNames.add(name);
        methodImpl.add(func);
    }

    @Override
    public boolean canAttachToSide(int side) {
        return true;
    }
    
    @Override
    public void attach(IComputerAccess computer, String computerSide) {
    }

    @Override
    public void detach(IComputerAccess computer) {
    }

    @Override
    public Object[] callMethod(IComputerAccess computer, int method, Object[] args)
            throws Exception {
        ApiCallable<Object[]> func;
        
        try {
            func = methodImpl.get(method);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Method does not exist");
        }
        
        try {
            return func.call(args);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    @Override
    public String[] getMethodNames() {
        return methodNames.toArray(new String[methodNames.size()]);
    }

    protected Object[] wrap(float value) {
        return new Object[] { value };
    }

    protected Object[] wrap(double value) {
        return new Object[] { value };
    }

    protected Object[] wrap(int value) {
        return new Object[] { value };
    }

    protected Object[] wrap(long value) {
        return new Object[] { value };
    }

    protected Object[] wrap(Object value) {
        return new Object[] { value };
    }
    
}
