package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.gerrit.extensions.common.LabelDefinitionInput;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

/**
 * @author Réda Housni Alaoui
 */
public class LabelApiRestClientTest {

    public static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);

    @Test
    public void testCreateLabelForProject() throws Exception {
        String projectName = "sandbox";
        String labelName = "Foo";
        LabelDefinitionInput input = new LabelDefinitionInput();
        input.commitMessage = "Create label";
        input.name = labelName;
        input.function = "MaxWithBlock";
        input.values = new HashMap<>();
        input.values.put("-1", "Fails");
        input.values.put("0", "No score");
        input.values.put("+1", "Verified");
        input.defaultValue = 0;
        input.branches = new ArrayList<>();
        input.branches.add("master");
        input.branches.add("main");
        input.canOverride = true;
        input.copyAnyScore = true;
        input.copyMinScore = true;
        input.copyMaxScore = true;
        input.copyAllScoresIfNoChange = true;
        input.copyAllScoresIfNoCodeChange = true;
        input.copyAllScoresOnTrivialRebase = true;
        input.copyAllScoresOnMergeFirstParentUpdate = true;
        input.copyValues = Collections.singletonList((short) -1);
        input.allowPostSubmit = true;
        input.ignoreSelfApproval = true;

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGetGson()
            .expectPut("/projects/sandbox/labels/" + labelName, "{\"name\":\"Foo\",\"function\":\"MaxWithBlock\"," +
                "\"values\":{\"0\":\"No score\",\"-1\":\"Fails\",\"+1\":\"Verified\"}," +
                "\"default_value\":0,\"branches\":[\"master\",\"main\"],\"can_override\":true,\"copy_any_score\":true," +
                "\"copy_min_score\":true,\"copy_max_score\":true,\"copy_all_scores_if_no_change\":true," +
                "\"copy_all_scores_if_no_code_change\":true,\"copy_all_scores_on_trivial_rebase\":true," +
                "\"copy_all_scores_on_merge_first_parent_update\":true,\"copy_values\":[-1],\"allow_post_submit\":true," +
                "\"ignore_self_approval\":true,\"commit_message\":\"Create label\"}", MOCK_JSON_ELEMENT)
            .get();

        createProjectApiRestClient(gerritRestClient, projectName).label(labelName)
            .create(input);

        EasyMock.verify(gerritRestClient);
    }

    private ProjectApiRestClient createProjectApiRestClient(GerritRestClient gerritRestClient, String projectName) {
        return new ProjectApiRestClient(gerritRestClient, new ProjectsParserBuilder()
            .get(), new BranchInfoParserBuilder()
            .get(), new TagInfoParserBuilder().get(), projectName);
    }
}
