package org.axolotlj.RemoteHealth.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.Enumeration;
import java.util.function.Consumer;

import org.glassfish.tyrus.client.ClientManager;

import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

/**
 * Utilidad para validar estados de conectividad de red.
 */
public class NetworkUtil {

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
        return isReachable("8.8.8.8"); // Google DNS
    }

    /**
     * Verifica si hay conectividad IPv6 global.
     *
     * @return true si puede alcanzar una dirección IPv6 pública
     */
    public static boolean isGlobalIPv6Available() {
        try {
            InetAddress ipv6 = InetAddress.getByName("2001:4860:4860::8888"); // Google IPv6 DNS
            return ipv6.isReachable(1000);
        } catch (Exception e) {
            System.err.println("NetworkUtil::isGlobalIPv6Available - Error IPv6: " + e.getMessage());
            return false;
        }
    }
    
    @Deprecated
    public static void ping(URI uri, Consumer<Boolean> resultHandler) {
        ClientManager client = ClientManager.createClient();
        try {
			client.asyncConnectToServer(new Endpoint() {
			    @Override
			    public void onOpen(Session session, EndpointConfig config) {
			        try {
			            session.close(); // conexión exitosa, cerrar de inmediato
			        } catch (Exception ignored) { }
			        resultHandler.accept(true);
			    }

			    @Override
			    public void onError(Session session, Throwable thr) {
			        resultHandler.accept(false);
			    }
			}, uri);
		} catch (DeploymentException e) {
			resultHandler.accept(false);
		}
    }
}

