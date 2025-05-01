package org.axolotlj.RemoteHealth.service.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import umontreal.ssj.randvar.NormalGen;
import umontreal.ssj.rng.MRG32k3a;

import org.glassfish.tyrus.server.Server;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simulador de servidor WebSocket que envía datos de prueba en formato texto y
 * binario.
 */
public class WebSocketServerSimu {
	
	private static final MRG32k3a rng = new MRG32k3a();
	private static final NormalGen noiseGen = new NormalGen(rng, 0.0, 1.0); // Media 0, desviación estándar 1

	private static double t = 0.0;
	private static final double DT = 0.003; // 4ms = 250Hz


	private Server server;
	private ScheduledExecutorService executor;

	/**
	 * Inicia el servidor WebSocket en el puerto 8081.
	 * 
	 * @throws Exception Si ocurre un error al iniciar.
	 */
	public void start() {
		try {
			server = new Server("localhost", 8081, "", null, SimulatedEndpoint.class);
			server.start();
			System.out.println("Servidor WebSocket iniciado en ws://localhost:8081/simulator");
		} catch (Exception e) {
			System.err.println("No se pudo iniciar el simulador: " + e.getMessage());
		}
	}

	/**
	 * Detiene el servidor y sus tareas programadas.
	 * 
	 * @throws Exception Si ocurre un error al detener.
	 */
	public void stop() throws Exception {
		if (executor != null && !executor.isShutdown()) {
			executor.shutdownNow();
		}
		if (server != null) {
			server.stop();
			server = null;
		}
		System.out.println("WebSocket Server detenido.");
	}

	@ServerEndpoint("/simulator")
	public static class SimulatedEndpoint {

		private ScheduledExecutorService executor;

		@OnOpen
		public void onOpen(Session session) {
			executor = Executors.newSingleThreadScheduledExecutor();
			executor.scheduleAtFixedRate(() -> {
				try {
					long timestamp = System.currentTimeMillis();
					String simulatedMessage = Long.toHexString(timestamp) + generateSimulatedPayload();
					session.getBasicRemote().sendText(simulatedMessage);
				} catch (IOException e) {
					System.err.println("Error al enviar mensaje simulado: " + e.getMessage());
				}
			}, 5000, 3, TimeUnit.MILLISECONDS); // Envía cada 4ms directamente
		}

		@OnMessage
		public void onMessage(String message, Session session) {
			System.out.println("Mensaje recibido del cliente: " + message);
		}

		@OnClose
		public void onClose(Session session) {
			if (executor != null && !executor.isShutdown()) {
				executor.shutdownNow();
				try {
					if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
						System.err.println("Executor no se cerró correctamente para cliente: " + session.getId());
					}
				} catch (InterruptedException e) {
					System.err.println("Error al cerrar executor: " + e.getMessage());
				}
			}
		}
	}

	private static String generateSimulatedPayload() {
	    // Señal ECG simulada con ruido blanco gaussiano
	    double ecg = Math.sin(2 * Math.PI * 1 * t) + 0.5 * Math.sin(2 * Math.PI * 60 * t) + 0.2 * noiseGen.nextDouble();
	    int ecgHex = (int) Math.round(ecg * 1000 + 2048); // Escalar y centrar en 12 bits

	    // Señal IR simulada con ruido blanco gaussiano
	    double irSignal = 2048 + 200 * Math.sin(2 * Math.PI * 1.2 * t) + 0.1 * noiseGen.nextDouble();
	    int ir = (int) Math.round(irSignal);

	    // Señal RED simulada con ruido blanco gaussiano
	    double redSignal = 2048 + 180 * Math.cos(2 * Math.PI * 0.8 * t) + 0.1 * noiseGen.nextDouble();
	    int red = (int) Math.round(redSignal);

	    t += DT;

	    String hexEcg = Integer.toHexString(clamp(ecgHex, 0, 0xFFFF)).toUpperCase();
	    String hexIr = Integer.toHexString(clamp(ir, 0, 0xFFFF)).toUpperCase();
	    String hexRed = Integer.toHexString(clamp(red, 0, 0xFFFF)).toUpperCase();

	    return "," + hexEcg + ",0.99,36.5," + hexIr + "," + hexRed;
	}

	private static int clamp(int val, int min, int max) {
	    return Math.max(min, Math.min(max, val));
	}

}
