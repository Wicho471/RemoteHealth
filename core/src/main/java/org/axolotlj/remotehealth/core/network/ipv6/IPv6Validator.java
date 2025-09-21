package org.axolotlj.remotehealth.core.network.ipv6;

import static org.axolotlj.remotehealth.core.network.ipv6.IPv6Parser.parseIPv6;

import java.net.Inet6Address;
import java.net.InetAddress;

public class IPv6Validator {
	public static boolean isValidIPv6(String ipv6) {
		return parseIPv6(ipv6) != null;
	}

	public static boolean isGlobalIPv6(String ipv6) {
		Inet6Address addr = parseIPv6(ipv6);
		return isGlobalIPv6(addr);
	}

	public static boolean isGlobalIPv6(InetAddress addr) {
		if (addr == null) return false;
		if (addr instanceof Inet6Address) return checkGlobal(addr);
		return false;
	}

	public static boolean isGlobalIPv6(Inet6Address addr) {
		if (addr == null) return false;
		return checkGlobal(addr);
	}

	private static boolean checkGlobal(InetAddress addr) {
		return !(addr.isAnyLocalAddress() || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()
				|| addr.isLoopbackAddress() || addr.isMulticastAddress());
	}

	public static boolean isLocalIPv6(String ipv6) {
		Inet6Address addr = parseIPv6(ipv6);
		return isLocalIPv6(addr);
	}

	public static boolean isLocalIPv6(Inet6Address addr) {
		if (addr == null)
			return false;
		return addr.isLinkLocalAddress() || addr.isSiteLocalAddress();
	}
}
