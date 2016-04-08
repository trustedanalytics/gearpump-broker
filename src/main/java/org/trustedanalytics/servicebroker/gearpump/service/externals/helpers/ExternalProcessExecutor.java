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
import org.springframework.stereotype.Service;
import org.trustedanalytics.servicebroker.gearpump.service.externals.ExternalProcessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExternalProcessExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProcessExecutor.class);

    private void updateEnvOfProcessBuilder(Map<String, String> processBuilderEnv, Map<String, String> properties) {
        if (properties != null) {
            processBuilderEnv.putAll(properties);
        }
    }

    public ExternalProcessExecutorResult run(String[] command, String workingDir, Map<String, String> properties) {

        String lineToRun = Arrays.asList(command).stream().collect(Collectors.joining(" "));

        LOGGER.info("===================");
        LOGGER.info("Command to invoke: {}", lineToRun);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        updateEnvOfProcessBuilder(processBuilder.environment(), properties);

        if (workingDir != null) {
            processBuilder.directory(new File(workingDir));
        }
        processBuilder.redirectErrorStream(true);

        StringBuilder processOutput = new StringBuilder();
        Process process = null;
        try {
            process = processBuilder.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

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

        } catch (IOException e) {
            LOGGER.error("Problem executing external process.", e);
            return new ExternalProcessExecutorResult(Integer.MIN_VALUE, "", e);
        }

        ExternalProcessExecutorResult result = new ExternalProcessExecutorResult(process.exitValue(), processOutput.toString(), null);

        LOGGER.info("Exit value: {}", result.getExitCode());
        LOGGER.info("===================");
        return result;
    }

    public String runCommand(String[] command, String workingDir, Map<String, String> properties) throws ExternalProcessException {
        ExternalProcessExecutorResult result = run(command, workingDir, properties);

        if (result.getException() != null) {
            throw new ExternalProcessException("GearPump driver exited with exception " + result.getException(), result.getException());
        }
        if (result.getExitCode() != 0) {
            throw new ExternalProcessException("GearPump driver exited with code " + result.getExitCode());
        }

        return result.getOutput();
    }
}
