package io.bluzy.poc.pingfederate.adapters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sourceid.saml20.adapter.conf.Configuration;
import org.sourceid.saml20.adapter.conf.FieldList;
import org.sourceid.saml20.adapter.conf.Row;
import org.sourceid.saml20.adapter.conf.Table;
import org.sourceid.saml20.adapter.gui.AdapterConfigurationGuiDescriptor;
import org.sourceid.saml20.adapter.gui.CheckBoxFieldDescriptor;
import org.sourceid.saml20.adapter.gui.FieldDescriptor;
import org.sourceid.saml20.adapter.gui.SelectFieldDescriptor;
import org.sourceid.saml20.adapter.gui.TableDescriptor;
import org.sourceid.saml20.adapter.gui.TextFieldDescriptor;
import org.sourceid.saml20.adapter.gui.validation.ConfigurationValidator;
import org.sourceid.saml20.adapter.gui.validation.RowValidator;
import org.sourceid.saml20.adapter.gui.validation.ValidationException;
import org.sourceid.saml20.adapter.gui.validation.impl.HttpURLValidator;
import org.sourceid.saml20.adapter.gui.validation.impl.RequiredFieldValidator;
import org.sourceid.saml20.adapter.idp.authn.IdpAuthnAdapterDescriptor;

import com.pingidentity.sdk.IdpAuthenticationAdapterV2;

public abstract class AbstractLoginLogoutJournalingAdapter  implements IdpAuthenticationAdapterV2 {

    static final String CONTRACT_NAME_JOURNAL_INFO = "journal_info";
    static final String CONTRACT_NAME_JOURNAL_SUCCESS = "journal_success";
    static final String CONTRACT_NAME_JOURNAL_A2R_HEADERS = "journal_a2r_headers";
    static final String DEFAULT_USERSERVICE_URL = "https://server/service";
    static final String ADAPTER_VERSION = "1.0";
    static final String ADAPTER_NAME = "LoginLogoutJournalingAdapter";
    static final String ADAPTER_GUI_DESCRIPTION = "Please fill out the requested parameters for connecting to the User Service";

    static final String FIELD_LOGOUT_REASON_PARAM_NAME = "Lougout Reason Param Name";
    static final String FIELD_URL_JOURNAL_LOGIN = "URL to Journal Service for Login";
    static final String FIELD_URL_JOURNAL_LOGOUT = "URL to Journal Service for Logout";

    static final String FIELD_RETURN_VALUE_ATTRIBUTE = "Retun Value Attribute";
    static final String FIELD_RETURN_VALUE_SUCCESS = "Value for \"success\"";

    static final String CB_SEND_JSON = "Send JSON";
    static final String CB_LOG_DATA = "Log Data";
    static final String CB_USE_OAUTH = "Use OAUTH";
    static final String FIELD_OAUTH_PASSWORD = "Password";
    static final String FIELD_OAUTH_CLIENTID = "Client ID";
    static final String FIELD_IDPENPOINT = "IDP End Point";
    static final String FIELD_SCOPE = "Scope";
    static final String FIELD_TIMESTAMP_NAME_LOGIN = "Timestamp Attr.Name Login";
    static final String FIELD_TIMESTAMP_NAME_LOGOUT = "Timestamp Attr.Name Logout";

    static final String SELECT_RETURN_VALUE_ATTRIBUTE_TYPE = "Return Value Attribute Type";
    // table
    static final String FIELD_ATTR_JOURNAL_NAME = "Journal Attribute Name";
    static final String FIELD_ATTR_CONTRACT_NAME = "Contract Attribute Name";
    static final String CB_ATTR_STATIC_VALUE = "Static Attribute Value";
    static final String CB_USE_IN_LOGOUT = "Use In Logout";

    static final String TABLE_HEADER_ATTRIBUTES = "Journal Attributes - Contract Values";

    static final String SELECT_HTTP_TYPE = "HTTP Type";

    static final String[] HTTP_TYPE_VALUES = { "GET", "POST", "PUT", "REST" };
    static final String[] RETURN_VALUE_ATTRIBUTE_VALUES = { "BOOL", "STRING" };

    static final String DESC_LOGOUT_REASON_PARAM_NAME = "This field specifies the name of the parameter for the logout reasen, e.g. reason=session_timeout, when using POST or PUT. If no name is specified, it is ignored. If the parameter is empty, the value \"default\" is used";
    static final String DESC_URL_JOURNAL_LOGIN = "The path specifies the URL to the Journal-Service for Logins, if empty, no request is sent";
    static final String DESC_URL_JOURNAL_LOGOUT = "The path specifies the URL to the Journal-Service for Logouts, if empty, no request is sent";
    static final String DESC_SEND_JSON = "check to send the data as JSON Object";
    static final String DESC_OAUTH_PASSWORD = "The Password of the Client";
    static final String DESC_OAUTH_CLIENTID = "The ClientID for the OAuth token";
    static final String DESC_SCOPE = "The scope to request for the OAuth token (\"client credential flow\")";
    static final String DESC_IDPENPOINT = "The endpoint to request the OAuth token (\"client credential flow\")";
    static final String DESC_USE_OAUTH = "check to get an OAuth token to access the endpoint";
    static final String DESC_LOG_DATA = "check to log the request data in the DEBUG log information";
    static final String DESC_HTTP_TYPE = "The HTTP method to send the data, when selected \"REST\", the values are added in the order to the URL e.g. .../param1/parame2/..";
    static final String DESC_ATTR_STATIC_VALUE = "select to use the request parameter as static value";
    static final String DESC_ATTR_JOURNAL_NAME = "The name of the journal attribute";
    static final String DESC_ATTR_CONTRACT_NAME = "The name of contract value to set as value for the journal attribute";
    static final String DESC_HEADER_ATTRIBUTES = "This talbe defines the JSON objects sent to the journaling service";
    static final String DESC_USE_IN_LOGOUT = "Also include this parameter in the logout request";

    static final String DESC_RETURN_VALUE_ATTRIBUTE = "The attribute for the status returned in the JSON object";
    static final String DESC_RETURN_VALUE_ATTRIBUTE_TYPE = "The type of the return value to parse the json object";
    static final String DESC_RETURN_VALUE_SUCCESS = "Value to indicate the Journaling call was successful";
    static final String DESC_TIMESTAMP_NAME_LOGIN = "The name of the attribute for the timestamp to include in the request for Login. If empty, no timestamp will be included";
    static final String DESC_TIMESTAMP_NAME_LOGOUT = "The name of the attribute for the timestamp to include in the request for Logout. If empty, no timestamp will be included";

    static final String DEFAULT_IDPENPOINT = "https://server/idp/";
    static final String CB_ALLWAYS_SUCCEED = "Allways Succeed";
    static final String DESC_ALLWAYS_SUCCEED = "Allways succeed the adapter even if an error occours during journaling login or logout";

    static final String FIELD_A2H_PARAMS = "A2H Names";
    static final String DESC_A2H_PARAMS = "Names of attributes which will be inserted as header in the request to the webservice. Seperate by ','. If empty this will be ingored";

    static final String FIELD_RETRY_COUNT = "Retry count";
    static final String DESC_RETRY_COUNT = "Retry count to send the log entry to the remote server in case of a failure";
    static final int DEFAULT_RETRY_COUNT = 3;

    static final String FIELD_RETRY_PERIOD = "Retry period";
    static final String DESC_RETRY_PERIOD = "Retry period in microseconds after a failure to send the log entry to the remote server";
    static final int DEFAULT_RETRY_PERIOD = 1000;

    protected Log m_log;
    protected final IdpAuthnAdapterDescriptor m_descriptor;

    public AbstractLoginLogoutJournalingAdapter() {
        m_log = LogFactory.getLog(this.getClass());

        AdapterConfigurationGuiDescriptor guiDescriptor = new AdapterConfigurationGuiDescriptor(
                ADAPTER_GUI_DESCRIPTION);

        // configure the needed attributes
        List<FieldDescriptor> row = new ArrayList<FieldDescriptor>();
        TextFieldDescriptor fieldAttrJournalName = new TextFieldDescriptor(FIELD_ATTR_JOURNAL_NAME,
                DESC_ATTR_CONTRACT_NAME);
        fieldAttrJournalName.addValidator(new RequiredFieldValidator());
        row.add(fieldAttrJournalName);
        TextFieldDescriptor fieldAttrContractName = new TextFieldDescriptor(FIELD_ATTR_CONTRACT_NAME,
                DESC_ATTR_JOURNAL_NAME);
        fieldAttrContractName.addValidator(new RequiredFieldValidator());
        row.add(fieldAttrContractName);
        row.add(new CheckBoxFieldDescriptor(CB_ATTR_STATIC_VALUE, DESC_ATTR_STATIC_VALUE));
        row.add(new CheckBoxFieldDescriptor(CB_USE_IN_LOGOUT, DESC_USE_IN_LOGOUT));
        TableDescriptor table = new TableDescriptor(TABLE_HEADER_ATTRIBUTES, DESC_HEADER_ATTRIBUTES, row);
        table.addValidator(new HTTPParamRowValidator());
        guiDescriptor.addTable(table);

        TextFieldDescriptor fieldTimeStampNameLogin = new TextFieldDescriptor(FIELD_TIMESTAMP_NAME_LOGIN,
                DESC_TIMESTAMP_NAME_LOGIN);
        guiDescriptor.addField(fieldTimeStampNameLogin);

        TextFieldDescriptor fieldTimeStampNameLogout = new TextFieldDescriptor(FIELD_TIMESTAMP_NAME_LOGOUT,
                DESC_TIMESTAMP_NAME_LOGOUT);
        guiDescriptor.addField(fieldTimeStampNameLogout);

        TextFieldDescriptor fieldLougoutReasonParamName = new TextFieldDescriptor(FIELD_LOGOUT_REASON_PARAM_NAME,
                DESC_LOGOUT_REASON_PARAM_NAME);
        guiDescriptor.addField(fieldLougoutReasonParamName);

        TextFieldDescriptor fieldJournalURLLogin = new TextFieldDescriptor(FIELD_URL_JOURNAL_LOGIN,
                DESC_URL_JOURNAL_LOGIN);
        fieldJournalURLLogin.setDefaultValue(DEFAULT_USERSERVICE_URL);
        // fieldJournalURLLogin.addValidator(new HttpURLValidator());
        guiDescriptor.addField(fieldJournalURLLogin);

        TextFieldDescriptor fieldJournalURLLogout = new TextFieldDescriptor(FIELD_URL_JOURNAL_LOGOUT,
                DESC_URL_JOURNAL_LOGOUT);
        fieldJournalURLLogout.setDefaultValue(DEFAULT_USERSERVICE_URL);
        // fieldJournalURLLogout.addValidator(new HttpURLValidator());
        guiDescriptor.addField(fieldJournalURLLogout);

        SelectFieldDescriptor fdSelectHTTP = new SelectFieldDescriptor(SELECT_HTTP_TYPE, DESC_HTTP_TYPE,
                HTTP_TYPE_VALUES);
        fdSelectHTTP.setDefaultValue(HTTP_TYPE_VALUES[0]);
        guiDescriptor.addField(fdSelectHTTP);

        CheckBoxFieldDescriptor cbSendJSON = new CheckBoxFieldDescriptor(CB_SEND_JSON, DESC_SEND_JSON);
        guiDescriptor.addField(cbSendJSON);

        TextFieldDescriptor fieldRequestToHeaderParams = new TextFieldDescriptor(FIELD_A2H_PARAMS, DESC_A2H_PARAMS);
        guiDescriptor.addField(fieldRequestToHeaderParams);

        TextFieldDescriptor descrReturnAttr = new TextFieldDescriptor(FIELD_RETURN_VALUE_ATTRIBUTE,
                DESC_RETURN_VALUE_ATTRIBUTE);
        guiDescriptor.addField(descrReturnAttr);

        SelectFieldDescriptor fdSelectReturnAttrTyp = new SelectFieldDescriptor(SELECT_RETURN_VALUE_ATTRIBUTE_TYPE,
                DESC_RETURN_VALUE_ATTRIBUTE_TYPE, RETURN_VALUE_ATTRIBUTE_VALUES);
        fdSelectReturnAttrTyp.setDefaultValue(RETURN_VALUE_ATTRIBUTE_VALUES[0]);

        guiDescriptor.addField(fdSelectReturnAttrTyp);

        TextFieldDescriptor descrReturnValueSuccess = new TextFieldDescriptor(FIELD_RETURN_VALUE_SUCCESS,
                DESC_RETURN_VALUE_SUCCESS);
        guiDescriptor.addField(descrReturnValueSuccess);

        CheckBoxFieldDescriptor cbAllwaysSuccedd = new CheckBoxFieldDescriptor(CB_ALLWAYS_SUCCEED,
                DESC_ALLWAYS_SUCCEED);
        guiDescriptor.addField(cbAllwaysSuccedd);

        CheckBoxFieldDescriptor cbLogData = new CheckBoxFieldDescriptor(CB_LOG_DATA, DESC_LOG_DATA);
        guiDescriptor.addField(cbLogData);

        CheckBoxFieldDescriptor cbUseOAuth = new CheckBoxFieldDescriptor(CB_USE_OAUTH, DESC_USE_OAUTH);
        guiDescriptor.addField(cbUseOAuth);

        TextFieldDescriptor idpEndPointField = new TextFieldDescriptor(FIELD_IDPENPOINT, DESC_IDPENPOINT);

        idpEndPointField.addValidator(new HttpURLValidator());
        idpEndPointField.setDefaultValue(DEFAULT_IDPENPOINT);
        guiDescriptor.addField(idpEndPointField);

        TextFieldDescriptor scopeField = new TextFieldDescriptor(FIELD_SCOPE, DESC_SCOPE);
        guiDescriptor.addField(scopeField);

        TextFieldDescriptor clientIDField = new TextFieldDescriptor(FIELD_OAUTH_CLIENTID, DESC_OAUTH_CLIENTID);
        guiDescriptor.addField(clientIDField);

        TextFieldDescriptor clientPasswordField = new TextFieldDescriptor(FIELD_OAUTH_PASSWORD, DESC_OAUTH_PASSWORD,
                true);
        guiDescriptor.addField(clientPasswordField);

        TextFieldDescriptor retryCount = new TextFieldDescriptor(FIELD_RETRY_COUNT, DESC_RETRY_COUNT);
        retryCount.setDefaultValue(String.valueOf(DEFAULT_RETRY_COUNT));
        guiDescriptor.addField(retryCount);

        TextFieldDescriptor retryPeriod = new TextFieldDescriptor(FIELD_RETRY_PERIOD, DESC_RETRY_PERIOD);
        retryPeriod.setDefaultValue(String.valueOf(DEFAULT_RETRY_PERIOD));
        guiDescriptor.addField(retryPeriod);

        guiDescriptor.addValidator(new HTTPParamConfigurationValidator());

        Set<String> contract = new HashSet<String>();
        contract.add(CONTRACT_NAME_JOURNAL_INFO);
        contract.add(CONTRACT_NAME_JOURNAL_SUCCESS);
        contract.add(CONTRACT_NAME_JOURNAL_A2R_HEADERS);
        m_descriptor = new IdpAuthnAdapterDescriptor(this, ADAPTER_NAME, contract, false, guiDescriptor, false, ADAPTER_VERSION);
    }

    private class HTTPParamRowValidator implements RowValidator {
        private HTTPParamRowValidator() {
        }

        public void validate(FieldList fieldsInRow) throws ValidationException {
            String strAttrJournalName = fieldsInRow.getFieldValue(FIELD_ATTR_JOURNAL_NAME);
            String strAttrContractName = fieldsInRow.getFieldValue(FIELD_ATTR_CONTRACT_NAME);

            if (((strAttrJournalName == null || strAttrContractName == null || strAttrJournalName.isEmpty()
                    || strAttrContractName.isEmpty()))) {
                throw new ValidationException("Row contains empty values");
            }
        }
    }

    private class HTTPParamConfigurationValidator implements ConfigurationValidator {
        private HTTPParamConfigurationValidator() {
        }

        public void validate(Configuration configuration) throws ValidationException {
            Table table = configuration.getTable(TABLE_HEADER_ATTRIBUTES);
            List<Row> rows = table.getRows();
            if (rows.isEmpty()) {
                throw new ValidationException("Please add at least one parameter to the table.");
            }
            List<String> errors = new ArrayList<String>();
            Set<String> setAttrJournalNames = new HashSet<String>();
            Set<String> setAttrContractNames = new HashSet<String>();
            for (Row row : rows) {
                String strAttrJournalName = row.getFieldValue(FIELD_ATTR_JOURNAL_NAME);
                String strAttrContractName = row.getFieldValue(FIELD_ATTR_CONTRACT_NAME);

                if (!setAttrJournalNames.add(strAttrJournalName)) {
                    errors.add("Duplicate Journal-Parametername: " + strAttrJournalName);
                }

                if (!setAttrContractNames.add(strAttrContractName)) {
                    errors.add("Duplicate Contract-Parametername: " + strAttrContractName);
                }
            }
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
    }

}

