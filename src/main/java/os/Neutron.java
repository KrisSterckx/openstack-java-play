package os;

import org.openstack4j.api.Builders;
import org.openstack4j.api.networking.*;
import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.network.*;


public class Neutron {
    private static final String network_prefix = "172.16.0.";
    private static final int subMask = 29;
    private static int nextSubIp = 0;
    private Connect os;
    private NetworkingService networking;

    public Neutron(Connect os) {
        this.os = os;
        this.networking = os.getClient().networking();
    }
    
    public RouterHandle router(String name) {
        return new RouterHandle(os, os_router(name));
    }

    public NetworkHandle network(String name, boolean connect, Router router) {
        return new NetworkHandle(os, new RouterHandle(os, router), os_network(name, connect, router));
    }

    public Router os_router(String name) {
        Router router = getRouterByName(name);
        if (router != null) {
            System.out.println("Router found.");
        } else {
            router = router().create(Builders.router()
                    .name(name)
                    .externalGateway(getNetworkByName("public").getId())
                    .build());
            System.out.printf("Router %s created.\n", router.getName());
        }
        return router;
    }

    public Network os_network(String name, boolean connect, Router router) {

        Network network = getNetworkByName(name);
        if (network != null) {
            System.out.println("Network found.");
        } else {
            network = network().create(Builders.network()
                    .name(name)
                    .adminStateUp(true)
                    .build());
            System.out.printf("Network %s created.\n", network.getName());
        }

        Subnet subnet = getSubnetByName(name);
        if (subnet != null) {
            System.out.println("Subnet found.");
        } else {
            subnet = subnet().create(Builders.subnet()
                    .name(name)
                    .networkId(network.getId())
                    .ipVersion(IPVersionType.V4)
                    .cidr(allocateSubnet())
                    .enableDHCP(true)
                    .build());
            System.out.printf("Subnet %s %s created.\n", subnet.getName(), subnet.getCidr());

            // Attach to Router, conditionally
            if (connect) {
                if (router == null) {
                    router = os_router(name);
                }
                attach(subnet, router);
            }
        }

        return network;
    }

    RouterInterface attach(Subnet subnet, Router router) {
        RouterInterface routerInterface = router().attachInterface(
                router.getId(),
                AttachInterfaceType.SUBNET, subnet.getId());

        System.out.printf("Network %s is routed.\n", subnet.getName());

        return routerInterface;
    }

    Network getNetworkByName(String name) {
        for (Network n : network().list()) {
            if (n.getName().equals(name)) {
                return n;
            }
        }
        return null;
    }

    Subnet getSubnetByName(String name) {
        for (Subnet s : subnet().list()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    Router getRouterByName(String name) {
        for (Router r : router().list()) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }

    public void delete_ports() {
        for (Port p : port().list()) {
            if (p.getDeviceOwner().equals("network:router_interface")) {
                String routerId = p.getDeviceId();
                for (String subnetId : network().get(p.getNetworkId()).getSubnets()) {
                    System.out.println("Disassociating network");
                    router().detachInterface(routerId, subnetId, p.getId());
                }
            } else {
                System.out.println("Deleting port");
                ActionResponse response = port().delete(p.getId());
                if (!response.isSuccess()) {
                    if (!response.getFault().contains("could not be found")) {
                        System.out.println("Deleting port failed:" + response.getFault());
                    }
                }
            }
        }
    }

    public void delete_routers() {
        for (Router r : router().list()) {
            System.out.println("Deleting router " + r.getName());
            ActionResponse response = router().delete(r.getId());
            if (!response.isSuccess()) {
                System.out.println("Deleting router failed." + response.getFault());
            }
        }
    }

    public void delete_networks() {
        for (Network n : network().list()) {
            if (!n.isRouterExternal()) {
                System.out.println("Deleting network " + n.getName());
                ActionResponse response = network().delete(n.getId());
                if (!response.isSuccess()) {
                    System.out.println("Deleting network failed." + response.getFault());
                    ;
                }
            }
        }
    }

    private RouterService router() {
        return networking.router();
    }

    private NetworkService network() {
        return networking.network();
    }

    private SubnetService subnet() {
        return networking.subnet();
    }

    private PortService port() {
        return networking.port();
    }

    private static String allocateSubnet() {
        String allocated = network_prefix + nextSubIp + "/" + subMask;
        nextSubIp = (int) (nextSubIp + Math.pow(2, (32 - subMask)));
        return allocated;
    }
}
