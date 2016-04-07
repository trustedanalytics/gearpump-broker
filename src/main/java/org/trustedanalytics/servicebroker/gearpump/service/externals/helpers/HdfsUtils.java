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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trustedanalytics.hadoop.config.client.*;
import org.trustedanalytics.hadoop.config.client.helper.Hdfs;
import org.trustedanalytics.servicebroker.gearpump.kerberos.KerberosService;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class HdfsUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HdfsUtils.class);

    private final FileSystem hdfsFs;
    private final String hdfsUri;

    private final KerberosService kerberosService;
    private final ServiceInstanceConfiguration helper;

    @Autowired
    public HdfsUtils(KerberosService kerberosService) throws IOException, LoginException, InterruptedException, URISyntaxException {
        LOGGER.info("=-=========================== create hdfs filesystem");
        this.kerberosService = kerberosService;
        helper = Configurations.newInstanceFromEnv().getServiceConfig(ServiceType.HDFS_TYPE);

        hdfsUri = helper.getProperty(Property.HDFS_URI).get();
        hdfsFs = createHdfsFs();

        LOGGER.info("hdfsUri + " + hdfsUri);
        hdfsFs.setWorkingDirectory(new Path(this.hdfsUri));
    }

    private FileSystem createHdfsFs() throws IOException, LoginException, InterruptedException, URISyntaxException {
        if (kerberosService.isKerberosEnabled()) {
            return Hdfs.newInstance().createFileSystem();
        } else {
            Configuration hadoopConf = helper.asHadoopConfiguration();
            String user = kerberosService.getKerberosUser();
            return FileSystem.get(new URI(hdfsUri), hadoopConf, user);
        }
    }

    public boolean exists(String name) throws IOException {
        boolean result;
        Path thePath = new Path(makeLocal(name));
        LOGGER.info("The directory path is {}.", thePath);
        result = hdfsFs.exists(thePath);
        return result;
    }

    public boolean directoryExists(String name) throws IOException {
        boolean result;
        Path thePath = new Path(makeLocal(name));
        LOGGER.info("The directory path is {}.", thePath.toUri());
        result = hdfsFs.exists(thePath) && hdfsFs.isDirectory(thePath);
        return result;
    }


    public void createDir(String name) throws IOException {
        Path thePath = new Path(makeLocal(name));
        hdfsFs.mkdirs(thePath);
    }

    public void elevatePermissions(String name) throws IOException {
        Path thePath = new Path(makeLocal(name));
        hdfsFs.setPermission(thePath, new FsPermission(FsAction.ALL,FsAction.ALL,FsAction.ALL));
    }

    public void upload(String localPath, String remotePath) throws IOException {
        Path localFilePath = new Path(localPath);
        Path hdfsFilePath = new Path(makeLocal(remotePath));
        LOGGER.info("The remote path is {}.", hdfsFilePath.toUri());
        hdfsFs.copyFromLocalFile(localFilePath, hdfsFilePath);
    }

    public static String makeLocal(String path) {
        return path.startsWith(("/")) ? path.substring(1) : path;
    }

    public static String ensureTrailingSlash(String text) {
        return text + (text.endsWith("/") ? "" : "/");
    }


    public String getHdfsUri() {
        return hdfsUri;
    }
}
