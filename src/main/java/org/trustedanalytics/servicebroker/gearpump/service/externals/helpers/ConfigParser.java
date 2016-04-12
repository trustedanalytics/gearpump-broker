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

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ConfigParser {
    private static final Pattern MASTERS_PATTERN = Pattern.compile("hdfsRoot = \"(.*?)\"");
    private static final String REPLACEMENT_TEMPLATE = "hdfsRoot = \"%s%s\"";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public String ensureProperConfigFolder(String input, String provisionedFolder, String hdfsDirectory) {
        String result = "";
        Matcher m = MASTERS_PATTERN.matcher(input);
        String replacement = String.format(REPLACEMENT_TEMPLATE, provisionedFolder, hdfsDirectory);
        result = m.replaceAll(replacement);
        return result;
    }

    public void overwriteFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(CHARSET));
    }
}
