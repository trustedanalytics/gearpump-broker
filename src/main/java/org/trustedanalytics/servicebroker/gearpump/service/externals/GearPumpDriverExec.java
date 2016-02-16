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

import com.google.common.base.Strings;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.trustedanalytics.servicebroker.gearpump.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.ExternalProcessExecutor;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.HdfsUtils;
import org.trustedanalytics.servicebroker.gearpump.service.file.ResourceManagerService;

import java.io.IOException;
import java.util.Arrays;

public class GearPumpDriverExec {
    private static final Logger LOGGER = LoggerFactory.getLogger(GearPumpDriverExec.class);

    private static final String COMMAND_LINE_TEMPLATE_SPAWN = "bin/yarnclient launch -package %s -output %s";

    private String outputReportFilePath;

    @Autowired
    private GearPumpCredentialsParser gearPumpCredentialsParser;

    @Autowired
    private GearPumpOutputReportReader gearPumpOutputReportReader;

    @Autowired
    private ResourceManagerService resourceManagerService;

    @Autowired
    private ExternalConfiguration externalConfiguration;

    @Autowired
    private HdfsUtils hdfsUtils;

    @Autowired
    private ExternalProcessExecutor externalProcessExecutor;

    @Value("${yarn.conf.dir}")
    private String yarnConfDir;

    private String destDir;

    public GearPumpCredentials spawnGearPumpOnYarn(String numberOfWorkers) throws IOException, ExternalProcessException {
        destDir = resourceManagerService.getRealPath(externalConfiguration.getGearPumpDestinationFolder());
        outputReportFilePath = createOutputReportFilePath(destDir);

        setBinariesExecutable();
        copyYarnConfigFiles(); // yarnclient ignores HADOOP_CONF_DIR. workaround is to put config files to gp/conf dir
        String yarnClientOutput = pushGearpumpOnYarn(numberOfWorkers);

        LOGGER.info("Yarn spawn output - " + yarnClientOutput);

        String mastersUrl = gearPumpOutputReportReader.fromOutput(outputReportFilePath).getMasterUrl();
        String yarnApplicationId = gearPumpCredentialsParser.getApplicationId(yarnClientOutput);

        if (Strings.isNullOrEmpty(mastersUrl) || Strings.isNullOrEmpty(yarnApplicationId)) {
            throw new ExternalProcessException("Couldn't obtain yarn credentials.");
        }

        gearPumpOutputReportReader.deleteReportFile();

        return new GearPumpCredentials(mastersUrl, yarnApplicationId);
    }

    private String pushGearpumpOnYarn(String numberOfWorkers) throws IOException, ExternalProcessException {
        String[] command = getGearPumpYarnCommand();
        return runCommand(command, numberOfWorkers);
    }

    private void setBinariesExecutable() throws IOException, ExternalProcessException {
        String[] command = new String[]{"chmod", "-R", "+x", "bin"};
        runCommand(command);
    }

    private void copyYarnConfigFiles() throws IOException, ExternalProcessException {
        String[] command = new String[]{"cp", "-R", String.format("%s/.", yarnConfDir), String.format("%s/conf/", destDir)};
        runCommand(command);
    }

    private String runCommand(String[] command, String numberOfWorkers) throws IOException, ExternalProcessException {
        LOGGER.debug("Executing command: {}", Arrays.toString(command) + "; workersNumber: " + numberOfWorkers);
        return externalProcessExecutor.runWithProcessBuilder(command, destDir, numberOfWorkers);
    }

    private String runCommand(String[] command) throws IOException, ExternalProcessException {
        LOGGER.debug("Executing command: {}", Arrays.toString(command));
        return externalProcessExecutor.runWithProcessBuilder(command, destDir, null);
    }

    private String createOutputReportFilePath(String gearPumpDestinationFolderPath) {
        return String.format("%s/output-%d-%s.conf", gearPumpDestinationFolderPath, System.currentTimeMillis(), RandomStringUtils.randomNumeric(4));
    }

    private String[] getGearPumpYarnCommand() {
        String gearpumpPackUri = hdfsUtils.getHdfsUri() + externalConfiguration.getHdfsGearPumpPackPath();
        return String.format(COMMAND_LINE_TEMPLATE_SPAWN, gearpumpPackUri, outputReportFilePath).split(" ");
    }
}
