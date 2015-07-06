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

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.trustedanalytics.servicebroker.gearpump.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.gearpump.config.KerberosConfig;
import org.trustedanalytics.servicebroker.gearpump.kerberos.KerberosService;
import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;
import org.trustedanalytics.servicebroker.gearpump.service.externals.ExternalProcessException;
import org.trustedanalytics.servicebroker.gearpump.service.externals.GearPumpDriverExec;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.RandomStringGenerator;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Map;

public class GearPumpSpawner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GearPumpSpawner.class);

    private final GearPumpDriverExec gearPumpDriver;

    private final ApplicationBrokerService applicationBrokerService;

    //TODO can be Autowired in future
    private final KerberosService kerberosService;

    public GearPumpSpawner(GearPumpDriverExec gearPumpDriver, ApplicationBrokerService applicationBrokerService) throws IOException, LoginException {
        this.gearPumpDriver = gearPumpDriver;
        this.applicationBrokerService = applicationBrokerService;
        kerberosService = new KerberosService(new KerberosConfig().getKerberosProperties());
    }

    private GearPumpCredentials provisionOnYarn() throws IOException, ExternalProcessException {
        return gearPumpDriver.spawnGearPumpOnYarn();
    }

    private void provisionOnCf(GearPumpCredentials gearPumpCredentials, String spaceId, String serviceInstanceId) {
        LOGGER.info("provisioning on Cloud Foundry");

        String UIServiceInstanceName = "gearpump-ui-" + serviceInstanceId;
        String username = RandomStringGenerator.generate();
        String password = RandomStringGenerator.generate();

        Map<String, String> dashboardData = applicationBrokerService.deployUI(UIServiceInstanceName, username, password, gearPumpCredentials.getMasters(), spaceId);

        gearPumpCredentials.setUsername(username);
        gearPumpCredentials.setPassword(password);
        gearPumpCredentials.setPort("80");
        gearPumpCredentials.setDashboardUrl(dashboardData.get("uiAppUrl"));
        gearPumpCredentials.setHostname(dashboardData.get("uiAppUrl"));
        gearPumpCredentials.setDashboardGuid(dashboardData.get("uiServiceInstanceGuid"));
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
    
    private String errorMsg(String serviceInstanceId) {
        return "Unable to provision gearPump for: " + serviceInstanceId;
    }
}
