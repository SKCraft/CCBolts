package com.sk89q.custombolts.cc;

public interface ApiCallable<T> {

    T call(Object[] args);
    
}
