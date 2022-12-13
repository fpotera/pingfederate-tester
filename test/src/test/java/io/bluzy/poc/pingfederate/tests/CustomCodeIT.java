package io.bluzy.poc.pingfederate.tests;

import static io.bluzy.poc.pingfederate.tests.config.URLs.*;
import static java.lang.System.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

class CustomCodeIT {

	@Test
	void test() throws Exception {
		OAuthClient oAuthClient = new OAuthClient();

		FirefoxOptions firefoxOptions = new FirefoxOptions();
		firefoxOptions.setAcceptInsecureCerts(true);

		WebDriver driver = new RemoteWebDriver(new URL("http://selenium:4444"), firefoxOptions);

		String url = oAuthClient.buildImplicitFlow().toURL().toString();
		out.println(url);

		try {
			driver.get(url);
			driver.manage().window().maximize();

			WebElement userField = driver.findElement(By.id("username"));
			userField.clear();
			userField.sendKeys("florin");
			WebElement passwordField = driver.findElement(By.id("password"));
			passwordField.clear();
			passwordField.sendKeys("florin");

			WebElement submit = driver.findElement(By.id("signOnButton"));
			submit.click();
		}
		finally {
			driver.quit();
		}

		assertTrue(true);
	}

	@BeforeAll
	static void beforeAll() throws IOException, URISyntaxException {

		APIConfigurator apiConfigurator = new APIConfigurator();

		apiConfigurator.applyConfigChange(PING_FED_TOKEN_MGR_URL, "/TestTokenManager,json", null);
		apiConfigurator.applyConfigChange(PING_FED_OIDC_POLICY_URL, "/TestOIDCPolicy.json", null);
		apiConfigurator.applyConfigChange(PING_FED_CLIENT_URL, "/TestClient.json", null);
		apiConfigurator.applyConfigChange(PING_FED_CV_URL, "/TestCV.json", null);
		apiConfigurator.applyConfigChange(PING_FED_IDP_ADAPTER_URL, "/TestIdPAdapter.json", null);
		apiConfigurator.applyConfigChange(PING_FED_IDP_ADAPTER_GRANT_MAPPING_URL, "/TestIdPAdapterGrantMapping.json", null);
		apiConfigurator.applyConfigChange(PING_FED_ACCESS_TOKEN_MAPPING_URL, "/TestIdPAdapterTokenManagerMapping.json", null);
		apiConfigurator.putConfig(PING_FED_SERVER_SETTINGS_URL, "/ServerSettings.json");
	}

	@AfterAll
	static void afterAll() throws IOException {
	}
}