package org.axolotlj.remotehealth.core.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.concurrent.Task;

public class AsyncExecutor {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private static ThreadFactory namedThreadFactory(String baseName) {
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(baseName + "-" + poolNumber.getAndIncrement());
            return thread;
        };
    }

    public static <T> void runFilterTask(String executorName, Supplier<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        ExecutorService executor = Executors.newSingleThreadExecutor(namedThreadFactory(executorName));

        Task<T> javafxTask = new Task<>() {
            @Override
            protected T call() {
                return task.get();
            }
        };

        javafxTask.setOnSucceeded(e -> {
            onSuccess.accept(javafxTask.getValue());
            executor.shutdown();
        });

        javafxTask.setOnFailed(e -> {
            onError.accept(javafxTask.getException());
            executor.shutdown(); 
        });

        executor.submit(javafxTask);
    }
}
