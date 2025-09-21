package org.axolotlj.remotehealth.core.service.watchdog;

/**
 * Acción que se ejecuta cuando el watchdog detecta un timeout.
 */
public interface OnWatchdogTrigger {
    void onTriggered(long elapsedMs, String threadDump);
}
