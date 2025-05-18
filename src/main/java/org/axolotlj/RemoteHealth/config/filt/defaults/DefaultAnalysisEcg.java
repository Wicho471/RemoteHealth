package org.axolotlj.RemoteHealth.config.filt.defaults;

import org.axolotlj.RemoteHealth.config.PropertiesManager;
import org.axolotlj.RemoteHealth.util.paths.Paths;

public class DefaultAnalysisEcg {
    private static final PropertiesManager props = new PropertiesManager(Paths.DEFAULTS_ANALYSIS_FILTERS_PROPERTIES.substring(1));

    public static final int ECG_BANDPASS_ORDER = Integer.parseInt(props.get("ecg.bandpass.order"));
    public static final double ECG_BANDPASS_LOW = Double.parseDouble(props.get("ecg.bandpass.low"));
    public static final double ECG_BANDPASS_HIGH = Double.parseDouble(props.get("ecg.bandpass.high"));
    public static final boolean ECG_BANDPASS_ENABLED = Boolean.parseBoolean(props.get("ecg.bandpass.enabled"));

    public static final int ECG_BANDSTOP_ORDER = Integer.parseInt(props.get("ecg.bandstop.order"));
    public static final double ECG_BANDSTOP_LOW = Double.parseDouble(props.get("ecg.bandstop.low"));
    public static final double ECG_BANDSTOP_HIGH = Double.parseDouble(props.get("ecg.bandstop.high"));
    public static final boolean ECG_BANDSTOP_ENABLED = Boolean.parseBoolean(props.get("ecg.bandstop.enabled"));

    public static final String ECG_WAVELET_TYPE = props.get("ecg.wavelet.type");
    public static final int ECG_WAVELET_LEVEL = Integer.parseInt(props.get("ecg.wavelet.level"));
    public static final double ECG_WAVELET_THRESHOLD = Double.parseDouble(props.get("ecg.wavelet.threshold"));
    public static final boolean ECG_WAVELET_SOFT = Boolean.parseBoolean(props.get("ecg.wavelet.soft"));
    public static final boolean ECG_WAVELET_ENABLED = Boolean.parseBoolean(props.get("ecg.wavelet.enabled"));

    public static final int ECG_SG_WINDOW = Integer.parseInt(props.get("ecg.sg.window"));
    public static final int ECG_SG_POLY = Integer.parseInt(props.get("ecg.sg.poly"));
    public static final boolean ECG_SG_ENABLED = Boolean.parseBoolean(props.get("ecg.sg.enabled"));
}
