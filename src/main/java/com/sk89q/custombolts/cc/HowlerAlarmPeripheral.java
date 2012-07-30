package com.sk89q.custombolts.cc;

import nuclearcontrol.TileEntityHowlerAlarm;

public class HowlerAlarmPeripheral extends PeripheralAdapter<TileEntityHowlerAlarm> {
    
    public HowlerAlarmPeripheral(TileEntityHowlerAlarm base) {
        super(base);
        
        register("getSoundName", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getSoundName());
            }
        });
        
        register("getRange", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getRange());
            }
        });
        
        register("setSoundName", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                if (args.length >= 1) {
                    String soundName = String.valueOf(args[0]);
                    if (soundName.length() < 1 || soundName.length() > 50) {
                        throw new IllegalArgumentException("Invalid sound name");
                    }
                    getBase().setSoundName(soundName);
                    return null;
                } else {
                    throw new IllegalArgumentException("Need sound name as first argument");
                }
            }
        });
        
        register("play", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                getBase().setPowered(true);
                return null;
            }
        });
        
        register("stop", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                getBase().setPowered(false);
                return null;
            }
        });
    }

    @Override
    public String getType() {
        return "IC2EnergyStorageUnit";
    }

}
