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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GearPumpCredentialsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GearPumpCredentialsParser.class);

    private static final Pattern APPLICATION_ID_PATTERN = Pattern.compile("Submitted application (\\S*)");
    private static final Pattern MASTERS_PATTERN = Pattern.compile("masters=(.*?)$");

    /**
     *
     * @param output expects yarnclient output
     */
    public String getMasterUrl(String output) {
        LOGGER.info("getMasterUrl");
        String result = null;
        Matcher m = MASTERS_PATTERN.matcher(output);
        if (m.find()) {
            result = m.group(1);
        }
        return result;
    }

    /**
     *
     * @param output expects yarnclient output
     */
    public String getApplicationId(String output) {
        LOGGER.info("getApplicationId");
        String result = null;
        Matcher m = APPLICATION_ID_PATTERN.matcher(output);
        LOGGER.debug("m {}", m);
        if (m.find()) {
            LOGGER.info("FOUND {}", m.groupCount());

            result = m.group(1);
        } else {
            LOGGER.info("NOT FOUND {}");
        }
        return result;
    }
}
