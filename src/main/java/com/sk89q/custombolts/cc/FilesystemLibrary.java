package com.sk89q.custombolts.cc;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import dan200.computer.core.Computer;

public class FilesystemLibrary extends AbstractAPI {
    
    public FilesystemLibrary(Computer computer) {
        super(computer);
    }
    
    @ApiMethod("list")
    public class ListMethod extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue val) {
            tryAbort();
            
            LuaTable files = new LuaTable();
            return files;
        }
    }
    

}
