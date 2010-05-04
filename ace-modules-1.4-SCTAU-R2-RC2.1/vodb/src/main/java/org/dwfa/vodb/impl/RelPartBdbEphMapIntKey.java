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
import java.util.HashMap;
import java.util.Set;

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.I_StoreInBdb;
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
import com.sleepycat.je.PreloadConfig;

/**
 * Use hash maps generated ephemerally instead of using a secondary database.
 * 
 * @author kec
 * 
 */
public class RelPartBdbEphMapIntKey implements I_StoreInBdb, I_StoreRelParts<Integer> {

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

    private RelPartVersionedBinding relPartBinding = new RelPartVersionedBinding();
    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    private Database relPartDb;
    private PartIdGenerator partIdGenerator;
    private HashMap<I_RelPart, Integer> partIdMap = new HashMap<I_RelPart, Integer>();
    private HashMap<Integer, I_RelPart> idPartMap = new HashMap<Integer, I_RelPart>();

    public RelPartBdbEphMapIntKey(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
        super();

        relPartDb = env.openDatabase(null, "relPartDb", dbConfig);
        PreloadConfig preloadConfig = new PreloadConfig();
        preloadConfig.setLoadLNs(true);
        relPartDb.preload(preloadConfig);
        Cursor partCursor = relPartDb.openCursor(null, null);
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        while (partCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            int relPartId = (Integer) intBinder.entryToObject(foundKey);
            I_RelPart relPart = (I_RelPart) relPartBinding.entryToObject(foundData);
            partIdMap.put(relPart, relPartId);
            idPartMap.put(relPartId, relPart);
        }
        partCursor.close();
        AceLog.getAppLog().info("rel part map size: " + partIdMap.size());
        partIdGenerator = new PartIdGenerator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPartId(org.dwfa.ace.api
     * .I_RelPart)
     */
    public Integer getRelPartId(I_RelPart relPart) throws DatabaseException {
        assert relPart.getTypeId() != Integer.MAX_VALUE;
        if (partIdMap.containsKey(relPart)) {
            return partIdMap.get(relPart);
        }
        synchronized (partIdMap) {
            int relPartId = partIdGenerator.nextId();
            DatabaseEntry partKey = new DatabaseEntry();
            DatabaseEntry partValue = new DatabaseEntry();
            intBinder.objectToEntry((Integer) relPartId, partKey);
            relPartBinding.objectToEntry(relPart, partValue);
            relPartDb.put(BdbEnv.transaction, partKey, partValue);
            partIdMap.put(relPart, relPartId);
            idPartMap.put(relPartId, relPart);
            if (partIdMap.size() % 250 == 0) {
                AceLog.getAppLog().info("rel part map size now: " + partIdMap.size());
            }
            // AceLog.getAppLog().info("Writing part id: " + newPartId + " " +
            // part);
            return relPartId;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPart(int)
     */
    public I_RelPart getRelPart(Integer partId) throws DatabaseException {
        if (idPartMap.containsKey(partId)) {
            I_RelPart part = idPartMap.get(idPartMap);
            assert part.getTypeId() != Integer.MAX_VALUE;
            return part;
        }
        throw new DatabaseException("Rel part: " + partId + " not found.");
    }

    public void close() throws DatabaseException {
        relPartDb.close();
    }

    public void sync() throws DatabaseException {
        relPartDb.sync();
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException, IOException {
        // nothing to do...

    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

}
