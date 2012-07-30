package com.sk89q.custombolts.cc;

import ic2.common.TileEntityElectricBlock;

public class IC2BatteryPeripiheral extends PeripheralAdapter<TileEntityElectricBlock> {
    
    public IC2BatteryPeripiheral(TileEntityElectricBlock base) {
        super(base);
        
        register("getChargeLevel", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getChargeLevel());
            }
        });
        
        register("getMaxEnergyOutput", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getMaxEnergyOutput());
            }
        });
        
        register("getEnergy", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().energy);
            }
        });
    }

    @Override
    public String getType() {
        return "IC2EnergyStorageUnit";
    }

}
