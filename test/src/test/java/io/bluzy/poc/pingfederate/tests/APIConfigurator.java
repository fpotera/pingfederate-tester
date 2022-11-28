package io.bluzy.poc.pingfederate.tests;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.hc.core5.http.HttpHeaders;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.Objects.nonNull;
import static java.util.regex.Matcher.quoteReplacement;

public class APIConfigurator {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient.Builder()
            .hostnameVerifier((hostname, session) -> true)
            .authenticator((route, response) -> {
                if (response.request().header(HttpHeaders.AUTHORIZATION) != null)
                    return null;

                String credential = Credentials.basic("Administrator", "2FederateM0re");
                return response.request().newBuilder().header(HttpHeaders.AUTHORIZATION, credential).build();
            })
            .build();

    public String applyConfigChange(String url, String fileName, Map<String, String> params) throws IOException, URISyntaxException {

        String[] json = new String[] { Files.readString(
                Paths.get(getClass().getResource(fileName).toURI()), Charset.defaultCharset()
        )};

        if(nonNull(params)) {
            params.forEach((k,v)->json[0] = json[0].replaceAll(quoteReplacement(k),v));
        }

        RequestBody body = RequestBody.create(json[0], JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("X-XSRF-Header", "PingFederate")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String getConfig(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-XSRF-Header", "PingFederate")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String putConfig(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("X-XSRF-Header", "PingFederate")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
