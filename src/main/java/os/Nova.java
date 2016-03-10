package os;

import org.openstack4j.api.Builders;
import org.openstack4j.api.compute.ComputeService;
import org.openstack4j.model.compute.*;
import org.openstack4j.model.network.Network;

import java.util.Collections;


public class Nova {

    private ComputeService compute;
    private String key_pair;

    public Nova(Connect os) {
        compute = os.getClient().compute();
    }

    public void set_key_pair(String key_pair) {
        this.key_pair = key_pair;
    }

    public Server server(String name, Network net) {

        Server server = compute.servers().boot(Builders.server()
                .name(name)
                .flavor(getFlavorByName("m1.small").getId())
                .image(getImageByName("ubuntu14.04-LTS").getId())
                .networks(Collections.singletonList(net.getId()))
                .addSecurityGroup(getSecurityGroupByName("public-ssh").getName())
                .keypairName(key_pair)
                .addPersonality("/etc/motd", "Welcome to your new VM " + name + "!\n")
                .build());
        System.out.println("Server created.");

        server = compute.servers().get(server.getId());
        System.out.printf("Server in state %s.", server.getStatus());
        while (server.getStatus().toString().equals("BUILD")) {
            sleep(1000);
            server = compute.servers().get(server.getId());
            System.out.printf(".");
        }
        System.out.printf("\nServer in state %s.\n", server.getStatus());
        return server;
    }

    Flavor getFlavorByName(String name) {
        for (Flavor f : compute.flavors().list()) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    Image getImageByName(String name) {
        for (Image i : compute.images().list()) {
            if (i.getName().equals(name)) {
                return i;
            }
        }
        return null;
    }

    SecGroupExtension getSecurityGroupByName(String name) {
        for (SecGroupExtension sg: compute.securityGroups().list()) {
            if (sg.getName().equals(name)) {
                return sg;
            }
        }
        return null;
    }

    public SecGroupExtension new_public_ssh_sg() {

        // Check for existence
        SecGroupExtension sg = getSecurityGroupByName("public-ssh");
        if (sg != null) {
            return sg;
        }

        sg = compute.securityGroups().create("public-ssh", "Grant SSH access");

        // Allow ssh
        compute.securityGroups().createRule(Builders.secGroupRule()
                .parentGroupId(sg.getId())
                .protocol(IPProtocol.TCP)
                .cidr("0.0.0.0/0")
                .range(22, 22)
                .build());
        // Allow ping
        compute.securityGroups().createRule(Builders.secGroupRule()
                .parentGroupId(sg.getId())
                .protocol(IPProtocol.ICMP)
                .cidr("0.0.0.0/0")
                .build());

        System.out.printf("Security group %s created.\n", sg.getName());

        return sg;
    }

    public FloatingIP assignFloatingIp(Server server) {

        FloatingIP floatingIP = allocateFloatingIp();

        ActionResponse response = compute.floatingIps().addFloatingIP(server, floatingIP.getFloatingIpAddress());
        if (response.isSuccess()) {
            System.out.println("Public IP assigned.");
        } else {
            System.out.println("Public IP assign failed: " + response.getFault());
        }

        return floatingIP;
    }

    private FloatingIP allocateFloatingIp() {

        FloatingIP floatingIP = compute.floatingIps().allocateIP("public");

        System.out.printf("Public IP %s created.\n" + floatingIP.getFloatingIpAddress());
        return floatingIP;
    }

    public void delete_floating_ips() {
        for (FloatingIP ip : compute.floatingIps().list()) {
            System.out.println("Deleting IP " + ip.getFloatingIpAddress());
            ActionResponse response = compute.floatingIps().deallocateIP(ip.getId());
            if (!response.isSuccess()) {
                System.out.println("Deleting IP failed." + response.getFault());
            }
        }
    }

    public void delete_servers() {
        for (Server s : compute.servers().list()) {
            System.out.println("Deleting server " + s.getName());
            ActionResponse response = compute.servers().delete(s.getId());
            if (!response.isSuccess()) {
                System.out.println("Deleting server failed." + response.getFault());
            }
        }
    }

    public void delete_security_groups() {
        for (SecGroupExtension sg : compute.securityGroups().list()) {
            if (!sg.getName().equals("default") && !sg.getName().equals("public-ssh")) {
                System.out.println("Deleting security group " + sg.getName());
                ActionResponse response = compute.securityGroups().delete(sg.getId());
                if (!response.isSuccess()) {
                    System.out.println("Deleting security group failed." + response.getFault());
                }
            }
        }
    }

    static public void sleep(int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted.");
        }
    }
}
