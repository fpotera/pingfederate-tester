package io.bluzy.poc.pingfederate.tests;

import static io.bluzy.clients.docker.base.DockerConfig.UNIX_SOCKET_URL;
import static io.bluzy.poc.pingfederate.tests.config.URLs.*;
import static java.lang.System.*;
import static java.nio.file.Paths.get;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CustomCodeIT {

	private static PingFederateContainer pingFederateContainer = new PingFederateContainer(UNIX_SOCKET_URL, false, null);
	private static MockServerContainer mockServerContainer = new MockServerContainer(UNIX_SOCKET_URL, false, null);

	@Test
	void test() throws Exception {
		OAuthClient oAuthClient = new OAuthClient();
		oAuthClient.callImplicitFlow();

		assertTrue(true);
	}

	@BeforeAll
	static void beforeAll() throws IOException, URISyntaxException {

		File[] files = new File[] {
				get(getProperty("oltu.path")).toAbsolutePath().toFile(),
				get(getProperty("custom.code.jar.path")).toAbsolutePath().toFile()
		};
//		pingFederateContainer.pushFiles(files, "/opt/out/instance/server/default/deploy");

		APIConfigurator apiConfigurator = new APIConfigurator();

		apiConfigurator.applyConfigChange(PING_FED_TOKEN_MGR_URL, "/TestTokenManager,json", null);
		apiConfigurator.applyConfigChange(PING_FED_OIDC_POLICY_URL, "/TestOIDCPolicy.json", null);
		apiConfigurator.applyConfigChange(PING_FED_CLIENT_URL, "/TestClient.json", null);
		apiConfigurator.applyConfigChange(PING_FED_CV_URL, "/TestCV.json", null);
		apiConfigurator.applyConfigChange(PING_FED_IDP_ADAPTER_URL, "/TestBasicIdPAdapter.json", null);
		apiConfigurator.applyConfigChange(PING_FED_IDP_ADAPTER_GRANT_MAPPING_URL, "/TestIdPAdapterGrantMapping.json", null);
		apiConfigurator.applyConfigChange(PING_FED_ACCESS_TOKEN_MAPPING_URL, "/TestIdPAdapterTokenManagerMapping.json", null);
	}

	@AfterAll
	static void afterAll() throws IOException {
	}
}