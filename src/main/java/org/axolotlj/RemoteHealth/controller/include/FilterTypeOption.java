package org.axolotlj.RemoteHealth.controller.include;

public enum FilterTypeOption {
    ECG_ANALYSIS("ECG Analisis"),
    PLETH_ANALYSIS("Pleth Analisis"),
    ECG_REAL_TIME("ECG para tiempo real"),
    PLETH_REAL_TIME("Pleth para tiempo real");

    private final String displayName;

    private FilterTypeOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
