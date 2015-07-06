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
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceBindingServiceStore;
import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;

import java.io.IOException;
import java.util.Map;

public class GearPumpServiceInstanceBindingService extends ForwardingServiceInstanceBindingServiceStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(GearPumpServiceInstanceBindingService.class);

    private CredentialPersistorService credentialPersistorService;

    public GearPumpServiceInstanceBindingService(ServiceInstanceBindingService instanceBindingService, CredentialPersistorService credentialPersistorService) {
        super(instanceBindingService);
        this.credentialPersistorService = credentialPersistorService;
    }

    @Override
    public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
            throws ServiceInstanceBindingExistsException, ServiceBrokerException {
        return withCredentials(super.createServiceInstanceBinding(request));
    }

    private ServiceInstanceBinding withCredentials(ServiceInstanceBinding serviceInstanceBinding) {
        return new ServiceInstanceBinding(serviceInstanceBinding.getId(),
                serviceInstanceBinding.getServiceInstanceId(),
                getCredentialsFor(serviceInstanceBinding.getServiceInstanceId()),
                serviceInstanceBinding.getSyslogDrainUrl(),
                serviceInstanceBinding.getAppGuid());
    }

    private Map<String, Object> getCredentialsFor(String serviceInstanceId) {
        LOGGER.info("getCredentialsFor serviceInstanceId = [" + serviceInstanceId + "]");
        GearPumpCredentials gearpumpCredentials = null;
        try {
            gearpumpCredentials = credentialPersistorService.readCredentials(serviceInstanceId);
            return gearpumpCredentials.toMap();
        } catch (IOException e) {
            LOGGER.error("Couldn't obtain credentials", e);
        }
        return null;
    }
}
