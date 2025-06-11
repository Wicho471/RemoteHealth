package org.axolotlj.RemoteHealth.config.filt.defaults;

import org.axolotlj.RemoteHealth.common.Paths;
import org.axolotlj.RemoteHealth.config.PropertiesManager;

public class DefaultRealTimePleth {
    private static final PropertiesManager props = new PropertiesManager(Paths.DEFAULTS_REAL_TIME_FILTERS_PROPERTIES.substring(1));

    public static final int PLETH_BANDPASS_ORDER = Integer.parseInt(props.get("pleth.bandpass.order"));
    public static final double PLETH_BANDPASS_LOW = Double.parseDouble(props.get("pleth.bandpass.low"));
    public static final double PLETH_BANDPASS_HIGH = Double.parseDouble(props.get("pleth.bandpass.high"));
    public static final boolean PLETH_BANDPASS_ENABLED = Boolean.parseBoolean(props.get("pleth.bandpass.enabled"));

    public static final int PLETH_BANDSTOP_ORDER = Integer.parseInt(props.get("pleth.bandstop.order"));
    public static final double PLETH_BANDSTOP_LOW = Double.parseDouble(props.get("pleth.bandstop.low"));
    public static final double PLETH_BANDSTOP_HIGH = Double.parseDouble(props.get("pleth.bandstop.high"));
    public static final boolean PLETH_BANDSTOP_ENABLED = Boolean.parseBoolean(props.get("pleth.bandstop.enabled"));
}

