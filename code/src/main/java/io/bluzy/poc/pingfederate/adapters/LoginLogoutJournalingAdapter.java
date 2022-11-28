package io.bluzy.poc.pingfederate.adapters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import io.bluzy.poc.pingfederate.utils.HTTPRequester;
import org.sourceid.saml20.adapter.AuthnAdapterException;
import org.sourceid.saml20.adapter.attribute.AttributeValue;
import org.sourceid.saml20.adapter.conf.Configuration;
import org.sourceid.saml20.adapter.conf.Row;
import org.sourceid.saml20.adapter.conf.Table;
import org.sourceid.saml20.adapter.idp.authn.AuthnPolicy;
import org.sourceid.saml20.adapter.idp.authn.IdpAuthnAdapterDescriptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.pingidentity.sdk.AuthnAdapterResponse;
import com.pingidentity.sdk.AuthnAdapterResponse.AUTHN_STATUS;

import static io.bluzy.poc.pingfederate.utils.Utils.getQueryMap;

public class LoginLogoutJournalingAdapter extends AbstractLoginLogoutJournalingAdapter {

    private boolean m_bLogData;
    private boolean m_bUseOAuth;
    private String m_strClientID;
    private String m_strIDPEndpoint;
    private String m_strPassword;
    private String m_strScope;
    private HTTPRequester m_httpRequester;
    private String m_strHTTPType;
    private Table m_tableParameters;
    private String m_strURLJournalLogin;
    private String m_strURLJournalLogout;
    private boolean m_bSendJSON;
    private String m_strReturnValueAttribute;
    private String m_strReturnValueType;
    private boolean m_bReturnValueTypeIsBool;
    private String m_strReturnValueSuccess;
    private String m_strTimeStampNameLogout;
    private String m_strLogoutReasonParamName;
    private String m_strTimeStampNameLogin;
    private boolean m_bAllwaysSucceed;
    private String m_strA2HNames;
    private String m_strRetryCount;
    private String m_strRetryPeriod;

    public LoginLogoutJournalingAdapter() {
        super();
    }

    @Override
    public void configure(Configuration configuration) {

        m_tableParameters = configuration.getTable(TABLE_HEADER_ATTRIBUTES);

        m_strTimeStampNameLogin = configuration.getFieldValue(FIELD_TIMESTAMP_NAME_LOGIN);

        m_strTimeStampNameLogout = configuration.getFieldValue(FIELD_TIMESTAMP_NAME_LOGOUT);

        m_strLogoutReasonParamName = configuration.getFieldValue(FIELD_LOGOUT_REASON_PARAM_NAME);

        m_strURLJournalLogin = configuration.getFieldValue(FIELD_URL_JOURNAL_LOGIN);

        m_strURLJournalLogout = configuration.getFieldValue(FIELD_URL_JOURNAL_LOGOUT);

        m_strHTTPType = configuration.getFieldValue(SELECT_HTTP_TYPE);

        if (m_strHTTPType == null || m_strHTTPType.isEmpty()) {
            m_strHTTPType = HTTP_TYPE_VALUES[0];
        }

        if (m_strHTTPType.equalsIgnoreCase("REST")) {
            if (!m_strURLJournalLogin.isEmpty() && !m_strURLJournalLogin.endsWith("/")) {
                m_strURLJournalLogin += "/";
            }
            if (!m_strURLJournalLogout.isEmpty() && !m_strURLJournalLogout.endsWith("/")) {
                m_strURLJournalLogout += "/";
            }
        }

        m_bSendJSON = configuration.getBooleanFieldValue(CB_SEND_JSON);

        m_strReturnValueAttribute = configuration.getFieldValue(FIELD_RETURN_VALUE_ATTRIBUTE);

        m_strReturnValueType = configuration.getFieldValue(SELECT_RETURN_VALUE_ATTRIBUTE_TYPE);

        m_bReturnValueTypeIsBool = m_strReturnValueType.equals(RETURN_VALUE_ATTRIBUTE_VALUES[0]);

        m_strReturnValueSuccess = configuration.getFieldValue(FIELD_RETURN_VALUE_SUCCESS);

        m_bAllwaysSucceed = configuration.getBooleanFieldValue(CB_ALLWAYS_SUCCEED);

        m_strA2HNames = configuration.getFieldValue(FIELD_A2H_PARAMS);

        if (m_strA2HNames == null) {
            m_strA2HNames = "";
        }

        m_bLogData = configuration.getBooleanFieldValue(CB_LOG_DATA);

        m_bUseOAuth = configuration.getBooleanFieldValue(CB_USE_OAUTH);

        m_strIDPEndpoint = configuration.getFieldValue(FIELD_IDPENPOINT);

        m_strScope = configuration.getFieldValue(FIELD_SCOPE);

        m_strClientID = configuration.getFieldValue(FIELD_OAUTH_CLIENTID);

        m_strPassword = configuration.getFieldValue(FIELD_OAUTH_PASSWORD);

        m_strRetryCount = configuration.getFieldValue(FIELD_RETRY_COUNT);
        m_strRetryPeriod = configuration.getFieldValue(FIELD_RETRY_PERIOD);

        if (m_log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("loaded configuration: ").append(" Parameters=").append(m_tableParameters)
                    .append(" LogoutReasonParamName=").append(m_strLogoutReasonParamName).append(" TimestampNameLogin=")
                    .append(m_strTimeStampNameLogin).append(" TimestampNameLogout=").append(m_strTimeStampNameLogout)
                    .append("JournalURLLogin=").append(m_strURLJournalLogin).append("JournalURLLogout=")
                    .append(m_strURLJournalLogout).append(" HttpType=").append(m_strHTTPType).append(" SendJson=")
                    .append(m_bSendJSON).append(" ReturnValueAttribute=").append(m_strReturnValueAttribute)
                    .append(" ReturnValueAttributeType=").append(m_strReturnValueType).append(" SuccessValue=")
                    .append(m_strReturnValueSuccess).append(" AllwaysSucceed=").append(m_bAllwaysSucceed)
                    .append(" A2HNames=").append(m_strA2HNames).append(" LogData=").append(m_bLogData)
                    .append(" UseOAuth=").append(m_bUseOAuth).append(" IDPEndpoint=").append(m_strIDPEndpoint)
                    .append(" ClientID=").append(m_strClientID).append(" Scope=").append(m_strScope)
                    .append(" RetryCount=").append(m_strRetryCount).append(" RetryPeriod=").append(m_strRetryPeriod);

            m_log.debug(sb.toString());
        }

        m_httpRequester = new HTTPRequester(m_bUseOAuth, m_strIDPEndpoint, m_strScope, m_strClientID, m_strPassword,
                m_bLogData);

    }

    public AuthnAdapterResponse lookupAuthN(HttpServletRequest req, HttpServletResponse resp,
                                            Map<String, Object> inParameters) throws AuthnAdapterException, IOException {

        AuthnAdapterResponse authnAdapterResponse = new AuthnAdapterResponse();
        StringBuilder sbRequest = new StringBuilder();
        String strHttpTypActual = m_strHTTPType;
        String strData = "";
        String strURL = m_strURLJournalLogin;
        HashMap<String, Object> returnObject = new HashMap<String, Object>();
        @SuppressWarnings("unchecked")
        Map<String, AttributeValue> mapChainedAttr = (Map<String, AttributeValue>) inParameters
                .get(IN_PARAMETER_NAME_CHAINED_ATTRIBUTES);

        String strContentType = m_bSendJSON ? "application/json" : "application/x-www-form-urlencoded";

        if (m_strHTTPType.equalsIgnoreCase("REST")) {
            prepareRestCall(mapChainedAttr, sbRequest);
        } else {
            if (m_bSendJSON) {
                prepareJsonRawCall(mapChainedAttr, sbRequest);
            } else {
                prepareRawCall(mapChainedAttr, sbRequest);
            }
        }

        strData = sbRequest.substring(1);

        returnObject.put(CONTRACT_NAME_JOURNAL_INFO, strData);

        if (m_strURLJournalLogin.isEmpty()) {
            // do not send any data
            returnObject.put(CONTRACT_NAME_JOURNAL_SUCCESS, "YES");
        } else {

            try {
                HashMap<String, String> mapHeaders = prepareHeaders(mapChainedAttr, returnObject);

                URL url = prepareUrl(strURL, strData);
                if (m_strHTTPType.equalsIgnoreCase("rest")) {
                    strHttpTypActual = "GET";
                }
                strData = null;

                JsonObject responsePayloadJSON = send(url, strHttpTypActual, strData,
                        strContentType, mapHeaders, (m_bLogData ? url.toString() : m_strURLJournalLogin));

                if (m_log.isDebugEnabled()) {
                    m_log.debug("http request returned " + responsePayloadJSON.toString());
                }

                prepareResult(responsePayloadJSON, returnObject);

                authnAdapterResponse.setAuthnStatus(AUTHN_STATUS.SUCCESS);
                authnAdapterResponse.setAttributeMap(returnObject);

            } catch (Exception e) {
                if (m_log.isErrorEnabled()) {
                    m_log.error((new StringBuilder()).append(e.getMessage()), e);
                }
                if (m_bAllwaysSucceed) {
                    returnObject.put(CONTRACT_NAME_JOURNAL_SUCCESS, "NO");

                    authnAdapterResponse.setAuthnStatus(AUTHN_STATUS.SUCCESS);
                    authnAdapterResponse.setAttributeMap(returnObject);
                } else {
                    authnAdapterResponse.setErrorMessage("An error occured during journaling the data");
                    authnAdapterResponse.setAuthnStatus(AUTHN_STATUS.FAILURE);
                }
            }
        }

        return authnAdapterResponse;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean logoutAuthN(Map authnIdentifiers, HttpServletRequest req, HttpServletResponse resp,
                               String resumePath) throws AuthnAdapterException, IOException {

        boolean bResult = true;
        String strHttpTypActual = m_strHTTPType;
        String strData = (String) authnIdentifiers.get(CONTRACT_NAME_JOURNAL_INFO);
        @SuppressWarnings("unchecked")
        HashMap<String, String> mapHeaders = (HashMap<String, String>) authnIdentifiers
                .get(CONTRACT_NAME_JOURNAL_A2R_HEADERS);
        String strURL = m_strURLJournalLogout;
        String strResult = "";

        String strContentType = m_bSendJSON ? "application/json" : "application/x-www-form-urlencoded";

        try {
            if (!m_strURLJournalLogout.isEmpty()) {

                if (m_strHTTPType.equalsIgnoreCase("rest")) {
                    strHttpTypActual = "GET";
                    strURL += strData;
                    strData = null;
                } else {
                    if (m_bSendJSON) {
                        strData = prepareJsonCall(strData);
                    } else {
                        strData = prepareParametrisedCall(strData);
                    }

                    if (m_strHTTPType.equalsIgnoreCase("get")) {
                        strURL += "?" + strData;
                        strData = null;
                    } else {
                        if (!m_strLogoutReasonParamName.isEmpty()) {
                            String strReason = req.getParameter(m_strLogoutReasonParamName);
                            strURL += "?" + m_strLogoutReasonParamName + "=";
                            if (strReason != null) {
                                strURL += strReason;
                            } else {
                                strURL += "default";
                            }
                        }
                    }
                }

                URL url = new URL(strURL);
                if (m_log.isDebugEnabled()) {
                    m_log.debug((new StringBuilder()).append("Calling User Service URL: ")
                            .append((m_bLogData ? url.toString() : m_strURLJournalLogout)));
                }

                JsonObject responsePayloadJSON = send(url, strHttpTypActual, strData,
                        strContentType, mapHeaders, (m_bLogData ? url.toString() : m_strURLJournalLogout));

                if (m_log.isDebugEnabled()) {
                    m_log.debug("http request returned " + responsePayloadJSON.toString());
                }

                if (m_bReturnValueTypeIsBool) {
                    strResult = responsePayloadJSON.get(m_strReturnValueAttribute).getAsBoolean() ? "true" : "false";
                } else {
                    strResult = responsePayloadJSON.get(m_strReturnValueAttribute).getAsString();
                }

                bResult = strResult.equalsIgnoreCase(m_strReturnValueSuccess);
            }

        } catch (Exception e) {
            if (m_log.isErrorEnabled()) {
                m_log.error((new StringBuilder()).append(e.getMessage()), e);
            }
            bResult = false;
        }

        bResult |= m_bAllwaysSucceed;

        return bResult;
    }

    @Override
    public Map<String, Object> getAdapterInfo() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map lookupAuthN(HttpServletRequest arg0, HttpServletResponse arg1, String arg2, AuthnPolicy arg3,
                           String arg4) throws AuthnAdapterException, IOException {
        return null;
    }

    @Override
    public IdpAuthnAdapterDescriptor getAdapterDescriptor() {
        return m_descriptor;
    }

    private String prepareParametrisedCall(String strData) {
        List<Row> rows = m_tableParameters.getRows();
        Map<String, String> mapQuery = getQueryMap(strData);
        StringBuilder sbRequest = new StringBuilder();
        String strTempData = null;

        for (Row row : rows) {
            if (row.getBooleanFieldValue(CB_USE_IN_LOGOUT)) {
                strTempData = mapQuery.get(row.getFieldValue(FIELD_ATTR_JOURNAL_NAME));
                if (strTempData != null) {
                    sbRequest.append("&").append(row.getFieldValue(FIELD_ATTR_JOURNAL_NAME)).append("=").append(strTempData);
                }
            }
        }
        if (!m_strTimeStampNameLogout.isEmpty()) {
            sbRequest.append("&").append(m_strTimeStampNameLogout).append("=").append(
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return sbRequest.substring(1);
    }

    private String prepareJsonCall(String strData) {
        List<Row> rows = m_tableParameters.getRows();
        JsonParser parser = new JsonParser();
        JsonObject jsonLoginData = parser.parse(strData).getAsJsonObject();
        JsonObject jsonLogoutData = new JsonObject();
        JsonElement jsonElem = null;

        for (Row row : rows) {
            if (row.getBooleanFieldValue(CB_USE_IN_LOGOUT)) {
                jsonElem = jsonLoginData.get(row.getFieldValue(FIELD_ATTR_JOURNAL_NAME));
                if (jsonElem != null) {
                    jsonLogoutData.add(row.getFieldValue(FIELD_ATTR_JOURNAL_NAME), jsonElem);
                }
            }
        }
        if (!m_strTimeStampNameLogout.isEmpty()) {
            jsonLogoutData.addProperty(m_strTimeStampNameLogout,
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        }
        return jsonLogoutData.toString();
    }

    private void prepareRestCall(Map<String, AttributeValue> mapChainedAttr, StringBuilder sbRequest) {
        List<Row> rows = m_tableParameters.getRows();
        AttributeValue attrValue = null;
        String strAttrContractValue;
        for (Row row : rows) {
            if (row.getBooleanFieldValue(CB_ATTR_STATIC_VALUE)) {
                sbRequest.append("/").append(row.getFieldValue(FIELD_ATTR_CONTRACT_NAME));
                continue;
            }
            attrValue = mapChainedAttr.get(row.getFieldValue(FIELD_ATTR_CONTRACT_NAME));
            if (attrValue == null) {
                break;
            }
            strAttrContractValue = attrValue.getValue();

            if (strAttrContractValue == null) {
                break;
            }
            sbRequest.append("/").append(strAttrContractValue);
        }
        if (!m_strTimeStampNameLogin.isEmpty()) {
            sbRequest.append("/")
                    .append(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private void prepareJsonRawCall(Map<String, AttributeValue> mapChainedAttr, StringBuilder sbRequest) {
        List<Row> rows = m_tableParameters.getRows();
        AttributeValue attrValue = null;

        JsonObject data = new JsonObject();
        JsonParser parser = new JsonParser();

        for (Row row : rows) {
            if (row.getBooleanFieldValue(CB_ATTR_STATIC_VALUE)) {
                data.addProperty(row.getFieldValue(FIELD_ATTR_JOURNAL_NAME), row.getFieldValue(FIELD_ATTR_CONTRACT_NAME));
                continue;
            }
            attrValue = mapChainedAttr.get(row.getFieldValue(FIELD_ATTR_CONTRACT_NAME));
            if (attrValue == null) {
                continue;
            }

            try {
                data.add(row.getFieldValue(FIELD_ATTR_JOURNAL_NAME), parser.parse(attrValue.getValue()));
            } catch (JsonParseException ex) {
                data.addProperty(row.getFieldValue(FIELD_ATTR_JOURNAL_NAME), attrValue.getValue());
            }

        }
        if (!m_strTimeStampNameLogin.isEmpty()) {
            data.addProperty(m_strTimeStampNameLogin,
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        sbRequest.append("?").append(data.toString());
    }

    private void prepareRawCall(Map<String, AttributeValue> mapChainedAttr, StringBuilder sbRequest) {
        List<Row> rows = m_tableParameters.getRows();
        AttributeValue attrValue = null;
        String strAttrContractValue;

        for (Row row : rows) {
            if (row.getBooleanFieldValue(CB_ATTR_STATIC_VALUE)) {
                sbRequest.append("&").append(row.getFieldValue(FIELD_ATTR_JOURNAL_NAME)).append("=").append(row.getFieldValue(FIELD_ATTR_CONTRACT_NAME));
                continue;
            }
            attrValue = mapChainedAttr.get(row.getFieldValue(FIELD_ATTR_CONTRACT_NAME));
            if (attrValue == null) {
                continue;
            }
            strAttrContractValue = attrValue.getValue();

            if (strAttrContractValue == null) {
                continue;
            }
            sbRequest.append("&").append(row.getFieldValue(FIELD_ATTR_JOURNAL_NAME)).append("=").append(strAttrContractValue);
        }
        if (!m_strTimeStampNameLogin.isEmpty()) {
            sbRequest.append("&").append(m_strTimeStampNameLogin).append("=")
                    .append(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private HashMap<String, String> prepareHeaders(Map<String, AttributeValue> mapChainedAttr, HashMap<String, Object> returnObject) {
        HashMap<String, String> mapHeaders = new HashMap<String, String>();

        if (!m_strA2HNames.isEmpty()) {
            String[] strNames = m_strA2HNames.split(",");
            AttributeValue atrValue = null;
            String strValue = "";
            for (String strName : strNames) {
                strName = strName.trim();
                atrValue = mapChainedAttr.get(strName);
                if (atrValue != null) {
                    strValue = atrValue.getValue();
                    if (strValue != null && !strValue.isEmpty()) {
                        mapHeaders.put(strName, strValue);
                    }
                }
            }

            returnObject.put(CONTRACT_NAME_JOURNAL_A2R_HEADERS, mapHeaders);
        }

        return mapHeaders;
    }

    private URL prepareUrl(String strURL, String strData) throws MalformedURLException {
        if (m_strHTTPType.equalsIgnoreCase("get")) {
            strURL += "?" + strData;
        } else if (m_strHTTPType.equalsIgnoreCase("rest")) {
            strURL += strData;
        }

        URL url = new URL(strURL);
        if (m_log.isDebugEnabled()) {
            m_log.debug((new StringBuilder()).append("Calling User Service URL: ")
                    .append((m_bLogData ? url.toString() : m_strURLJournalLogin)));
        }

        return url;
    }

    private void prepareResult(JsonObject responsePayloadJSON, HashMap<String, Object> returnObject) {
        String strResult;
        if (m_bReturnValueTypeIsBool) {
            strResult = responsePayloadJSON.get(m_strReturnValueAttribute).getAsBoolean() ? "true" : "false";
        } else {
            strResult = responsePayloadJSON.get(m_strReturnValueAttribute).getAsString();
        }

        if (strResult.equalsIgnoreCase(m_strReturnValueSuccess)) {
            returnObject.put(CONTRACT_NAME_JOURNAL_SUCCESS, "YES");
        } else {
            returnObject.put(CONTRACT_NAME_JOURNAL_SUCCESS, "NO");
        }
    }

    private JsonObject send(URL url, String strHTTPRequestType, String strData, String strContentType,
                            Map<String, String> mapHeaders, String strURLToLog) throws Exception {

        JsonObject result = null;
        Exception exception = null;

        for(int i=0;i<NumberUtils.toInt(m_strRetryCount, DEFAULT_RETRY_COUNT);i++) {
            try {
                result = m_httpRequester.sendHTTPRequest(url, strHTTPRequestType, strData, strContentType, mapHeaders, strURLToLog);
                exception = null;
                break;
            }
            catch(Exception e) {
                exception = e;
                Thread.sleep(NumberUtils.toInt(m_strRetryPeriod, DEFAULT_RETRY_PERIOD));
            }
        }
        if(Objects.nonNull(exception)) {
            throw exception;
        }

        return result;
    }
}

