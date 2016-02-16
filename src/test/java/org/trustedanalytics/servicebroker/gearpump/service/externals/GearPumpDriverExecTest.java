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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.servicebroker.gearpump.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.ExternalProcessExecutor;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.HdfsUtils;
import org.trustedanalytics.servicebroker.gearpump.service.file.ResourceManagerService;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GearPumpDriverExecTest {

    private static final String APPLICATION_ID = "application_1451314095720_0012";
    private static final String DESTINATION_PATH = "/home/user/vcap/gearpump";
    private static final String HDFS_PATH = "/home/user/gearpump";
    private static final String HDFS_URI = "hdfs://nameservice1/org/intel/hdfsbroker/userspace/18159a56-de7a-4987-8fa1-4dd65416568e/user/gearpump";
    private static final String DESTINATION_FOLDER = "gearpump-0.7.4";
    private static final String MASTER_URL = "http://cdh-worker-0.node.trustedanalytics.consul:44965";
    private static final String COMMAND_OUTPUT = "";

    @Mock
    private GearPumpCredentialsParser gearPumpCredentialsParser;
    @Mock
    private GearPumpOutputReportReader gearPumpOutputReportReader;
    @Mock
    private ResourceManagerService resourceManagerService;
    @Mock
    private ExternalConfiguration externalConfiguration;
    @Mock
    private HdfsUtils hdfsUtils;
    @Mock
    private ExternalProcessExecutor externalProcessExecutor;

    @InjectMocks
    private GearPumpDriverExec gearPumpDriverExec;

    @Before
    public void init() throws IOException, ExternalProcessException {
        gearPumpDriverExec = new GearPumpDriverExec();
        MockitoAnnotations.initMocks(this);
        createMocks();
    }

    private void createMocks() throws IOException, ExternalProcessException {
        when(gearPumpCredentialsParser.getApplicationId(Mockito.anyString())).thenReturn(APPLICATION_ID);
        when(resourceManagerService.getRealPath(Mockito.anyString())).thenReturn(DESTINATION_PATH);

        when(externalConfiguration.getHdfsGearPumpPackPath()).thenReturn(HDFS_PATH);
        when(externalConfiguration.getGearPumpDestinationFolder()).thenReturn(DESTINATION_FOLDER);

        when(hdfsUtils.getHdfsUri()).thenReturn(HDFS_URI);

        when(gearPumpOutputReportReader.fromOutput(Mockito.anyString())).thenReturn(gearPumpOutputReportReader);
        when(gearPumpOutputReportReader.getMasterUrl()).thenReturn(MASTER_URL);

        when(externalProcessExecutor.runWithProcessBuilder(Mockito.<String[]>any(), Mockito.anyString(), Mockito.anyString())).thenReturn(COMMAND_OUTPUT);
    }

    @Test
    public void spawnGearPumpOnYarnSuccess() throws IOException, ExternalProcessException {
        GearPumpCredentials credentials = gearPumpDriverExec.spawnGearPumpOnYarn("");
        assertThat(credentials.getYarnApplicationId(), equalTo(APPLICATION_ID));
        assertThat(credentials.getMasters(), equalTo(MASTER_URL));
    }

    @Test(expected = ExternalProcessException.class)
    public void spawnGearPumpOnYarnThrowsExternalProcessExceptionWhenMasterIsNull() throws IOException, ExternalProcessException {
        when(gearPumpOutputReportReader.getMasterUrl()).thenReturn(null);
        gearPumpDriverExec.spawnGearPumpOnYarn(null);
    }

    @Test(expected = ExternalProcessException.class)
    public void spawnGearPumpOnYarnThrowsExternalProcessExceptionWhenApplicationIdIsNull() throws IOException, ExternalProcessException {
        when(gearPumpCredentialsParser.getApplicationId(Mockito.anyString())).thenReturn(null);
        gearPumpDriverExec.spawnGearPumpOnYarn(null);
    }
}
