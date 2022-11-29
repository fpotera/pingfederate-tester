package io.bluzy.poc.pingfederate.tests.config;

import static io.bluzy.poc.pingfederate.tests.PingConfig.PING_FEDERATE_HOST;
import static java.text.MessageFormat.format;

public interface URLs {

    String PING_FED_TOKEN_MGR_URL = format("https://{0}:9999/pf-admin-api/v1/oauth/accessTokenManagers", PING_FEDERATE_HOST);

    String PING_FED_OIDC_POLICY_URL = format("https://{0}:9999/pf-admin-api/v1/oauth/openIdConnect/policies", PING_FEDERATE_HOST);

    String PING_FED_CLIENT_URL = format("https://{0}:9999/pf-admin-api/v1/oauth/clients", PING_FEDERATE_HOST);

    String PING_FED_CV_URL = format("https://{0}:9999/pf-admin-api/v1/passwordCredentialValidators", PING_FEDERATE_HOST);

    String PING_FED_IDP_ADAPTER_URL = format("https://{0}:9999/pf-admin-api/v1/idp/adapters", PING_FEDERATE_HOST);

    String PING_FED_POLICY_CONTRACT_URL = format("https://{0}:9999/pf-admin-api/v1/authenticationPolicyContracts", PING_FEDERATE_HOST);

    String PING_FED_POLICY_URL = format("https://{0}:9999/pf-admin-api/v1/authenticationPolicies/policy", PING_FEDERATE_HOST);

    String PING_FED_POLICY_CONTRACT_MAPPING_URL = format("https://{0}:9999/pf-admin-api/v1/oauth/authenticationPolicyContractMappings", PING_FEDERATE_HOST);

    String PING_FED_ACCESS_TOKEN_MAPPING_URL = format("https://{0}:9999/pf-admin-api/v1/oauth/accessTokenMappings", PING_FEDERATE_HOST);

}
