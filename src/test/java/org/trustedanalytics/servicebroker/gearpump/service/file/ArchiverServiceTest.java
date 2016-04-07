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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArchiverServiceTest {

    @Mock
    private ResourceManagerService resourceManagerService;

    @Mock
    private FileWriterService fileWriterService;

    @InjectMocks
    private ArchiverService archiverService;

    private byte[] zipFile;
    private byte[] tarGzFile;
    private final static byte[] fileTestContent = "Content".getBytes();


    @Before
    public void init() throws IOException {
        prepareCompressedFiles();
        archiverService = new ArchiverService();
        MockitoAnnotations.initMocks(this);
        mockFileWiter();
        mockResourceLoader();
    }

    private void mockResourceLoader() throws IOException {
        when(resourceManagerService.getResourceInputStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream(tarGzFile));
    }

    public void prepareCompressedFiles() throws IOException {
        zipFile = FileHelper.prepareZipFile(fileTestContent);
        tarGzFile = FileHelper.prepareTarGzFile(fileTestContent);
    }

    private void mockFileWiter() throws IOException {
        when(fileWriterService.intoDestination(Mockito.anyString())).thenReturn(fileWriterService);
        when(fileWriterService.withOverride(Mockito.anyBoolean())).thenReturn(fileWriterService);
        when(fileWriterService.writeToFile(Mockito.<InputStream>any(), Mockito.anyBoolean())).thenReturn(true);
    }

    @Test
    public void testUnzipSuccess() throws IOException {
        try (InputStream is = new ByteArrayInputStream(zipFile)) {
            archiverService.intoDestination("").unzip(is, true);
        }
    }

    @Test
    public void testUntarFromInputStreamSuccess() throws IOException {
        try (InputStream is = new ByteArrayInputStream(tarGzFile)) {
            archiverService.intoDestination("").untar(is);
        }
    }

    @Test
    public void testUntarFromResourceSuccess() throws IOException {
        String fakeResourcePath = "/";
        try (InputStream is = new ByteArrayInputStream(tarGzFile)) {
            archiverService.intoDestination("").untar(fakeResourcePath);
        }
    }
}
