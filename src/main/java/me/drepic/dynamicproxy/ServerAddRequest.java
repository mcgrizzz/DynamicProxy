package me.drepic.dynamicproxy;

public class ServerAddRequest {

    private final String secret;
    private final String id;
    private final String host;
    private final int port;
    private final String motd;
    private final boolean restricted;

    public ServerAddRequest(String secret, String id, String host, int port, String motd, boolean restricted) {
        this.secret = secret;
        this.id = id;
        this.host = host;
        this.port = port;
        this.motd = motd;
        this.restricted = restricted;
    }

    public String getID() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getMotd() {
        return motd;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public String toString() {
        return "ServerAddRequest{" +
                "secret='" + secret + '\'' +
                ", id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", motd='" + motd + '\'' +
                ", restricted=" + restricted +
                '}';
    }
}
