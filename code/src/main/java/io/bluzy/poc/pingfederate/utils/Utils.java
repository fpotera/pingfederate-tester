package io.bluzy.poc.pingfederate.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sourceid.saml20.adapter.attribute.AttributeValue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Utils {

    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        String[] pair = null;
        String name = null;
        String value = null;
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            pair = param.split("=");
            if (pair.length >= 2) {
                name = pair[0];
                value = pair[1];
                map.put(name, value);
            }
        }
        return map;
    }


    public static void aggregateContractDataFromResponseAndRequestData(HashMap<String, Object> mapContractData,
                                                                       Set<String> setContractAttributes, JsonObject jsonResponseData, JsonObject jsonRequestData, JsonObject jsonPathData) {
        JsonElement jsonElem = null;
        for (String key : setContractAttributes) {
            // add response elements to contract
            jsonElem = jsonResponseData.get(key);
            if (jsonElem != null && !jsonElem.isJsonNull()) {
                if (jsonElem.isJsonObject()) {
                    mapContractData.put(key, new AttributeValue(jsonElem.getAsJsonObject().toString()));
                } else if (jsonElem.isJsonArray()) {
                    mapContractData.put(key, new AttributeValue(jsonElem.getAsJsonArray().toString()));
                } else {
                    mapContractData.put(key, new AttributeValue(jsonElem.getAsString()));
                }
            } else {
                // add request elements to contract
                jsonElem = jsonRequestData.get(key);
                if (jsonElem != null && !jsonElem.isJsonNull()) {
                    if (jsonElem.isJsonObject()) {
                        mapContractData.put(key, new AttributeValue(jsonElem.getAsJsonObject().toString()));
                    } else if (jsonElem.isJsonArray()) {
                        mapContractData.put(key, new AttributeValue(jsonElem.getAsJsonArray().toString()));
                    } else {
                        mapContractData.put(key, new AttributeValue(jsonElem.getAsString()));
                    }
                } else {
                    jsonElem = jsonPathData.get(key);
                    if (jsonElem != null && !jsonElem.isJsonNull()) {
                        if (jsonElem.isJsonObject()) {
                            mapContractData.put(key, new AttributeValue(jsonElem.getAsJsonObject().toString()));
                        } else if (jsonElem.isJsonArray()) {
                            mapContractData.put(key, new AttributeValue(jsonElem.getAsJsonArray().toString()));
                        } else {
                            mapContractData.put(key, new AttributeValue(jsonElem.getAsString()));
                        }
                    }
                }
            }
        }
    }
}
