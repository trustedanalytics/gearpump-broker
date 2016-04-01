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

import com.google.common.base.Strings;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.trustedanalytics.servicebroker.gearpump.config.CatalogConfig;
import org.trustedanalytics.servicebroker.gearpump.config.KerberosConfig;
import org.trustedanalytics.servicebroker.gearpump.kerberos.KerberosService;
import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;
import org.trustedanalytics.servicebroker.gearpump.service.externals.ExternalProcessException;
import org.trustedanalytics.servicebroker.gearpump.service.externals.GearPumpDriverExec;
import org.trustedanalytics.servicebroker.gearpump.yarn.YarnAppManager;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GearPumpSpawner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GearPumpSpawner.class);

    private final GearPumpDriverExec gearPumpDriver;
    private final CloudFoundryService cloudFoundryService;
    private final YarnAppManager yarnAppManager;

    //TODO can be Autowired in future
    private final KerberosService kerberosService;

    @Autowired
    private CatalogConfig configuration;

    public GearPumpSpawner(GearPumpDriverExec gearPumpDriver, CloudFoundryService cloudFoundryService,
                           YarnAppManager yarnAppManager) throws IOException, LoginException {
        this.gearPumpDriver = gearPumpDriver;
        this.cloudFoundryService = cloudFoundryService;
        this.yarnAppManager = yarnAppManager;
        kerberosService = new KerberosService(new KerberosConfig().getKerberosProperties());
    }

    private GearPumpCredentials provisionOnYarn(String numberOfWorkers) throws IOException, ExternalProcessException {
        HashMap<String, String> arguments = new HashMap<>();
        arguments.put("numberOfWorkers", numberOfWorkers);
        return gearPumpDriver.spawnGearPumpOnYarn(arguments);
    }

    private void provisionOnCf(GearPumpCredentials gearPumpCredentials, String spaceId, String orgId, String serviceInstanceId)
            throws DashboardServiceException, CloudFoundryServiceException {
        LOGGER.info("Pcd ge rovisioning on Cloud Foundry");

        String UIServiceInstanceName = "gearpump-ui-" + serviceInstanceId;
        String username = RandomStringUtils.randomAlphanumeric(10).toLowerCase();
        String password = RandomStringUtils.randomAlphanumeric(10).toLowerCase();

        Map<String, String> dashboardData
                = cloudFoundryService.deployUI(UIServiceInstanceName, username, password, gearPumpCredentials.getMasters(), spaceId, orgId);

        updateCredentials(gearPumpCredentials, dashboardData);
    }

    private void updateCredentials(GearPumpCredentials gearPumpCredentials, Map<String, String> dashboardData) {
        gearPumpCredentials.setDashboardUrl(dashboardData.get("uiAppUrl"));
        gearPumpCredentials.setDashboardGuid(dashboardData.get("uiServiceInstanceGuid"));
        gearPumpCredentials.setUsername(dashboardData.get("username"));
        gearPumpCredentials.setPassword(dashboardData.get("password"));
    }

    private void cleanUp(GearPumpCredentials gearPumpCredentials) {
        LOGGER.info("cleanUp [" + gearPumpCredentials + "]");
        if ( !(gearPumpCredentials != null && Strings.isNullOrEmpty(gearPumpCredentials.getYarnApplicationId()))) {
            LOGGER.debug("Found yarnApplicationId {}", gearPumpCredentials.getYarnApplicationId());
            try {
                yarnAppManager.killApplication(gearPumpCredentials.getYarnApplicationId());
                LOGGER.debug("killApplication finished");
            } catch (YarnException e) {
                LOGGER.warn("YARN problem while cleaning up.", e);
            }
        }
    }

    public GearPumpCredentials provisionInstance(String serviceInstanceId, String spaceId, String orgId, String planId)
            throws LoginException, IOException, DashboardServiceException, CloudFoundryServiceException, ExternalProcessException {
        LOGGER.info("Trying to provision gearPump for: " + serviceInstanceId);
        kerberosService.logIn();

        GearPumpCredentials credentials = null;
        try {
            credentials = provisionOnYarn(configuration.getNumberOfWorkers(planId));
            provisionOnCf(credentials, spaceId, orgId, serviceInstanceId);
        } catch (Exception e) {
            cleanUp(credentials);
            throw e;
        }

        return credentials;
    }

    public void deprovisionInstance(GearPumpCredentials gearPumpCredentials) throws YarnException {
        yarnAppManager.killApplication(gearPumpCredentials.getYarnApplicationId());
        LOGGER.info("GearPump instance on Yarn has been deleted: {}", gearPumpCredentials.getYarnApplicationId());

        try {
            cloudFoundryService.undeployUI(gearPumpCredentials.getDashboardGuid(), gearPumpCredentials.getUsername());
        } catch (DashboardServiceException e) {
            LOGGER.info("GearPump Dashboard instance has not been deleted");
        }
    }

    private String errorMsg(String serviceInstanceId) {
        return "Unable to provision gearPump for: " + serviceInstanceId;
    }
}
