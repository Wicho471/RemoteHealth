package org.axolotlj.remotehealth.core.util;

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
    
    /**
     * Verifica si es que se tiene una interfaz Inet6Address
     * @return 
     */
    public static boolean isSupportedIpv6() {
		try {
			for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
			    for (InetAddress address : java.util.Collections.list(ni.getInetAddresses())) {
			    	DATA_LOGGER.logDebug(address.getHostName() + " -> " + address.getHostAddress());
			        if (address instanceof java.net.Inet6Address) {
			        	DATA_LOGGER.logDebug("Encontrada direccion IPv6: " + address.getHostAddress());
			        	
			            return true;
			        }
			    }
			}
			DATA_LOGGER.logWarn("No se encontro direccion Ipv6");
			return false;
		} catch (SocketException e) {
			DATA_LOGGER.logException("Ocurrio un erro inesperado", e);
			return false;
		} 
	}

    /**
     * Obtiene la primera dirección IPv4 válida del host.
     *
     * @return String con la IPv4 encontrada, o null si no existe
     */
    public static String getIPv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            DATA_LOGGER.logException("Error al obtener IPv4", e);
        }
        return null;
    }
    
    /**
     * Obtiene la primera dirección IPv6 global del host.
     *
     * @return String con la IPv6 global, o null si no existe
     */
    public static String getGlobalIPv6() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (isGlobalIPv6Address(addr)) {
                        return addr.getHostAddress().split("%")[0]; // limpiar scope ID
                    }
                }
            }
        } catch (SocketException e) {
            DATA_LOGGER.logException("Error al obtener IPv6 global", e);
        }
        return null;
    }

    
    /**
     * Verifica si una IPv6 es global unicast (válida para Internet).
     */
    private static boolean isGlobalIPv6Address(InetAddress addr) {
        return (addr instanceof java.net.Inet6Address)
                && !addr.isLoopbackAddress()
                && !addr.isLinkLocalAddress()
                && !addr.isSiteLocalAddress();
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

