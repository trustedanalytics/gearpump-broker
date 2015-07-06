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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileWriterService {

    private static final Logger logger = LoggerFactory.getLogger(FileWriterService.class);

    private final static int BUFFER = 2048;

    private boolean shouldOverride;

    @Autowired
    private FileService fileWriter;

    public FileWriterService withOverride(boolean shouldOverride) {
        this.shouldOverride = shouldOverride;
        return this;
    }

    public FileWriterService intoDestination(String destinationDir) {
        fileWriter.setFilePath(destinationDir);
        return this;
    }

    public boolean writeToFile(InputStream inputStream, boolean isDirectory) throws IOException {
        if (isOutputExists() && !shouldOverride) {
            logger.info("File {} exists and won't be overriden.", fileWriter.getOutputName());
            return false;
        }
        if (isDirectory) {
            fileWriter.createDirectories();
        } else {
            fileWriter.createPrentDirectories();
            writeData(inputStream);
        }
        return true;
    }

    public boolean isOutputExists() {
        return fileWriter.isExists();
    }

    public boolean delete() {
        return fileWriter.deleteFile();
    }

    private void writeData(InputStream inputStream) throws IOException {
        int count;
        byte data[] = new byte[BUFFER];
        OutputStream fos = fileWriter.getOutputStream();
        try(BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)){
            while ((count = inputStream.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
        }
    }
}
