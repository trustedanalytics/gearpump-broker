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
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(MockitoJUnitRunner.class)
public class ConfigParserTest extends TestCase {
    public static final String YARN_CONF = "gearpump {\n"+
            "  yarn {\n"+
            "    client {\n"+
            "      hdfsRoot = \"/user/gearpump/\"\n"+
            "      jars = \"target/pack/lib\"\n"+
            "      excludejars = \"\"\n"+
            "    }\n"+
            "\n"+
            "    applicationmaster {\n"+
            "      name = \"GearPump\"\n"+
            "      main = \"io.gearpump.experiments.yarn.master.YarnApplicationMaster\"\n"+
            "      command = \"$JAVA_HOME/bin/java\"\n"+
            "      memory = \"2048\"\n"+
            "      vcores = \"1\"\n"+
            "      queue = \"default\"\n"+
            "      port = \"10999\"\n"+
            "    }\n"+
            "  }\n"+
            "\n"+
            "  master {\n"+
            "    command = \"$JAVA_HOME/bin/java  -Xmx1024m\"\n"+
            "    main = \"io.gearpump.cluster.main.Master\"\n"+
            "    containers = \"1\"\n"+
            "    ip = \"\"\n"+
            "    port = \"3000\"\n"+
            "    memory = \"1024\"\n"+
            "    vcores = \"1\"\n"+
            "    logname = \"master.log\"\n"+
            "  }\n"+
            "\n"+
            "  worker {\n"+
            "    command = \"$JAVA_HOME/bin/java  -Xmx1024m\"\n"+
            "    main = \"io.gearpump.cluster.main.Worker\"\n"+
            "    containers = \"1\"\n"+
            "    memory = \"1024\"\n"+
            "    vcores = \"1\"\n"+
            "    logname = \"worker.log\"\n"+
            "  }\n"+
            "  services {\n"+
            "    enabled = true\n"+
            "    command = \"$JAVA_HOME/bin/java\"\n"+
            "    main = \"io.gearpump.services.main.Services\"\n"+
            "    containers = \"1\"\n"+
            "    memory = \"1024\"\n"+
            "    vcores = \"1\"\n"+
            "    port = \"8099\"\n"+
            "    logname = \"services.log\"\n"+
            "\n"+
            "  }\n"+
            "}\n";

    private ConfigParser configParser;

    @Before
    public void setUp() throws Exception {
        this.configParser = new ConfigParser();
        super.setUp();
    }

    @Test
    public void testEnsureProperConfigFolder() throws Exception {
        boolean containsExpectedString = false;
        containsExpectedString = YARN_CONF.contains("/ala/ma/kota");
        assertTrue("Precondition not met. Test string is already in the config.", !containsExpectedString);
        // ensureProperConfigFolder(String input, String provisionedFolder, String hdfsDirectory)
        String newContent = configParser.ensureProperConfigFolder(YARN_CONF, "/ala", "/ma/kota");
        containsExpectedString = newContent.contains("/ala/ma/kota");
        assertTrue("Test string was not injected to config.", containsExpectedString);
    }

    @Test
    public void testOverwriteFile() throws IOException {
        String testFileName = "test.txt";
        String originalContent = "111111111111111111";
        String newContent = "22222222222222222222";
        //have some known file (known content)
        Path testFilePath = Files.write(Paths.get(testFileName), originalContent.getBytes(StandardCharsets.UTF_8));
        // overwrite the content
        configParser.overwriteFile(testFilePath, newContent);
        // verify the content
        String verifyContent = new String(Files.readAllBytes(testFilePath), StandardCharsets.UTF_8);
        assertTrue("File replacement haven't succeed.", verifyContent.contains("2"));
        assertFalse("File replacement haven't succeed.", verifyContent.contains("1"));
    }
}