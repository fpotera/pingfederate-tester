package io.bluzy.poc.pingfederate.tests;

import io.bluzy.clients.docker.base.DockerConfig;
import io.bluzy.clients.docker.base.DockerContainer;

public class PingFederateContainer extends DockerContainer {

    private static final String REPO = "pingidentity/pingfederate";
    private static final String TAG = "edge";
    private static final String IMAGE_NAME = "pingfederate";
    private static final String TMPFS = "/run/secrets";
    private static final int[] PORT = {9999,9031};
    private static final Long SHM = null;
    private static final String[] ENV = new String[] {"SERVER_PROFILE_URL=https://github.com/pingidentity/pingidentity-server-profiles.git",
            "SERVER_PROFILE_PATH=getting-started/pingfederate",
            "PING_IDENTITY_ACCEPT_EULA=YES"
    };

    public PingFederateContainer(DockerConfig dockerConfig, boolean pullImage, String publishHostIp) {
        super(dockerConfig.getUrl(), REPO, TAG, "/pingfederate-tester-pingfederate-1", TMPFS, PORT, SHM, ENV, pullImage, publishHostIp);
    }
}

