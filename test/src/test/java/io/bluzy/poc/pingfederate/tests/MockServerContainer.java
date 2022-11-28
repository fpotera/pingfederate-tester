package io.bluzy.poc.pingfederate.tests;

import io.bluzy.clients.docker.base.DockerConfig;
import io.bluzy.clients.docker.base.DockerContainer;

public class MockServerContainer extends DockerContainer {
    private static final String REPO = "mockserver/mockserver";
    private static final String TAG = "mockserver-5.11.2";
    private static final String IMAGE_NAME = "mockserver";
    private static final String TMPFS = null;
    private static final int[] PORT = {1080};
    private static final Long SHM = null;
    private static final String[] ENV = new String[] {};

    public MockServerContainer(DockerConfig dockerConfig, boolean pullImage, String publishHostIp) {
        super(dockerConfig.getUrl(), REPO, TAG, IMAGE_NAME, TMPFS, PORT, SHM, ENV, pullImage, publishHostIp);
    }
}
