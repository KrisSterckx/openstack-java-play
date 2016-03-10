package os;

import org.openstack4j.model.compute.FloatingIP;
import java.net.InetAddress;


public class FIP {
    private Connect os;
    private ServerHandle server;
    private FloatingIP floatingIp;

    public FIP(Connect os, ServerHandle server, FloatingIP floatingIp) {
        this.os = os;
        this.server = server;
        this.floatingIp = floatingIp;
    }

    public String ip() {
        return floatingIp.getFloatingIpAddress();
    }

    @Override
    public String toString() {
        return ip();
    }

    public ServerHandle server() {
        return server;
    }

    public RouterHandle router() {
        return server.network().router();
    }

    public boolean is_reachable() {
        try {
            // Does not work ?? :-((((  ....
            return InetAddress.getByAddress(toByteArray(ip())).isReachable(10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static byte[] toByteArray(String ipAddress) {
        String[] octets = ipAddress.split(java.util.regex.Pattern.quote("."));
        byte[] ip = new byte[4];
        for (int i = 0; i <= 3; i++) {
            ip[i] = (byte)(Integer.valueOf(octets[i]).intValue());
        }
        return ip;
    }
}
