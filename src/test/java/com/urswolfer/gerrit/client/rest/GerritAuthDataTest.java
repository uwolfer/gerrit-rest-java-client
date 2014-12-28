package com.urswolfer.gerrit.client.rest;

import com.google.common.truth.Truth;
import org.testng.annotations.Test;

public class GerritAuthDataTest {
    @Test
    public void basicAuth() throws Exception {
        String host = "http://localhost:8080";
        GerritAuthData.Basic authData = new GerritAuthData.Basic(host);
        Truth.assertThat(authData.getHost()).is(host);
        Truth.assertThat(authData.getLogin()).isEmpty();
        Truth.assertThat(authData.getPassword()).isEmpty();
        Truth.assertThat(authData.isLoginAndPasswordAvailable()).isFalse();
    }

    @Test
    public void basicAuthWithUsernameAndPassword() throws Exception {
        String host = "http://localhost:8080";
        String user = "foo";
        String password = "bar";
        GerritAuthData.Basic authData = new GerritAuthData.Basic(host, user, password);
        Truth.assertThat(authData.getHost()).is(host);
        Truth.assertThat(authData.getLogin()).is(user);
        Truth.assertThat(authData.getPassword()).is(password);
        Truth.assertThat(authData.isLoginAndPasswordAvailable()).isTrue();
    }
}
