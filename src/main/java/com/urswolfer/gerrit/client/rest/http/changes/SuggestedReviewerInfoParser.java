package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Forrer
 */
public class SuggestedReviewerInfoParser {
    private static final Type TYPE = new TypeToken<List<SuggestedReviewerInfo>>() {}.getType();

    private final Gson gson;

    public SuggestedReviewerInfoParser(Gson gson) {
        this.gson = gson;
    }

    public List<SuggestedReviewerInfo> parseSuggestReviewerInfos(JsonElement result) throws RestApiException {
        if (!result.isJsonArray()) {
            return Collections.singletonList(gson.fromJson(result, SuggestedReviewerInfo.class));
        }
        return gson.fromJson(result, TYPE);
    }

}
