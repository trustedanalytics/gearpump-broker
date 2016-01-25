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

package org.trustedanalytics.servicebroker.gearpump.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class GearPumpCredentials {

    private String masters;
    private String dashboardUrl;
    private String dashboardGuid;
    private String yarnApplicationId;
    private String username;
    private String password;

    public Map<String, Object> toMap() {
        return ImmutableMap.<String, Object>builder()
                .put("masters", masters)
                .put("yarnApplicationId", yarnApplicationId)
                .put("dashboardUrl", dashboardUrl)
                .put("dashboardGuid", dashboardGuid)
                .put("username", username)
                .put("password", password)
                .build();
    }

    public GearPumpCredentials(String masters, String yarnApplicationId) {
        this.masters = masters;
        this.yarnApplicationId = yarnApplicationId;
    }

    public GearPumpCredentials(String masters, String yarnApplicationId, String dashboardUrl, String dashboardGuid,
                               String username, String password) {
        this.masters = masters;
        this.yarnApplicationId = yarnApplicationId;
        this.dashboardUrl = dashboardUrl;
        this.dashboardGuid = dashboardGuid;
        this.username = username;
        this.password = password;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    public String getDashboardGuid() {
        return dashboardGuid;
    }

    public void setDashboardGuid(String dashboardGuid) {
        this.dashboardGuid = dashboardGuid;
    }

    public String getMasters() {
        return masters;
    }

    public void setMasters(String masters) {
        this.masters = masters;
    }

    public String getYarnApplicationId() {
        return yarnApplicationId;
    }

    public void setYarnApplicationId(String yarnApplicationId) {
        this.yarnApplicationId = yarnApplicationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "GearPumpCredentials{" +
                "masters='" + masters + '\'' +
                ", yarnApplicationId='" + yarnApplicationId + '\'' +
                ", dashboardUrl='" + dashboardUrl + '\'' +
                ", dashboardGuid='" + dashboardGuid + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}