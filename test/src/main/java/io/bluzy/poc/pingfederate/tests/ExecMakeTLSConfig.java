package io.bluzy.poc.pingfederate.tests;

import java.io.File;
import java.net.Inet4Address;

import static io.bluzy.poc.pingfederate.tests.PingConfig.MOCK_SERVER_HOST;
import static io.bluzy.poc.pingfederate.tests.PingConfig.PING_FEDERATE_HOST;

public class ExecMakeTLSConfig {
    public static void main(String[] args) throws Exception {

        String pingFedHost = Inet4Address.getByName(PING_FEDERATE_HOST).getHostAddress();
        String mockServHost = Inet4Address.getByName(MOCK_SERVER_HOST).getHostAddress();

        new File("jssecacerts").delete();

        InstallCert.main(new String[] {pingFedHost+":9999", "--quiet"});
        InstallCert.main(new String[] {mockServHost+":1080", "--quiet"});
    }
}
