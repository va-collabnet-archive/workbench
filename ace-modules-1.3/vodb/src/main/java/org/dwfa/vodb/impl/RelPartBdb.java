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
package org.dwfa.vodb.impl;

import java.io.IOException;
import java.util.Set;

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinRelPart;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class RelPartBdb implements I_StoreInBdb, I_StoreRelParts<Integer> {

    private class PartIdGenerator {
        private int lastId = Integer.MIN_VALUE;

        private PartIdGenerator() throws DatabaseException {
            Cursor idCursor = relPartDb.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            lastId = Integer.MIN_VALUE;
            if (idCursor.getPrev(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                lastId = (Integer) intBinder.entryToObject(foundKey);
            }
            idCursor.close();
        }

        public synchronized int nextId() {
            lastId++;
            return lastId;
        }
    }

    private static class RelPartVersionedBinding extends TupleBinding {

        public ThinRelPart entryToObject(TupleInput ti) {
            ThinRelPart part = new ThinRelPart();
            part.setPathId(ti.readInt());
            part.setVersion(ti.readInt());
            part.setStatusId(ti.readInt());
            part.setCharacteristicId(ti.readInt());
            part.setGroup(ti.readInt());
            part.setRefinabilityId(ti.readInt());
            part.setRelTypeId(ti.readInt());
            return part;
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            ThinRelPart part = (ThinRelPart) obj;
            to.writeInt(part.getPathId());
            to.writeInt(part.getVersion());
            to.writeInt(part.getStatusId());
            to.writeInt(part.getCharacteristicId());
            to.writeInt(part.getGroup());
            to.writeInt(part.getRefinabilityId());
            to.writeInt(part.getRelTypeId());
        }

    }

    public class RelPartKeyCreator implements SecondaryKeyCreator {

        public boolean createSecondaryKey(SecondaryDatabase secDb, DatabaseEntry keyEntry, DatabaseEntry dataEntry,
                DatabaseEntry resultEntry) throws DatabaseException {
            I_RelPart part = (I_RelPart) relPartBinding.entryToObject(dataEntry);
            relPartBinding.objectToEntry(part, resultEntry);
            return true;
        }

    }

    private RelPartVersionedBinding relPartBinding = new RelPartVersionedBinding();
    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    private Database relPartDb;
    private SecondaryDatabase relPartMapDb;
    private PartIdGenerator partIdGenerator;

    public RelPartBdb(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
        super();
        relPartDb = env.openDatabase(null, "relPartDb", dbConfig);

        RelPartKeyCreator relPartKeyCreator = new RelPartKeyCreator();
        SecondaryConfig relPartConfig = new SecondaryConfig();
        relPartConfig.setReadOnly(VodbEnv.isReadOnly());
        relPartConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
        relPartConfig.setAllowCreate(!VodbEnv.isReadOnly());
        relPartConfig.setSortedDuplicates(false);
        relPartConfig.setKeyCreator(relPartKeyCreator);
        relPartConfig.setAllowPopulate(true);
        relPartConfig.setTransactional(VodbEnv.isTransactional());
        relPartMapDb = env.openSecondaryDatabase(null, "relPartMapDb", relPartDb, relPartConfig);
        partIdGenerator = new PartIdGenerator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPartId(org.dwfa.ace.api
     * .I_RelPart)
     */
    public Integer getRelPartId(I_RelPart part) throws DatabaseException {
        DatabaseEntry partKey = new DatabaseEntry();
        DatabaseEntry primaryKey = new DatabaseEntry();
        DatabaseEntry partValue = new DatabaseEntry();
        relPartBinding.objectToEntry(part, partKey);
        if (relPartMapDb.get(BdbEnv.transaction, partKey, primaryKey, partValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {

            int partId = (Integer) intBinder.entryToObject(primaryKey);
            // AceLog.getAppLog().info("returning part id: " + partId + " for:"
            // + part);
            return partId;
        }
        int newPartId = partIdGenerator.nextId();
        intBinder.objectToEntry((Integer) newPartId, partKey);
        relPartBinding.objectToEntry(part, partValue);
        relPartDb.put(BdbEnv.transaction, partKey, partValue);
        // AceLog.getAppLog().info("Writing part id: " + newPartId + " " +
        // part);
        return newPartId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPart(int)
     */
    public I_RelPart getRelPart(Integer partId) throws DatabaseException {
        DatabaseEntry partKey = new DatabaseEntry();
        DatabaseEntry partValue = new DatabaseEntry();
        intBinder.objectToEntry(partId, partKey);
        if (relPartDb.get(BdbEnv.transaction, partKey, partValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return (I_RelPart) relPartBinding.entryToObject(partValue);
        }
        throw new DatabaseException("Rel part: " + partId + " not found.");
    }

    public void close() throws DatabaseException {
        relPartDb.close();
        relPartMapDb.close();
    }

    public void sync() throws DatabaseException {
        relPartDb.sync();
        relPartMapDb.sync();
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException, IOException {
        // nothing to do

    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

}
