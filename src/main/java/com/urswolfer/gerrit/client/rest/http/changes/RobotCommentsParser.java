package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.common.RobotCommentInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TreeMap;

/**
 * @author EFregnan
 */

public class RobotCommentsParser {

    private static final Type TYPE = new TypeToken<TreeMap<String, List<RobotCommentInfo>>>() {}.getType();

    private final Gson gson;

    public RobotCommentsParser(Gson gson) {
        this.gson = gson;
    }

    public TreeMap<String, List<RobotCommentInfo>> parseRobotCommentInfos(JsonElement result) {
        return gson.fromJson(result, TYPE);
    }

    public RobotCommentInfo parseSingleRobotCommentInfo(JsonObject result) {
        return gson.fromJson(result, RobotCommentInfo.class);
    }

}

