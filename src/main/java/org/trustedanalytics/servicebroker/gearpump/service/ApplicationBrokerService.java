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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import org.trustedanalytics.servicebroker.gearpump.config.CfCallerConfiguration;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.CfCaller;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApplicationBrokerService {

    @Autowired
    private CfCallerConfiguration cfCallerConfiguration;

    @Autowired
    CfCaller cfCaller;

    @Value("${api.endpoint}")
    private String apiEndpoint;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationBrokerService.class);

    private static final String RESOURCES_0_METADATA_GUID = "/resources/0/metadata/guid";
    private static final String METADATA_GUID = "/metadata/guid";
    private static final String APP_URL = "/entity/credentials/url";
    private static final String CREATE_SERVICE_BODY_TEMPLATE = "{\"name\":\"%s\", \"space_guid\":\"%s\", \"service_plan_guid\":\"%s\", \"parameters\":{\"name\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"GEARPUMP_MASTER\":\"%s\"}}";
    private static final String CREATE_SERVICE_BINDING_BODY_TEMPLATE = "{\"app_guid\":\"%s\", \"service_instance_guid\":\"%s\", \"async\":true}";
    private static final String CREATE_SERVICE_KEY_BODY_TEMPLATE = "{\"service_instance_guid\":\"%s\", \"name\": temp_param}";
    private static final String GET_ORG_GUID_URL = "{apiUrl}/v2/organizations?q=name:{orgName}";
    private static final String GET_SPACE_GUID_URL = "{apiUrl}/v2/organizations/{orgId}/spaces?q=name:{spaceName}";
    private static final String GET_SERVICE_GUID_URL = "{apiUrl}/v2/spaces/{spaceId}/services?q=label:{applicationName}";
    private static final String GET_SERVICE_PLAN_GUID_URL = "{apiUrl}/v2/service_plans?q=service_guid:{serviceGuid}";
    private static final String CREATE_SERVICE_INSTANCE_URL = "{apiUrl}/v2/service_instances";
    private static final String POST_SERVICE_BINDINGS_URL = "{apiUrl}/v2/service_bindings";
    private static final String POST_CREATE_SERVICE_KEY_URL = "{apiUrl}/v2/service_keys";
    private static final String DELETE_SERVICE_KEY_URL = "{apiUrl}/v2/service_keys/{serviceKeyGuid}";
    private static final String DELETE_SERVICE_URL = "{apiUrl}/v2/service_instances/{serviceId}";

    private ResponseEntity<String> execute(String url, HttpMethod method, String body, Object... urlVariables) {
        RestTemplate restTemplate = cfCaller.createRestTemplate();
        HttpEntity<String> request = cfCaller.createJsonRequest(body);
        URI expanded = (new UriTemplate(url)).expand(urlVariables);
        LOGGER.info("Performing call : " + expanded.toString());
        ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class, urlVariables);
        LOGGER.debug("Response status: " + response.getStatusCode());
        return response;
    }

    private String getUIOrgGuid() throws IOException {
        LOGGER.info("Getting UI Org Guid");
        ResponseEntity<String> response = execute(GET_ORG_GUID_URL, HttpMethod.GET, "", apiEndpoint, cfCallerConfiguration.getGearpumpUiOrg());
        String uiOrgGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
        LOGGER.debug("UI Org Guid {}", uiOrgGuid);
        return uiOrgGuid;
    }

    private String getUISpaceGuid(String uiOrgGuid) throws IOException {
        LOGGER.info("Getting UI Space Guid");
        ResponseEntity<String> response = execute(GET_SPACE_GUID_URL, HttpMethod.GET, "", apiEndpoint, uiOrgGuid, cfCallerConfiguration.getGearpumpUiSpace());
        String uiSpaceGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
        LOGGER.debug("UI Space Guid {}", uiSpaceGuid);
        return uiSpaceGuid;
    }

    private String getUIServiceGuid(String uiSpaceGuid) throws IOException {
        LOGGER.info("Getting UI Service Guid");
        ResponseEntity<String> response = execute(GET_SERVICE_GUID_URL, HttpMethod.GET, "", apiEndpoint, uiSpaceGuid, cfCallerConfiguration.getGearpumpUiName());
        String uiServiceGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
        LOGGER.debug("UI Service Guid {}", uiServiceGuid);
        return uiServiceGuid;
    }

    private String getUIServicePlanGuid(String serviceGuid) throws IOException {
        LOGGER.info("Getting Service Plan Guid");
        ResponseEntity<String> response = execute(GET_SERVICE_PLAN_GUID_URL, HttpMethod.GET, "", apiEndpoint, serviceGuid);
        String uiServicePlanGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
        LOGGER.debug("UI Service Plan Guid " + uiServicePlanGuid);
        return uiServicePlanGuid;
    }

    private String createUIInstance(String uiInstanceName, String spaceId, String uiServicePlanGuid, String username, String password, String gearpumpMaster) throws IOException {
        LOGGER.info("Creating Service Instance");
        String body = String.format(CREATE_SERVICE_BODY_TEMPLATE, uiInstanceName, spaceId, uiServicePlanGuid, uiInstanceName, username, password, gearpumpMaster);
        LOGGER.debug("Create app body: {}", body);
        ResponseEntity<String> response = execute(CREATE_SERVICE_INSTANCE_URL, HttpMethod.POST, body, apiEndpoint);
        String uiServiceInstanceGuid = cfCaller.getValueFromJson(response.getBody(), METADATA_GUID);
        LOGGER.debug("UI Service Instance Guid " + uiServiceInstanceGuid);
        return uiServiceInstanceGuid;
    }

    private void bindServiceInstanceToUIApp(String uiAppGuid, String uiServiceInstanceGuid) {
        LOGGER.info("Binding UI App to Service Instance");
        String body = String.format(CREATE_SERVICE_BINDING_BODY_TEMPLATE, uiAppGuid, uiServiceInstanceGuid);
        execute(POST_SERVICE_BINDINGS_URL, HttpMethod.POST, body, apiEndpoint, uiAppGuid, uiServiceInstanceGuid);
    }

    private String getUIAppUrl(String uiServiceInstanceGuid) throws IOException {
        LOGGER.info("Getting UI App URL using create service key function");
        String body = String.format(CREATE_SERVICE_KEY_BODY_TEMPLATE, uiServiceInstanceGuid);
        ResponseEntity<String> response = execute(POST_CREATE_SERVICE_KEY_URL, HttpMethod.POST, body, apiEndpoint);
        String uiAppUrl = cfCaller.getValueFromJson(response.getBody(), APP_URL);
        String serviceKeyGuid = cfCaller.getValueFromJson(response.getBody(), METADATA_GUID);
        LOGGER.info("Deleting service key");
        execute(DELETE_SERVICE_KEY_URL, HttpMethod.DELETE, "", apiEndpoint, serviceKeyGuid);
        LOGGER.debug("UI App url {}", uiAppUrl);
        return uiAppUrl;
    }

    public void deleteUIServiceInstance(String uiServiceGuid) {
        LOGGER.info("UI Service with GUID " + uiServiceGuid + " will be deleted");
        execute(DELETE_SERVICE_URL, HttpMethod.DELETE, "", apiEndpoint, uiServiceGuid);
    }

    public Map<String, String> deployUI(String uiInstanceName, String username, String password, String gearpumpMaster, String spaceId) throws IOException {
        // TODO the following could be obtrained only once
        String uiOrgGuid = getUIOrgGuid();
        String uiSpaceGuid = getUISpaceGuid(uiOrgGuid);
        String uiServiceGuid = getUIServiceGuid(uiSpaceGuid);
        String uiServicePlanGuid = getUIServicePlanGuid(uiServiceGuid);

        String uiServiceInstanceGuid = createUIInstance(uiInstanceName, spaceId, uiServicePlanGuid, username, password, gearpumpMaster);
        String uiAppUrl = getUIAppUrl(uiServiceInstanceGuid);
        //bindServiceInstanceToUIApp(uiAppGuid, uiServiceInstanceGuid);
        Map<String, String> dashboardData = new HashMap<String, String>();
        dashboardData.put("uiServiceInstanceGuid", uiServiceInstanceGuid);
        dashboardData.put("uiAppUrl", uiAppUrl);
        dashboardData.put("uiAppPort", "80");
        dashboardData.put("username", username);
        dashboardData.put("password", password);

        return dashboardData;
    }
}
