package org.axolotlj.remotehealth.core.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;

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
     * Registra en el log un informe detallado de todas las interfaces de red
     * que la aplicación puede detectar, incluyendo sus estados y direcciones.
     * Este método es crucial para diagnosticar problemas de conectividad en
     * entornos nativos.
     */
    public static void logNetworkInterfaces() {
        DATA_LOGGER.logDebug("==== INICIO REPORTE DE INTERFACES DE RED ====");
        try {
            for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                DATA_LOGGER.logDebug("Interfaz encontrada: " + ni.getDisplayName() + " (" + ni.getName() + ")");
                DATA_LOGGER.logDebug("  -> Estado: " + (ni.isUp() ? "Activa" : "Inactiva"));
                DATA_LOGGER.logDebug("  -> Es Virtual: " + (ni.isVirtual() ? "Sí" : "No"));
                DATA_LOGGER.logDebug("  -> Es Loopback: " + (ni.isLoopback() ? "Sí" : "No"));
                
                if (ni.getHardwareAddress() != null) {
                    // Formatear la dirección MAC para que sea legible
                    byte[] mac = ni.getHardwareAddress();
                    StringBuilder macBuilder = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        macBuilder.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                    }
                    DATA_LOGGER.logDebug("  -> MAC: " + macBuilder.toString());
                } else {
                    DATA_LOGGER.logDebug("  -> MAC: No disponible");
                }

                for (InetAddress address : java.util.Collections.list(ni.getInetAddresses())) {
                    String ipType = (address instanceof java.net.Inet6Address) ? "IPv6" : "IPv4";
                    String ipAddress = address.getHostAddress();
                    
                    // En IPv6, a veces las direcciones tienen un scope ID (e.g., %wlan0)
                    if (address instanceof java.net.Inet6Address) {
                       ipAddress = ipAddress.split("%")[0]; // Limpiar el scope id para claridad
                    }
                    
                    DATA_LOGGER.logDebug("  -> Dirección [" + ipType + "]: " + ipAddress);
                    DATA_LOGGER.logDebug("    -> Es Link-Local: " + (address.isLinkLocalAddress() ? "Sí" : "No"));
                    DATA_LOGGER.logDebug("    -> Es Global: " + (!address.isSiteLocalAddress() && !address.isLinkLocalAddress() && !address.isLoopbackAddress() ? "Sí" : "No"));
                }
            }
        } catch (SocketException e) {
            DATA_LOGGER.logException("Error al obtener interfaces de red", e);
        }
        DATA_LOGGER.logDebug("==== FIN REPORTE DE INTERFACES DE RED ====");
    }
}

