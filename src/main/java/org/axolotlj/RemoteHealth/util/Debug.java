package org.axolotlj.RemoteHealth.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Debug {
	private static Set<String> before;
	
	public static void printAllThreads(boolean skip) {
		Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();

		for (Thread thread : allThreads.keySet()) {
			String threadName = thread.getName();
			if(skip) {
				if(threadName.startsWith("Grizzly") || threadName.startsWith("grizzly")) {
					
				}
			}
				
		    System.out.println("Hilo activo: " + threadName);
		}

	}
	
    /**
     * Compara dos listas de nombres de hilos y muestra los hilos nuevos.
     * 
     * @param oldSet Set con los nombres de hilos antes.
     * @param newSet Set con los nombres de hilos después.
     */
    public static void printNewThreads(Set<String> oldSet, Set<String> newSet) {
        Set<String> newThreads = new HashSet<>(newSet);
        newThreads.removeAll(oldSet);  // Quita los que ya existían
        if (newThreads.isEmpty()) {
            System.out.println("No hay nuevos hilos.");
        } else {
            System.out.println("🔍 Hilos nuevos detectados:");
            newThreads.stream()
                      .sorted()
                      .forEach(threadName -> System.out.println("🆕 " + threadName));
        }
    }

    /**
     * Captura los nombres de todos los hilos activos actualmente.
     */
    public static Set<String> getCurrentThreadNames() {
        Set<String> names = new HashSet<>();
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            names.add(t.getName());
        }
        return names;
    }
    
    public static void saveLastThreads() {
    	before = getCurrentThreadNames();
	}
    
    public static Set<String> getBefore() {
		return before;
	}
}
