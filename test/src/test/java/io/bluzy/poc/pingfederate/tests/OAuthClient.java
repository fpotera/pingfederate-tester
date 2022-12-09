package io.bluzy.poc.pingfederate.tests;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import okhttp3.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static java.lang.System.out;

public class OAuthClient {

    private OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .hostnameVerifier((hostname, session) -> true)
            .authenticator((route, response) -> {
                String credential = Credentials.basic("florin", "florin");
                return response.request().newBuilder().header("Authorization", credential).build();
            })
            .build();

    private String authzEndpURL = "https://pingfederate:9031/as/authorization.oauth2";

    private String clientId = "TestClient";

    private String callbackURL = "http://mockserver:1080/callback";

    public String callImplicitFlow() throws IOException, URISyntaxException {
        URL url = buildImplicitFlow().toURL();
        out.println("URL: "+url);
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private URI buildImplicitFlow() throws URISyntaxException {

        URI authzEndpoint = new URI(authzEndpURL);

        ClientID clientID = new ClientID(clientId);

        Scope scope = new Scope();

        URI callback = new URI(callbackURL);

        State state = new State();

        AuthorizationRequest request = new AuthorizationRequest.Builder(
                new ResponseType(ResponseType.Value.TOKEN), clientID)
                .scope(scope)
                .state(state)
                .redirectionURI(callback)
                .endpointURI(authzEndpoint)
                .build();

        return request.toURI();
    }
}
