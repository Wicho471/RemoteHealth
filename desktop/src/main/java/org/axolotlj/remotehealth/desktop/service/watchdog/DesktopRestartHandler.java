package org.axolotlj.remotehealth.desktop.service.watchdog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.service.watchdog.OnWatchdogTrigger;
import org.axolotlj.remotehealth.core.service.watchdog.ThreadDumpGenerator;

public class DesktopRestartHandler implements OnWatchdogTrigger {
	private final DataLogger dataLogger = Log.get();

	public DesktopRestartHandler() {
	}

	@Override
	public void onTriggered(long elapsedMs, String threadDump) {
		dataLogger.logWarn("[DesktopRestartHandler] Hang detectado. elapsed=" + elapsedMs + "ms");
		dataLogger.logFatal(ThreadDumpGenerator.generateThreadDump());
		try {
			ThreadDumpGenerator.generateThreadDump(elapsedMs, elapsedMs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Runtime.getRuntime().halt(0); // uso halt para forzar cierre rápido

		try {
			//relaunchAndHalt();
		} catch (Throwable t) {
			dataLogger.logException("Fallo relanzando aplicación", t);
			Runtime.getRuntime().halt(20);
		}
	}

	private void relaunchAndHalt() throws Exception {
		String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		String commandProperty = System.getProperty("sun.java.command", "");
		List<String> command = new ArrayList<>();

		// Si fue lanzado desde un JAR, obtenemos el jar path desde la CodeSource (de
		// esta clase)
		Optional<Path> maybeJar = getRunningJar();

		if (maybeJar.isPresent()) {
			command.add(javaBin);
			command.add("-jar");
			command.add(maybeJar.get().toAbsolutePath().toString());
		} else if (!commandProperty.isEmpty()) {
			// sun.java.command suele ser: "com.my.Main arg1 arg2" o "app.jar arg1"
			String[] parts = commandProperty.split(" ");
			String mainOrJar = parts[0];
			String[] args = Arrays.copyOfRange(parts, 1, parts.length);

			if (mainOrJar.endsWith(".jar")) {
				command.add(javaBin);
				command.add("-jar");
				command.add(mainOrJar);
				command.addAll(Arrays.asList(args));
			} else {
				command.add(javaBin);
				command.add("-cp");
				command.add(System.getProperty("java.class.path"));
				command.add(mainOrJar);
				command.addAll(Arrays.asList(args));
			}
		} else {
			dataLogger.logFatal("No se pudo determinar cómo relanzar la aplicación (no jar ni sun.java.command)");
		}

		if (!command.isEmpty()) {
			dataLogger.logInfo("Re-lanzando con: " + command.stream().collect(Collectors.joining(" ")));
			new ProcessBuilder(command).start();
		}

		// Esperar un poco para que el proceso hijo arranque y luego forzar el cierre
		// del actual
		try {
			Thread.sleep(400);
		} catch (InterruptedException ignored) {
		}
		dataLogger.logInfo("Halt forzado del proceso actual");
		Runtime.getRuntime().halt(0); // uso halt para forzar cierre rápido
	}

	private Optional<Path> getRunningJar() {
		try {
			CodeSource cs = getClass().getProtectionDomain().getCodeSource();
			if (cs != null && cs.getLocation() != null) {
				URI location = cs.getLocation().toURI();
				Path p = Paths.get(location);
				if (Files.isRegularFile(p))
					return Optional.of(p);
			}
		} catch (Exception ignored) {
		}
		return Optional.empty();
	}
}
