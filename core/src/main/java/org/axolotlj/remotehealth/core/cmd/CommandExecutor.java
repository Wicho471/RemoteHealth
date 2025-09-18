package org.axolotlj.remotehealth.core.cmd;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CommandExecutor {
	private final CommandCommunicator communicator;
	private final Map<CommandType, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

	public CommandExecutor(CommandCommunicator communicator) {
		this.communicator = communicator;
		communicator.onDataReceived(this::handleResponse);
	}

	public CompletableFuture<String> sendCommandAndWait(CommandType command, long timeoutMillis) {
		CompletableFuture<String> future = new CompletableFuture<>();
		pending.put(command, future);

		communicator.sendCommand(command.getCommandText());

		// Timeout
		CompletableFuture<String> timeoutFuture = failAfter(timeoutMillis);
		return future.applyToEither(timeoutFuture, resp -> resp);
	}

	private void handleResponse(String json) {
	    if (!json.startsWith("{"))
	        return;

	    System.out.println("Respuesta recibida: " + json);

	    CommandType type = CommandResponseParser.identifyType(json);
	    System.out.println("Tipo identificado: " + type);

	    CompletableFuture<String> future = pending.remove(type);
	    if (future != null) {
	        System.out.println("Completando future para " + type);
	        future.complete(json);
	    } else {
	        System.out.println("No había future pendiente para " + type);
	    }
	}

	private CompletableFuture<String> failAfter(long timeoutMillis) {
		CompletableFuture<String> promise = new CompletableFuture<>();
		new Thread(() -> {
			try {
				Thread.sleep(timeoutMillis);
				promise.completeExceptionally(new RuntimeException("Timeout esperando respuesta"));
			} catch (InterruptedException ignored) {
			}
		}).start();
		return promise;
	}

	/**
	 * Ejecuta un comando PING y mide el tiempo de respuesta en milisegundos.
	 * 
	 * @param timeoutMillis Tiempo máximo a esperar antes de fallar.
	 * @return CompletableFuture con el tiempo de respuesta en milisegundos.
	 */
	public CompletableFuture<Long> measurePing(long timeoutMillis) {
		long start = System.nanoTime();
		return sendCommandAndWait(CommandType.PING, timeoutMillis).thenApply(resp -> {
			long end = System.nanoTime();
			return (end - start) / 1_000_000; // nanos a ms
		});
	}
}
