package org.axolotlj.remotehealth.core.cmd.response;

public record NetworkStatus(boolean STA, boolean internet, String ipv4, String ipv6) {
	
}
