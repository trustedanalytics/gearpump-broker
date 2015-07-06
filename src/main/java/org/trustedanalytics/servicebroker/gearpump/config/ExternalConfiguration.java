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

package org.trustedanalytics.servicebroker.gearpump.config;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
//@Data
public class ExternalConfiguration {

    @Value("${cf.serviceid}")
    @NotNull
    private String cfServiceId;

    @Value("${cf.servicename}")
    @NotNull
    private String cfServiceName;

    @Value("${gearpump.gearpumpUri}")
    private String gearpumpUri;

    @Value("${cf.baseId}")
    @NotNull
    private String cfBaseId;

    @Value("${metadata.imageUrl}")
    @NotNull
    private String imageUrl;

    // TODO think of moving the following properties to GearPumpSpawnerConfig
    //<-- spawning related
    @Value("${yarn.conf.dir}")
    @NotNull
    private String yarnConfDir;

    @Value("${gearpump.pack.name}")
    @NotNull
    private String gearPumpPackName;

    @Value("${gearpump.pack.version}")
    @NotNull
    private String gearPumpPackVersion;

    @Value("${gearpump.destinationFolder}")
    @NotNull
    private String gearPumpDestinationFolder;

    @Value("${gearpump.hdfsDir}")
    @NotNull
    private String hdfsDir;

    public String getHdfsGearPumpPackPath() {
        return String.format("%s/%s", hdfsDir, gearPumpPackName);
    };

    //>-- spawning related

    public String getCfServiceName() {
        return cfServiceName;
    }

    public void setCfServiceName(String cfServiceName) {
        this.cfServiceName = cfServiceName;
    }

    public String getCfServiceId() {
        return cfServiceId;
    }

    public void setCfServiceId(String cfServiceId) {
        this.cfServiceId = cfServiceId;
    }

    public String getGearpumpUri() {
        return gearpumpUri;
    }

    public void setGearpumpUri(String gearpumpUri) {
        this.gearpumpUri = gearpumpUri;
    }

    public String getCfBaseId() {
        return cfBaseId;
    }

    public void setCfBaseId(String cfBaseId) {
        this.cfBaseId = cfBaseId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getYarnConfDir() {
        return yarnConfDir;
    }

    public void setYarnConfDir(String yarnConfDir) {
        this.yarnConfDir = yarnConfDir;
    }

    public String getGearPumpPackName() {
        return gearPumpPackName;
    }

    public void setGearPumpPackName(String gearPumpPackName) {
        this.gearPumpPackName = gearPumpPackName;
    }

    public String getGearPumpPackVersion() {
        return gearPumpPackVersion;
    }

    public void setGearPumpPackVersion(String gearPumpPackVersion) {
        this.gearPumpPackVersion = gearPumpPackVersion;
    }

    public String getHdfsDir() {
        return hdfsDir;
    }

    public void setHdfsDir(String hdfsDir) {
        this.hdfsDir = hdfsDir;
    }

    public String getGearPumpDestinationFolder() {
        return gearPumpDestinationFolder;
    }

    @Override
    public String toString() {
        return "ExternalConfiguration{" +
                "cfServiceId='" + cfServiceId + '\'' +
                ", cfServiceName='" + cfServiceName + '\'' +
                ", gearpumpUri='" + gearpumpUri + '\'' +
                ", cfBaseId='" + cfBaseId + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", yarnConfDir='" + yarnConfDir + '\'' +
                ", gearPumpPackName='" + gearPumpPackName + '\'' +
                ", gearPumpPackVersion='" + gearPumpPackVersion + '\'' +
                ", hdfsDir='" + hdfsDir + '\'' +
                '}';
    }

}