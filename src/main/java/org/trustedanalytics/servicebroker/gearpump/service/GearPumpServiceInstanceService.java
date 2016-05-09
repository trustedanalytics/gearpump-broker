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

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.DeleteServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceServiceStore;
import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;

import java.io.IOException;

public class GearPumpServiceInstanceService extends ForwardingServiceInstanceServiceStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(GearPumpServiceInstanceService.class);

    @Autowired
    private GearPumpSpawner gearPumpSpawner;
    @Autowired
    private CredentialPersistorService credentialPersistorService;

    public GearPumpServiceInstanceService(ServiceInstanceService delegate) {
        super(delegate);
    }

    @Override
    public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request) throws ServiceInstanceExistsException, ServiceBrokerException {
        LOGGER.info("Spawning GearPump instance {}", request);
        ServiceInstance instance =  super.createServiceInstance(request);
        String instanceId = instance.getServiceInstanceId();
        String spaceId = request.getSpaceGuid();
        String orgId = request.getOrganizationGuid();
        String planId = request.getPlanId();
        LOGGER.debug("GearPump service instance guid: {}; Space id: {}; Org id: {}; Plan id: {}", instanceId, spaceId, orgId, planId);

        GearPumpCredentials gearPumpCredentials;

        try {
            gearPumpCredentials = gearPumpSpawner.provisionInstance(instanceId, spaceId, orgId, planId);
            LOGGER.info("GearPump instance has been spawned");
        } catch (Exception e) {
            LOGGER.error("Couldn't spawn GearPump instance", e);
            throw prepareSBException("Couldn't spawn GearPump instance", e);
        }

        if (gearPumpCredentials == null) {
            LOGGER.error("Couldn't spawn GearPump instance");
            throw new ServiceBrokerException("Couldn't spawn GearPump instance (null credentials).");
        }

        try {
            credentialPersistorService.persistCredentials(instanceId, gearPumpCredentials.toMap());
            LOGGER.info("Persisted GearPump credentials {}", gearPumpCredentials);
        } catch (IOException e) {
            LOGGER.error("Couldn't persist credentials", e);
            throw prepareSBException("Couldn't persist credentials", e);
        }
        return instance;
    }

    @Override
    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException {
        LOGGER.info("Deleting GearPump service instance with guid: {}", request.getServiceInstanceId());

        GearPumpCredentials gearpumpCredentials;

        try {
            gearpumpCredentials = credentialPersistorService.readCredentials(request.getServiceInstanceId());
        } catch (IOException e) {
            LOGGER.error("Couldn't obtain credentials.", e);
            throw prepareSBException("Couldn't obtain credentials", e);
        }

        LOGGER.debug("Obtained Apache Gearpump credentials: {}", gearpumpCredentials);
        try {
            gearPumpSpawner.deprovisionInstance(gearpumpCredentials);
            credentialPersistorService.removeCredentials(request.getServiceInstanceId());
        } catch (Exception e) {
            LOGGER.error("Couldn't delete Apache Gearpump instance", e);
            throw prepareSBException("Couldn't delete Apache Gearpump instance", e);
        }

        return super.deleteServiceInstance(request);
    }

    private ServiceBrokerException prepareSBException(String message, Exception e) {
        return new ServiceBrokerException(String.format("%s (%s)", message, e.getMessage()), e);
    }
}
