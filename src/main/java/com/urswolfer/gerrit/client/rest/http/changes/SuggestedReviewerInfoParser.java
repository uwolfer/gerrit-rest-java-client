package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.collect.Lists;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Forrer
 */
public class SuggestedReviewerInfoParser {
    private final Gson gson;

    public SuggestedReviewerInfoParser(Gson gson) {
        this.gson = gson;
    }

    public List<SuggestedReviewerInfo> parseSuggestReviewerInfos(JsonElement result) throws RestApiException {
        if (!result.isJsonArray()) {
            if (!result.isJsonObject()) {
                throw new RestApiException(String.format("Unexpected JSON result format: %s", result));
            }
            return Collections.singletonList(parseSingleSuggestReviewerInfo(result.getAsJsonObject()));
        }

        List<SuggestedReviewerInfo> changeInfoList = Lists.newArrayList();
        for (JsonElement element : result.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                throw new RestApiException(String.format("This element should be a JsonObject: %s%nTotal JSON response: %n%s", element, result));
            }
            changeInfoList.add(parseSingleSuggestReviewerInfo(element.getAsJsonObject()));
        }
        return changeInfoList;
    }

    public SuggestedReviewerInfo parseSingleSuggestReviewerInfo(JsonObject result) {
        return gson.fromJson(result, SuggestedReviewerInfo.class);
    }

}
