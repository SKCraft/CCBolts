package com.sk89q.custombolts.cc;

import ic2.common.TileEntityNuclearReactor;

public class IC2NuclearReactorPeripheral extends PeripheralAdapter<TileEntityNuclearReactor> {
    
    public IC2NuclearReactorPeripheral(TileEntityNuclearReactor base) {
        super(base);
        
        register("getHeat", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getHeat());
            }
        });
        
        register("getMaxEnergyOutput", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getMaxEnergyOutput());
            }
        });
    }

    @Override
    public String getType() {
        return "IC2EnergyStorageUnit";
    }

}
