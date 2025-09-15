package org.axolotlj.remotehealth.core.cmd.response;

public record SensorStatus(boolean infrarred, boolean oximeter, boolean accelerometer) {

}
