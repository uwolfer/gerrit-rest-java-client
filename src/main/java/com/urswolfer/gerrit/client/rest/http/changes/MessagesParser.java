package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.common.ChangeMessageInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author EFregnan
 */
public class MessagesParser {

    private static final Type TYPE = new TypeToken<List<ChangeMessageInfo>>() {}.getType();

    private final Gson gson;

    public MessagesParser(Gson gson) {
        this.gson = gson;
    }

    public List<ChangeMessageInfo> parseChangeMessageInfos(JsonElement result) {
        return gson.fromJson(result, TYPE);
    }

    public ChangeMessageInfo parseSingleChangeMessageInfo(JsonObject result) {
        return gson.fromJson(result, ChangeMessageInfo.class);
    }
}
