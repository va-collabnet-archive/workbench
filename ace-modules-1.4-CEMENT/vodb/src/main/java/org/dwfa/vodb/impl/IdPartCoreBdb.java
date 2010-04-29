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

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinIdPartCore;

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

public class IdPartCoreBdb implements I_StoreInBdb {

    private class PartIdGenerator {
        private short lastId = Short.MIN_VALUE;

        private PartIdGenerator() throws DatabaseException {
            Cursor idCursor = idPartDb.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            lastId = Short.MIN_VALUE;
            if (idCursor.getPrev(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                lastId = (Short) shortBinder.entryToObject(foundKey);
            }
            idCursor.close();
        }

        public synchronized short nextId() {
            lastId++;
            return lastId;
        }
    }

    public static class ThinIdCoreBinding extends TupleBinding {

        public ThinIdPartCore entryToObject(TupleInput ti) {
            ThinIdPartCore id = new ThinIdPartCore();
            id.setPathId(ti.readInt());
            id.setVersion(ti.readInt());
            id.setIdStatus(ti.readInt());
            id.setSource(ti.readInt());
            return id;
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            ThinIdPartCore idPartCore = (ThinIdPartCore) obj;
            to.writeInt(idPartCore.getPathId());
            to.writeInt(idPartCore.getVersion());
            to.writeInt(idPartCore.getIdStatus());
            to.writeInt(idPartCore.getSource());
        }
    }

    private ThinIdCoreBinding idCoreBinding = new ThinIdCoreBinding();
    private TupleBinding shortBinder = TupleBinding.getPrimitiveBinding(Short.class);

    private Database idPartDb;
    private PartIdGenerator partIdGenerator;
    private HashMap<ThinIdPartCore, Short> partIdMap = new HashMap<ThinIdPartCore, Short>();
    private HashMap<Short, ThinIdPartCore> idPartMap = new HashMap<Short, ThinIdPartCore>();

    public IdPartCoreBdb(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
        super();
        idPartDb = env.openDatabase(null, "idPartDb", dbConfig);

        Cursor partCursor = idPartDb.openCursor(null, null);
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        while (partCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            short conAttrPartId = (Short) shortBinder.entryToObject(foundKey);
            ThinIdPartCore conAttrPart = (ThinIdPartCore) idCoreBinding.entryToObject(foundData);
            partIdMap.put(conAttrPart, conAttrPartId);
            idPartMap.put(conAttrPartId, conAttrPart);
        }
        partCursor.close();
        AceLog.getAppLog().info("id part map size: " + partIdMap.size());
        partIdGenerator = new PartIdGenerator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPartId(org.dwfa.ace.api
     * .I_RelPart)
     */
    public short getIdPartCoreId(ThinIdPartCore idPartCore) throws DatabaseException {
        if (partIdMap.containsKey(idPartCore)) {
            return partIdMap.get(idPartCore);
        }
        synchronized (partIdMap) {
            Short conAttrPartId = partIdGenerator.nextId();
            DatabaseEntry partKey = new DatabaseEntry();
            DatabaseEntry partValue = new DatabaseEntry();
            shortBinder.objectToEntry(conAttrPartId, partKey);
            idCoreBinding.objectToEntry(idPartCore, partValue);
            idPartDb.put(BdbEnv.transaction, partKey, partValue);
            partIdMap.put(idPartCore, conAttrPartId);
            idPartMap.put(conAttrPartId, idPartCore);
            if (partIdMap.size() % 100 == 0) {
                AceLog.getAppLog().info("id part core map size now: " + partIdMap.size());
            }
            // AceLog.getAppLog().info("Writing part id: " + newPartId + " " +
            // part);
            return conAttrPartId;
        }
    }

    public short getIdPartCoreId(I_IdPart idPart) throws DatabaseException {
        ThinIdPartCore idPartCore = new ThinIdPartCore();
        idPartCore.setIdStatus(idPart.getIdStatus());
        idPartCore.setPathId(idPart.getPathId());
        idPartCore.setSource(idPart.getSource());
        idPartCore.setVersion(idPart.getVersion());
        return getIdPartCoreId(idPartCore);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPart(int)
     */
    public ThinIdPartCore getIdPartCore(short partId) throws DatabaseException {
        if (idPartMap.containsKey(partId)) {
            return idPartMap.get(partId);
        }
        throw new DatabaseException("Id part: " + partId + " not found.");
    }

    public void close() throws DatabaseException {
        idPartDb.close();
    }

    public void sync() throws DatabaseException {
        idPartDb.sync();
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException {
        // nothing to do...

    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

}
