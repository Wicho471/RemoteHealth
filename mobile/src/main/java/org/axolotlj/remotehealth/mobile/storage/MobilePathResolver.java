package org.axolotlj.remotehealth.mobile.storage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.axolotlj.remotehealth.core.config.PathResolver;

import com.gluonhq.attach.storage.StorageService;

public class MobilePathResolver implements PathResolver {
	
    @Override
    public Path resolveMainDir() {
        Optional<StorageService> storage = StorageService.create();
        Optional<File> base = storage.flatMap(StorageService::getPrivateStorage);

        if (base.isPresent()) {
            return base.get().toPath().resolve("RemoteHealth");
        } else {
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
}
