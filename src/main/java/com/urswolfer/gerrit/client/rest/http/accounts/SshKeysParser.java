package com.urswolfer.gerrit.client.rest.http.accounts;

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.SshKeyInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Created by Phil Moore on 24/04/2020.
 */
public class SshKeysParser {
    private static final Type TYPE = new TypeToken<List<SshKeyInfo>>() {}.getType();

    private final Gson gson;

    public SshKeysParser(Gson gson) {
        this.gson = gson;
    }

    public SshKeyInfo parseSshKeyInfo(JsonElement result) {
        return gson.fromJson(result, SshKeyInfo.class);
    }

    public List<SshKeyInfo> parseSshKeyInfos(JsonElement result) {
        if (!result.isJsonArray()) {
            return Collections.singletonList(parseSshKeyInfo(result));
        }
        return gson.fromJson(result, TYPE);
    }
}
