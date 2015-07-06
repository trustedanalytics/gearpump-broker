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

package org.trustedanalytics.servicebroker.gearpump.service.externals;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GearPumpCredentialsParserTest extends TestCase {
    private static final String EXAMPLE_OUTPUT =
            "[ec2-user@cdh-master-0 gearpump]$ output/target/pack/bin/yarnclient -version gearpump-pack-2.11.5-0.7.1-SNAPSHOT -config conf/yarn.conf  -verbose\n"+
            "[WARN] [12/11/2015 04:54:07.294] [NativeCodeLoader] Unable to load native-hadoop library for your platform... usingbuiltin-java classes where applicable\n"+
            "[WARN] [12/11/2015 04:54:07.384] [DomainSocketFactory] The short-circuit local reads feature cannot be used because libhadoop cannot be loaded.\n"+
            "[INFO] [12/11/2015 04:54:07.399] [Client] Starting AM \n"+
            "[INFO] [12/11/2015 04:54:07.868] [Client] conf/yarn.conf uploaded to /user/gearpump/conf/gearpump_on_yarn.conf\n"+
            "[INFO] [12/11/2015 04:54:08.076] [Client] command=$JAVA_HOME/bin/java  -cp pack/gearpump-pack-2.11.5-0.7.1-SNAPSHOT/conf:pack/gearpump-pack-2.11.5-0.7.1-SNAPSHOT/dashboard:pack/gearpump-pack-2.11.5-0.7.1-SNAPSHOT/lib/*:pack/gearpump-pack-2.11.5-0.7.1-SNAPSHOT/lib/daemon/*:pack/gearpump-pack-2.11.5-0.7.1-SNAPSHOT/lib/services/*:pack/gearpump-pack-2.11.5-0.7.1-SNAPSHOT/lib/yarn/*:yarnConf:$CLASSPATH -Dgearpump.home={{LOCAL_DIRS}}/{{CONTAINER_ID}}/pack io.gearpump.experiments.yarn.master.YarnApplicationMaster -version gearpump-pack-2.11.5-0.7.1-SNAPSHOT 1><LOG_DIR>/stdout 2><LOG_DIR>/stderr\n"+
            "[INFO] [12/11/2015 04:54:08.107] [DFSClient] Created HDFS_DELEGATION_TOKEN token 495 for cf on ha-hdfs:nameservice1\n"+
            "[INFO] [12/11/2015 04:54:08.408] [TokenCache] Got dt for hdfs://nameservice1; Kind: HDFS_DELEGATION_TOKEN, Service:ha-hdfs:nameservice1, Ident: (HDFS_DELEGATION_TOKEN token 495 for cf)\n"+
            "[WARN] [12/11/2015 04:54:08.409] [Token] Cannot find class for token kind kms-dt\n"+
            "[INFO] [12/11/2015 04:54:08.409] [TokenCache] Got dt for hdfs://nameservice1; Kind: kms-dt, Service: 10.10.10.215:16000, Ident: 00 02 63 66 04 79 61 72 6e 00 8a 01 51 90 76 42 b8 8a 01 51 b4 82 c6 b8 05 03\n"+
            "[INFO] [12/11/2015 04:54:08.667] [YarnClientImpl] Submitted application application_1449655657443_0004\n"+
            "[INFO] [12/11/2015 04:54:22.688] [Client] Application application_1449655657443_0004 is RUNNING trackingURL=http://cdh-master-0.node.gotapaaseu.consul:8088/proxy/application_1449655657443_0004/\n"+
            "[INFO] [12/11/2015 04:54:24.214] [Client] trackingURL=http://cdh-master-0.node.gotapaaseu.consul:8088/proxy/application_1449655657443_0004/\n"+
            "[INFO] [12/11/2015 04:54:29.237] [Client] host=cdh-master-0.node.gotapaaseu.consul port=8088 uri=/proxy/application_1449655657443_0004/api/v1.0/master\n"+
            "[INFO] [12/11/2015 04:54:30.861] [Client] status code=200\n"+
            "[INFO] [12/11/2015 04:54:30.957] [Client] leader=master@cdh-worker-0.node.gotapaaseu.consul:3000\n"+
            "[INFO] [12/11/2015 04:54:30.958] [Client] masters=cdh-worker-0.node.gotapaaseu.consul:3000";

    private static final String EXPECTED_MASTER = "cdh-worker-0.node.gotapaaseu.consul:3000";
    private static final String EXPECTED_APPLICATION_ID = "application_1449655657443_0004";


    private GearPumpCredentialsParser gearPumpCredentialsParser;

    @Before
    public void init() {
        gearPumpCredentialsParser = new GearPumpCredentialsParser();
    }

    @Test
    public void testExtractMaster() throws Exception {
        String masterUrl = gearPumpCredentialsParser.getMasterUrl(EXAMPLE_OUTPUT);
        assertThat(masterUrl, equalTo(EXPECTED_MASTER));
    }

    @Test
    public void testExtractApplicationId() throws Exception {
        String applicationId = gearPumpCredentialsParser.getApplicationId(EXAMPLE_OUTPUT);
        assertThat(applicationId, equalTo(EXPECTED_APPLICATION_ID));
    }
}