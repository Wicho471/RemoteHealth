package org.axolotlj.remotehealth.core.network.ipv4;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;

public class IPv4Network {
	
	private final static DataLogger DATA_LOGGER = Log.get();

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
}
