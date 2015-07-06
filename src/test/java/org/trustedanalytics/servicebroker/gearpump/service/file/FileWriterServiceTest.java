package org.trustedanalytics.servicebroker.gearpump.service.file;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FileWriterServiceTest {

    @InjectMocks
    private FileWriterService fileWriterService;

    @Mock
    private FileService fileWriter;

    private ByteArrayInputStream inputStream;
    private ByteArrayOutputStream outputStream;

    private static final byte[] INPUT_CONTENT = "Test Content".getBytes();

    @Before
    public void init() throws IOException {
        fileWriterService = new FileWriterService();
        MockitoAnnotations.initMocks(this);
        prepareStreams();
        mockFileWriter();
    }

    @After
    public void after() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
    }

    @Test
    public void testWriteToFileNoOverride() throws IOException {
        when(fileWriter.isExists()).thenReturn(true);

        fileWriterService.withOverride(false);

        boolean result = fileWriterService.writeToFile(inputStream, false);
        assertThat(result, equalTo(false));
    }

    @Test
    public void testWriteToFileOverride() throws IOException {
        when(fileWriter.isExists()).thenReturn(true);
        fileWriterService.withOverride(true);
        fileWriterService.intoDestination("");

        boolean result = fileWriterService.writeToFile(inputStream, false);

        assertThat(result, equalTo(true));
        assertThat(Arrays.equals(outputStream.toByteArray(), INPUT_CONTENT), equalTo(true));
    }

    @Test
    public void testWriteToFileCreateDirectory() throws IOException {
        boolean result = fileWriterService.writeToFile(inputStream, true);

        assertThat(result, equalTo(true));
        assertThat(outputStream.toByteArray().length, equalTo(0));
    }

    private void prepareStreams() {
        inputStream = new ByteArrayInputStream(INPUT_CONTENT);
        outputStream = new ByteArrayOutputStream(INPUT_CONTENT.length);
    }

    private void mockFileWriter() throws IOException {
        doNothing().when(fileWriter).createDirectories();
        doNothing().when(fileWriter).createPrentDirectories();
        doNothing().when(fileWriter).setFilePath(Mockito.anyString());
        when(fileWriter.getOutputStream()).thenReturn(outputStream);
        when(fileWriter.isExists()).thenReturn(false);
    }
}
