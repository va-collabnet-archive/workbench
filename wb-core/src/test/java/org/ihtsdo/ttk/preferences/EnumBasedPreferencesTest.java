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

import org.ihtsdo.ttk.queue.QueueAddress;
import org.ihtsdo.ttk.queue.QueueList;
import org.ihtsdo.ttk.queue.QueuePreferences;
import org.ihtsdo.ttk.queue.QueueType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import junit.framework.TestCase;

/**
 * Tests of {@link EnumBasedPreferences} class.
 *
 * @author ocarlsen
 */
public class EnumBasedPreferencesTest extends TestCase {

    private EnumBasedPreferences testPrefs;
    private String appPrefix;

    @Override
    public void setUp() throws BackingStoreException {

        // Create EnumBasedPreferences with random appPrefix.
        String version = ((Long) System.currentTimeMillis()).toString();
        String userName = UUID.randomUUID().toString();
        this.appPrefix = version + ":" + userName;

        this.testPrefs = new EnumBasedPreferences(appPrefix);
    }

    @Override
    public void tearDown() throws BackingStoreException {

        // Remove from backing store.
        testPrefs.removeNode();
        testPrefs.flush();
    }

    public void testQueueListSettingsPreserved() throws BackingStoreException, IOException {

        // Construct a default QueuePreferences object.
        QueuePreferences qp0 = new QueuePreferences();

        // Construct a custom QueuePreferences object with some service item properties.
        final String queueDisplayName = "some other display";
        final String queueId = UUID.randomUUID().toString();
        final File queueDirectory = new File("some other location");
        final Boolean readInsteadOfTake = Boolean.TRUE;
        final QueueType queueType = new QueueType(QueueType.Types.OUTBOX);
        QueuePreferences qp1 = new QueuePreferences(queueDisplayName, queueId, queueDirectory,
                readInsteadOfTake, queueType);
        final String queueAddress = "qp2 address";
        qp1.getServiceItemProperties().add(new QueueAddress(queueAddress));

        // Add QueuePreferences to a QueueList.
        QueueList queueList = new QueueList();
        queueList.getQueueList().add(qp0);
        queueList.getQueueList().add(qp1);

        // Export to preferences.
        queueList.exportFields(testPrefs);

        // DEBUG ONLY!
        //testPrefs.exportSubtree(new FileOutputStream("Prefs.xml"));

        // Bring preferences up-to-date.
        testPrefs.sync();

        // Now look up the QueueList and confirm its QueuePreferences were saved.
        QueueList resultQueueList = new QueueList(testPrefs);
        List<QueuePreferences> resultQueuePreferences = resultQueueList.getQueueList();
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
        assertEquals(2, resultQP1.getServiceItemProperties().size());

        // Confirm custom service item properties were preserved.
        List<PreferenceObject> serviceItemProperties = resultQP1.getServiceItemProperties();
        assertEquals(2, serviceItemProperties.size());
        QueueType resultQueueType = (QueueType) serviceItemProperties.get(0);
        assertEquals(queueType, resultQueueType);
    }
}
