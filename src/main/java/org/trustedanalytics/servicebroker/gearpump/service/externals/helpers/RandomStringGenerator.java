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

public class RandomStringGenerator {
    public static String generate(){
        return RandomStringGenerator.generate(10);
    }

    public static String generate(int length) {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < length; i++) {
            builder.append((char)((int)(Math.random()*100) % 25 + 97));
        }

        return builder.toString();
    }
}
