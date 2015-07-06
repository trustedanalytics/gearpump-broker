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

package org.trustedanalytics.servicebroker.gearpump.integration;

import org.junit.Ignore;
import org.trustedanalytics.servicebroker.gearpump.Application;
import org.trustedanalytics.servicebroker.gearpump.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.gearpump.service.ConfigurationTest;
import org.trustedanalytics.servicebroker.gearpump.service.GearPumpServiceInstanceBindingService;
import org.trustedanalytics.servicebroker.gearpump.service.GearPumpServiceInstanceService;
import org.cloudfoundry.community.servicebroker.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, ConfigurationTest.class})
@IntegrationTest
@ActiveProfiles("test")
public class GearpumpIntegrationTest {

    @Autowired
    private ExternalConfiguration conf;

    @Autowired
    private GearPumpServiceInstanceService isntanceService;

    @Autowired
    private GearPumpServiceInstanceBindingService bindingService;

    @Ignore("not yet implemented")
    @Test
    public void testCreateServiceInstance_success_shouldReturnCreatedInstance() throws Exception {
        CreateServiceInstanceRequest request = getCreateInstanceRequest("instanceId");
        isntanceService.createServiceInstance(request);
        ServiceInstance instance = isntanceService.getServiceInstance(request.getServiceInstanceId());
        assertThat(instance.getServiceInstanceId(), equalTo("instanceId"));
    }

    @Ignore("not yet implemented")
    @Test
    public void testDeleteServiceInstance_success_shouldReturnRemovedInstance() throws Exception {
        ServiceInstance instance = isntanceService.createServiceInstance(getCreateInstanceRequest("instanceId3"));
        ServiceInstance removedInstance = isntanceService.deleteServiceInstance(new DeleteServiceInstanceRequest(
                instance.getServiceInstanceId(),
                instance.getServiceDefinitionId(),
                instance.getPlanId()));
        assertThat(instance.getServiceInstanceId(), equalTo(removedInstance.getServiceInstanceId()));
    }

    @Ignore("not yet implemented")
    @Test
    public void testCreateInstanceBinding_success_shouldReturnBinding() throws Exception {
        CreateServiceInstanceBindingRequest request = new CreateServiceInstanceBindingRequest(
                getServiceInstance("serviceId4").getServiceDefinitionId(), "planId", "appGuid").
                withBindingId("bindingId").withServiceInstanceId("serviceId");
        ServiceInstanceBinding binding = bindingService.createServiceInstanceBinding(request);
        assertThat(binding.getServiceInstanceId(), equalTo("serviceId"));
    }

    private ServiceInstance getServiceInstance(String id) {
        return new ServiceInstance(new CreateServiceInstanceRequest(id, "planId", "organizationGuid", "spaceGuid"));
    }

    private CreateServiceInstanceRequest getCreateInstanceRequest(String serviceId) {
        return new CreateServiceInstanceRequest("serviceDefinitionId", "planId", "organizationGuid","spaceGuid").
                withServiceInstanceId(serviceId);
    }
}
