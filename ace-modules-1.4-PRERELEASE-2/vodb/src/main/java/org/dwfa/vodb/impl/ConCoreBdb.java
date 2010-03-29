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
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinConPart;

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

public class ConCoreBdb implements I_StoreInBdb {

    private class PartIdGenerator {
        private int lastId = Integer.MIN_VALUE;

        private PartIdGenerator() throws DatabaseException {
            Cursor idCursor = conPartDb.openCursor(null, null);
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

    private static class ThinConPartVersionedBinding extends TupleBinding<I_ConceptAttributePart> {

        public ThinConPart entryToObject(TupleInput ti) {
            ThinConPart con = new ThinConPart();
            con.setPathId(ti.readInt());
            con.setVersion(ti.readInt());
            con.setStatusId(ti.readInt());
            con.setDefined(ti.readBoolean());
            return con;
        }

        public void objectToEntry(I_ConceptAttributePart con, TupleOutput to) {
            to.writeInt(con.getPathId());
            to.writeInt(con.getVersion());
            to.writeInt(con.getStatusId());
            to.writeBoolean(con.isDefined());
        }

    }

    private ThinConPartVersionedBinding conPartBinding = new ThinConPartVersionedBinding();
    private TupleBinding<Integer> intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    private Database conPartDb;
    private PartIdGenerator partIdGenerator;
    private HashMap<I_ConceptAttributePart, Integer> partIdMap = new HashMap<I_ConceptAttributePart, Integer>();
    private HashMap<Integer, I_ConceptAttributePart> conPartMap = new HashMap<Integer, I_ConceptAttributePart>();

    public ConCoreBdb(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
        super();
        conPartDb = env.openDatabase(null, "conPartDb", dbConfig);

        Cursor partCursor = conPartDb.openCursor(null, null);
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        while (partCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            int conAttrPartId = (Integer) intBinder.entryToObject(foundKey);
            I_ConceptAttributePart conAttrPart = (I_ConceptAttributePart) conPartBinding.entryToObject(foundData);
            partIdMap.put(conAttrPart, conAttrPartId);
            conPartMap.put(conAttrPartId, conAttrPart);
        }
        partCursor.close();
        AceLog.getAppLog().info("Con attr part map size: " + partIdMap.size());
        partIdGenerator = new PartIdGenerator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPartId(org.dwfa.ace.api
     * .I_RelPart)
     */
    public int getConPartId(I_ConceptAttributePart conAttrPart) throws DatabaseException {
        if (partIdMap.containsKey(conAttrPart)) {
            return partIdMap.get(conAttrPart);
        }
        synchronized (partIdMap) {
            int conAttrPartId = partIdGenerator.nextId();
            DatabaseEntry partKey = new DatabaseEntry();
            DatabaseEntry partValue = new DatabaseEntry();
            intBinder.objectToEntry((Integer) conAttrPartId, partKey);
            conPartBinding.objectToEntry(conAttrPart, partValue);
            conPartDb.put(BdbEnv.transaction, partKey, partValue);
            partIdMap.put(conAttrPart, conAttrPartId);
            conPartMap.put(conAttrPartId, conAttrPart);
            if (partIdMap.size() % 100 == 0) {
                AceLog.getAppLog().info("rel part map size now: " + partIdMap.size());
            }
            // AceLog.getAppLog().info("Writing part id: " + newPartId + " " +
            // part);
            return conAttrPartId;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.crel.I_StoreRelParts#getRelPart(int)
     */
    public I_ConceptAttributePart getConPart(int partId) throws DatabaseException {
        if (conPartMap.containsKey(partId)) {
            return conPartMap.get(partId);
        }
        throw new DatabaseException("Con part: " + partId + " not found.");
    }

    public void close() throws DatabaseException {
        conPartDb.close();
    }

    public void sync() throws DatabaseException {
        conPartDb.sync();
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException {
        // Nothing to do...

    }

    public void setupBean(ConceptBean cb) {
        // nothing to do
    }

    public void searchConcepts(I_TrackContinuation tracker, IntList matches, CountDownLatch latch,
            List<I_TestSearchResults> checkList, I_ConfigAceFrame config) throws DatabaseException, IOException,
            ParseException {
        throw new UnsupportedOperationException();
    }

}
