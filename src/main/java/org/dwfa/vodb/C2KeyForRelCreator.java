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
package org.dwfa.vodb;

import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.vodb.bind.ThinRelVersionedBinding;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class C2KeyForRelCreator implements SecondaryKeyCreator {
    ThinRelVersionedBinding relBinding;
    EntryBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    public static class RelAndC2Id {
        int relId;
        int c2Id;

        public RelAndC2Id() {
            super();
        }

        public RelAndC2Id(int c2Id, int relId) {
            super();
            this.relId = relId;
            this.c2Id = c2Id;
        }

        public int getC2Id() {
            return c2Id;
        }

        public void setC2Id(int c2Id) {
            this.c2Id = c2Id;
        }

        public int getRelId() {
            return relId;
        }

        public void setRelId(int relId) {
            this.relId = relId;
        }
    }

    public static class RelAndC2IdBinding extends TupleBinding {

        public RelAndC2Id entryToObject(TupleInput ti) {
            return new RelAndC2Id(ti.readInt(), ti.readInt());
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            RelAndC2Id id = (RelAndC2Id) obj;
            to.writeInt(id.getC2Id());
            to.writeInt(id.getRelId());
        }

    }

    RelAndC2Id relAndC2Id = new RelAndC2Id();
    RelAndC2IdBinding relAndC2IdBinding = new RelAndC2IdBinding();

    public C2KeyForRelCreator(ThinRelVersionedBinding binding) {
        super();
        this.relBinding = binding;
    }

    public synchronized boolean createSecondaryKey(SecondaryDatabase secDb, DatabaseEntry keyEntry,
            DatabaseEntry dataEntry, DatabaseEntry resultEntry) throws DatabaseException {
        I_RelVersioned rel = (I_RelVersioned) relBinding.entryToObject(dataEntry);
        relAndC2Id.setC2Id(rel.getC2Id());
        relAndC2Id.setRelId(rel.getRelId());
        relAndC2IdBinding.objectToEntry(relAndC2Id, resultEntry);
        return true;
    }

    public synchronized boolean createSecondaryKey(int relId, int concId, DatabaseEntry resultEntry)
            throws DatabaseException {
        relAndC2Id.setC2Id(concId);
        relAndC2Id.setRelId(relId);
        relAndC2IdBinding.objectToEntry(relAndC2Id, resultEntry);
        return true;
    }

}
