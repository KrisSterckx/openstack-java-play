/*
 *  Main OpenStack scenario flow
 */

import os.*;


public class Main {

    public static void main(String[] args) {

        FIP ip = one_network_topo();

        System.out.printf("Your new cloud ip %s is ready.\n", ip);
    }


    public static FIP one_network_topo() {

        return new Connect("Kris").neutron().router("Kris").network("Kris").server("Kris").floatingIp();

    }

    public static void two_network_topo() {

        new Connect("Kris").neutron().router("Kris2").network("Kris21").server("Kris21").floatingIp()
                .router().network("Kris22").server("Kris22");

    }

    public static void three_network_topo() {

        new Connect("Kris").neutron().router("Kris3").network("Kris31").server("Kris32").floatingIp()
                .router().network("Kris32").server("Kris32")
                .router().network("Kris33").server("Kris33");

    }

}
