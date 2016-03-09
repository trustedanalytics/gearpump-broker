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
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class YarnAppManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(YarnAppManager.class);

    @Autowired
    private YarnClientFactory yarnClientFactory;

    @Bean
    public YarnAppManager yarnAppManager() {
        return new YarnAppManager();
    }

    /**
     * Remove application from YARN.
     * @param applicationId should be in format application_1449093574559_0004
     * @throws YarnException
     */
    public void killApplication(String applicationId) throws YarnException {
        try (YarnClient yarnClient = getYarnClient()) {
            yarnClient.killApplication(getApplicationId(applicationId));
        } catch (ApplicationNotFoundException anfe) {
            LOGGER.warn("Haven't found application {}. Assuming it was removed manually.", applicationId);
        } catch (IOException e) {
            throw new YarnException("YARN error during application removal.", e);
        }
    }

    private ApplicationId getApplicationId(String applicationId) throws IOException, YarnException {
        return new YarnAppIdParser(applicationId).getApplicationId();
    }

    private YarnClient getYarnClient() {
        return yarnClientFactory.getYarnClient();
    }
}
