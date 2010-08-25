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

import java.util.UUID;

import junit.framework.TestCase;

public class OidUuidFactoryTest extends TestCase {

    /*
     * Tests derived from
     * http://www.itu.int/ITU-T/studygroups/com17/oid/X.667-E.pdf
     * EXAMPLE - The following is an example of the string
     * representation of a UUID as a URN:
     * urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6
     * 
     * NOTE - An alternative URN format (see [6]) is available,
     * but is not recommended for URNs generated using UUIDs. This
     * alternative format uses the single integer value of the UUID
     * specified in 6.3, and represents the above example as
     * 
     * "urn:oid:2.25.329800735698586629295641978511506172918".
     */
    String testOid = "2.25.329800735698586629295641978511506172918";
    UUID testUuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

    public void testGetUUID() {
        UUID uid = OidUuidFactory.get(testOid);
        assertEquals(testUuid, uid);
    }

    public void testGetString() {
        String oid = OidUuidFactory.get(testUuid);
        assertEquals(testOid, oid);
    }

}
