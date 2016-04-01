/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.servicebroker.gearpump.service.externals.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public final class CfCaller {

    private static ObjectMapper MAPPER = new ObjectMapper();

    private RestTemplate restTemplate;

    public CfCaller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RestTemplate createRestTemplate() {
        return this.restTemplate;
    }

    public HttpEntity<String> createJsonRequest() {
        return new HttpEntity<String>(createJsonHeaders());
    }

    public HttpEntity<String> createJsonRequest(HttpHeaders headers) {
        return new HttpEntity<String>(headers == null ? createJsonHeaders() : headers);
    }

    public  HttpEntity<String> createJsonRequest(String body, HttpHeaders headers) {
        return new HttpEntity<String>(body, headers == null ? createJsonHeaders() : headers);
    }

    public HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Content-type", "application/json");
        return headers;
    }

    public String getValueFromJson(String json, String valuePath) throws IOException {
        JsonNode root = MAPPER.readTree(json);
        return root.at(valuePath).asText();
    }
}