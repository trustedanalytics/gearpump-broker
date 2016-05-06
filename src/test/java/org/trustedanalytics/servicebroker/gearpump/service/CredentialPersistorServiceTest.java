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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CredentialPersistorServiceTest {

    private CredentialPersistorService credentialPersistorService;

    @Mock
    private ZookeeperClient zookeeperClient;

    @Before
    public void init() {
        this.credentialPersistorService = new CredentialPersistorService(zookeeperClient);
    }

    @Test
    public void testToJSONString() throws Exception {
        GearPumpCredentials gpc = new GearPumpCredentials("masters", "yarnApplicationId", "dashboardUrl", "dashboardGuid", "username", "password", "uaaClientName");
        String expected = "{\"masters\":\"masters\",\"yarnApplicationId\":\"yarnApplicationId\",\"dashboardUrl\":\"dashboardUrl\",\"dashboardGuid\":\"dashboardGuid\",\"username\":\"username\",\"password\":\"password\",\"uaaClientName\":\"uaaClientName\"}";
        String result = credentialPersistorService.toJSONString(gpc.toMap());
        assertThat(result, equalTo(expected));
    }

    @Test
    public void testFromJSONString() throws Exception {
        String source = "{\"masters\":\"masters\",\"yarnApplicationId\":\"yarnApplicationId\",\"dashboardUrl\":\"dashboardUrl\",\"dashboardGuid\":\"dashboardGuid\",\"username\":\"username\",\"password\":\"password\",\"uaaClientName\":\"uaaClientName\"}";
        String result = credentialPersistorService.toJSONString(CredentialPersistorService.fromJSONString(source).toMap());
        assertThat(result, equalTo(source));
    }
}