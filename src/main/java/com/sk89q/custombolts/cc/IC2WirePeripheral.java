package com.sk89q.custombolts.cc;

import ic2.api.IEnergyConductor;

public class IC2WirePeripheral extends PeripheralAdapter<IEnergyConductor> {
    
    public IC2WirePeripheral(IEnergyConductor base) {
        super(base);
        
        register("getConductionLoss", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getConductionLoss());
            }
        });
        
        register("getConductorBreakdownEnergy", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getConductorBreakdownEnergy());
            }
        });
        
        register("getInsulationEnergyAbsorption", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getInsulationEnergyAbsorption());
            }
        });
        
        register("getInsulationBreakdownEnergy", new ApiCallable<Object[]>() {
            @Override
            public Object[] call(Object[] args) {
                return wrap(getBase().getInsulationBreakdownEnergy());
            }
        });
    }

    @Override
    public String getType() {
        return "IC2EnergyConductor";
    }

}
