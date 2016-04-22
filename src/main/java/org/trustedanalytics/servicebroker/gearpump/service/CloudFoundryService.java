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
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import org.trustedanalytics.servicebroker.gearpump.config.CfCallerConfiguration;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.CfCaller;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudFoundryService {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    @Autowired
    private CfCallerConfiguration cfCallerConfiguration;

    @Autowired
    CfCaller cfCaller;

    @Value("${api.endpoint}")
    private String cfApiEndpoint;

    @Value("${uaa.endpoint}")
    private String uaaApiEndpoint;

    @Value("${uaa.token_uri}")
    private String uaaTokenApiEndpoint;

    @Value("${uaa.login_uri}")
    private String loginApiEndpoint;

    @Value("${uaa.admin_client.id}")
    private String ssoAdminClientId;

    @Value("${uaa.admin_client.secret}")
    private String ssoAdminClientSecret;

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudFoundryService.class);

    private static final String RESOURCES_0_METADATA_GUID = "/resources/0/metadata/guid";
    private static final String METADATA_GUID = "/metadata/guid";
    private static final String UAA_ACCESS_TOKEN = "/access_token";
    private static final String UAA_TOKEN_TYPE = "/token_type";
    private static final String APP_URL = "/entity/credentials/url";
    private static final String CREATE_SERVICE_BODY_TEMPLATE = "{\"name\":\"%s\",\"space_guid\":\"%s\",\"service_plan_guid\":\"%s\",\"parameters\":{\"push_argument\":\"--no-start\",\"name\":\"%s\",\"USERNAME\":\"%s\",\"PASSWORD\":\"%s\",\"GEARPUMP_MASTER\":\"%s\",\"UAA_CLIENT_ID\":\"%s\",\"UAA_CLIENT_SECRET\":\"%s\",\"UAA_HOST\":\"%s\",\"CF_API_ENDPOINT\":\"%s\",\"ORG_ID\":\"%s\"}}";
    private static final String UPDATE_APP_ENV_BODY_TEMPLATE = "{\"environment_json\":{\"USERNAME\":\"%s\",\"PASSWORD\":\"%s\",\"GEARPUMP_MASTER\":\"%s\",\"UAA_CLIENT_ID\":\"%s\",\"UAA_CLIENT_SECRET\":\"%s\",\"UAA_HOST\":\"%s\",\"CF_API_ENDPOINT\":\"%s\",\"ORG_ID\":\"%s\",\"CALLBACK\":\"%s\"}}";
    private static final String CREATE_SERVICE_KEY_BODY_TEMPLATE = "{\"service_instance_guid\":\"%s\",\"name\":\"temp_param\"}";
    private static final String CREATE_UAA_TOKEN_BODY_TEMPLATE = "grant_type=client_credentials&response_type=token";
    private static final String CREATE_UAA_CLIENT_BODY_TEMPLATE = "{\"client_id\":\"%s\",\"name\":\"%s\",\"client_secret\":\"%s\",\"scope\":[\"cloud_controller.read\",\"openid\"],\"resource_ids\":[\"none\"],\"authorities\":[\"cloud_controller.read\",\"cloud_controller.write\",\"openid\"],\"authorized_grant_types\":[\"client_credentials\",\"authorization_code\",\"refresh_token\"],\"autoapprove\":true,\"access_token_validity\":43200,\"redirect_uri\":[\"%s\"]}";
    private static final String STATUS_STOPPED_BODY = "{\"state\":\"STOPPED\"}";
    private static final String STATUS_STARTED_BODY = "{\"state\":\"STARTED\"}";
    private static final String GET_ORG_GUID_URL = "{apiUrl}/v2/organizations?q=name:{orgName}";
    private static final String GET_SPACE_GUID_URL = "{apiUrl}/v2/organizations/{orgId}/spaces?q=name:{spaceName}";
    private static final String GET_SERVICE_GUID_URL = "{apiUrl}/v2/spaces/{spaceId}/services?q=label:{applicationName}";
    private static final String GET_SERVICE_PLAN_GUID_URL = "{apiUrl}/v2/service_plans?q=service_guid:{serviceGuid}";
    private static final String CREATE_SERVICE_INSTANCE_URL = "{apiUrl}/v2/service_instances";
    private static final String GET_APP_GUID_URL = "{apiUrl}/v2/spaces/{spaceGuid}/apps?q=name:{appName}";
    private static final String UPDATE_APP_URL = "{apiUrl}/v2/apps/{appGuid}";
    private static final String POST_CREATE_SERVICE_KEY_URL = "{apiUrl}/v2/service_keys";
    private static final String DELETE_SERVICE_KEY_URL = "{apiUrl}/v2/service_keys/{serviceKeyGuid}";
    private static final String DELETE_SERVICE_URL = "{apiUrl}/v2/service_instances/{serviceId}";
    private static final String CREATE_UAA_TOKEN_URL = "{uaaTokenUrl}";
    private static final String CREATE_UAA_CLIENT_URL = "{uaaUrl}/oauth/clients";
    private static final String DELETE_UAA_CLIENT_URL = "{uaaUrl}/oauth/clients/{client_id}";
    private static final String REDIRECT_URI_SUFIX = "/login/oauth2/cloudfoundryuaa/callback";

    private static String uiOrgGuid;
    private static String uiSpaceGuid;
    private static String uiServiceGuid;
    private static String uiServicePlanGuid;

    @PostConstruct
    private void init() throws DashboardServiceException {
        LOGGER.info("Obtaining org, space, service and plan GUIDs.");
        uiOrgGuid = getUIOrgGuid();
        uiSpaceGuid = getUISpaceGuid(uiOrgGuid);
        uiServiceGuid = getUIServiceGuid(uiSpaceGuid);
        uiServicePlanGuid = getUIServicePlanGuid(uiServiceGuid);
    }

    private String getUIOrgGuid() throws DashboardServiceException {
        LOGGER.debug("getUIOrgGuid()");
        if (uiOrgGuid == null) {
            LOGGER.info("Getting UI Org GUID from CF");
            ResponseEntity<String> response = execute(GET_ORG_GUID_URL, HttpMethod.GET, "", cfApiEndpoint, cfCallerConfiguration.getGearpumpUiOrg());
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
            ResponseEntity<String> response = execute(GET_SPACE_GUID_URL, HttpMethod.GET, "", cfApiEndpoint, uiOrgGuid, cfCallerConfiguration.getGearpumpUiSpace());
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
            ResponseEntity<String> response = execute(GET_SERVICE_GUID_URL, HttpMethod.GET, "", cfApiEndpoint, uiSpaceGuid, cfCallerConfiguration.getGearpumpUiName());
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
            ResponseEntity<String> response = execute(GET_SERVICE_PLAN_GUID_URL, HttpMethod.GET, "", cfApiEndpoint, serviceGuid);
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

    private ResponseEntity<String> execute(String url, HttpMethod method, String body, Object... urlVariables) throws RestClientException {
        return this.executeWithHeaders(url, method, body, new HttpHeaders(), urlVariables);
    }

    private ResponseEntity<String> executeWithHeaders(String url, HttpMethod method, String body, HttpHeaders headers, Object... urlVariables)
            throws RestClientException {
        RestTemplate restTemplate = cfCaller.createRestTemplate();
        HttpEntity<String> request = cfCaller.createJsonRequest(body, headers);
        URI expanded = (new UriTemplate(url)).expand(urlVariables);
        LOGGER.info("Performing call: {}", expanded.toString());
        ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class, urlVariables);
        LOGGER.debug("Response status: {}", response.getStatusCode());
        return response;
    }

    private String createUIInstance(String uiInstanceName, String spaceId, String orgId, String uiServicePlanGuid, String username,
                                    String password, String gearpumpMaster) throws IOException {
        LOGGER.info("Creating Service Instance");
        String body = String.format(CREATE_SERVICE_BODY_TEMPLATE, uiInstanceName, spaceId, uiServicePlanGuid, uiInstanceName,
                username, password, gearpumpMaster, username, password, loginHost(), cfApiEndpoint, orgId);
        LOGGER.debug("Create app body: {}", body);
        ResponseEntity<String> response = execute(CREATE_SERVICE_INSTANCE_URL, HttpMethod.POST, body, cfApiEndpoint);
        String uiServiceInstanceGuid = cfCaller.getValueFromJson(response.getBody(), METADATA_GUID);
        LOGGER.debug("UI Service Instance Guid '{}'", uiServiceInstanceGuid);
        return uiServiceInstanceGuid;
    }

    private String getUIAppGuid(String uiAppName, String spaceId) throws IOException {
        LOGGER.info("Getting App guid");
        ResponseEntity<String> response = execute(GET_APP_GUID_URL, HttpMethod.GET, "", cfApiEndpoint, spaceId, uiAppName);
        String uiAppGuid = cfCaller.getValueFromJson(response.getBody(), RESOURCES_0_METADATA_GUID);
        LOGGER.debug("App guid is: {}", uiAppGuid);
        return uiAppGuid;
    }

    private void updateUIApp(String orgId, String username, String uiCallback, String uiAppGuid,
                                    String password, String gearpumpMaster) throws IOException {
        LOGGER.info("Updating App Environments Instance");
        String body = String.format(UPDATE_APP_ENV_BODY_TEMPLATE, username,
                password, gearpumpMaster, username, password, loginHost(), cfApiEndpoint, orgId, uiCallback);
        LOGGER.debug("Update app body: {}", body);
        execute(UPDATE_APP_URL, HttpMethod.PUT, body, cfApiEndpoint, uiAppGuid);
    }

    private void restartUIApp(String uiAppGuid) throws IOException {
        LOGGER.info("Stopping ui app");
        execute(UPDATE_APP_URL, HttpMethod.PUT, STATUS_STOPPED_BODY, cfApiEndpoint, uiAppGuid);
        LOGGER.info("Started ui app");
        execute(UPDATE_APP_URL, HttpMethod.PUT, STATUS_STARTED_BODY, cfApiEndpoint, uiAppGuid);
    }

    public String loginHost() {
        return loginApiEndpoint.replaceAll("/oauth/authorize", "");
    }

    private String getUIAppUrl(String uiServiceInstanceGuid) throws IOException {
        LOGGER.info("Getting UI App URL using create service key function");
        String body = String.format(CREATE_SERVICE_KEY_BODY_TEMPLATE, uiServiceInstanceGuid);
        ResponseEntity<String> response = execute(POST_CREATE_SERVICE_KEY_URL, HttpMethod.POST, body, cfApiEndpoint);
        String uiAppUrl = cfCaller.getValueFromJson(response.getBody(), APP_URL);
        String serviceKeyGuid = cfCaller.getValueFromJson(response.getBody(), METADATA_GUID);
        LOGGER.info("Deleting service key");
        execute(DELETE_SERVICE_KEY_URL, HttpMethod.DELETE, "", cfApiEndpoint, serviceKeyGuid);
        LOGGER.debug("UI App url '{}'", uiAppUrl);
        return uiAppUrl;
    }

    public void deleteUIServiceInstance(String uiServiceGuid) {
        try {
            execute(DELETE_SERVICE_URL, HttpMethod.DELETE, "", cfApiEndpoint, uiServiceGuid);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                LOGGER.warn("GearPump UI instance with GUID {} doesn't exist. Skipping.", uiServiceGuid);
            } else {
                LOGGER.debug("Cannot delete GearPump UI instance with GUID {} - rethrowing excepiton.", uiServiceGuid);
                throw e;
            }
        }
    }

    private String createUaaToken(String clientId, String clientSecret) throws DashboardServiceException {
        LOGGER.info("Creating new UAA token");

        String autorizationString = clientId + ":" + clientSecret;
        autorizationString = new String(Base64.getEncoder().encode(autorizationString.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, "Basic " + autorizationString);
        headers.add(CONTENT_TYPE_HEADER, "application/x-www-form-urlencoded");

        ResponseEntity<String> response = executeWithHeaders(CREATE_UAA_TOKEN_URL, HttpMethod.POST, CREATE_UAA_TOKEN_BODY_TEMPLATE, headers, uaaTokenApiEndpoint);
        String uaaToken;
        try {
             uaaToken = cfCaller.getValueFromJson(response.getBody(), UAA_TOKEN_TYPE)
                        + " " + cfCaller.getValueFromJson(response.getBody(), UAA_ACCESS_TOKEN);
        } catch (IOException e) {
            throw new DashboardServiceException("Cannot obtain UAA token.", e);
        }
        LOGGER.debug("UAA access token has been obtained.");
        return uaaToken;
    }

    private String createUaaClient(String clientId, String clientName, String clientSecret, String redirectUri, String token) throws DashboardServiceException {
        LOGGER.info("Creating new UAA client");
        String body = String.format(CREATE_UAA_CLIENT_BODY_TEMPLATE, clientId, clientName, clientSecret, "http://" + redirectUri + REDIRECT_URI_SUFIX);

        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, token);
        headers.add(CONTENT_TYPE_HEADER, "application/json");

        ResponseEntity<String> response = executeWithHeaders(CREATE_UAA_CLIENT_URL, HttpMethod.POST, body, headers, uaaApiEndpoint);
        LOGGER.debug("Created UAA client: {}", response.getBody());
        return response.getBody();
    }

    private String deleteUaaClient(String clientId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, token);
        headers.add(CONTENT_TYPE_HEADER, "application/json");

        try {
            LOGGER.debug("Deleting UAA client: {}", clientId);
            return executeWithHeaders(DELETE_UAA_CLIENT_URL, HttpMethod.DELETE, "", headers, uaaApiEndpoint, clientId).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                LOGGER.debug("Cannot delete UAA client: {}. It is not exists.", clientId);
            } else {
                LOGGER.debug("Cannot delete UAA client: {} Error: {}", clientId, e.getStatusText());
                throw e;
            }
        }
        return null;
    }

    public Map<String, String> deployUI(String uiInstanceName, String username, String password, String gearpumpMaster, String spaceId, String orgId)
            throws DashboardServiceException, CloudFoundryServiceException {
        String uiServiceInstanceGuid;
        String uiAppUrl;
        try {
            uiServiceInstanceGuid = createUIInstance(uiInstanceName, spaceId, orgId, uiServicePlanGuid, username, password, gearpumpMaster);
            uiAppUrl = getUIAppUrl(uiServiceInstanceGuid);
        } catch (IOException e) {
            throw new CloudFoundryServiceException("Cannot create UI instance.", e);
        }

        try {
            String uiAppGuid = getUIAppGuid(uiAppUrl.replaceAll("\\.(.*)", ""), spaceId);
            updateUIApp(orgId, username, uiAppUrl, uiAppGuid, password, gearpumpMaster);
            restartUIApp(uiAppGuid);
        } catch (IOException e) {
            throw new CloudFoundryServiceException("Cannot set environments and restart UI instance", e);
        }

        String uaaToken = createUaaToken(ssoAdminClientId, ssoAdminClientSecret);
        createUaaClient(username, username, password, uiAppUrl, uaaToken);

        Map<String, String> dashboardData = new HashMap<>();
        dashboardData.put("uiServiceInstanceGuid", uiServiceInstanceGuid);
        dashboardData.put("uiAppUrl", uiAppUrl);
        dashboardData.put("username", username);
        dashboardData.put("password", password);

        return dashboardData;
    }

    public String undeployUI(String dashboardUri, String clientId) throws DashboardServiceException {
        deleteUIServiceInstance(dashboardUri);

        String uaaToken = createUaaToken(ssoAdminClientId, ssoAdminClientSecret);
        return deleteUaaClient(clientId, uaaToken);
    }
}
