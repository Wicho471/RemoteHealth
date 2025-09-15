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
		CommandType type = CommandResponseParser.identifyType(json);

		CompletableFuture<String> future = pending.remove(type);
		if (future != null) {
			future.complete(json);
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
}
