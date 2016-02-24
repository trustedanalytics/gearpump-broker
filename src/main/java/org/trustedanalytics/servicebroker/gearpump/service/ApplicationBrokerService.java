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


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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
    private static final String CREATE_SERVICE_BODY_TEMPLATE = "{\"name\":\"%s\", \"space_guid\":\"%s\", \"service_plan_guid\":\"%s\", \"parameters\":{\"name\":\"%s\",\"USERNAME\":\"%s\",\"PASSWORD\":\"%s\",\"GEARPUMP_MASTER\":\"%s\"}}";
    private static final String CREATE_SERVICE_KEY_BODY_TEMPLATE = "{\"service_instance_guid\":\"%s\", \"name\": \"temp_param\"}";
    private static final String GET_ORG_GUID_URL = "{apiUrl}/v2/organizations?q=name:{orgName}";
    private static final String GET_SPACE_GUID_URL = "{apiUrl}/v2/organizations/{orgId}/spaces?q=name:{spaceName}";
    private static final String GET_SERVICE_GUID_URL = "{apiUrl}/v2/spaces/{spaceId}/services?q=label:{applicationName}";
    private static final String GET_SERVICE_PLAN_GUID_URL = "{apiUrl}/v2/service_plans?q=service_guid:{serviceGuid}";
    private static final String CREATE_SERVICE_INSTANCE_URL = "{apiUrl}/v2/service_instances";
    private static final String POST_CREATE_SERVICE_KEY_URL = "{apiUrl}/v2/service_keys";
    private static final String DELETE_SERVICE_KEY_URL = "{apiUrl}/v2/service_keys/{serviceKeyGuid}";
    private static final String DELETE_SERVICE_URL = "{apiUrl}/v2/service_instances/{serviceId}";

    private static String uiOrgGuid;
    private static String uiSpaceGuid;
    private static String uiServiceGuid;
    private static String uiServicePlanGuid;

    private String getUIOrgGuid() throws DashboardServiceException {
        LOGGER.debug("getUIOrgGuid()");
        if (uiOrgGuid == null) {
            LOGGER.info("Getting UI Org GUID from CF");
            ResponseEntity<String> response = execute(GET_ORG_GUID_URL, HttpMethod.GET, "", apiEndpoint, cfCallerConfiguration.getGearpumpUiOrg());
            try {
                uiOrgGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
            } catch (IOException e) {
                throw new DashboardServiceException("Cannot obtain org GUID (check GEARPUMP_UI_ORG variable).", e);
            }
            LOGGER.debug("UI Org GUID '{}'", uiOrgGuid);
            if (StringUtils.isEmpty(uiOrgGuid)) {
                throw new DashboardServiceException("Cannot obtain org GUID (check GEARPUMP_UI_ORG variable).");
            }
        }
        return uiOrgGuid;
    }

    private String getUISpaceGuid(String uiOrgGuid) throws DashboardServiceException {
        LOGGER.debug("getUISpaceGuid({})", uiOrgGuid);
        if (uiSpaceGuid == null) {
            LOGGER.info("Getting UI Space GUID from CF");
            ResponseEntity<String> response = execute(GET_SPACE_GUID_URL, HttpMethod.GET, "", apiEndpoint, uiOrgGuid, cfCallerConfiguration.getGearpumpUiSpace());
            try {
                uiSpaceGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
            } catch (IOException e) {
                throw new DashboardServiceException("Cannot obtain space GUID (check GEARPUMP_UI_SPACE variable).", e);
            }
            LOGGER.debug("UI Space GUID '{}'", uiSpaceGuid);
            if (StringUtils.isEmpty(uiSpaceGuid)) {
                throw new DashboardServiceException("Cannot obtain space GUID (check GEARPUMP_UI_SPACE variable).");
            }
        }
        return uiSpaceGuid;
    }

    private String getUIServiceGuid(String uiSpaceGuid) throws DashboardServiceException {
        LOGGER.debug("getUIServiceGuid({})", uiSpaceGuid);
        if (uiServiceGuid == null) {
            LOGGER.info("Getting UI Service GUID from CF");
            ResponseEntity<String> response = execute(GET_SERVICE_GUID_URL, HttpMethod.GET, "", apiEndpoint, uiSpaceGuid, cfCallerConfiguration.getGearpumpUiName());
            try {
                uiServiceGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
            } catch (IOException e) {
                throw new DashboardServiceException("Cannot obtain dashboard service GUID (check GEARPUMP_UI_NAME variable).", e);
            }
            LOGGER.debug("UI Service GUID '{}'", uiServiceGuid);
            if (StringUtils.isEmpty(uiServiceGuid)) {
                throw new DashboardServiceException("Cannot obtain dashboard service GUID (check GEARPUMP_UI_NAME variable).");
            }
        }
        return uiServiceGuid;
    }

    private String getUIServicePlanGuid(String serviceGuid) throws DashboardServiceException {
        LOGGER.debug("getUIServicePlanGuid({})", serviceGuid);
        if (uiServicePlanGuid == null) {
            LOGGER.info("Getting Service Plan GUID from CF");
            ResponseEntity<String> response = execute(GET_SERVICE_PLAN_GUID_URL, HttpMethod.GET, "", apiEndpoint, serviceGuid);
            try {
                uiServicePlanGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
            } catch (IOException e) {
                throw new DashboardServiceException("Cannot obtain dashboard service plan GUID.", e);
            }
            LOGGER.debug("UI Service Plan GUID '{}'", uiServicePlanGuid);
            if (StringUtils.isEmpty(uiServicePlanGuid)) {
                throw new DashboardServiceException("Cannot obtain dashboard service plan GUID.");
            }
        }
        return uiServicePlanGuid;
    }

    private ResponseEntity<String> execute(String url, HttpMethod method, String body, Object... urlVariables) {
        RestTemplate restTemplate = cfCaller.createRestTemplate();
        HttpEntity<String> request = cfCaller.createJsonRequest(body);
        URI expanded = (new UriTemplate(url)).expand(urlVariables);
        LOGGER.info("Performing call: {}", expanded.toString());
        ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class, urlVariables);
        LOGGER.debug("Response status: {}", response.getStatusCode());
        return response;
    }

    private String createUIInstance(String uiInstanceName, String spaceId, String uiServicePlanGuid, String username, String password, String gearpumpMaster) throws IOException {
        LOGGER.info("Creating Service Instance");
        String body = String.format(CREATE_SERVICE_BODY_TEMPLATE, uiInstanceName, spaceId, uiServicePlanGuid, uiInstanceName, username, password, gearpumpMaster);
        LOGGER.debug("Create app body: {}", body);
        ResponseEntity<String> response = execute(CREATE_SERVICE_INSTANCE_URL, HttpMethod.POST, body, apiEndpoint);
        String uiServiceInstanceGuid = cfCaller.getValueFromJson(response.getBody(), METADATA_GUID);
        LOGGER.debug("UI Service Instance Guid '{}'", uiServiceInstanceGuid);
        return uiServiceInstanceGuid;
    }

    private String getUIAppUrl(String uiServiceInstanceGuid) throws IOException {
        LOGGER.info("Getting UI App URL using create service key function");
        String body = String.format(CREATE_SERVICE_KEY_BODY_TEMPLATE, uiServiceInstanceGuid);
        ResponseEntity<String> response = execute(POST_CREATE_SERVICE_KEY_URL, HttpMethod.POST, body, apiEndpoint);
        String uiAppUrl = cfCaller.getValueFromJson(response.getBody(), APP_URL);
        String serviceKeyGuid = cfCaller.getValueFromJson(response.getBody(), METADATA_GUID);
        LOGGER.info("Deleting service key");
        execute(DELETE_SERVICE_KEY_URL, HttpMethod.DELETE, "", apiEndpoint, serviceKeyGuid);
        LOGGER.debug("UI App url '{}'", uiAppUrl);
        return uiAppUrl;
    }

    public void deleteUIServiceInstance(String uiServiceGuid) {
        try {
            execute(DELETE_SERVICE_URL, HttpMethod.DELETE, "", apiEndpoint, uiServiceGuid);
        }
        catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                LOGGER.info("Cannot delete GearPump UI instance with GUID " + uiServiceGuid + " - it doesn't exist");
            }
            else {
                throw e;
            }
        }
    }

    public Map<String, String> deployUI(String uiInstanceName, String username, String password, String gearpumpMaster, String spaceId)
            throws DashboardServiceException, ApplicationBrokerServiceException {
        String uiOrgGuid = getUIOrgGuid();
        String uiSpaceGuid = getUISpaceGuid(uiOrgGuid);
        String uiServiceGuid = getUIServiceGuid(uiSpaceGuid);
        String uiServicePlanGuid = getUIServicePlanGuid(uiServiceGuid);

        String uiServiceInstanceGuid = null;
        String uiAppUrl = null;
        try {
            uiServiceInstanceGuid = createUIInstance(uiInstanceName, spaceId, uiServicePlanGuid, username, password, gearpumpMaster);
            uiAppUrl = getUIAppUrl(uiServiceInstanceGuid);
        } catch (IOException e) {
            throw new ApplicationBrokerServiceException("Cannot create UI instance.", e);
        }

        Map<String, String> dashboardData = new HashMap<>();
        dashboardData.put("uiServiceInstanceGuid", uiServiceInstanceGuid);
        dashboardData.put("uiAppUrl", uiAppUrl);
        dashboardData.put("username", username);
        dashboardData.put("password", password);

        return dashboardData;
    }
}
