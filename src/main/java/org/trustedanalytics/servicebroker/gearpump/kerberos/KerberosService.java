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
package org.trustedanalytics.servicebroker.gearpump.kerberos;

import org.apache.hadoop.conf.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.trustedanalytics.hadoop.config.client.AppConfiguration;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;
import org.trustedanalytics.hadoop.config.client.ServiceType;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.ExternalProcessEnvBuilder;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class KerberosService {

    private static final String AUTHENTICATION_METHOD = "kerberos";
    private static final String AUTHENTICATION_METHOD_PROPERTY = "hadoop.security.authentication";

    private final KerberosProperties kerberosProperties;
    private final AppConfiguration helper;
    private final ServiceInstanceConfiguration hdfsConf;

    @Autowired
    public KerberosService(KerberosProperties kerberosProperties) throws IOException {
        this.kerberosProperties = kerberosProperties;
        helper = Configurations.newInstanceFromEnv();
        hdfsConf = helper.getServiceConfig(ServiceType.HDFS_TYPE);
    }

    public org.apache.hadoop.conf.Configuration logIn() throws LoginException, IOException {
        if (isKerberosEnabled(hdfsConf.asHadoopConfiguration())) {
            KrbLoginManager loginManager = KrbLoginManagerFactory.getInstance()
                    .getKrbLoginManagerInstance(kerberosProperties.getKdc(), kerberosProperties.getRealm());
            loginManager.loginInHadoop(loginManager.loginWithCredentials(kerberosProperties.getUser(), kerberosProperties.getPassword().toCharArray()),
                    hdfsConf.asHadoopConfiguration());

            return hdfsConf.asHadoopConfiguration();
        } else {
            return helper.getServiceConfig(ServiceType.HDFS_TYPE).asHadoopConfiguration();
        }
    }

    public String getKerberosUser() {
        return kerberosProperties.getUser();
    }

    public String getKerberosJavaOpts() {
        return isKerberosEnabled() ? String.format("%s %s", buildKdcOption(), buildRealmOption()) : null;
    }

    public boolean isKerberosEnabled() {
        return isKerberosEnabled(hdfsConf.asHadoopConfiguration());
    }

    public static boolean isKerberosEnabled(Configuration hadoopConf) {
        return AUTHENTICATION_METHOD.equals(hadoopConf.get(AUTHENTICATION_METHOD_PROPERTY));
    }

    private String buildRealmOption() {
        return ExternalProcessEnvBuilder.buildJavaParam(KerberosProperties.KRB5_REALM_PROP, kerberosProperties.getRealm());
    }

    private String buildKdcOption() {
        return ExternalProcessEnvBuilder.buildJavaParam(KerberosProperties.KRB5_KDC_PROP, kerberosProperties.getKdc());
    }
}
