package org.axolotlj.RemoteHealth.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class GCInvoker {
    public static void triggerGC() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        System.out.println("Antes del GC:");
        printMemoryUsage(memoryBean);
        
        System.out.println("Solicitando GC...");
        memoryBean.gc();

        System.out.println("Después del GC:");
        printMemoryUsage(memoryBean);
    }

    private static void printMemoryUsage(MemoryMXBean memoryBean) {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();

        System.out.printf("Heap usado: %d MB de %d MB%n",
                heap.getUsed() / (1024 * 1024),
                heap.getCommitted() / (1024 * 1024));

        System.out.printf("Non-Heap usado: %d MB de %d MB%n",
                nonHeap.getUsed() / (1024 * 1024),
                nonHeap.getCommitted() / (1024 * 1024));
    }
}
