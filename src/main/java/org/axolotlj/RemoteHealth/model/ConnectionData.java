package org.axolotlj.RemoteHealth.model;

import java.net.URI;
import java.util.UUID;

public class ConnectionData {
	private UUID uuid;
    private String ipv4;
    private String ipv6;
    private String path;
    private int port;
    private String name;
    private URI uri4;
    private URI uri6;
    
    public ConnectionData(UUID uuid, String ipV4, String ipV6, String path, int port, String name) {
    	this.uuid = uuid;
    	this.ipv4 = ipV4;
        this.ipv6 = ipV6;
        this.path = path;
        this.port = port;
        this.name = name;
        this.uri4 = createUri(ipV4, false);
        this.uri6 = createUri(ipV6, true);
    }

    private URI createUri(String ip, boolean isIPv6) {
        if (ip == null || ip.isBlank()) return null;

        String host = isIPv6 ? "[" + ip + "]" : ip;
        String uriStr = "ws://" + host + ":" + port + path;
        return URI.create(uriStr);
    }

    public UUID getUuid() {
		return uuid;
	}
    
    public URI getUri4() {
        return uri4;
    }

    public URI getUri6() {
        return uri6;
    }

    public String getIpV4() {
        return ipv4;
    }

    public String getIpV6() {
        return ipv6;
    }

    public String getPath() {
        return path;
    }

    public int getPort() {
        return port;
    }
    
    public String getName() {
		return name;
	}
    
    public void setName(String name) {
		this.name = name;
	}
    
    public void setIpv4(String ipv4) {
		this.ipv4 = ipv4;
	}
    
    public void setIpv6(String ipv6) {
		this.ipv6 = ipv6;
	}
    
    public void setPort(int port) {
		this.port = port;
	}
    
    public void setPort(String port) {
		this.port = Integer.valueOf(port);
	}
    
    public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

    public void setPath(String path) {
		this.path = path;
	}
    
    @Override
    public String toString() {
        return "ConnectionData{" +
                "ipV4='" + ipv4 + '\'' +
                ", ipV6='" + ipv6 + '\'' +
                ", path='" + path + '\'' +
                ", port=" + port +
                ", uri4=" + uri4 +
                ", uri6=" + uri6 +
                '}';
    }
}
