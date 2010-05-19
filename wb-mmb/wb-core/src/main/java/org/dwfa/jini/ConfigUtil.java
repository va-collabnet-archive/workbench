/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
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
package org.dwfa.jini;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class ConfigUtil {
    public static String getHostIPAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private static String uniqueJvmGroup;

    public static String getUniqueJvmGroup() {
        if (uniqueJvmGroup == null) {
            uniqueJvmGroup = UUID.randomUUID().toString();
        }
        return uniqueJvmGroup;
    }

    public static void setUniqueJvmGroup(String uniqueJvmGroup) {
        ConfigUtil.uniqueJvmGroup = uniqueJvmGroup;
    }

    public static String getJiniPort() {
        if (System.getProperties().get("org.dwfa.jiniport") != null) {
            return (String) System.getProperties().get("org.dwfa.jiniport");
        }
        return "8081";
    }

    public static String getJiniPortUrlPart() {
        return ":" + getJiniPort() + "/";
    }

    public static void main(String[] args) {
        try {
            System.out.println("getHostIPAddress(): " + InetAddress.getLocalHost().getHostAddress());

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
