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

package org.trustedanalytics.servicebroker.gearpump.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.servicebroker.gearpump.service.ApplicationBrokerService;
import org.trustedanalytics.servicebroker.gearpump.service.CredentialPersistorService;
import org.trustedanalytics.servicebroker.gearpump.service.GearPumpSpawner;
import org.trustedanalytics.servicebroker.gearpump.service.externals.GearPumpCredentialsParser;
import org.trustedanalytics.servicebroker.gearpump.service.externals.GearPumpDriverExec;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@Configuration
public class GearPumpSpawnerConfig {

    @Bean
    public GearPumpSpawner getGearPumpSpawner(GearPumpDriverExec gearPumpDriver,
                                              ApplicationBrokerService applicationBrokerService) throws IOException, LoginException {
        return new GearPumpSpawner(gearPumpDriver, applicationBrokerService);
    }

    @Bean
    public GearPumpDriverExec gearPumpDriverExec() {
        return new GearPumpDriverExec();
    }

    @Bean
    public GearPumpCredentialsParser gearPumpCredentialsParser() {
        return new GearPumpCredentialsParser();
    }

    @Bean
    public CredentialPersistorService credentialPersistorService(ZookeeperClient getZKClient) {
        return new CredentialPersistorService(getZKClient);
    }

}
