package os;

import org.openstack4j.model.compute.Server;


public class ServerHandle {
    private Connect os;
    private NetworkHandle network;
    private Server server;

    public ServerHandle(Connect os, NetworkHandle network, Server server) {
        this.os = os;
        this.network = network;
        this.server = server;
    }

    public RouterHandle router() {
        return network.router();
    }

    public NetworkHandle network() {
        return network;
    }

    public FIP floatingIp() {
        try {
            return new FIP(os, this, os.nova().assignFloatingIp(server));
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
