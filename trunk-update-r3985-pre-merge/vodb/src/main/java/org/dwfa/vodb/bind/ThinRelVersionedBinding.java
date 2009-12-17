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

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinRelVersionedBinding extends TupleBinding {

    public ThinRelVersioned entryToObject(TupleInput ti) {
        int relId = ti.readInt();
        int c1id = ti.readInt();
        int c2id = ti.readInt();
        int size = ti.readInt();
        ThinRelVersioned versioned = new ThinRelVersioned(relId, c1id, c2id, size);
        for (int x = 0; x < size; x++) {
            ThinRelPart rel = new ThinRelPart();
            rel.setPathId(ti.readInt());
            rel.setVersion(ti.readInt());
            rel.setStatusId(ti.readInt());
            rel.setCharacteristicId(ti.readInt());
            rel.setGroup(ti.readInt());
            rel.setRefinabilityId(ti.readInt());
            rel.setRelTypeId(ti.readInt());
            versioned.addVersionNoRedundancyCheck(rel);
        }
        return versioned;
    }

    public void objectToEntry(Object obj, TupleOutput to) {
        I_RelVersioned versioned = (I_RelVersioned) obj;
        to.writeInt(versioned.getRelId());
        to.writeInt(versioned.getC1Id());
        to.writeInt(versioned.getC2Id());
        to.writeInt(versioned.versionCount());
        for (I_RelPart rel : versioned.getVersions()) {
            to.writeInt(rel.getPathId());
            to.writeInt(rel.getVersion());
            to.writeInt(rel.getStatusId());
            to.writeInt(rel.getCharacteristicId());
            to.writeInt(rel.getGroup());
            to.writeInt(rel.getRefinabilityId());
            to.writeInt(rel.getRelTypeId());
        }
    }
}
