package os;

import org.openstack4j.model.network.Router;


public class RouterHandle {
    private Connect os;
    private org.openstack4j.model.network.Router router;

    public RouterHandle(Connect os, Router router) {
        this.os = os;
        this.router = router;
    }

    public NetworkHandle network(String name) {
        return os.neutron().network(name, true, router);
    }
}

