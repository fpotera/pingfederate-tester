package io.bluzy.poc.pingfederate.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class HTTPStatusException extends Exception {

    private static final long serialVersionUID = -5115394666925133407L;
    private boolean bAllowMessagePropagation = false;

    public boolean isAllowMessagePropagation() {
        return bAllowMessagePropagation;
    }

    public HTTPStatusException() {
    }

    public HTTPStatusException(String message) {
        super(message);
    }

    public HTTPStatusException(Throwable cause) {
        super(cause);
    }

    public HTTPStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTTPStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getHTTPStatusMessage() {

        String strMessage = "";
        try {
            JsonObject json = (new JsonParser()).parse(this.getMessage()).getAsJsonObject();

            if (json.get("body").isJsonObject() && json.getAsJsonObject("body").has("code")) {
                strMessage = json.getAsJsonObject("body").get("code").getAsString();
            } else {
                strMessage = json.get("body").getAsString();
            }
        } catch (JsonParseException ex) {
        }

        return strMessage;
    }

}

