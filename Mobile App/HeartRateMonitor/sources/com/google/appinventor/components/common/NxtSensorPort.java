package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

public enum NxtSensorPort implements OptionList<String> {
    Port1("1", 0),
    Port2("2", 1),
    Port3("3", 2),
    Port4("4", 3);
    
    private static final Map<String, NxtSensorPort> lookup = null;
    private final int intValue;
    private final String value;

    static {
        int i;
        lookup = new HashMap();
        for (NxtSensorPort port : values()) {
            lookup.put(port.toUnderlyingValue(), port);
        }
    }

    private NxtSensorPort(String port, int intPort) {
        this.value = port;
        this.intValue = intPort;
    }

    public String toUnderlyingValue() {
        return this.value;
    }

    public Integer toInt() {
        return Integer.valueOf(this.intValue);
    }

    public static NxtSensorPort fromUnderlyingValue(String port) {
        return lookup.get(port);
    }
}
