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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class FileHelper {

    private FileHelper(){}

    public static final String FILE_NAME = "testFile";

    public static byte[] prepareZipFile(byte[] zipFileTestContent) throws IOException {
        ByteArrayOutputStream byteOutput = null;
        ZipArchiveOutputStream zipOutput = null;
        try  {
            byteOutput = new ByteArrayOutputStream();
            zipOutput = new ZipArchiveOutputStream(byteOutput);
            ZipArchiveEntry entry = new ZipArchiveEntry(FILE_NAME);
            entry.setSize(zipFileTestContent.length);
            addArchiveEntry(zipOutput, entry, zipFileTestContent);
        } finally {
            zipOutput.close();
            byteOutput.close();
        }

        return byteOutput.toByteArray();
    }

    private static void addArchiveEntry(ArchiveOutputStream outputStream, ArchiveEntry entry, byte[] content) throws IOException {
        outputStream.putArchiveEntry(entry);
        outputStream.write(content);
        outputStream.closeArchiveEntry();
    }

    public static byte[] prepareTarGzFile(byte[] tarFileContent) throws IOException {
        ByteArrayOutputStream byteOutput = null;
        GzipCompressorOutputStream gzOutput = null;
        TarArchiveOutputStream tarOutput = null;
        try  {
            byteOutput = new ByteArrayOutputStream();
            gzOutput = new GzipCompressorOutputStream(byteOutput);
            tarOutput = new TarArchiveOutputStream(gzOutput);
            TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(FILE_NAME);
            tarArchiveEntry.setSize(tarFileContent.length);
            addArchiveEntry(tarOutput, tarArchiveEntry, tarFileContent);
        } finally {
            gzOutput.close();
            byteOutput.close();
        }

        return byteOutput.toByteArray();
    }

}
