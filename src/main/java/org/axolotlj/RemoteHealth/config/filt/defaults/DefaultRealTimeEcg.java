package org.axolotlj.RemoteHealth.config.filt.defaults;

import org.axolotlj.RemoteHealth.common.Paths;
import org.axolotlj.RemoteHealth.config.PropertiesManager;

public class DefaultRealTimeEcg {
    private static final PropertiesManager props = new PropertiesManager(Paths.DEFAULTS_REAL_TIME_FILTERS_PROPERTIES.substring(1));

    public static final int ECG_BANDPASS_ORDER = Integer.parseInt(props.get("ecg.bandpass.order"));
    public static final double ECG_BANDPASS_LOW = Double.parseDouble(props.get("ecg.bandpass.low"));
    public static final double ECG_BANDPASS_HIGH = Double.parseDouble(props.get("ecg.bandpass.high"));
    public static final boolean ECG_BANDPASS_ENABLED = Boolean.parseBoolean(props.get("ecg.bandpass.enabled"));

    public static final int ECG_BANDSTOP_ORDER = Integer.parseInt(props.get("ecg.bandstop.order"));
    public static final double ECG_BANDSTOP_LOW = Double.parseDouble(props.get("ecg.bandstop.low"));
    public static final double ECG_BANDSTOP_HIGH = Double.parseDouble(props.get("ecg.bandstop.high"));
    public static final boolean ECG_BANDSTOP_ENABLED = Boolean.parseBoolean(props.get("ecg.bandstop.enabled"));
}