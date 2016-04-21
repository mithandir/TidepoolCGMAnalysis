/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2016.
 */

package de.qfotografie.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.qfotografie.akm.DataPoint;
import de.qfotografie.akm.LoginData;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
@EnableAutoConfiguration
public class TidepoolController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TidepoolController.class);

    private String token;
    private String userId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void login(String username, String password) {
        LOGGER.info("Login started ...");
        ResponseEntity<LoginData> loginResponse = restTemplate.exchange("https://api.tidepool.org/auth/login", HttpMethod.POST, createBasicLoginHeader(username, password), LoginData.class);

        userId = loginResponse.getBody().getUserid();
        token = loginResponse.getHeaders().get("x-tidepool-session-token").get(0);
        LOGGER.info("Login done");
    }

    @Cacheable("datapoints")
    public List<DataPoint> getDataPoints() {
        if (!isAuthenticated() && !isDevelopMode()) {
            login(System.getProperty("username"), System.getProperty("password"));
        }

        LOGGER.info("Retrieving Data ...");
        ResponseEntity<List<DataPoint>> rateResponse;

        if (isDevelopMode()) {
            rateResponse = mockRESTServiceResponse();
        } else {
            rateResponse = restTemplate.exchange("https://api.tidepool.org/data/" + userId,
                    HttpMethod.GET, createSessionTokenHeader(token), new ParameterizedTypeReference<List<DataPoint>>() {
                    });
        }
        LOGGER.info("Data retrieved");

        return rateResponse.getBody();
    }

    private boolean isAuthenticated() {
        return token != null && userId != null;

    }

    private static HttpEntity<String> createSessionTokenHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-tidepool-session-token", token);

        return new HttpEntity<>("parameters", headers);
    }

    private static HttpEntity<String> createBasicLoginHeader(String username, String password) {
        HttpHeaders headers = new HttpHeaders();

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        return new HttpEntity<>(headers);
    }

    private boolean isDevelopMode() {
        return Boolean.parseBoolean(System.getProperty("developMode", "false"));
    }

    private ResponseEntity<List<DataPoint>> mockRESTServiceResponse() {
        ResponseEntity<List<DataPoint>> rateResponse;
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        converters.stream().filter(converter -> converter instanceof MappingJackson2HttpMessageConverter).forEach(converter -> {
            MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
            jsonConverter.setObjectMapper(new ObjectMapper());
            jsonConverter.setSupportedMediaTypes(Arrays.asList(new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET), new MediaType("text", "plain", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET)));
        });
        rateResponse = restTemplate.exchange("https://raw.githubusercontent.com/mithandir/TidepoolCGMAnalysis/master/src/test/resources/testdata/testdata.json",
                HttpMethod.GET, createSessionTokenHeader(token), new ParameterizedTypeReference<List<DataPoint>>() {
                });
        return rateResponse;
    }
}
