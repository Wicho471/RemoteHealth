package org.axolotlj.remotehealth.desktop.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.axolotlj.remotehealth.core.config.PathResolver;

public class DesktopPathResolver implements PathResolver {
    @Override
    public Path resolveMainDir() {
        String os = System.getProperty("os.name").toLowerCase();
        Path baseDir;
        if (os.contains("win")) {
            baseDir = Paths.get(System.getenv("LOCALAPPDATA"));
        } else if (os.contains("mac")) {
            baseDir = Paths.get(System.getProperty("user.home"), "Library", "Application Support");
        } else {
            baseDir = Paths.get(System.getProperty("user.home"), ".config");
        }
        return baseDir.resolve("RemoteHealth");
    }
}
