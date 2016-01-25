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

package org.trustedanalytics.servicebroker.gearpump.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.hadoop.config.client.*;
import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CredentialPersistorService {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private ZookeeperClient zookeeperClient;

    public CredentialPersistorService(ZookeeperClient zookeeperClient) {
        this.zookeeperClient = zookeeperClient;
    }

    public String toJSONString(Map<String, Object> map) throws JsonProcessingException {
        return mapper.writeValueAsString(map);
    }

    public void persistCredentials(String serviceInstanceId, Map<String, Object> map) throws IOException {
        zookeeperClient.addZNode(this.getZNodePath(serviceInstanceId), toJSONString(map).getBytes(CHARSET));
    }

    public GearPumpCredentials readCredentials(String serviceInstanceId) throws IOException {
        byte[] bytes = zookeeperClient.getZNode(this.getZNodePath(serviceInstanceId));
        return fromJSONString(new String(bytes));
    }


    public static GearPumpCredentials fromJSONString(String json) throws IOException {
        GearPumpCredentials result = null;

        Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, String>>(){});

        if (map != null && map.size() != 0) {
            result = new GearPumpCredentials(
                    (String) map.get("masters"),
                    (String) map.get("yarnApplicationId"),
                    (String) map.get("dashboardUrl"),
                    (String) map.get("dashboardGuid"),
                    (String) map.get("username"),
                    (String) map.get("password"));
        }

        return result;
    }

    private String getZNodePath(String serviceInstanceId) {
        return  String.format("/additionalData/%s", serviceInstanceId);
    }
}
