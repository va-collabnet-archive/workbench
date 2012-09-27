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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import junit.framework.TestCase;

public class Type5UuidFactoryTest extends TestCase {

    public void testGet() {
        try {
            UUID nameSpace_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
            UUID testId = UUID.fromString("886313e1-3b8a-5372-9b90-0c9aee199e5d");
            UUID generatedId = Type5UuidFactory.get(nameSpace_DNS, "python.org");

            assertEquals(testId, generatedId);
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        }

    }
    /*
     * public void testFromSNOMED() {
     * 
     * uuid.uuid5(uuid.NAMESPACE_DNS, 'python.org')
     * + UUID('886313e1-3b8a-5372-9b90-0c9aee199e5d');
     * 
     * fail("Not yet implemented");
     * }
     */
}
