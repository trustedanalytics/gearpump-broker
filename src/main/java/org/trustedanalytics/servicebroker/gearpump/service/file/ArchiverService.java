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

package org.trustedanalytics.servicebroker.gearpump.service.file;


import java.io.*;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class ArchiverService {

    private static final Logger logger = LoggerFactory.getLogger(ArchiverService.class);

    @Autowired
    private ResourceManagerService resourceManager;

    @Autowired
    private FileWriterService fileWriter;

    private String destinationDir;

    private boolean shouldOverrideFiles;

    public ArchiverService intoDestination(String destinationDir) {
        this.destinationDir = destinationDir;
        return this;
    }

    /**
     *
     * @param archiveFile path to archive file _relative to classpath_.
     * @throws IOException
     */
    public void untar(String archiveFile) throws IOException {
        untar(resourceManager.getResourceInputStream(archiveFile));
    }

    public void untar(InputStream inputStream) throws IOException {
        this.shouldOverrideFiles = true;

        TarArchiveInputStream tarInput = null;
        try {
            tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(inputStream)));
            unpack(tarInput);
        } finally {
            tarInput.close();
        }

        logger.debug("untar completed successfully!!");
    }

    public void unzip(String archiveFile) throws IOException {
        unzip(resourceManager.getResourceInputStream(archiveFile), true);
    }

    public void unzip(InputStream inputStream, boolean overrideFiles) throws IOException {
        this.shouldOverrideFiles = overrideFiles;
        try (ZipArchiveInputStream zipIn = new ZipArchiveInputStream(inputStream)) {
            unpack(zipIn);
        }
    }

    private void unpack(ArchiveInputStream inputStream) throws IOException {
        ArchiveEntry entry = null;
        while ((entry = inputStream.getNextEntry()) != null) {
            logger.info("Extracting: {}", entry.getName());
            fileWriter.intoDestination(destinationDir + entry.getName())
                    .withOverride(shouldOverrideFiles)
                    .writeToFile(inputStream, entry.isDirectory());
        }
    }


}
