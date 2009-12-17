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

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class ConceptIdKeyForDescCreator implements SecondaryKeyCreator {
    ThinDescVersionedBinding descBinding;
    EntryBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    public static class DescAndConceptId {
        int descId;
        int conId;

        public DescAndConceptId() {
            super();
        }

        public DescAndConceptId(int conId, int descId) {
            super();
            this.descId = descId;
            this.conId = conId;
        }

        public int getConId() {
            return conId;
        }

        public void setConId(int conId) {
            this.conId = conId;
        }

        public int getDescId() {
            return descId;
        }

        public void setDescId(int descId) {
            this.descId = descId;
        }
    }

    public static class DescAndConceptIdBinding extends TupleBinding {

        public DescAndConceptId entryToObject(TupleInput ti) {
            return new DescAndConceptId(ti.readInt(), ti.readInt());
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            DescAndConceptId id = (DescAndConceptId) obj;
            to.writeInt(id.getConId());
            to.writeInt(id.getDescId());
        }

    }

    DescAndConceptId descAndConceptId = new DescAndConceptId();
    DescAndConceptIdBinding conceptIdKeyBinding = new DescAndConceptIdBinding();

    public ConceptIdKeyForDescCreator(ThinDescVersionedBinding binding) {
        super();
        this.descBinding = binding;
    }

    public synchronized boolean createSecondaryKey(SecondaryDatabase secDb, DatabaseEntry keyEntry,
            DatabaseEntry dataEntry, DatabaseEntry resultEntry) throws DatabaseException {
        I_DescriptionVersioned desc = (I_DescriptionVersioned) descBinding.entryToObject(dataEntry);
        int descId = (Integer) intBinder.entryToObject(keyEntry);
        descAndConceptId.setConId(desc.getConceptId());
        descAndConceptId.setDescId(descId);
        conceptIdKeyBinding.objectToEntry(descAndConceptId, resultEntry);
        return true;
    }

    public synchronized boolean createSecondaryKey(int descId, int concId, DatabaseEntry resultEntry)
            throws DatabaseException {
        descAndConceptId.setConId(concId);
        descAndConceptId.setDescId(descId);
        conceptIdKeyBinding.objectToEntry(descAndConceptId, resultEntry);
        return true;
    }

}
