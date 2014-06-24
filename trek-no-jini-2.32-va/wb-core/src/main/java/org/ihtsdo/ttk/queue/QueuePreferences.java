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



package org.ihtsdo.ttk.queue;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.ttk.preferences.EnumBasedPreferences;
import org.ihtsdo.ttk.preferences.PreferenceObject;
import org.ihtsdo.ttk.preferences.PreferenceWithDefaultEnumBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class QueuePreferences implements Comparable<QueuePreferences>, PreferenceObject {
    List<PreferenceObject> serviceItemProperties = new ArrayList<>();
    File                   queueDirectory        = new File(Fields.QUEUE_DIRECTORY.getDefaultValue().toString());
    Boolean                readInsteadOfTake     = (Boolean) Fields.READ_INSTEAD_OF_TAKE.getDefaultValue();
    String                 id                    = Fields.ID.getDefaultValue().toString();
    String                 displayName           = Fields.DISPLAY_NAME.getDefaultValue().toString();

    public QueuePreferences() {}

    public QueuePreferences(EnumBasedPreferences preferences) {
        this.queueDirectory        = new File(preferences.get(Fields.QUEUE_DIRECTORY));
        this.serviceItemProperties =
            (List<PreferenceObject>) preferences.getList(Fields.QUEUE_INSTANCE_PROPERTIES_LIST);
        this.readInsteadOfTake = preferences.getBoolean(Fields.READ_INSTEAD_OF_TAKE);
        this.id = preferences.get(Fields.ID);
        this.displayName = preferences.get(Fields.DISPLAY_NAME);
    }

    public QueuePreferences(String displayName, String id, File queueDirectory,
            Boolean readInsteadOfTake, QueueType queueType) {
        this.displayName    = displayName;
        this.id             = id;
        this.queueDirectory = queueDirectory;
        this.readInsteadOfTake = readInsteadOfTake;
        this.serviceItemProperties.add(queueType);
    }

    public enum Fields implements PreferenceWithDefaultEnumBI<Object> {
        DISPLAY_NAME("unnamed queue"), ID("00000000-0000-0000-c000-000000000046"), QUEUE_DIRECTORY("queues/queue.inbox"),
        QUEUE_INSTANCE_PROPERTIES_LIST(0), READ_INSTEAD_OF_TAKE(Boolean.FALSE);

        final Object defaultValue;

        private Fields(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object getDefaultValue() {
            if (this == ID) {
                return UUID.randomUUID().toString();
            }

            return defaultValue;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getReadInsteadOfTake() {
        return readInsteadOfTake;
    }

    public void setReadInsteadOfTake(Boolean readInsteadOfTake) {
        this.readInsteadOfTake = readInsteadOfTake;
    }

    @Override
    public void exportFields(EnumBasedPreferences preferences) {
        preferences.put(Fields.DISPLAY_NAME, displayName);
        preferences.put(Fields.ID, id);
        String path = queueDirectory.getPath();
        int indexOf = path.indexOf("queues");
        String relativePath = path.substring(indexOf);
        preferences.put(Fields.QUEUE_DIRECTORY, relativePath);
        preferences.putList(Fields.QUEUE_INSTANCE_PROPERTIES_LIST, serviceItemProperties);
        preferences.putBoolean(Fields.READ_INSTEAD_OF_TAKE, readInsteadOfTake);
    }

    @Override
    public int compareTo(QueuePreferences o) {
        return this.queueDirectory.compareTo(o.queueDirectory);
    }

    @Override
    public String toString() {
        return "QueuePreferences{" + "serviceItemProperties=" + serviceItemProperties + ", queueDirectory="
               + queueDirectory + '}';
    }

    public List<PreferenceObject> getServiceItemProperties() {
        return serviceItemProperties;
    }

    public File getQueueDirectory() {
        return queueDirectory;
    }

    public void setQueueDirectory(File queueDirectory) {
        this.queueDirectory = queueDirectory;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 47 * hash + Objects.hashCode(this.queueDirectory);

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final QueuePreferences other = (QueuePreferences) obj;

        if (!Objects.equals(this.queueDirectory, other.queueDirectory)) {
            return false;
        }

        return true;
    }
}
