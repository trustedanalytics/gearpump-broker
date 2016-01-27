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

package org.trustedanalytics.servicebroker.gearpump.yarn;

import org.apache.hadoop.conf.Configuration;
import org.springframework.context.annotation.Bean;
import org.trustedanalytics.hadoop.config.client.*;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;
import org.trustedanalytics.servicebroker.gearpump.kerberos.KerberosService;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@org.springframework.context.annotation.Configuration
public class YarnConfig {

    @Bean
    public Configuration yarnConfiguration() throws IOException, LoginException {
        AppConfiguration helper = Configurations.newInstanceFromEnv();
        ServiceInstanceConfiguration yarnConf = helper.getServiceConfig(ServiceType.YARN_TYPE);
        ServiceInstanceConfiguration krbConf = helper.getServiceConfig("kerberos-service");

        Configuration hadoopConf = yarnConf.asHadoopConfiguration() ;

        if(KerberosService.isKerberosEnabled(hadoopConf)) {
            String kdc = getKrbServiceProperty(krbConf, Property.KRB_KDC);
            String realm = getKrbServiceProperty(krbConf, Property.KRB_REALM);
            KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(kdc, realm);
        }

        return hadoopConf;
    }

    String getKrbServiceProperty(ServiceInstanceConfiguration krbServiceConfiguration, Property property) {
        return krbServiceConfiguration.getProperty(property)
                .orElseThrow(() -> new IllegalStateException(property.name()
                        + " not found in configuration!"));
    }

}