package os;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.openstack.OSFactory;


public class Connect {
    private OSClient os;
    private Nova nova;
    private Neutron neutron;

    public Connect() {
       this(
           System.getenv("OS_USERNAME"), System.getenv("OS_PASSWORD"),
               System.getenv("OS_TENANT_NAME"), System.getenv("OS_AUTH_URL"));
    }

    public Connect(String name) {
        this();
        nova.set_key_pair(name + "_key");
    }

    public Connect(String os_username, String os_password, String os_tenant_name, String os_auth_url) {

        if (os_username == null || os_password == null || os_tenant_name == null || os_auth_url == null) {
            System.out.println("Authentication credentials missing.");
            System.exit(-1);
        }

        try {
            os = OSFactory.builder()
                    .endpoint(os_auth_url)
                    .credentials(os_username, os_password)
                    .tenantName(os_tenant_name)
                    .authenticate();
            System.out.println("Authentication successful.");

            nova = new Nova(this);
            neutron = new Neutron(this);

            // add SSH security group
            nova.new_public_ssh_sg();

        } catch (AuthenticationException e) {
            System.out.println("Authentication failed.");
            System.exit(1);
        }
    }

    public OSClient getClient() {
        return os;
    }

    public Nova nova() {
        return nova;
    }

    public Neutron neutron() {
        return neutron;
    }

    public void trash() {
        nova.delete_floating_ips();
        nova.delete_servers();
        neutron.delete_ports();
        nova.delete_security_groups();
        neutron.delete_networks();
        neutron.delete_routers();
    }
}
