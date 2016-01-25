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

import org.apache.commons.lang.RandomStringUtils;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.servicebroker.gearpump.config.KerberosConfig;
import org.trustedanalytics.servicebroker.gearpump.kerberos.KerberosService;
import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;
import org.trustedanalytics.servicebroker.gearpump.service.externals.ExternalProcessException;
import org.trustedanalytics.servicebroker.gearpump.service.externals.GearPumpDriverExec;
import org.trustedanalytics.servicebroker.gearpump.yarn.YarnAppManager;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Map;

public class GearPumpSpawner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GearPumpSpawner.class);

    private final GearPumpDriverExec gearPumpDriver;
    private final ApplicationBrokerService applicationBrokerService;
    private YarnAppManager yarnAppManager;

    //TODO can be Autowired in future
    private final KerberosService kerberosService;

    public GearPumpSpawner(GearPumpDriverExec gearPumpDriver,
                           ApplicationBrokerService applicationBrokerService,
                           YarnAppManager yarnAppManager) throws IOException, LoginException {
        this.gearPumpDriver = gearPumpDriver;
        this.applicationBrokerService = applicationBrokerService;
        this.yarnAppManager = yarnAppManager;
        kerberosService = new KerberosService(new KerberosConfig().getKerberosProperties());
    }

    private GearPumpCredentials provisionOnYarn() throws IOException, ExternalProcessException {
        return gearPumpDriver.spawnGearPumpOnYarn();
    }

    private void provisionOnCf(GearPumpCredentials gearPumpCredentials, String spaceId, String serviceInstanceId) throws IOException {
        LOGGER.info("provisioning on Cloud Foundry");

        String UIServiceInstanceName = "gearpump-ui-" + serviceInstanceId;
        String username = RandomStringUtils.randomAlphanumeric(10).toLowerCase();
        String password = RandomStringUtils.randomAlphanumeric(10).toLowerCase();

        Map<String, String> dashboardData = applicationBrokerService.deployUI(UIServiceInstanceName, username, password, gearPumpCredentials.getMasters(), spaceId);

        updateCredentials(gearPumpCredentials, dashboardData);
    }

    private void updateCredentials(GearPumpCredentials gearPumpCredentials, Map<String, String> dashboardData) {
        gearPumpCredentials.setDashboardUrl(dashboardData.get("uiAppUrl"));
        gearPumpCredentials.setDashboardGuid(dashboardData.get("uiServiceInstanceGuid"));
        gearPumpCredentials.setUsername(dashboardData.get("username"));
        gearPumpCredentials.setPassword(dashboardData.get("password"));
    }

    public GearPumpCredentials provisionInstance(String serviceInstanceId, String spaceId) throws Exception {
        LOGGER.info("Trying to provision gearPump for: " + serviceInstanceId);
        try {
            kerberosService.logIn();

            GearPumpCredentials credentials = provisionOnYarn();
            provisionOnCf(credentials, spaceId, serviceInstanceId);

            return credentials;
        } catch (Exception e) {
            throw new Exception(errorMsg(serviceInstanceId), e);
        }
    }

    public void deprovisionInstance(GearPumpCredentials gearpumpCredentials) throws YarnException {
        yarnAppManager.killApplication(gearpumpCredentials.getYarnApplicationId());
        LOGGER.info("GearPump instance on Yarn has been deleted: {}", gearpumpCredentials.getYarnApplicationId());

        applicationBrokerService.deleteUIServiceInstance(gearpumpCredentials.getDashboardGuid());
    }

    private String errorMsg(String serviceInstanceId) {
        return "Unable to provision gearPump for: " + serviceInstanceId;
    }
}
