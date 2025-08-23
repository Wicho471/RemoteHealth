package org.axolotlj.remotehealth.mobile.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;

public class DualStackDns implements Dns {

    private final boolean preferIPv6;

    public DualStackDns(boolean preferIPv6) {
        this.preferIPv6 = preferIPv6;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        List<InetAddress> all = Dns.SYSTEM.lookup(hostname);
        if (all == null || all.isEmpty()) {
            throw new UnknownHostException("No addresses for " + hostname);
        }

        List<InetAddress> ipv4 = new ArrayList<>();
        List<InetAddress> ipv6 = new ArrayList<>();

        for (InetAddress addr : all) {
            if (addr instanceof java.net.Inet6Address) {
                ipv6.add(addr);
            } else {
                ipv4.add(addr);
            }
        }

        List<InetAddress> ordered = new ArrayList<>();
        if (preferIPv6) {
            ordered.addAll(ipv6);
            ordered.addAll(ipv4);
        } else {
            ordered.addAll(ipv4);
            ordered.addAll(ipv6);
        }

        return ordered;
    }
}