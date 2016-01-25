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

import org.apache.hadoop.yarn.api.records.ApplicationId;

import java.util.regex.Pattern;

class YarnAppIdParser {

    private String applicationId;

    private static final String APPLICATION_ID_FORMAT_PATTERN = "application_[0-9]{13}_[0-9]+";

    public YarnAppIdParser(String applicationId) {
        this.applicationId = applicationId;
        if (!isAppIdValid()) {
            throw new IllegalArgumentException("Wrong format off applicationId - " + applicationId);
        }
    }

    private boolean isAppIdValid() {
        return applicationId.matches(APPLICATION_ID_FORMAT_PATTERN);
    }

    public ApplicationId getApplicationId() {
        String[] applicationIdSplited = applicationId.split("_");
        long timestamp = Long.valueOf(applicationIdSplited[1]);
        int id = Integer.valueOf(applicationIdSplited[2]);
        return ApplicationId.newInstance(timestamp, id);
    }
}
