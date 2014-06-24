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

import org.ihtsdo.ttk.preferences.EnumBasedPreferences;
import org.ihtsdo.ttk.preferences.PreferenceObject;
import org.ihtsdo.ttk.preferences.PreferenceWithDefaultEnumBI;

/**
 *
 * @author kec
 */
public class QueueType implements PreferenceObject {
    enum Fields implements PreferenceWithDefaultEnumBI<Types> {
        QUEUE_TYPE;

        @Override
        public Types getDefaultValue() {
            return Types.INBOX;
        }
    }
    
    public enum Types { INBOX, OUTBOX };
    
    private Types type;
    
    public QueueType(EnumBasedPreferences preferences) {
        type = (Types) preferences.getEnum(Fields.QUEUE_TYPE);
    }
    
    public QueueType(Types type) {
        this.type = type;
    }
    
    public QueueType() {
        this.type = Fields.QUEUE_TYPE.getDefaultValue();
    }
    
    @Override
    public void exportFields(EnumBasedPreferences preferences) {
        preferences.putEnum(Fields.QUEUE_TYPE, type);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.type != null ? this.type.hashCode() : 0);
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
        final QueueType other = (QueueType) obj;
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "QueueType: " + type;
    }
}
