package org.axolotlj.remotehealth.core.network.ipv6;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IPv6Network {

	private final static DataLogger DATA_LOGGER = Log.get();

	/**
	 * Verifica si hay conectividad IPv6 global probando con un servidor público.
	 */
	public static boolean isGlobalIPv6Available() {
		OkHttpClient client = new OkHttpClient();
		String testUrl = "http://[2606:4700:4700::1111]/";

		Request request = new Request.Builder().url(testUrl).build();

		try (Response response = client.newCall(request).execute()) {
			return response.isSuccessful();
		} catch (Exception e) {
			DATA_LOGGER.logWarn("Error al revisar conectividad IPv6 -> " + e.getMessage());
			return false;
		} finally {
			client.dispatcher().executorService().shutdown();
			client.connectionPool().evictAll();

			if (client.cache() != null) {
				try {
					client.cache().close();
				} catch (IOException e) {
					DATA_LOGGER.logException("Error cerrando cache de OkHttp", e);
				}
			}
		}
	}

	/**
	 * Verifica si el host soporta IPv6.
	 */
	public static boolean isSupportedIPv6() {
		try {
			for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
				for (InetAddress address : java.util.Collections.list(ni.getInetAddresses())) {
					DATA_LOGGER.logDebug(address.getHostName() + " -> " + address.getHostAddress());
					if (address instanceof Inet6Address) {
						DATA_LOGGER.logDebug("Encontrada dirección IPv6: " + address.getHostAddress());
						return true;
					}
				}
			}
			DATA_LOGGER.logWarn("No se encontró dirección IPv6");
			return false;
		} catch (SocketException e) {
			DATA_LOGGER.logException("Error verificando soporte IPv6", e);
			return false;
		}
	}

	/**
	 * Obtiene la primera dirección IPv6 global del host.
	 */
	public static String getGlobalIPv6() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				if (!ni.isUp() || ni.isLoopback() || ni.isVirtual())
					continue;

				Enumeration<InetAddress> addresses = ni.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (IPv6Validator.isGlobalIPv6(addr)) {
						return addr.getHostAddress().split("%")[0]; // limpiar scope ID
					}
				}
			}
		} catch (SocketException e) {
			DATA_LOGGER.logException("Error al obtener IPv6 global", e);
		}
		return "::";
	}
}
