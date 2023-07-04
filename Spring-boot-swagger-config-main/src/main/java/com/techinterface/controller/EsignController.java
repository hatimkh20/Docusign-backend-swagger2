package com.techinterface.controller;

import io.swagger.annotations.Api;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Api(tags = "ESign Controller")
@CrossOrigin(
        origins = {"*"},
        methods = {RequestMethod.PUT, RequestMethod.GET, RequestMethod.HEAD})
@RestController
@RequestMapping(path="/api/v1/esign", name = "ESign Controller")
public class EsignController {
    @Value("${docusign.clientId}")
    private String CLIENT_ID;

    @Value("${docusign.clientSecret}")
    private String CLIENT_SECRET;

    @Value("${docusign.redirectUri}")
    private String REDIRECT_URI;


    @GetMapping("/login")
    public ResponseEntity<String> startOAuth() {
        String authUrl = "https://account-d.docusign.com/oauth/auth?response_type=code&scope=signature&client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI;
        return ResponseEntity.ok(authUrl);
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(CallbackRequest request) {
        String accessToken = getAccessToken(request.getCode());
        return ResponseEntity.ok(accessToken);
    }

    private String getAccessToken(String authCode) {
        try {
            String clientIdAndSecret = CLIENT_ID + ":" + CLIENT_SECRET;
            String encodedClientIdAndSecret = Base64.getEncoder().encodeToString(clientIdAndSecret.getBytes(StandardCharsets.UTF_8));

            HttpClient httpClient = HttpClients.createDefault();
            RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add("Authorization", "Basic " + encodedClientIdAndSecret);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "authorization_code");
            map.add("code", authCode);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity("https://account-d.docusign.com/oauth/token/", request, String.class);

            return response.getBody();
        } catch (HttpClientErrorException ex) {
            System.out.println(ex);
        }
        return null;
    }


    static class CallbackRequest {
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
