package io.bluzy.poc.pingfederate.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class HTTPRequester {

    public final static String EXCEPTION_HTTPSTATUS_OBJECT = "httpstatus";
    public final static String EXCEPTION_BODY_OBJECT = "body";

    private Log m_log;
    private boolean m_bUseOAuth;
    private String m_strClientID;
    private String m_strScope;
    private String m_strIDPEndpoint;
    private String m_strPassword;
    private String m_strAccessToken;
    private boolean m_blogData;
    private OAuthClient m_oAuthClient;
    private Instant m_tsTokenEpire;

    private UUID m_uuid;

    public HTTPRequester(boolean bUseOAuth, String strIDPEndpoint, String strScope, String strClientID,
                         String strPassword, boolean bLogData) {

        this.m_uuid = UUID.randomUUID();
        this.m_log = LogFactory.getLog(this.getClass());
        this.m_bUseOAuth = bUseOAuth;
        this.m_strClientID = strClientID;
        this.m_strIDPEndpoint = strIDPEndpoint;
        this.m_strPassword = strPassword;
        this.m_strScope = strScope;
        this.m_blogData = bLogData;

        if (m_log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Configuration stored: UseOAuth=").append(m_bUseOAuth).append(" IDPEndpoint=")
                    .append(m_strIDPEndpoint).append(" ClientID=").append(m_strClientID).append(" Scope=")
                    .append(m_strScope).append(" LogData=").append(m_blogData).append(" uniqueID=").append(m_uuid);
            m_log.debug(sb.toString());
        }

        m_oAuthClient = new OAuthClient(new URLConnectionClient());

        m_tsTokenEpire = Instant.EPOCH;
    }

    /**
     * @param url
     * @param strHTTPRequestType
     * @param strData
     * @return
     * @throws IOException
     * @throws ProtocolException
     * @throws OAuthProblemException
     * @throws OAuthSystemException
     * @throws HTTPStatusException
     */
    public JsonObject sendHTTPRequest(URL url, String strHTTPRequestType, String strData)
            throws IOException, ProtocolException, OAuthSystemException, OAuthProblemException, HTTPStatusException {

        return sendHTTPRequest(url, strHTTPRequestType, strData, "application/x-www-form-urlencoded", null);

    }

    public JsonObject sendHTTPRequest(URL url, String strHTTPRequestType, String strData, String strContentType,
                                      String strURLToLog) throws IOException, ProtocolException, OAuthSystemException, OAuthProblemException, HTTPStatusException {

        return sendHTTPRequest(url, strHTTPRequestType, strData, strContentType, null, strURLToLog);
    }

    public JsonObject sendHTTPRequest(URL url, String strHTTPRequestType, String strData, String strContentType,
                                      Map<String, String> mapHeaders, String strURLToLog)
            throws IOException, ProtocolException, OAuthSystemException, OAuthProblemException, HTTPStatusException {

        JsonObject jsonObj = null;

        if (strURLToLog == null || strURLToLog.isEmpty()) {
            strURLToLog = url.toString();
        }

        if (m_log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Handling http request: URL=").append(strURLToLog).append(" HttpType=").append(strHTTPRequestType)
                    .append(" CustomHeaders=").append(mapHeaders).append(" Data=");

            if (strData != null) {
                if (m_blogData) {
                    sb.append(strData);
                } else {
                    sb.append("**********");
                }
            } else {
                sb.append("null");
            }

            m_log.debug(sb.toString());
        }

        jsonObj = sendRawHTTPRequest(url, strHTTPRequestType, strData, strContentType, mapHeaders);

        return jsonObj;
    }

    /**
     * @return
     * @throws OAuthSystemException
     * @throws OAuthProblemException
     */
    private void checkOAuthToken(Boolean bForceRefresh) throws OAuthSystemException, OAuthProblemException {

        if (m_log.isDebugEnabled()) {
            m_log.debug("checkOAuthToken(" + bForceRefresh + ") call for ID " + m_uuid.toString());
        }

        if (bForceRefresh || m_tsTokenEpire.isBefore(Instant.now())) {

            String strOldAccessToken = m_strAccessToken;

            OAuthClientRequest request = OAuthClientRequest.tokenLocation(m_strIDPEndpoint)
                    .setGrantType(GrantType.CLIENT_CREDENTIALS).setClientId(m_strClientID)
                    .setClientSecret(m_strPassword).setScope(m_strScope).buildBodyMessage();

            OAuthJSONAccessTokenResponse response = m_oAuthClient.accessToken(request);

            m_strAccessToken = response.getAccessToken();
            long lExpiresIn = response.getExpiresIn();
            lExpiresIn -= 5;

            m_tsTokenEpire = Instant.now().plusSeconds(lExpiresIn);

            if (strOldAccessToken != null && m_strAccessToken.equalsIgnoreCase(strOldAccessToken)) {
                if (m_log.isErrorEnabled()) {
                    m_log.error("fetching new access token resulted in same access token");
                }
            }

            if (m_log.isDebugEnabled()) {
                m_log.debug("checkOAuthToken(" + bForceRefresh + ") call for ID " + m_uuid.toString()
                        + " --> new access token");
            }
        }

    }

    private JsonObject sendRawHTTPRequest(URL url, String strHTTPRequestType, String strData, String strContentType,
                                          Map<String, String> mapHeaders)
            throws IOException, ProtocolException, OAuthSystemException, OAuthProblemException, HTTPStatusException {

        int iResponseCode = 0;
        // BufferedReader br = null;
        String strJsonResponse = "";
        boolean bForceRefreshOAuth = false;
        InputStream is = null;
        boolean bRetry = false;
        UUID uuid = null;

        for (int i = 0; i <= 1; i++) {

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (m_bUseOAuth) {
                checkOAuthToken(bForceRefreshOAuth);
                conn.setRequestProperty("Authorization", "Bearer " + m_strAccessToken);
            }

            conn.setRequestMethod(strHTTPRequestType);
            if (strData != null) {
                conn.setRequestProperty("Content-Type", strContentType);
            }
            conn.setRequestProperty("Accept", "application/json");

            if (mapHeaders != null) {
                for (Map.Entry<String, String> entry : mapHeaders.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            uuid = UUID.randomUUID();
            conn.setRequestProperty("tracker", uuid.toString());

            conn.setDoOutput(true);

            if (strData != null) {
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(strData);
                wr.flush();
            }

            iResponseCode = conn.getResponseCode();

            if (iResponseCode < 200 || iResponseCode > 299) {
                is = conn.getErrorStream();
            } else {
                is = conn.getInputStream();
            }

            if (is != null) {
                // br = new BufferedReader(new InputStreamReader(is));
                // strJsonResponse = readAll(br);
                strJsonResponse = OAuthUtils.saveStreamAsString(is);
                is.close();

            } else {
                strJsonResponse = "";

                if (m_log.isInfoEnabled()) {
                    m_log.info("input stream was null (http status:" + iResponseCode + ")");
                }
            }

            if (m_log.isTraceEnabled()) {
                m_log.trace("http request returned status:" + iResponseCode + ", Body:" + strJsonResponse);

                if (strJsonResponse.isEmpty()) {

                    String strDebug = conn.getResponseMessage();
                    m_log.trace("http tracker id: " + uuid.toString());
                    m_log.trace("http response: " + (strDebug == null ? "null" : strDebug));

                    Map<String, List<String>> hdrs = conn.getHeaderFields();
                    Set<String> hdrKeys = hdrs.keySet();

                    for (String k : hdrKeys)
                        m_log.trace("http headers: Key: " + k + "  Value: " + hdrs.get(k));

                }
            }

            // workaround for retrieving empty response
            if (strJsonResponse.isEmpty() && !(iResponseCode < 200 || iResponseCode > 299)) {
                String strContentLength = conn.getHeaderField("Content-Length");
                m_log.trace("http request content length: " + strContentLength);
                if (!bRetry && !strContentLength.equalsIgnoreCase("0")) {
                    m_log.trace("http request retry");
                    if (url.getPath().contains("idtoken") || url.getPath().contains("browser")
                            || url.getPath().contains("/userservice/index.php")) {
                        i--;
                        bRetry = true;
                        continue;

                    }
                }
            }

            if (iResponseCode == 401 && m_bUseOAuth) {

                if (strJsonResponse.isEmpty()) {
                    String strWWWAuthHeader = conn.getHeaderField("WWW-Authenticate");

                    if (strWWWAuthHeader != null) {
                        strJsonResponse = "WWW-Authenticate: " + strWWWAuthHeader;
                    } else {
                        strJsonResponse = "Access Denied";
                    }
                }

                if (m_log.isErrorEnabled()) {
                    String strDebug = conn.getResponseMessage();
                    m_log.error("http tracker id: " + uuid.toString());
                    m_log.error("http response: " + (strDebug == null ? "null" : strDebug));

                    Map<String, List<String>> hdrs = conn.getHeaderFields();
                    Set<String> hdrKeys = hdrs.keySet();

                    for (String k : hdrKeys)
                        m_log.error("http headers: Key: " + k + "  Value: " + hdrs.get(k));

                }

                bForceRefreshOAuth = true;
                conn.disconnect();
                continue;
            }
            conn.disconnect();
            break;
        }

        if (iResponseCode < 200 || iResponseCode > 299) {
            JsonObject error = new JsonObject();
            error.addProperty(EXCEPTION_HTTPSTATUS_OBJECT, iResponseCode);
            try {
                error.add(EXCEPTION_BODY_OBJECT, (new JsonParser()).parse(strJsonResponse));
            } catch (JsonParseException ex) {
                error.addProperty(EXCEPTION_BODY_OBJECT, strJsonResponse);
            }
            throw new HTTPStatusException(error.toString());
        }

        JsonObject jsonReturn = null;
        try {
            jsonReturn = (new JsonParser()).parse(strJsonResponse).getAsJsonObject();
        } catch (RuntimeException ex) {
            if (m_log.isErrorEnabled()) {
                m_log.error("failed to parse return value as JSON. Value was [" + strJsonResponse + "]");
            }
            throw new RuntimeException("failed to parse return value as JSON", ex);
        }

        return jsonReturn;
    }

    /*
     * private static String readAll(Reader rd) throws IOException {
     * StringBuilder sb = new StringBuilder(); int cp; while ((cp = rd.read())
     * != -1) { sb.append((char) cp); } return sb.toString(); }
     */

}

