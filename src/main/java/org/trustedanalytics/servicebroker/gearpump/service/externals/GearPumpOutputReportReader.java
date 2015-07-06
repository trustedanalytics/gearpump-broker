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

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trustedanalytics.servicebroker.gearpump.service.file.FileReaderService;
import org.trustedanalytics.servicebroker.gearpump.service.file.FileWriterService;

import java.io.IOException;

@Service
public class GearPumpOutputReportReader {

    private static final String GEARPUMP_TAG = "gearpump";
    private static final String CLUSTER_TAG = "cluster";
    private static final String MASTERS_TAG = "masters";

    @Autowired
    private FileReaderService fileReaderService;

    @Autowired
    private FileWriterService fileWriterService;

    private String outputFileReport;

    public GearPumpOutputReportReader fromOutput(String path) {
        outputFileReport = path;
        return this;
    }

    public String getMasterUrl() throws GearpumpOutputException {
        try {
            String outputReport = fileReaderService.read(outputFileReport);
            return parseOutputReport(outputReport);
        } catch (JsonParseException | IOException e) {
            throw new GearpumpOutputException("Unable to parse gearpump launch output report", e);
        }
    }

    public void deleteReportFile() {
        fileWriterService.intoDestination(outputFileReport);
        if (fileWriterService.isOutputExists()) {
            fileWriterService.delete();
        }
    }
    
    private String parseOutputReport(String outputReport) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject)jsonParser.parse(removeCommentLines(outputReport));
        return jsonObject.getAsJsonObject(GEARPUMP_TAG).getAsJsonObject(CLUSTER_TAG).getAsJsonArray(MASTERS_TAG).getAsString();
    }

    private String removeCommentLines(String input) {
        return input.replaceAll("(?m)^#.*", "");
    }
}
