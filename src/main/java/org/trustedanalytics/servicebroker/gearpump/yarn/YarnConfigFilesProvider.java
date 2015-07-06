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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.servicebroker.gearpump.service.file.ArchiverService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class YarnConfigFilesProvider {

    private byte[] decodedConfigFiles;
    private static final String CONFIG_FILES_DIR = "";

    @Value("${yarn.conf.override}")
    private boolean shouldOverride;

    @Autowired
    private YarnVcapServiceReader yarnVcapServiceReader;

    @Autowired
    private ArchiverService archiverService;

    @Bean
    public YarnConfigFilesProvider yarnConfigFilesProvider() {
        return new YarnConfigFilesProvider();
    }

    private String getConfigFiles() {
        return yarnVcapServiceReader.getConfigZipFiles();
    }

    private void decode() {
        decodedConfigFiles = Base64.getDecoder().decode(getConfigFiles());
    }

    private void unzipAndStoreFiles(String destinationDir) throws IOException {
        try (InputStream is = new ByteArrayInputStream(decodedConfigFiles)) {
            archiverService.intoDestination(destinationDir)
                    .unzip(is, shouldOverride);
        }
    }
    
    public void prepareConfigFiles() throws IOException {
        try {
            decode();
            unzipAndStoreFiles(CONFIG_FILES_DIR);
        } catch (IllegalArgumentException e) {
            throw new IOException("Unable to prepare yarn config files", e);
        }
    }
}
