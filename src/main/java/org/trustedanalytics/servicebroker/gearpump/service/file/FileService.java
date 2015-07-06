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

import org.springframework.stereotype.Service;

import java.io.*;

@Service
class FileService {

    private File file;

    public void setFilePath(String path) {
        file = new File(path);
    }

    public boolean isExists() {
        return file.exists();
    }

    public void createDirectories() {
        file.mkdirs();
    }

    public void createPrentDirectories() {
        file.getParentFile().mkdirs();
    }

    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    public Reader getFileReader() throws FileNotFoundException {
        return new FileReader(file);
    }

    public boolean deleteFile() {
        return file.delete();
    }

    public String getOutputName() {
        return file.getName();
    }
}
