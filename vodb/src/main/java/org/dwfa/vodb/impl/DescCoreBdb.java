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

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinDescPartCore;

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

public class DescCoreBdb implements I_StoreInBdb {

    private class PartIdGenerator {
        private int lastId = Integer.MIN_VALUE;

        private PartIdGenerator() throws DatabaseException {
            Cursor idCursor = descPartDb.openCursor(null, null);
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

    public class ThinDescPartCoreBinding extends TupleBinding {

        public ThinDescPartCore entryToObject(TupleInput ti) {
            ThinDescPartCore descPartCore = new ThinDescPartCore();
            descPartCore.setPathId(ti.readInt());
            descPartCore.setVersion(ti.readInt());
            descPartCore.setStatusId(ti.readInt());
            descPartCore.setInitialCaseSignificant(ti.readBoolean());
            descPartCore.setLang(ti.readString());
            descPartCore.setTypeId(ti.readInt());
            return descPartCore;
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            ThinDescPartCore descPartCore = (ThinDescPartCore) obj;
            to.writeInt(descPartCore.getPathId());
            to.writeInt(descPartCore.getVersion());
            to.writeInt(descPartCore.getStatusId());
            to.writeBoolean(descPartCore.getInitialCaseSignificant());
            to.writeString(descPartCore.getLang());
            to.writeInt(descPartCore.getTypeId());
        }

    }

    private ThinDescPartCoreBinding descPartCoreBinding = new ThinDescPartCoreBinding();
    private Database descPartDb;
    private PartIdGenerator partIdGenerator;
    private HashMap<ThinDescPartCore, Integer> partIdMap = new HashMap<ThinDescPartCore, Integer>();
    private HashMap<Integer, ThinDescPartCore> conPartMap = new HashMap<Integer, ThinDescPartCore>();
    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    public DescCoreBdb(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
        super();
        descPartDb = env.openDatabase(null, "descPartDb", dbConfig);

        Cursor partCursor = descPartDb.openCursor(null, null);
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        while (partCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            int descPartCoreId = (Integer) intBinder.entryToObject(foundKey);
            ThinDescPartCore descPartCore = (ThinDescPartCore) descPartCoreBinding.entryToObject(foundData);
            partIdMap.put(descPartCore, descPartCoreId);
            conPartMap.put(descPartCoreId, descPartCore);
        }
        partCursor.close();
        AceLog.getAppLog().info("desc part core map size: " + partIdMap.size());
        partIdGenerator = new PartIdGenerator();
    }

    public int getDescPartCoreId(I_DescriptionPart descPart) throws DatabaseException {
        ThinDescPartCore core = new ThinDescPartCore();
        core.setInitialCaseSignificant(descPart.getInitialCaseSignificant());
        core.setLang(descPart.getLang());
        core.setPathId(descPart.getPathId());
        core.setStatusId(descPart.getStatusId());
        core.setTypeId(descPart.getTypeId());
        core.setVersion(descPart.getVersion());
        return getDescPartCoreId(core);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPartId(org.dwfa.ace.api
     * .I_RelPart)
     */
    public int getDescPartCoreId(ThinDescPartCore descPartCore) throws DatabaseException {
        if (partIdMap.containsKey(descPartCore)) {
            return partIdMap.get(descPartCore);
        }
        synchronized (partIdMap) {
            int descPartCoreId = partIdGenerator.nextId();
            DatabaseEntry partKey = new DatabaseEntry();
            DatabaseEntry partValue = new DatabaseEntry();
            intBinder.objectToEntry((Integer) descPartCoreId, partKey);
            descPartCoreBinding.objectToEntry(descPartCore, partValue);
            descPartDb.put(BdbEnv.transaction, partKey, partValue);
            partIdMap.put(descPartCore, descPartCoreId);
            conPartMap.put(descPartCoreId, descPartCore);
            if (partIdMap.size() % 100 == 0) {
                AceLog.getAppLog().info("desc core part map size now: " + partIdMap.size());
            }
            // AceLog.getAppLog().info("Writing part id: " + newPartId + " " +
            // part);
            return descPartCoreId;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPart(int)
     */
    public ThinDescPartCore getDescPartCore(int partId) throws DatabaseException {
        if (conPartMap.containsKey(partId)) {
            return conPartMap.get(partId);
        }
        throw new DatabaseException("Desc part core: " + partId + " not found.");
    }

    public void close() throws DatabaseException {
        descPartDb.close();
    }

    public void sync() throws DatabaseException {
        descPartDb.sync();
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException {
        // nothing to do...

    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

}
