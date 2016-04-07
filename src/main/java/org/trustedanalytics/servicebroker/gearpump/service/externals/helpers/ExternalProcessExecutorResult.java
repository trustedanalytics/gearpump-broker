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

public class ExternalProcessExecutorResult {

    private final String output;
    private final int exitCode;
    private final Exception exception;

    public ExternalProcessExecutorResult(int exitCode, String output, Exception exception) {
        this.exitCode = exitCode;
        this.output = output;
        this.exception = exception;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "ExternalProcessExecutorResult{" +
                "exception=" + exception +
                ", output='" + ( output != null ? output.substring(1, 255) : "" ) + '\'' +
                ", exitCode=" + exitCode +
                '}';
    }
}
