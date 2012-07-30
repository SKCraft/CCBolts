package com.sk89q.custombolts.cc;

import ic2.api.IEnergyConductor;
import ic2.common.TileEntityElectricBlock;
import ic2.common.TileEntityNuclearReactor;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.TileEntity;
import net.minecraft.server.World;
import nuclearcontrol.TileEntityHowlerAlarm;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.TwoArgFunction;

import dan200.computer.api.IPeripheral;
import dan200.computer.core.Computer;

public class CCHook {

    private static Logger logger = Logger.getLogger(CCHook.class
            .getCanonicalName());

    private CCHook() {
    }

    public static void setupGlobals(Computer computer, LuaTable globals) {
        protectLuaEnvironment();
        
        globals.set("skcraft", getTable(new SKCraftLibrary(computer)));
        globals.set("coroutine", getTable(new CoroutineProxy(computer, (LuaTable) globals.get("coroutine"))));
        //globals.set("fs", getTable(new FilesystemLibrary(computer)));
        
        LuaTable fs = (LuaTable) globals.get("fs");
        
        fs.set("isReadOnly", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg0) {
                return LuaValue.TRUE;
            }
        });
        
        fs.set("makeDir", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg0) {
                throw new LuaError("Temporarily disabled - to be enabled soon!");
            }
        });
        
        fs.set("move", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg0, LuaValue arg1) {
                throw new LuaError("Temporarily disabled - to be enabled soon!");
            }
        });
        
        fs.set("copy", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg0, LuaValue arg1) {
                throw new LuaError("Temporarily disabled - to be enabled soon!");
            }
        });
        
        fs.set("delete", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg0) {
                throw new LuaError("Temporarily disabled - to be enabled soon!");
            }
        });
        
        final LuaFunction open = (LuaFunction) fs.get("open");
        
        fs.set("open", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg0, LuaValue arg1) {
                if (arg1.checkstring().toString().matches("^[rb]{1,2}$")) {
                    return open.call(arg0, arg1);
                }
                throw new LuaError("Temporarily disabled - to be enabled soon!");
            }
        });
    }
    
    private static void protectLuaEnvironment() {
        PackageLib.instance.LOADED.set("package", new LuaTable());
        PackageLib.instance.LOADED.set("luajava", new LuaTable());
        PackageLib.instance.LOADED.set("debug", new LuaTable());
        PackageLib.instance.LOADED.set("io", new LuaTable());
        PackageLib.instance.LOADED.set("os", new LuaTable());
    }

    public static IPeripheral getPeripheral(World world, int x, int y, int z) {
        if (y >= 0 && y < world.getHeight()) {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity == null) {
                return null;
            } else if (tileEntity instanceof TileEntityHowlerAlarm) {
                return new HowlerAlarmPeripheral(
                        (TileEntityHowlerAlarm) tileEntity);
            } else if (tileEntity instanceof TileEntityNuclearReactor) {
                return new IC2NuclearReactorPeripheral(
                        (TileEntityNuclearReactor) tileEntity);
            } else if (tileEntity instanceof IEnergyConductor) {
                return new IC2WirePeripheral(
                        (IEnergyConductor) tileEntity);
            } else if (tileEntity instanceof TileEntityElectricBlock) {
                return new IC2BatteryPeripiheral(
                        (TileEntityElectricBlock) tileEntity);
            } else {
                return null;
            }
        }

        return null;
    }

    private static LuaValue getTable(Object object) {
        LuaTable table = new LuaTable();

        for (Class<?> subclass : object.getClass().getClasses()) {
            if (!LuaValue.class.isAssignableFrom(subclass))
                continue;

            ApiMethod method = subclass.getAnnotation(ApiMethod.class);
            if (method == null)
                continue;

            try {
                Constructor<?> constr = subclass.getConstructor(object
                        .getClass());
                LuaValue val = (LuaValue) constr.newInstance(object);
                table.set(method.value(), val);
            } catch (Throwable e) {
                logger.log(Level.WARNING,
                        "Failed to get constructor for API class", e);
            }
        }

        return table;
    }

}
