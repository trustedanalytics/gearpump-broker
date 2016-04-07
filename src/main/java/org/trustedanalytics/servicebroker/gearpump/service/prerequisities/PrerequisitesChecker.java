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

package org.trustedanalytics.servicebroker.gearpump.service.prerequisities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.trustedanalytics.servicebroker.gearpump.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.gearpump.service.externals.ExternalProcessException;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.ConfigParser;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.ExternalProcessExecutor;
import org.trustedanalytics.servicebroker.gearpump.service.externals.helpers.HdfsUtils;
import org.trustedanalytics.servicebroker.gearpump.service.file.ArchiverService;
import org.trustedanalytics.servicebroker.gearpump.service.file.ResourceManagerService;
import org.trustedanalytics.servicebroker.gearpump.yarn.YarnConfigFilesProvider;

import java.io.IOException;
import java.util.Arrays;


@Service
public class PrerequisitesChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrerequisitesChecker.class);

    @Autowired
    private HdfsUtils hdfsUtils;

    @Autowired
    private ArchiverService archiverService;

    @Autowired
    private ResourceManagerService resourceManagerService;

    @Autowired
    private ExternalConfiguration externalConfiguration;

    @Autowired
    private YarnConfigFilesProvider yarnConfigFilesProvider;

    @Autowired
    private ExternalProcessExecutor externalProcessExecutor;

    @Value("${yarn.conf.dir}")
    private String yarnConfDir;

    private String destDir;

    /**
     * Ensure that requirements are met in order to start provisioning:
     * <li>gearpump binary pack (archive) is present</li>
     * <li>the archive is unpacked</li>
     * <li>the archive is stored on hdfs</li>
     */
    public void ensurePrerequisities() throws PrerequisitesException {
        LOGGER.info("ensure prerequisities invoked");

        String gearPumpPackName = externalConfiguration.getGearPumpPackName();
        String archiveLocalPath = String.format("gearpump/%s", gearPumpPackName);
        // 1. check if the archive exists
        LOGGER.info("Checking if archive {} exists. ", archiveLocalPath);
        boolean archivePresent = resourceManagerService.checkIfExists(archiveLocalPath);
        if (!archivePresent) {
            LOGGER.info("GearPump archive not present. Downloading it...");
            // for now it's always there - it's included in the build
            LOGGER.error("Not yet implemented");
            throw new PrerequisitesException("Downloading archive not yet implemented.");
        }
        LOGGER.info("GearPump archive {} is present. ", gearPumpPackName);

        // 2. check if unpacked version exists
        boolean archiveUnpacked = resourceManagerService.checkIfExists(externalConfiguration.getGearPumpDestinationFolder());

        LOGGER.info("Checking if the archive was unpacked");
        if (!archiveUnpacked) {
            LOGGER.info("GearPump archive was not unpacked. Doing it now ...");
            try {
                archiverService.intoDestination("").unzip(archiveLocalPath);
            } catch (IOException e) {
                LOGGER.error("Cannot untar GearPump archive.", e);
                throw new PrerequisitesException("Cannot untar GearPump archive.", e);
            }
        }

        LOGGER.info("Checking yarn-conf files...");
        try {
            yarnConfigFilesProvider.prepareConfigFiles();
        } catch (IOException e) {
            LOGGER.error("Cannot unzip and store yarn config files from yarn-gearpump");
            // TODO XXX choke the exception for now - until we make sure yarn broker returns zipped configs
            //throw new PrerequisitesException("Cannot unzip and store yarn config files from yarn-gearpump");
            LOGGER.info("choke the exception for now - until we make sure yarn broker returns zipped configs");
        }

        try {
            destDir = resourceManagerService.getRealPath(externalConfiguration.getGearPumpDestinationFolder());
            setBinariesExecutable();
        } catch (IOException | ExternalProcessException e) {
            LOGGER.error("Error making GearPump binaires executable.", e);
            throw new PrerequisitesException("Error making GearPump binaires executable.", e);
        }

        try {
            copyYarnConfigFiles(); // yarnclient ignores HADOOP_CONF_DIR. workaround is to put config files to gp/conf dir
        } catch (ExternalProcessException e) {
            LOGGER.error("Error checking HDFS directory for GearPump archive.", e);
            throw new PrerequisitesException("Error checking HDFS directory for GearPump archive.", e);
        }




        // 3. check if hdfs directory exists
        String hdfsDirectory = externalConfiguration.getHdfsDir();
        LOGGER.info("Check if HDFS directory for GearPump archive exists.");
        boolean hdfsDirExists = false;
        try {
            hdfsDirExists = hdfsUtils.directoryExists(hdfsDirectory);
        } catch (IOException e) {
            LOGGER.error("Error checking HDFS directory for GearPump archive.", e);
            throw new PrerequisitesException("Error checking HDFS directory for GearPump archive.", e);
        }

        if (!hdfsDirExists) {
            LOGGER.info("HDFS directory doesn't exist. Creating it now ...");
            try {
                hdfsUtils.createDir(hdfsDirectory);
                // make sure VCAP user can use this directory
                hdfsUtils.elevatePermissions(hdfsDirectory);
            } catch (IOException e) {
                LOGGER.error("Error creating HDFS directory.", e);
                throw new PrerequisitesException("Error creating HDFS directory.", e);
            }
        }
        LOGGER.info("HDFS directory for GearPump archive exists.");

        //4. check if the pack is in hdfs
        String hdfsFilePath = externalConfiguration.getHdfsGearPumpPackPath();
        LOGGER.info("Checking if the archive ({}) is stored in hdfs", hdfsFilePath);
        boolean hdfsFileExists = false;
        try {
            hdfsFileExists = hdfsUtils.exists(hdfsFilePath);
        } catch (IOException e) {
            LOGGER.error("Error checking the archive presence.", e);
            throw new PrerequisitesException("Error checking the archive presence.", e);
        }

        if (!hdfsFileExists) {
            try {
                LOGGER.info("The archive is not on HDFS. Uploading it now...", hdfsFilePath);
                hdfsUtils.upload(archiveLocalPath, hdfsFilePath);
                // make sure VCAP user can use this file
                hdfsUtils.elevatePermissions(hdfsFilePath);
            } catch (IOException e) {
                LOGGER.error("Error uploading archive.", e);
                throw new PrerequisitesException("Error uploading archive.", e);
            }
        }
        LOGGER.info("Archive IS stored in hdfs");
    }



    private void setBinariesExecutable() throws ExternalProcessException {
        String[] command = new String[]{"chmod", "-R", "+x", "bin"};
        runCommand(command);
    }

    private void copyYarnConfigFiles() throws ExternalProcessException {
        String[] command = new String[]{"cp", "-R", String.format("%s/.", yarnConfDir), String.format("%s/conf/", destDir)};
        runCommand(command);
    }

    private void runCommand(String[] command) throws ExternalProcessException {
        LOGGER.debug("Executing command: {}", Arrays.toString(command));
        externalProcessExecutor.runCommand(command, destDir, null);
    }
}
