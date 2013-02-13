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

import java.util.prefs.Preferences;
import junit.framework.TestCase;

/**
 * Tests of {@link EnumPropertyKeyHelper} class.
 *
 * @author ocarlsen
 */
public class EnumPropertyKeyHelperTest extends TestCase {

    public void testGetKeyString_Class() {
        Class c = DummyClassWithEnum.class;
        String keyString = EnumPropertyKeyHelper.getKeyString(c);
        assertEquals("org/ihtsdo/ttk/preferences/DummyClassWithEnum", keyString);
    }

    public void testGetKeyString_InnerClass() {
        Class c = DummyClassWithEnum.Fields.class;  // Inner class.
        String keyString = EnumPropertyKeyHelper.getKeyString(c);
        assertEquals("org/ihtsdo/ttk/preferences/DummyClassWithEnum/Fields", keyString);
    }

    public void testGetKeyString_ClassWithLongName() {
        Class c = DummyClassWithEnumAndLongLongLongLongLongLongLongLongLongLongName.class;
        String keyString = EnumPropertyKeyHelper.getKeyString(c);
        assertEquals("org/ihtsdo/ttk/preferences/76d54987-3e59-3400-bd53-d1cb159bdc41", keyString);
        assertTrue(keyString.length() <= Preferences.MAX_KEY_LENGTH);
    }

    public void testGetKeyString_InnerClassWithLongName() {
        Class c = DummyClassWithEnumAndLongLongLongLongLongLongLongLongLongLongName.Fields.class;
        String keyString = EnumPropertyKeyHelper.getKeyString(c);
        assertEquals("org/ihtsdo/ttk/preferences/a4ca5edd-20d0-35d5-82eb-ece575681f58", keyString);
        assertTrue(keyString.length() <= Preferences.MAX_KEY_LENGTH);
    }

    public void testGetKeyString_PreferenceWithDefaultEnumBI() {
        PreferenceWithDefaultEnumBI pref = new DummyPreferenceWithDefaultEnumBI("abc");
        String keyString = EnumPropertyKeyHelper.getKeyString(pref);
        assertEquals("org/ihtsdo/ttk/preferences/DummyPreferenceWithDefaultEnumBI", keyString);
    }

    public void testGetKeyString_Enum() {
        Enum e = DummyEnum.JUNK1;
        String keyString = EnumPropertyKeyHelper.getKeyString(e);
        assertEquals("org/ihtsdo/ttk/preferences/DummyEnum", keyString);
    }

    public void testGetKeyString_InnerEnum() {
        Enum e = DummyClassWithEnum.Fields.JUNK;
        String keyString = EnumPropertyKeyHelper.getKeyString(e);
        assertEquals("org/ihtsdo/ttk/preferences/DummyClassWithEnum/Fields", keyString);
    }

    public void testGetKeyString_EnumWithLongName() {
        Enum e = DummyClassWithEnumAndLongLongLongLongLongLongLongLongLongLongName.Fields.JUNK;
        String keyString = EnumPropertyKeyHelper.getKeyString(e);
        assertEquals("org/ihtsdo/ttk/preferences/a4ca5edd-20d0-35d5-82eb-ece575681f58", keyString);
        assertTrue(keyString.length() <= Preferences.MAX_KEY_LENGTH);
    }
}
