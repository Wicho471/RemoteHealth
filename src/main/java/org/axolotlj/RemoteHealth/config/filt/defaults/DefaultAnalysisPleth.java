package org.axolotlj.RemoteHealth.config.filt.defaults;

import org.axolotlj.RemoteHealth.config.PropertiesManager;
import org.axolotlj.RemoteHealth.util.paths.Paths;

public class DefaultAnalysisPleth {
    private static final PropertiesManager props = new PropertiesManager(Paths.DEFAULTS_ANALYSIS_FILTERS_PROPERTIES.substring(1));

    public static final int PLETH_BANDPASS_ORDER = Integer.parseInt(props.get("pleth.bandpass.order"));
    public static final double PLETH_BANDPASS_LOW = Double.parseDouble(props.get("pleth.bandpass.low"));
    public static final double PLETH_BANDPASS_HIGH = Double.parseDouble(props.get("pleth.bandpass.high"));
    public static final boolean PLETH_BANDPASS_ENABLED = Boolean.parseBoolean(props.get("pleth.bandpass.enabled"));

    public static final int PLETH_BANDSTOP_ORDER = Integer.parseInt(props.get("pleth.bandstop.order"));
    public static final double PLETH_BANDSTOP_LOW = Double.parseDouble(props.get("pleth.bandstop.low"));
    public static final double PLETH_BANDSTOP_HIGH = Double.parseDouble(props.get("pleth.bandstop.high"));
    public static final boolean PLETH_BANDSTOP_ENABLED = Boolean.parseBoolean(props.get("pleth.bandstop.enabled"));

    public static final String PLETH_WAVELET_TYPE = props.get("pleth.wavelet.type");
    public static final int PLETH_WAVELET_LEVEL = Integer.parseInt(props.get("pleth.wavelet.level"));
    public static final double PLETH_WAVELET_THRESHOLD = Double.parseDouble(props.get("pleth.wavelet.threshold"));
    public static final boolean PLETH_WAVELET_SOFT = Boolean.parseBoolean(props.get("pleth.wavelet.soft"));
    public static final boolean PLETH_WAVELET_ENABLED = Boolean.parseBoolean(props.get("pleth.wavelet.enabled"));

    public static final int PLETH_SG_WINDOW = Integer.parseInt(props.get("pleth.sg.window"));
    public static final int PLETH_SG_POLY = Integer.parseInt(props.get("pleth.sg.poly"));
    public static final boolean PLETH_SG_ENABLED = Boolean.parseBoolean(props.get("pleth.sg.enabled"));
}
