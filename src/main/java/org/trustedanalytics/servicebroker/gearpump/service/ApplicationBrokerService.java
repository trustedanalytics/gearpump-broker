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

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApplicationBrokerService {

    @Autowired
    private CfCallerConfiguration cfCallerConfiguration;

    @Value("${api.endpoint}")
    private String apiEndpoint;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationBrokerService.class);

    public static final String RESOURCES_0_METADATA_GUID = "/resources/0/metadata/guid";
    public static final String APPLICATION_VCAP_URI = "/application_env_json/VCAP_APPLICATION/application_uris/0";
    public static final String METADATA_GUID = "/metadata/guid";

    private static String CREATE_SERVICE_BODY_TEMPLATE = "{\"name\":\"%s\", \"space_guid\":\"%s\", \"service_plan_guid\":\"%s\", \"parameters\":{\"name\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"GEARPUMP_MASTER\":\"%s\"}}";
    private static String PUT_ENV_BODY_TEMPLATE = "{\"environment_json\":{\"username\":\"%s\", \"password\":\"%s\",\"GEARPUMP_MASTER\":\"%s\"}}"; // add more env variables if you want to set them in dashboard
    private static String CREATE_SERVICE_BINDING_BODY_TEMPLATE = "{\"app_guid\":\"%s\", \"service_instance_guid\":\"%s\", \"async\":true}";

    private static String GET_ORG_GUID_URL = "{apiUrl}/v2/organizations?q=name:{orgName}";
    private static String GET_SPACE_GUID_URL = "{apiUrl}/v2/organizations/{orgId}/spaces?q=name:{spaceName}";

    private static String GET_SERVICE_GUID_URL = "{apiUrl}/v2/spaces/{spaceId}/services?q=label:{applicationName}";
    private static String GET_SERVICE_PLAN_GUID_URL = "{apiUrl}/v2/service_plans?q=service_guid:{serviceGuid}";
    private static String GET_APPLICATION_GUID_URL = "{apiUrl}/v2/spaces/{spaceId}/apps?q=name:{applicationName}";
    private static String GET_APPLICATION_URL = "{apiUrl}/v2/apps/{applicationId}/env";
    private static String GET_APPS_URL = "{apiUrl}/v2/apps";

    private static String PUT_ENVINONMENT_VARIABLES = "{apiUrl}/v2/apps/{applicationId}";
    private static String CREATE_SERVICE_INSTANCE_URL = "{apiUrl}/v2/service_instances";
    private static String POST_SERVICE_BINDINGS_URL = "{apiUrl}/v2/service_bindings";
    private static String POST_RESTAGE_URL = "{apiUrl}/v2/apps/{applicationId}/restage";

    private static String DELETE_SERVICE_URL = "{apiUrl}/v2/service_instances/{serviceId}";

    @Autowired
    CfCaller cfCaller;


    private ResponseEntity<String> execute (String url, HttpMethod method, String body, Object... urlVariables){
        RestTemplate restTemplate = cfCaller.createRestTemplate();
        HttpEntity<String> request = cfCaller.createJsonRequest(body);
        URI expanded = (new UriTemplate(url)).expand(urlVariables);
        LOGGER.info("%n Performing call : " + expanded.toString() + "%n");
        ResponseEntity<String> response =
                restTemplate.exchange(url, method, request, String.class, urlVariables);
        LOGGER.info("%n Response status: " + response.getStatusCode() + "%n");
        return response;
    }

    private String getUIOrgGuid(){
        LOGGER.info("%n Getting UI Org Guid %n");
        ResponseEntity<String> response = execute(GET_ORG_GUID_URL, HttpMethod.GET, "", apiEndpoint, cfCallerConfiguration.getGearpumpUiOrg());
        String uiOrgGuid = null;
        try {
            uiOrgGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("%nUI Org Guid " + uiOrgGuid + "%n");
        return uiOrgGuid;
    }

    private String getUISpaceGuid(String uiOrgGuid){
        LOGGER.info("%n Getting UI Space Guid %n");
        ResponseEntity<String> response = execute(GET_SPACE_GUID_URL, HttpMethod.GET, "", apiEndpoint, uiOrgGuid, cfCallerConfiguration.getGearpumpUiSpace());
        String uiSpaceGuid = null;
        try {
            uiSpaceGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("%nUI Space Guid " + uiSpaceGuid + "%n");
        return uiSpaceGuid;
    }

    private String getUIServiceGuid(String uiSpaceGuid){
        LOGGER.info("%n Getting UI Service Guid %n");
        ResponseEntity<String> response = execute(GET_SERVICE_GUID_URL, HttpMethod.GET, "", apiEndpoint, uiSpaceGuid, cfCallerConfiguration.getGearpumpUiName());
        String uiServiceGuid = null;
        try {
            uiServiceGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("%nUI Service Guid " + uiServiceGuid + "%n");
        return uiServiceGuid;
    }

    private String getUIServicePlanGuid(String serviceGuid){
        LOGGER.info("%n Getting Service Plan Guid %n");
        ResponseEntity<String> response = execute(GET_SERVICE_PLAN_GUID_URL, HttpMethod.GET, "", apiEndpoint, serviceGuid);
        String uiServicePlanGuid = null;
        try {
            uiServicePlanGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("%nUI Service Plan Guid " + uiServicePlanGuid + "%n");
        return uiServicePlanGuid;
    }

    private String createUIInstance(String uiInstanceName, String spaceId, String uiServicePlanGuid, String username, String password, String gearpumpMaster){
        LOGGER.info("%n Creating Service Instance %n");
        String body = String.format(CREATE_SERVICE_BODY_TEMPLATE, uiInstanceName, spaceId, uiServicePlanGuid, uiInstanceName, username, password, gearpumpMaster);
        LOGGER.info("%n Create app body: " + body + "%n");
        ResponseEntity<String> response = execute(CREATE_SERVICE_INSTANCE_URL, HttpMethod.POST, body, apiEndpoint);
        String uiServiceInstanceGuid = null;
        try {
            uiServiceInstanceGuid = cfCaller.getValueFromJson(response.getBody(), METADATA_GUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("%nUI Service Instance Guid " + uiServiceInstanceGuid + "%n");
        return uiServiceInstanceGuid;
    }

    private void bindServiceInstanceToUIApp(String uiAppGuid, String uiServiceInstanceGuid){
        LOGGER.info("%n Binding UI App to Service Instance %n");
        String body=String.format(CREATE_SERVICE_BINDING_BODY_TEMPLATE, uiAppGuid, uiServiceInstanceGuid);
        execute(POST_SERVICE_BINDINGS_URL, HttpMethod.POST, body, apiEndpoint, uiAppGuid, uiServiceInstanceGuid);
    }

    private String getUIAppUrl(String spaceId, String uiInstanceName){
        // TODO change it, to send request and get the uiAppAddress
        return "unknown";
    }

    public void deleteUIServiceInstance(String uiServiceGuid){
        LOGGER.info("%nUI Service with GUID " + uiServiceGuid + " will be deleted%n");
        execute(DELETE_SERVICE_URL, HttpMethod.DELETE, "", apiEndpoint, uiServiceGuid);
    }

    public Map<String, String> deployUI(String uiInstanceName, String username, String password, String gearpumpMaster, String spaceId) {
        String uiOrgGuid = getUIOrgGuid();
        String uiSpaceGuid = getUISpaceGuid(uiOrgGuid);
        String uiServiceGuid = getUIServiceGuid(uiSpaceGuid);
        String uiServicePlanGuid = getUIServicePlanGuid(uiServiceGuid);
        String uiServiceInstanceGuid = createUIInstance(uiInstanceName, spaceId, uiServicePlanGuid, username, password, gearpumpMaster);
        String uiAppUrl = getUIAppUrl(spaceId, uiInstanceName);
        //bindServiceInstanceToUIApp(uiAppGuid, uiServiceInstanceGuid);
        Map<String, String> dashboardData = new HashMap<String,String>();
        dashboardData.put("uiServiceInstanceGuid", uiServiceInstanceGuid);
        dashboardData.put("uiAppUrl", uiAppUrl);
        return dashboardData;
    }
}
