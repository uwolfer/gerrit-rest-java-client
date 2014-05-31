package com.urswolfer.gerrit.client.rest;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class GerritAuthDataTest {
    @Test
    public void basicAuth() throws Exception {
        String host = "http://localhost:8080";
        GerritAuthData.Basic authData = new GerritAuthData.Basic(host);
        assertEquals(host, authData.getHost());
        assertEquals("", authData.getLogin());
        assertEquals("", authData.getPassword());
        assertEquals(false, authData.isLoginAndPasswordAvailable());
    }

    @Test
    public void basicAuthWithUsernameAndPassword() throws Exception {
        String host = "http://localhost:8080";
        String user = "foo";
        String password = "bar";
        GerritAuthData.Basic authData = new GerritAuthData.Basic(host, user, password);
        assertEquals(host, authData.getHost());
        assertEquals(user, authData.getLogin());
        assertEquals(password, authData.getPassword());
        assertEquals(true, authData.isLoginAndPasswordAvailable());
    }
}