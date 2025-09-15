package org.axolotlj.remotehealth.core.cmd.response;

public record PreferencesStatus(String SSID_AP, String passwordAP, boolean APEnabled, int oximeterBrightnes) {

}
