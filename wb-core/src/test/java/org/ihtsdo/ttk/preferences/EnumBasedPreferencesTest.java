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

import static org.dwfa.bpa.util.AppInfoProperties.ARTIFACT_ID;
import static org.dwfa.bpa.util.AppInfoProperties.GROUP_ID;
import static org.dwfa.bpa.util.AppInfoProperties.VERSION;

import org.dwfa.bpa.util.AppInfoProperties;
import org.ihtsdo.ttk.queue.QueueAddress;
import org.ihtsdo.ttk.queue.QueueList;
import org.ihtsdo.ttk.queue.QueuePreferences;
import org.ihtsdo.ttk.queue.QueueType;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import junit.framework.TestCase;

/**
 * Tests of {@link EnumBasedPreferences} class.
 *
 * @author ocarlsen
 */
public class EnumBasedPreferencesTest extends TestCase {

    private static final int STRING_LENGTH = 8;
    
    private EnumBasedPreferences testPrefs;
    private String appPrefix;

    @Override
    public void setUp() throws BackingStoreException {

        // Create random appPrefix.
        // (EnumBasedPreferences.getDefaultAppPrefix is itself tested below.)
        String groupId = getRandomString(STRING_LENGTH);
        String artifactId = getRandomString(STRING_LENGTH);
        String version = getRandomString(STRING_LENGTH);
        String userName = getRandomString(STRING_LENGTH);
        this.appPrefix = EnumBasedPreferences.getDefaultAppPrefix(
                groupId, artifactId, version, userName);

        // Create test EnumBasedPreferences.
        this.testPrefs = new EnumBasedPreferences(appPrefix);
    }

    @Override
    public void tearDown() throws BackingStoreException {

        // Remove from backing store.
        testPrefs.removeNode();
        testPrefs.flush();
    }

    public void testGetDefaultAppPrefix_FromStrings() {
        // Create random arguments.
        String groupId = getRandomString(STRING_LENGTH);
        String artifactId = getRandomString(STRING_LENGTH);
        String version = getRandomString(STRING_LENGTH);
        String userName = getRandomString(STRING_LENGTH);
        
        // Confirm app prefix is calculated as expected.
        String expectedAppPrefix = groupId + ";" + artifactId + ";" + version + ";" + userName;
        assertEquals(expectedAppPrefix, EnumBasedPreferences.getDefaultAppPrefix(
                groupId, artifactId, version, userName));
    }

    public void testGetDefaultAppPrefix_FromProperties() {
        // Create random arguments.
        String groupId = getRandomString(STRING_LENGTH);
        String artifactId = getRandomString(STRING_LENGTH);
        String version = getRandomString(STRING_LENGTH);
        String userName = getRandomString(STRING_LENGTH);

        // Set them on a Properties object.
        Properties appInfoProperties = new Properties();
        appInfoProperties.setProperty(GROUP_ID, groupId);
        appInfoProperties.setProperty(ARTIFACT_ID, artifactId);
        appInfoProperties.setProperty(VERSION, version);
        
        // Confirm app prefix is calculated as expected.
        String expectedAppPrefix = groupId + ";" + artifactId + ";" + version + ";" + userName;
        assertEquals(expectedAppPrefix, EnumBasedPreferences.getDefaultAppPrefix(
                appInfoProperties, userName));
    }
    
    public void testPutGet() {
        PreferenceWithDefaultEnumBI<String> key = new DummyPreferenceWithDefaultEnumBI<>("");
        String value = getRandomString(STRING_LENGTH);
        testPrefs.put(key, value);
        assertEquals(value, testPrefs.get(key));
    }

    public void testPutGet_Boolean() {
        PreferenceWithDefaultEnumBI<Boolean> key = new DummyPreferenceWithDefaultEnumBI<>(false);
        boolean value = true;
        testPrefs.putBoolean(key, value);
        assertEquals(value, testPrefs.getBoolean(key));
    }

    public void testPutGet_ByteArray() {
        byte[] byteArray = new byte[]{0xa,0xb,0xc}; 
        PreferenceWithDefaultEnumBI<byte[]> key = new DummyPreferenceWithDefaultEnumBI<>(byteArray);
        byte[] value = new byte[]{0x1,0x2,0x3} ;
        testPrefs.putByteArray(key, value);
        assertTrue(Arrays.equals(value, testPrefs.getByteArray(key)));
    }

    public void testPutGet_Double() {
        PreferenceWithDefaultEnumBI<Double> key = new DummyPreferenceWithDefaultEnumBI<>(1.0);
        double value = 2.0;
        testPrefs.putDouble(key, value);
        assertEquals(value, testPrefs.getDouble(key));
    }

    public void testPutGet_Enum() {
        PreferenceWithDefaultEnumBI key = new DummyPreferenceWithDefaultEnumBI<>(DummyEnum.JUNK1);
        Enum value = DummyEnum.JUNK2;
        testPrefs.putEnum(key, value);
        assertEquals(value, testPrefs.getEnum(key));
    }

    public void testPutGet_Float() {
        PreferenceWithDefaultEnumBI<Float> key = new DummyPreferenceWithDefaultEnumBI<>(1.0f);
        float value = 2.0f;
        testPrefs.putFloat(key, value);
        assertEquals(value, testPrefs.getFloat(key));
    }

    public void testPutGet_Int() {
        PreferenceWithDefaultEnumBI<Integer> key = new DummyPreferenceWithDefaultEnumBI<>(1);
        int value = 2;
        testPrefs.putInt(key, value);
        assertEquals(value, testPrefs.getInt(key));
    }

    public void testPutGet_Long() {
        PreferenceWithDefaultEnumBI<Long> key = new DummyPreferenceWithDefaultEnumBI<>(3l);
        long value = System.currentTimeMillis();
        testPrefs.putLong(key, value);
        assertEquals(value, testPrefs.getLong(key));
    }

    public void testQueueListSettingsPreserved() throws BackingStoreException, IOException {

        // Construct a default QueuePreferences object.
        QueuePreferences qp0 = new QueuePreferences();

        // Construct a custom QueuePreferences object with some service item properties.
        final String queueDisplayName = "some other display";
        final String queueId = getRandomString(STRING_LENGTH);
        final File queueDirectory = new File("some other location");
        final Boolean readInsteadOfTake = Boolean.TRUE;
        final QueueType queueType = new QueueType(QueueType.Types.OUTBOX);
        QueuePreferences qp1 = new QueuePreferences(queueDisplayName, queueId, queueDirectory,
                readInsteadOfTake, queueType);
        final QueueAddress queueAddress = new QueueAddress("qp2 address");
        qp1.getServiceItemProperties().add(queueAddress);

        // Add QueuePreferences to a QueueList.
        QueueList queueList = new QueueList();
        queueList.getQueuePreferences().add(qp0);
        queueList.getQueuePreferences().add(qp1);

        // Export to preferences.
        queueList.exportFields(testPrefs);

        // DEBUG ONLY!
        //testPrefs.exportSubtree(new FileOutputStream("Prefs.xml"));

        // Bring preferences up-to-date.
        testPrefs.sync();

        // Now look up the QueueList and confirm its QueuePreferences were saved.
        QueueList resultQueueList = new QueueList(testPrefs);
        List<QueuePreferences> resultQueuePreferences = resultQueueList.getQueuePreferences();
        assertEquals(2, resultQueuePreferences.size());

        // Confirm default settings for QueuePreferences #0 were preserved.
        QueuePreferences resultQP0 = resultQueuePreferences.get(0);
        assertEquals(QueuePreferences.Fields.DISPLAY_NAME.getDefaultValue(),
                resultQP0.getDisplayName());
        assertEquals(new File((String) QueuePreferences.Fields.QUEUE_DIRECTORY.getDefaultValue()),
                resultQP0.getQueueDirectory());
        assertEquals(QueuePreferences.Fields.READ_INSTEAD_OF_TAKE.getDefaultValue(),
                resultQP0.getReadInsteadOfTake());
        assertEquals(QueuePreferences.Fields.QUEUE_INSTANCE_PROPERTIES_LIST.getDefaultValue(),
                resultQP0.getServiceItemProperties().size());

        // Confirm custom settings for QueuePreferences #1 were preserved.
        QueuePreferences resultQP1 = resultQueuePreferences.get(1);
        assertEquals(queueDisplayName, resultQP1.getDisplayName());
        assertEquals(queueId, resultQP1.getId());
        assertEquals(queueDirectory, resultQP1.getQueueDirectory());
        assertEquals(readInsteadOfTake, resultQP1.getReadInsteadOfTake());

        // Confirm custom service item properties were preserved.
        List<PreferenceObject> serviceItemProperties = resultQP1.getServiceItemProperties();
        assertEquals(2, serviceItemProperties.size());
        QueueType resultQueueType = (QueueType) serviceItemProperties.get(0);
        assertEquals(queueType, resultQueueType);
        QueueAddress resultQueueAddress = (QueueAddress) serviceItemProperties.get(1);
        assertEquals(queueAddress, resultQueueAddress);
    }

    /**
     * @param length
     * @return A random String of length {@code length} generated from random UUIDs.
     */
    private static String getRandomString(int length) {
        String uuidStr = UUID.randomUUID().toString();
        long seed = System.currentTimeMillis();
        int startIndex = (int) (seed % (uuidStr.length() - length));
        int endIndex = startIndex + length;
        return uuidStr.substring(startIndex, endIndex);
    }
}
