package os;

import org.openstack4j.model.network.Network;


public class NetworkHandle {
    private Connect os;
    private RouterHandle router;
    private Network network;

    public NetworkHandle(Connect os, RouterHandle router, Network network) {
        this.os = os;
        this.router = router;
        this.network = network;
    }

    public RouterHandle router() {
        return router;
    }

    public ServerHandle server(String name) {
        return new ServerHandle(os, this, os.nova().server(name, network));
    }
}
