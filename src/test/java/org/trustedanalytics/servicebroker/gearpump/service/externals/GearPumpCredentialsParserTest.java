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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GearPumpCredentialsParserTest extends TestCase {
    private static final String EXAMPLE_OUTPUT /*_0_7_6 */ =
            "OUT 2016-03-09 12:58:14.521  INFO 29 --- [qtp859865199-18] o.t.s.g.s.e.h.ExternalProcessExecutor    : Output: 16/03/09 12:57:58 INFO ClusterConfig$: loading config file application.conf...\n"+
            "OUT 16/03/09 12:57:59 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable\n"+
            "OUT 16/03/09 12:57:59 INFO YarnClient: Starting YarnClient...\n"+
            "OUT 16/03/09 12:58:00 INFO slf4j.Slf4jLogger: Slf4jLogger started\n"+
            "OUT 16/03/09 12:58:00 INFO Remoting: Starting remoting\n"+
            "OUT 16/03/09 12:58:00 INFO Remoting: Remoting started; listening on addresses :[akka.tcp://launchCluster@127.0.0.1:55569]\n"+
            "OUT 16/03/09 12:58:00 INFO Metrics$: Metrics is enabled...,  true\n"+
            "OUT 16/03/09 12:58:00 INFO LaunchCluster: Starting AM\n"+
            "OUT 16/03/09 12:58:00 INFO client.ConfiguredRMFailoverProxyProvider: Failing over to rm42\n"+
            "OUT 16/03/09 12:58:00 INFO YarnClient: Create application, appId: application_1456149538698_0014\n"+
            "OUT 16/03/09 12:58:00 INFO LaunchCluster: Uploading configuration files to remote HDFS(under hdfs://nameservice1/user/cf/.gearpump_application_1456149538698_0014/conf/)...\n"+
            "OUT 16/03/09 12:58:01 INFO YarnClient: Submit Application application_1456149538698_0014 to YARN...\n"+
            "OUT 16/03/09 12:58:01 INFO impl.YarnClientImpl: Submitted application application_1456149538698_0014\n"+
            "OUT 16/03/09 12:58:01 INFO LaunchCluster: Waiting application to finish...\n"+
            "OUT ......\n"+
            "OUT 16/03/09 12:58:07 INFO LaunchCluster: Application application_1456149538698_0014 finished with state RUNNING at 0, info:\n"+
            "OUT ================================================\n"+
            "OUT ==Application Id: application_1456149538698_0014\n"+
            "OUT 16/03/09 12:58:07 INFO LaunchCluster: Trying to download active configuration to output path: /home/vcap/app/gearpump-2.11-0.7.6/output-1457528277493-3821.conf\n"+
            "OUT 16/03/09 12:58:07 INFO LaunchCluster: Resolving YarnAppMaster ActorRef for application application_1456149538698_0014\n"+
            "OUT 16/03/09 12:58:10 INFO AppMasterResolver: appMasterPath=http://cdh-worker-0.node.trustedanalytics.consul:45660/supervisor-actor-path\n"+
            "OUT 16/03/09 12:58:10 INFO httpclient.HttpMethodDirector: I/O exception (java.net.ConnectException) caught when processing request: Connection refused\n"+
            "OUT 16/03/09 12:58:10 INFO httpclient.HttpMethodDirector: Retrying request\n"+
            "OUT 16/03/09 12:58:10 INFO httpclient.HttpMethodDirector: I/O exception (java.net.ConnectException) caught when processing request: Connection refused\n"+
            "OUT 16/03/09 12:58:10 INFO httpclient.HttpMethodDirector: Retrying request\n"+
            "OUT 16/03/09 12:58:10 INFO httpclient.HttpMethodDirector: I/O exception (java.net.ConnectException) caught when processing request: Connection refused\n"+
            "OUT 16/03/09 12:58:10 INFO httpclient.HttpMethodDirector: Retrying request\n"+
            "OUT Failed to connect YarnAppMaster(tried 1)... Connection refused\n"+
            "OUT 16/03/09 12:58:13 INFO AppMasterResolver: appMasterPath=http://cdh-worker-0.node.trustedanalytics.consul:45660/supervisor-actor-path\n"+
            "OUT 16/03/09 12:58:13 INFO AppMasterResolver: Successfully resolved AppMaster address: akka.tcp://GearpumpAM@cdh-worker-0.node.trustedanalytics.consul:59723/user/$a\n"+
            "OUT 16/03/09 12:58:13 INFO LaunchCluster: appMaster=akka.tcp://GearpumpAM@cdh-worker-0.node.trustedanalytics.consul:59723/user/$a host=19cdn9430kr\n"+
            "OUT 16/03/09 12:58:14 INFO remote.RemoteActorRefProvider$RemotingTerminator: Shutting down remote daemon.\n"+
            "OUT 16/03/09 12:58:14 INFO remote.RemoteActorRefProvider$RemotingTerminator: Remote daemon shut down; proceeding with flushing remote transports.\n"+
            "OUT 16/03/09 12:58:14 INFO remote.RemoteActorRefProvider$RemotingTerminator: Remoting shut down.";

    private static final String EXPECTED_APPLICATION_ID = "application_1456149538698_0014";

    private GearPumpCredentialsParser gearPumpCredentialsParser;

    @Before
    public void init() {
        gearPumpCredentialsParser = new GearPumpCredentialsParser();
    }

    @Test
    public void testExtractApplicationId() throws Exception {
        String applicationId = gearPumpCredentialsParser.getApplicationId(EXAMPLE_OUTPUT);
        assertThat(applicationId, equalTo(EXPECTED_APPLICATION_ID));
    }

    @Test
    public void testExtractRunningApplicationId() throws Exception {
        String applicationId = gearPumpCredentialsParser.getRunningApplicationId(EXAMPLE_OUTPUT);
        assertThat(applicationId, equalTo(EXPECTED_APPLICATION_ID));
    }
}