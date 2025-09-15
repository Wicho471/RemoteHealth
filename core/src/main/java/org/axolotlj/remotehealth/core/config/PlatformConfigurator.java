package org.axolotlj.remotehealth.core.config;

public interface PlatformConfigurator {

	void checkPaths();

	void getDeviceInfo();

	void getRuntimeArgs();

	void devConfigs();
}
