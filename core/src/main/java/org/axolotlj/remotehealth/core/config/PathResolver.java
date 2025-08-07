package org.axolotlj.remotehealth.core.config;

import java.nio.file.Path;

public interface PathResolver {
    Path resolveMainDir() throws NullPointerException;
}