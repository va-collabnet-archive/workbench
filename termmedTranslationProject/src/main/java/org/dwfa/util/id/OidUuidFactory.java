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
package org.dwfa.util.id;

import java.math.BigInteger;
import java.util.UUID;

public class OidUuidFactory {

    private final static String HEX_CHARS = "0123456789abcdefABCDEF";
    private static String oidPrefix = "2.25.";

    public static String get(UUID uid) {
        return get(oidPrefix, uid);
    }

    public static String get(String prefix, UUID uid) {
        BigInteger biggy = new BigInteger(uid.toString().replaceAll("-", ""), 16);
        return prefix + biggy.toString();
    }

    public static UUID get(String oid) {
        return get(oidPrefix, oid);
    }

    public static UUID get(String prefix, String oid) {
        if (oid.startsWith(prefix) == false) {
            throw new NumberFormatException("The oid must start with: " + prefix + " found: " + oid);
        }
        String bigIntPart = oid.substring(oid.lastIndexOf('.') + 1);
        BigInteger bigInt = new BigInteger(bigIntPart);
        // should return 17 bytes, 1 sign byte, and 16 content bytes...
        byte[] bytes = bigInt.toByteArray();
        if (bytes[0] != 0) {
            throw new NumberFormatException("The integer must be positive. " + bytes[0] + " found: " + oid);
        }
        StringBuffer b = new StringBuffer(36);

        for (int i = 1; i < 17; ++i) {
            // Need to bypass hyphens:
            switch (i) {
            case 5:
            case 7:
            case 9:
            case 11:
                b.append('-');
            }
            int hex = bytes[i] & 0xFF;
            b.append(HEX_CHARS.charAt(hex >> 4));
            b.append(HEX_CHARS.charAt(hex & 0x0f));
        }
        String uuidStr = b.toString();
        return UUID.fromString(uuidStr);
    }
}
