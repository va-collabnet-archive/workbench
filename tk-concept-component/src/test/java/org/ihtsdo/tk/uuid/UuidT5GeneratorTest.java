/*
 *  Copyright 2010 kec.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ihtsdo.tk.uuid;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kec
 */
public class UuidT5GeneratorTest {

    public UuidT5GeneratorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of get method, of class UuidT5Generator.
     */
    @Test
    public void testGet_UUID_String() throws Exception {
        try {
            UUID nameSpace_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
            UUID testId = UUID.fromString("886313e1-3b8a-5372-9b90-0c9aee199e5d");
            UUID generatedId = UuidT5Generator.get(nameSpace_DNS, "python.org");

            assertEquals(testId, generatedId);
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        }
    }
}