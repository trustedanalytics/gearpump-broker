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
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class YarnAppManagerTest {

    private static final String incorrect_application_id = "application_1449093574559_0004_aa";
    private static final String correct_application_id = "application_1449093574559_0004";

    @Mock
    private org.apache.hadoop.conf.Configuration yarnConfiguration;

    @Mock
    private YarnClient yarnClient;

    @Mock
    private YarnClientFactory yarnClientFactory;

    @InjectMocks
    private YarnAppManager yarnAppManager;

    @Before
    public void init() throws IOException, YarnException {
        yarnAppManager = new YarnAppManager();
        MockitoAnnotations.initMocks(this);
        mockYarnClient();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKillApplicationThrowsExceptionWithIncorrectApplicationId() throws IOException, YarnException {
        yarnAppManager.killApplication(incorrect_application_id);
    }

    @Test(expected = YarnException.class)
    public void testKillApplicationThrowsYarnExceptionWhenIOExceptionAppears() throws IOException, YarnException {
        doThrow(IOException.class).when(yarnClient).killApplication(Mockito.<ApplicationId>any());
        yarnAppManager.killApplication(correct_application_id);
    }

    private void mockYarnClient() throws IOException, YarnException {
        doNothing().when(yarnClient).start();
        doNothing().when(yarnClient).init(Mockito.<Configuration>any());
        doNothing().when(yarnClient).killApplication(Mockito.<ApplicationId>any());
        when(yarnClientFactory.getYarnClient()).thenReturn(yarnClient);
    }
}
