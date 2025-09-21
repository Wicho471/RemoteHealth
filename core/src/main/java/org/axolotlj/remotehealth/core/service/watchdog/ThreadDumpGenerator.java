package org.axolotlj.remotehealth.core.service.watchdog;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.axolotlj.remotehealth.core.config.ConfigFileHelper;

public class ThreadDumpGenerator {

	/**
	 * Genera un diagnóstico completo del sistema (información básica + memoria +
	 * thread dump).
	 * 
	 * @return String con toda la información del estado actual de la JVM
	 */
	public static String generateThreadDump() {
		StringBuilder sb = new StringBuilder();
		String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

		sb.append("==== WATCHDOG THREAD DUMP ====\n");
		sb.append("Generated at: ").append(ts).append("\n");
		sb.append("PID: ").append(ProcessHandle.current().pid()).append("\n");
		sb.append("Java: ").append(System.getProperty("java.version")).append("\n");
		sb.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version"))
				.append("\n\n");

		// Memory
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		sb.append("=== Memory ===\n");
		sb.append("Heap: ").append(mbean.getHeapMemoryUsage()).append("\n");
		sb.append("NonHeap: ").append(mbean.getNonHeapMemoryUsage()).append("\n\n");

		// Threads
		ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] infos = tbean.dumpAllThreads(true, true);

		sb.append("=== Thread dump ===\n");
		for (ThreadInfo ti : infos) {
			sb.append("\"").append(ti.getThreadName()).append("\" Id=").append(ti.getThreadId()).append(" ")
					.append(ti.getThreadState()).append("\n");
			if (ti.getLockName() != null) {
				sb.append("   Locked on: ").append(ti.getLockName()).append("\n");
			}
			if (ti.getLockOwnerName() != null) {
				sb.append("   Lock owner: ").append(ti.getLockOwnerName()).append("\n");
			}
			for (StackTraceElement ste : ti.getStackTrace()) {
				sb.append("   at ").append(ste).append("\n");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Genera un dump con: timestamp, sistema, memoria, thread dump (locks,
	 * stackframes)
	 */
	public static Path generateThreadDump(long elapsed, long lastResponseAt) throws IOException {
		String ts = new SimpleDateFormat("yyyyMMdd-HHmmssSSS").format(new Date());
		Path out = ConfigFileHelper.getCrashReportDir().resolve("watchdog-dump-" + ts + ".log");

		try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
			w.write("Watchdog dump generated: " + ts);
			w.newLine();
			w.write("Elapsed (ms): " + elapsed);
			w.newLine();
			w.write("Last response at: " + lastResponseAt + " (" + new Date(lastResponseAt) + ")");
			w.newLine();
			w.newLine();

			// System info
			w.write("=== System info ===");
			w.newLine();
			w.write("os.name=" + System.getProperty("os.name"));
			w.newLine();
			w.write("os.version=" + System.getProperty("os.version"));
			w.newLine();
			w.write("java.version=" + System.getProperty("java.version"));
			w.newLine();
			w.write("java.home=" + System.getProperty("java.home"));
			w.newLine();
			w.write("user.dir=" + System.getProperty("user.dir"));
			w.newLine();
			w.write("pid=" + ProcessHandle.current().pid());
			w.newLine();
			w.newLine();

			// Memory
			w.write("=== Memory ===");
			w.newLine();
			MemoryMXBean m = ManagementFactory.getMemoryMXBean();
			w.write("HeapMemoryUsage: " + m.getHeapMemoryUsage().toString());
			w.newLine();
			w.write("NonHeapMemoryUsage: " + m.getNonHeapMemoryUsage().toString());
			w.newLine();
			w.newLine();

			// Thread dump
			w.write("=== Thread dump ===");
			w.newLine();
			ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
			boolean canGetLockedMonitors = tbean.isObjectMonitorUsageSupported();
			ThreadInfo[] infos = tbean.dumpAllThreads(true, true);

			for (ThreadInfo ti : infos) {
				w.write(String.format("\"%s\" Id=%d %s", ti.getThreadName(), ti.getThreadId(), ti.getThreadState()));
				w.newLine();
				if (ti.getLockName() != null) {
					w.write("\tLocked on: " + ti.getLockName());
					w.newLine();
				}
				if (ti.getLockOwnerName() != null) {
					w.write("\tLock owner: " + ti.getLockOwnerName() + " Id=" + ti.getLockOwnerId());
					w.newLine();
				}
				for (StackTraceElement ste : ti.getStackTrace()) {
					w.write("\t at " + ste.toString());
					w.newLine();
				}
				// monitors
				if (canGetLockedMonitors) {
					for (MonitorInfo mi : ti.getLockedMonitors()) {
						w.write("\t - locked monitor: " + mi + " @ " + mi.getLockedStackDepth());
						w.newLine();
					}
				}
				w.newLine();
			}
			w.flush();
		}
		System.out.println("Thread dump escrito en: " + out.toAbsolutePath());
		return out;
	}
}
