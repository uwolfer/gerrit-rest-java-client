package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.gerrit.extensions.api.projects.LabelApi;
import com.google.gerrit.extensions.common.LabelDefinitionInput;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

/**
 * @author Réda Housni Alaoui
 */
public class LabelApiRestClient extends LabelApi.NotImplemented implements LabelApi {

    private final GerritRestClient gerritRestClient;
    private final ProjectApiRestClient projectApiRestClient;
    private final String name;

    public LabelApiRestClient(GerritRestClient gerritRestClient, ProjectApiRestClient projectApiRestClient, String name) {
        this.gerritRestClient = gerritRestClient;
        this.projectApiRestClient = projectApiRestClient;
        this.name = name;
    }

    @Override
    public LabelApi create(LabelDefinitionInput input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.putRequest(labelUrl(), body);
        return this;
    }

    protected String labelUrl() {
        return projectApiRestClient.projectsUrl() + "/labels/" + Url.encode(name);
    }
}
