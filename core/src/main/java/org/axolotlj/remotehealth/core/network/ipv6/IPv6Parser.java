package org.axolotlj.remotehealth.core.network.ipv6;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv6Parser {
    public static Inet6Address parseIPv6(String ipv6) {
        if (ipv6 == null || ipv6.isBlank()) return null;
        try {
            InetAddress addr = InetAddress.getByName(ipv6);
            if (addr instanceof Inet6Address) {
                return (Inet6Address) addr;
            }
        } catch (UnknownHostException ignored) {}
        return null;
    }
}
