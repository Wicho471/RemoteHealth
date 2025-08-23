package org.axolotlj.remotehealth.core.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Utilidad para validar estados de conectividad de red.
 */
public class NetworkUtil {
	
	public static final DataLogger DATA_LOGGER = Log.get();

    public static boolean isReachable(String ip) {
    	if (ip == null) return false;
        try {
            return InetAddress.getByName(ip).isReachable(1000); // timeout: 1000ms
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el host tiene al menos una interfaz de red activa y no loopback.
     *
     * @return true si hay una interfaz activa
     */
    @Deprecated
    public static boolean hasActiveNetworkInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isLoopback() && iface.isUp()) {
                    return true;
                }
            }
        } catch (SocketException e) {
            System.err.println("NetworkUtil::hasActiveNetworkInterface - Error al verificar interfaces: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Verifica si el host tiene al menos una interfaz de red habilitada (excluyendo loopback y virtuales).
     *
     * @return true si existe una interfaz de red habilitada y no loopback.
     */
    public static boolean hasEnabledNetworkInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isLoopback() && !iface.isVirtual() && iface.supportsMulticast()) {
                    return true;
                }
            }
        } catch (SocketException e) {
            System.err.println("NetworkUtil::hasEnabledNetworkInterface - Error al verificar interfaces: " + e.getMessage());
        }
        return false;
    }
    

    /**
     * Verifica si el host tiene acceso a la red local (LAN), independientemente del tipo de conexión.
     *
     * @return true si se puede alcanzar una dirección dentro de la red local
     */
    public static boolean isLocalNetworkAvailable() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                var addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && isReachable(addr.getHostAddress())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("NetworkUtil::isLocalNetworkAvailable - Error verificando red local: " + e.getMessage());
        }
        return false;
    }


    /**
     * Verifica si hay acceso a Internet usando una IP pública confiable.
     *
     * @return true si hay conexión a Internet
     */
    public static boolean isInternetAvailable() {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
		    .url("https://www.google.com/generate_204")
		    .build();
		try (Response response = client.newCall(request).execute()) {
		    return response.isSuccessful();
		} catch (Exception e) {
			DATA_LOGGER.logException("Ocurrio un error al intentar revisar conetividad a internet", e);
		} finally {
		    client.dispatcher().executorService().shutdown();
		    client.connectionPool().evictAll();
		    
		    if (client.cache() != null) {
		        try {
					client.cache().close();
				} catch (IOException e) {
					DATA_LOGGER.logException("Ocurrio un error eliminando cache", e);
				}
		    }
		}
		return false;
    }

    /**
     * Verifica si hay conectividad IPv6 global.
     *
     * @return true si puede alcanzar una dirección IPv6 pública
     */
    public static boolean isGlobalIPv6Available() {
        OkHttpClient client = new OkHttpClient();
        String testUrl = "http://[2606:4700:4700::1111]/";

        Request request = new Request.Builder()
                .url(testUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
        	DATA_LOGGER.logException("Ocurrio un error al intentar revisar conetividad a internet", e);
            return false;
        } finally {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            
		    if (client.cache() != null) {
		        try {
					client.cache().close();
				} catch (IOException e) {
					DATA_LOGGER.logException("Ocurrio un error eliminando cache", e);
				}
		    }
        }
    }
    
    public static boolean isSupportedIpv6() {
		try {
			for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
			    for (InetAddress address : java.util.Collections.list(ni.getInetAddresses())) {
			        if (address instanceof java.net.Inet6Address) {
			        	DATA_LOGGER.logDebug("Encontrada direccion IPv6: " + address.getHostAddress());
			            return true;
			        }
			    }
			}
			return false;
		} catch (SocketException e) {
			DATA_LOGGER.logException("Ocurrio un erro inesperado", e);
			return false;
		} 
	}
}

