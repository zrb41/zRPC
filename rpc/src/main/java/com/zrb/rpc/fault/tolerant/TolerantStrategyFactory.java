package com.zrb.rpc.fault.tolerant;

import com.zrb.rpc.spi.SpiLoader;

public class TolerantStrategyFactory {
    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    private static final TolerantStrategy DEFAULT_TOLERANT_STRATEGY=new FailFastTolerantStrategy();

    public static TolerantStrategy getInstance(String key){
        return SpiLoader.getInstance(TolerantStrategy.class,key);
    }
}
