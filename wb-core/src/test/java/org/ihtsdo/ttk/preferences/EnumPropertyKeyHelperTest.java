/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.ttk.preferences;

import org.ihtsdo.ttk.preferences.EnumPropertyKeyHelper;
import org.ihtsdo.ttk.preferences.PreferenceWithDefaultEnumBI;
import org.ihtsdo.ttk.queue.QueuePreferences;

import java.util.prefs.Preferences;
import junit.framework.TestCase;

/**
 * Tests of {@link EnumPropertyKeyHelper} class.
 *
 * @author ocarlsen
 */
public class EnumPropertyKeyHelperTest extends TestCase {

    public void testGetKeyStringClass() {
        Class c = TestClassWithEnum.class;
        String keyString = EnumPropertyKeyHelper.getKeyString(c);
        assertEquals("org/ihtsdo/ttk/preferences/TestClassWithEnum", keyString);
    }

    public void testGetKeyStringInnerClass() {
        Class c = TestClassWithEnum.Fields.class;  // Inner class.
        String keyString = EnumPropertyKeyHelper.getKeyString(c);
        assertEquals("org/ihtsdo/ttk/preferences/TestClassWithEnum.Fields", keyString);
    }

    public void testGetKeyStringClassWithLongName() {
        Class c = TestClassWithEnumAndLongLongLongLongLongLongLongLongLongLongName.class;
        String keyString = EnumPropertyKeyHelper.getKeyString(c);
        assertEquals("org/ihtsdo/ttk/preferences/0ba119b9-9af2-36fa-a629-a03004fda2bb", keyString);
        assertTrue(keyString.length() <= Preferences.MAX_KEY_LENGTH);
    }

    public void testGetKeyStringInnerClassWithLongName() {
        Class c = TestClassWithEnumAndLongLongLongLongLongLongLongLongLongLongName.Fields.class;
        String keyString = EnumPropertyKeyHelper.getKeyString(c);
        assertEquals("org/ihtsdo/ttk/preferences/a4ca5edd-20d0-35d5-82eb-ece575681f58", keyString);
        assertTrue(keyString.length() <= Preferences.MAX_KEY_LENGTH);
    }

    public void testGetKeyStringPreferenceWithDefaultEnumBI() {
        try {
            PreferenceWithDefaultEnumBI pref = new TestPreferenceWithDefaultEnumBI("abc");
            String keyString = EnumPropertyKeyHelper.getKeyString(pref);
            fail("Expected ClassCastException to be thrown.");
        } catch (ClassCastException ex) {
            // Expected.
        }
    }

    public void testGetKeyStringEnum() {
        Enum e = TestEnum.JUNK;
        String keyString = EnumPropertyKeyHelper.getKeyString(e);
        assertEquals("org/ihtsdo/ttk/preferences/TestEnum.JUNK", keyString);
    }

    public void testGetKeyStringInnerEnum() {
        Enum e = TestClassWithEnum.Fields.JUNK;
        String keyString = EnumPropertyKeyHelper.getKeyString(e);
        System.out.println(keyString);
        assertEquals("org/ihtsdo/ttk/preferences/TestClassWithEnum.Fields.JUNK", keyString);
    }

    public void testGetKeyStringEnumWithLongName() {
        Enum e = TestClassWithEnumAndLongLongLongLongLongLongLongLongLongLongName.Fields.JUNK;
        String keyString = EnumPropertyKeyHelper.getKeyString(e);
        System.out.println(keyString);
        assertEquals("org/ihtsdo/ttk/preferences/a4ca5edd-20d0-35d5-82eb-ece575681f58.JUNK", keyString);
        assertTrue(keyString.length() <= Preferences.MAX_KEY_LENGTH);
    }
}
