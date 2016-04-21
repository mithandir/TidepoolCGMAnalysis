/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2016.
 */

package de.qfotografie.REST;

import java.util.List;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import de.qfotografie.akm.DataPoint;
import de.qfotografie.akm.LoginData;

@Component
@EnableAutoConfiguration
public class TidepoolController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TidepoolController.class);

    private String token;
    private String userId;

    private RestTemplate restTemplate = new RestTemplate();

    public void login(String username, String password) {
        LOGGER.info("Login started ...");
        ResponseEntity<LoginData> loginResponse = restTemplate.exchange("https://api.tidepool.org/auth/login", HttpMethod.POST, createBasicLoginHeader(username, password), LoginData.class);

        userId = loginResponse.getBody().getUserid();
        token = loginResponse.getHeaders().get("x-tidepool-session-token").get(0);
        LOGGER.info("Login done");
    }

    @Cacheable("datapoints")
    public List<DataPoint> getDataPoints() {
        if (!isAuthenticated()) {
            login(System.getProperty("username"), System.getProperty("password"));
        }
        LOGGER.info("Retrieving Data ...");
        ResponseEntity<List<DataPoint>> rateResponse = restTemplate.exchange("https://api.tidepool.org/data/" + userId,
                HttpMethod.GET, createSessionTokenHeader(token), new ParameterizedTypeReference<List<DataPoint>>() {
                });
        LOGGER.info("Data retrieved");

        return rateResponse.getBody();
    }

    private boolean isAuthenticated() {
        if (token != null && userId != null) {
            return true;
        }

        return false;
    }

    private static HttpEntity<String> createSessionTokenHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-tidepool-session-token", token);

        return new HttpEntity<String>("parameters", headers);
    }

    private static HttpEntity<String> createBasicLoginHeader(String username, String password) {
        HttpHeaders headers = new HttpHeaders();

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        return new HttpEntity<String>(headers);
    }
}
