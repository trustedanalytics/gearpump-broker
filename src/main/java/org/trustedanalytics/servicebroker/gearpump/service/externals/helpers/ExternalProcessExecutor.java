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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trustedanalytics.servicebroker.gearpump.kerberos.KerberosService;
import org.trustedanalytics.servicebroker.gearpump.service.externals.ExternalProcessException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.common.base.Strings;

@Service
public class ExternalProcessExecutor {

    @Autowired
    private KerberosService kerberosService;

    private static final String WORKERS_NUMBER_SWITCH = "-Dgearpump.yarn.worker.containers=";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProcessExecutor.class);

    private void setEnvForProcessBuilder(Map<String, String> env, String numberOfWorkers) throws IOException {
        String env_options = Strings.nullToEmpty(kerberosService.getKerberosJavaOpts());

        if(!Strings.isNullOrEmpty(numberOfWorkers)) {
            env_options += " " + WORKERS_NUMBER_SWITCH + numberOfWorkers;
        }

        if (!env_options.isEmpty()) {
            LOGGER.info("JAVA_OPTS: {}", env_options);
            env.put("JAVA_OPTS", env_options);
        }
    }

    public String runWithProcessBuilder(String[] command, String workingDir, String numberOfWorkers) throws IOException, ExternalProcessException {
        String lineToRun = Arrays.asList(command).stream().collect(Collectors.joining(" "));

        LOGGER.info("===================");
        LOGGER.info("Command to invoke: {}", lineToRun);

        ProcessBuilder processBuilder = new ProcessBuilder( command );
        setEnvForProcessBuilder(processBuilder.environment(), numberOfWorkers);

        if (workingDir != null) {
            processBuilder.directory(new File(workingDir));
        }
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuffer processOutput = new StringBuffer();
        String line;
        while ((line = stdout.readLine()) != null) {
            LOGGER.debug(":::::: " + line);
            processOutput.append(line);
            processOutput.append('\n');
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            LOGGER.error("Command '" + lineToRun + "' interrupted.", e);
        }

        stdout.close();

        int exitCode = process.exitValue();
        LOGGER.info("Output: {}", processOutput.toString());
        LOGGER.info("Exit value: {}", exitCode);
        LOGGER.info("===================");

        if (exitCode != 0) {
            throw new ExternalProcessException("GearPump driver exited with code " + exitCode);
        }

        return processOutput.toString();
    }
}
