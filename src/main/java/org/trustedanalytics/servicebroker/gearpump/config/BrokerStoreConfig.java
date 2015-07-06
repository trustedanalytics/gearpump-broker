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

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.serialization.RepositoryDeserializer;
import org.trustedanalytics.cfbroker.store.serialization.RepositorySerializer;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClientBuilder;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperStore;
import org.trustedanalytics.hadoop.config.ConfigurationHelper;
import org.trustedanalytics.hadoop.config.ConfigurationHelperImpl;
import org.trustedanalytics.hadoop.config.PropertyLocator;
import org.trustedanalytics.servicebroker.gearpump.kerberos.KerberosProperties;

import java.io.IOException;

@Configuration
public class BrokerStoreConfig {

    @Autowired
    private KerberosProperties kerberosProperties;

    private ConfigurationHelper confHelper;

    private FactoryHelper helper;

    @Autowired
    @Qualifier(Qualifiers.SERVICE_INSTANCE)
    private RepositorySerializer<ServiceInstance> instanceSerializer;

    @Autowired
    @Qualifier(Qualifiers.SERVICE_INSTANCE)
    private RepositoryDeserializer<ServiceInstance> instanceDeserializer;

    @Autowired
    @Qualifier(Qualifiers.SERVICE_INSTANCE_BINDING)
    private RepositorySerializer<CreateServiceInstanceBindingRequest> bindingSerializer;

    @Autowired
    @Qualifier(Qualifiers.SERVICE_INSTANCE_BINDING)
    private RepositoryDeserializer<CreateServiceInstanceBindingRequest> bindingDeserializer;

    public BrokerStoreConfig() {
        this.helper = new FactoryHelper();
        this.confHelper = ConfigurationHelperImpl.getInstance();
    }

    BrokerStoreConfig(FactoryHelper helper) {
        this.helper = helper;
        this.confHelper = ConfigurationHelperImpl.getInstance();
    }

    @Bean
    @Qualifier(Qualifiers.SERVICE_INSTANCE)
    public BrokerStore<ServiceInstance> getServiceInstanceStore(ZookeeperClient zkClient) throws IOException {
        return new ZookeeperStore<ServiceInstance>(zkClient, instanceSerializer, instanceDeserializer);
    }

    @Bean
    @Qualifier(Qualifiers.SERVICE_INSTANCE_BINDING)
    public BrokerStore<CreateServiceInstanceBindingRequest> getServiceInstanceBindingStore(ZookeeperClient zkClient) throws IOException {
        return new ZookeeperStore<CreateServiceInstanceBindingRequest>(zkClient, bindingSerializer, bindingDeserializer);
    }

    @Bean
    @Profile("cloud")
    public ZookeeperClient getZKClient() throws IOException {
        ZookeeperClient zkClient = helper.getZkClientInstance(
            getPropertyFromCredentials(PropertyLocator.ZOOKEPER_URI),
            kerberosProperties.getUser(),
            kerberosProperties.getPassword(),
            getPropertyFromCredentials(PropertyLocator.ZOOKEPER_ZNODE));
        zkClient.init();
        return zkClient;
    }

    private String getPropertyFromCredentials(PropertyLocator property) throws IOException {
        return confHelper.getPropertyFromEnv(property)
            .orElseThrow(
                () -> new IllegalStateException(property.name() + " not found in VCAP_SERVICES"));
    }

    static final class FactoryHelper {
        ZookeeperClient getZkClientInstance(String zkCluster, String user, String pass, String zkNode) throws IOException {
            return new ZookeeperClientBuilder(zkCluster, user, pass, zkNode).build();
        }
    }
}
