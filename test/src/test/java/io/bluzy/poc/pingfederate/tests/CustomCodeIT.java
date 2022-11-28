package io.bluzy.poc.pingfederate.tests;

import static io.bluzy.clients.docker.base.DockerConfig.UNIX_SOCKET_URL;
import static io.bluzy.poc.pingfederate.tests.config.URLs.*;
import static java.lang.System.*;
import static java.nio.file.Paths.get;
import static java.util.Map.of;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CustomCodeIT {

	private static PingFederateContainer pingFederateContainer = new PingFederateContainer(UNIX_SOCKET_URL, false, null);
	private static MockServerContainer mockServerContainer = new MockServerContainer(UNIX_SOCKET_URL, false, null);

	@Test
	void test() {
		assertTrue(true);
	}

	@BeforeAll
	static void beforeAll() throws IOException, URISyntaxException {

		File[] files = new File[] {
				get(getProperty("oltu.path")).toAbsolutePath().toFile(),
				get(getProperty("custom.code.jar.path")).toAbsolutePath().toFile()
		};
		pingFederateContainer.pushFiles(files, "/opt/out/instance/server/default/deploy");

		APIConfigurator apiConfigurator = new APIConfigurator();

		apiConfigurator.applyConfigChange(PING_FED_TOKEN_MGR_URL, "/TestTokenManager,json", null);
		apiConfigurator.applyConfigChange(PING_FED_OIDC_POLICY_URL, "/TestOIDCPolicy.json", null);
		apiConfigurator.applyConfigChange(PING_FED_CLIENT_URL, "/TestClient.json", null);
		apiConfigurator.applyConfigChange(PING_FED_CV_URL, "/TestCV.json", null);
		apiConfigurator.applyConfigChange(PING_FED_IDP_ADAPTER_URL, "/TestIdPAdapter.json", null);
		String jsonString = apiConfigurator.applyConfigChange(PING_FED_POLICY_CONTRACT_URL, "/TestPolicyContract.json", null);
		JSONObject obj = new JSONObject(jsonString);
		String polConId = obj.getString("id");
		jsonString = apiConfigurator.applyConfigChange(PING_FED_POLICY_URL, "/TestPolicy.json", of("%contr%", polConId));
		obj = new JSONObject(jsonString);
		String polId = obj.getString("id");
		out.println("#### polid: "+polId);
	}

	@AfterAll
	static void afterAll() throws IOException {
	}
}