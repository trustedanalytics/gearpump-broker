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

import org.trustedanalytics.servicebroker.gearpump.model.GearPumpCredentials;

public class SpawnResult {
    public static final int STATUS_OK = 0;
    public static final int STATUS_ERR = -1;

    private final Exception exception;
    private final GearPumpCredentials gearPumpCredentials;
    private final int status;

    public SpawnResult(int status, GearPumpCredentials gearPumpCredentials, Exception exception) {
        this.status = status;
        this.exception = exception;
        this.gearPumpCredentials = gearPumpCredentials;
    }

    public Exception getException() {
        return exception;
    }

    public GearPumpCredentials getGearPumpCredentials() {
        return gearPumpCredentials;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "SpawnResult{" +
                "status=" + status +
                ", exception=" + exception +
                ", gearPumpCredentials=" + gearPumpCredentials +
                '}';
    }
}
