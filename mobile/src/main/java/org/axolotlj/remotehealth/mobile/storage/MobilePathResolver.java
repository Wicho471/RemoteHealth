package org.axolotlj.remotehealth.mobile.storage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.axolotlj.remotehealth.core.config.PathResolver;

import com.gluonhq.attach.storage.StorageService;

/**
 * Resolvedor de rutas para almacenamiento en dispositivos móviles. Se encarga
 * de obtener el directorio principal donde la aplicación almacenará los
 * archivos necesarios, manejando tanto almacenamiento privado como público
 * según disponibilidad.
 */
public class MobilePathResolver implements PathResolver {

	@Override
	public Path resolveMainDir() {
		try {
			String caller = StackWalker.getInstance().walk(frames -> frames.skip(1).findFirst()
					.map(f -> f.getClassName() + "." + f.getMethodName()).orElse("desconocido"));

			System.out.println("[Remote Health] Iniciando MobilePathResolver (llamado desde: " + caller + ")");

			Optional<StorageService> storage = StorageService.create();
			if (storage.isEmpty()) {
				System.out.println("[Remote Health] StorageService no disponible");
				return fallbackPath();
			}

			System.out.println("[Remote Health] StorageService disponible");

//			Optional<File> publicStorage = storage.flatMap(s -> s.getPublicStorage("Documents"));
//			if (publicStorage.isPresent()) {
//				Path path = publicStorage.get().toPath().resolve("RemoteHealth");
//				System.out.println("[Remote Health] Usando almacenamiento público: " + path);
//				return path;
//			}

			Optional<File> privateStorage = storage.flatMap(StorageService::getPrivateStorage);
			if (privateStorage.isPresent()) {
				Path path = privateStorage.get().toPath().resolve("RemoteHealth");
				System.out.println("[Remote Health] Usando almacenamiento privado: " + path);
				return path;
			}

			System.out.println("[Remote Health] Ningún almacenamiento válido encontrado, usando fallback");
			return fallbackPath();

		} catch (Exception e) {
			System.err.println("[Remote Health] Error en resolveMainDir: " + e.getMessage());
			return fallbackPath();
		}
	}

	private Path fallbackPath() {
		String os = System.getProperty("os.name").toLowerCase();
		Path baseDir;
		if (os.contains("win")) {
			baseDir = Paths.get(System.getenv("LOCALAPPDATA"));
		} else if (os.contains("mac")) {
			baseDir = Paths.get(System.getProperty("user.home"), "Library", "Application Support");
		} else {
			baseDir = Paths.get(System.getProperty("user.home"), ".config");
		}
		Path path = baseDir.resolve("RemoteHealth");
		System.out.println("[Remote Health] Ruta de fallback: " + path);
		return path;
	}
}
