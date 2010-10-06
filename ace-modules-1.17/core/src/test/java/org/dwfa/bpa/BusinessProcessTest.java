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
package org.dwfa.bpa;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import junit.framework.TestCase;

import org.dwfa.bpa.protocol.BpaProtocols;

public class BusinessProcessTest extends TestCase {

    String test1 = "this is a ${bpa:test1}";
    String test2 = "this is a ${bpa:test2} ${bpa:test2} ";
    String test3 = "this is a ${bpa:test3a} ${bpa:test3b} ${bpa:test3a} ";

    public void testGetLocators() {
        BusinessProcess bp = new BusinessProcess();
        Set<String> locators = bp.getLocators(test1);
        assertEquals(1, locators.size());
        assertEquals("bpa:test1", locators.iterator().next());

        locators = bp.getLocators(test2);
        assertEquals(1, locators.size());
        assertEquals("bpa:test2", locators.iterator().next());
        assertFalse("bpa:test1".equals(locators.iterator().next()));

        locators = bp.getLocators(test3);
        assertEquals(2, locators.size());
        assertTrue(locators.contains("bpa:test3a"));
        assertTrue(locators.contains("bpa:test3b"));
        assertFalse("bpa:test1".equals(locators.iterator().next()));
    }

    public void testGetObjectFromURL() {
        try {
            BpaProtocols.setupExtraProtocols();
            BusinessProcess bp = new BusinessProcess();
            Set<String> locators = bp.getLocators(test1);
            assertEquals(1, locators.size());
            assertEquals("bpa:test1", locators.iterator().next());
            bp.writeAttachment("test1", "test1 value");
            assertTrue("test1 value".equals(bp.getObjectFromURL(new URL("bpa:test1"))));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }

    public void testSubstituteProperties() {
        try {
            BpaProtocols.setupExtraProtocols();
            BusinessProcess bp = new BusinessProcess();
            Set<String> locators = bp.getLocators(test1);
            assertEquals(1, locators.size());
            assertEquals("bpa:test1", locators.iterator().next());
            bp.writeAttachment("test1", "test1 value");
            assertTrue("test1 value".equals(bp.getObjectFromURL(new URL("bpa:test1"))));
            assertEquals("this is a test1 value", bp.substituteProperties(test1));
            assertEquals("this is a [${bpa:test2} is null] [${bpa:test2} is null] ", bp.substituteProperties(test2));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
}
