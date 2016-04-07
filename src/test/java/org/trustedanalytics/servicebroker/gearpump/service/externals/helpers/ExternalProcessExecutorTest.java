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

package org.trustedanalytics.servicebroker.gearpump.service.externals.helpers;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.servicebroker.gearpump.service.externals.ExternalProcessException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class ExternalProcessExecutorTest extends TestCase {
    private String system;

    @InjectMocks
    private ExternalProcessExecutor externalProcessExecutor;

    @Before
    public void init() throws IOException {
        this.system = System.getProperty("os.name");
        externalProcessExecutor = new ExternalProcessExecutor();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRunCommand() throws Exception {
        if (system.startsWith("Win")) {
            return;
        }

        String expectedOutput = "hello world";

        String[] cmd = {"echo", expectedOutput};
        String output = externalProcessExecutor.runCommand(cmd, null, null);

        assertThat(output.trim(), equalTo(expectedOutput.trim()));
    }

    @Test(expected=ExternalProcessException.class)
    public void testRunCommandShouldFail() throws Exception {
        if (system.startsWith("Win")) {
            return;
        }
        String[] cmd = {"alamakota"};
        String output = externalProcessExecutor.runCommand(cmd, null, null);

        assertThat(output,  is(nullValue()));

    }
}