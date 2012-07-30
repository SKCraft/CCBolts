package com.sk89q.custombolts.cc;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;


import dan200.computer.core.Computer;

public class SKCraftLibrary extends AbstractAPI {
    
    public SKCraftLibrary(Computer computer) {
        super(computer);
    }
    
    @ApiMethod("getYourLove")
    public class TestMethod extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue val) {
            return LuaValue.valueOf("Guo Jingming");
        }
    }
    

}
