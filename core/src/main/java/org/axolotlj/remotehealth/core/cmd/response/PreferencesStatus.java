package org.axolotlj.remotehealth.core.cmd.response;

public record PreferencesStatus(
        String ssidSta,
        String passwordSta,
        String ssidAp,
        String passwordAp,
        boolean apEnabled,
        int oximeterBrightness
) {}
