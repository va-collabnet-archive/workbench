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
package org.dwfa.vodb.bind;

import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinIdVersionedBinding extends TupleBinding {

    private static final byte INTEGER_ID = 1;
    private static final byte LONG_ID = 2;
    private static final byte UUID_ID = 3;
    private static final byte STRING_ID = 4;

    public I_IdVersioned entryToObject(TupleInput ti) {
        int nativeId = ti.readInt();
        int size = ti.readInt();
        I_IdVersioned versioned = new ThinIdVersioned(nativeId, size);
        for (int x = 0; x < size; x++) {
            ThinIdPart id = new ThinIdPart();
            id.setPathId(ti.readInt());
            id.setVersion(ti.readInt());
            id.setIdStatus(ti.readInt());
            id.setSource(ti.readInt());
            byte idType = ti.readByte();
            switch (idType) {
            case INTEGER_ID:
                id.setSourceId(ti.readInt());
                break;
            case LONG_ID:
                id.setSourceId(ti.readLong());
                break;
            case UUID_ID:
                id.setSourceId(new UUID(ti.readLong(), ti.readLong()));
                break;
            case STRING_ID:
                id.setSourceId(ti.readString());
                break;
            }
            versioned.addVersion(id);
        }
        return versioned;
    }

    public void objectToEntry(Object obj, TupleOutput to) {
        I_IdVersioned versioned = (I_IdVersioned) obj;
        to.writeInt(versioned.getNativeId());
        to.writeInt(versioned.getVersions().size());
        for (I_IdPart id : versioned.getVersions()) {
            to.writeInt(id.getPathId());
            to.writeInt(id.getVersion());
            to.writeInt(id.getIdStatus());
            to.writeInt(id.getSource());
            if (Integer.class.isAssignableFrom(id.getSourceId().getClass())) {
                to.writeByte(INTEGER_ID);
                Integer sourceId = (Integer) id.getSourceId();
                to.writeInt(sourceId);
            } else if (Long.class.isAssignableFrom(id.getSourceId().getClass())) {
                to.writeByte(LONG_ID);
                Long sourceId = (Long) id.getSourceId();
                to.writeLong(sourceId);
            } else if (UUID.class.isAssignableFrom(id.getSourceId().getClass())) {
                to.writeByte(UUID_ID);
                UUID sourceId = (UUID) id.getSourceId();
                to.writeLong(sourceId.getMostSignificantBits());
                to.writeLong(sourceId.getLeastSignificantBits());
            } else if (String.class.isAssignableFrom(id.getSourceId().getClass())) {
                to.writeByte(STRING_ID);
                String sourceId = (String) id.getSourceId();
                to.writeString(sourceId);
            }
        }
    }
}
