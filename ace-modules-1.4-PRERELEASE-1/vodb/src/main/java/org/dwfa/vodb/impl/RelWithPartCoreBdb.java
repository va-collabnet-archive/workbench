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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.I_StoreRelationships;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessRelationshipEntries;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Transaction;

public class RelWithPartCoreBdb implements I_StoreRelationships {

    private class RelationshipIterator implements Iterator<I_RelVersioned> {

        DatabaseEntry foundKey = new DatabaseEntry();

        DatabaseEntry foundData = new DatabaseEntry();

        boolean hasNext;

        private ThinRelVersioned rel;

        private Cursor relCursor;

        private RelationshipIterator() throws IOException {
            super();
            try {
                relCursor = relDb.openCursor(null, null);
                getNext();
            } catch (DatabaseException e) {
                throw new ToIoException(e);
            }
        }

        private void getNext() {
            try {
                hasNext = (relCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
                if (hasNext) {
                    rel = (ThinRelVersioned) relWithPartCoreBinding.entryToObject(foundData);
                    int relId = (Integer) intBinder.entryToObject(foundKey);
                    rel.setRelId(relId);
                } else {
                    rel = null;
                    relCursor.close();
                }
            } catch (Exception ex) {
                try {
                    relCursor.close();
                } catch (DatabaseException e) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
                AceLog.getAppLog().alertAndLogException(ex);
                hasNext = false;
            }
        }

        public boolean hasNext() {
            return hasNext;
        }

        public I_RelVersioned next() {
            if (hasNext) {
                I_RelVersioned next = rel;
                getNext();
                return next;
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private class RelWithPartCoreBinding extends TupleBinding {
        public ThinRelVersioned entryToObject(TupleInput ti) {
            int c1id = ti.readInt();
            int c2id = ti.readInt();
            int size = ti.readShort();
            ThinRelVersioned versioned = new ThinRelVersioned(c1id, c2id, size);
            for (int x = 0; x < size; x++) {
                int relPartId = ti.readInt();
                I_RelPart relPart;
                try {
                    relPart = relPartBdb.getRelPart(relPartId);
                } catch (DatabaseException e) {
                    throw new RuntimeException(e);
                }
                versioned.addVersionNoRedundancyCheck(relPart);
            }
            return versioned;
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            ThinRelVersioned versioned = (ThinRelVersioned) obj;
            to.writeInt(versioned.getC1Id());
            to.writeInt(versioned.getC2Id());
            to.writeShort(versioned.versionCount());
            for (I_RelPart part : versioned.getVersions()) {
                try {
                    to.writeInt(relPartBdb.getRelPartId(part));
                } catch (DatabaseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class C1KeyForRelCreator implements SecondaryKeyCreator {
        RelWithPartCoreBinding relBinding;
        EntryBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

        public static class RelAndC1Id {
            int relId;
            int c1Id;

            public RelAndC1Id() {
                super();
            }

            public RelAndC1Id(int c1Id, int relId) {
                super();
                this.relId = relId;
                this.c1Id = c1Id;
            }

            public int getC1Id() {
                return c1Id;
            }

            public void setC1Id(int c1Id) {
                this.c1Id = c1Id;
            }

            public int getRelId() {
                return relId;
            }

            public void setRelId(int relId) {
                this.relId = relId;
            }
        }

        public static class RelAndC1IdBinding extends TupleBinding {

            public RelAndC1Id entryToObject(TupleInput ti) {
                return new RelAndC1Id(ti.readInt(), ti.readInt());
            }

            public void objectToEntry(Object obj, TupleOutput to) {
                RelAndC1Id id = (RelAndC1Id) obj;
                to.writeInt(id.getC1Id());
                to.writeInt(id.getRelId());
            }

        }

        RelAndC1Id relAndC1Id = new RelAndC1Id();
        RelAndC1IdBinding relAndC1IdBinding = new RelAndC1IdBinding();

        public C1KeyForRelCreator(RelWithPartCoreBinding binding) {
            super();
            this.relBinding = binding;
        }

        public boolean createSecondaryKey(SecondaryDatabase secDb, DatabaseEntry keyEntry, DatabaseEntry dataEntry,
                DatabaseEntry resultEntry) throws DatabaseException {
            I_RelVersioned emptyRel = (I_RelVersioned) relBinding.entryToObject(dataEntry);
            int relId = (Integer) intBinder.entryToObject(keyEntry);
            RelAndC1Id relAndC1Id = new RelAndC1Id(emptyRel.getC1Id(), relId);
            relAndC1IdBinding.objectToEntry(relAndC1Id, resultEntry);
            return true;
        }

        public boolean createSecondaryKey(int relId, int c1id, DatabaseEntry resultEntry) throws DatabaseException {
            RelAndC1Id relAndC1Id = new RelAndC1Id(c1id, relId);
            relAndC1IdBinding.objectToEntry(relAndC1Id, resultEntry);
            return true;
        }

    }

    public static class C2KeyForRelCreator implements SecondaryKeyCreator {
        RelWithPartCoreBinding relBinding;
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

        RelAndC2IdBinding relAndC2IdBinding = new RelAndC2IdBinding();

        public C2KeyForRelCreator(RelWithPartCoreBinding binding) {
            super();
            this.relBinding = binding;
        }

        public boolean createSecondaryKey(SecondaryDatabase secDb, DatabaseEntry keyEntry, DatabaseEntry dataEntry,
                DatabaseEntry resultEntry) throws DatabaseException {
            I_RelVersioned emptyRel = (I_RelVersioned) relBinding.entryToObject(dataEntry);
            int relId = (Integer) intBinder.entryToObject(keyEntry);
            RelAndC2Id relAndC2Id = new RelAndC2Id(emptyRel.getC2Id(), relId);
            relAndC2IdBinding.objectToEntry(relAndC2Id, resultEntry);
            return true;
        }

        public boolean createSecondaryKey(int relId, int concId, DatabaseEntry resultEntry) throws DatabaseException {
            RelAndC2Id relAndC2Id = new RelAndC2Id(concId, relId);
            relAndC2IdBinding.objectToEntry(relAndC2Id, resultEntry);
            return true;
        }

    }

    RelWithPartCoreBinding relWithPartCoreBinding = new RelWithPartCoreBinding();

    private Environment env;
    private Database relDb;
    private SecondaryDatabase c1RelMap;
    private SecondaryDatabase c2RelMap;

    private I_StoreRelParts<Integer> relPartBdb;

    private C1KeyForRelCreator c1ToRelKeyCreator;

    private C2KeyForRelCreator c2ToRelKeyCreator;

    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    public RelWithPartCoreBdb(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
        super();
        // File newHome = new File(env.getHome(), "relationships");
        // newHome.mkdirs();
        EnvironmentConfig envConfig = new EnvironmentConfig();

        envConfig.setTransactional(VodbEnv.isTransactional());
        envConfig.setTxnNoSync(VodbEnv.getTxnNoSync());
        envConfig.setTxnTimeout(VodbEnv.getTransactionTimeout());

        envConfig.setReadOnly(VodbEnv.isReadOnly());
        envConfig.setAllowCreate(!VodbEnv.isReadOnly());
        // this.env = new Environment(newHome, envConfig);
        this.env = env;

        openDatabases(dbConfig);
    }

    private void openDatabases(DatabaseConfig dbConfig) throws DatabaseException {
        relDb = env.openDatabase(null, "relDb", dbConfig);
        createC1RelMap();
        createC2RelMap();
        relPartBdb = new RelPartBdbEphMapIntKey(env, dbConfig);
    }

    private void createC1RelMap() throws DatabaseException {
        if (c1RelMap == null) {
            c1ToRelKeyCreator = new C1KeyForRelCreator(relWithPartCoreBinding);
            SecondaryConfig relByC1IdConfig = new SecondaryConfig();
            relByC1IdConfig.setReadOnly(VodbEnv.isReadOnly());
            relByC1IdConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
            relByC1IdConfig.setAllowCreate(!VodbEnv.isReadOnly());
            relByC1IdConfig.setSortedDuplicates(false);
            relByC1IdConfig.setKeyCreator(c1ToRelKeyCreator);
            relByC1IdConfig.setAllowPopulate(true);
            relByC1IdConfig.setTransactional(VodbEnv.isTransactional());
            c1RelMap = env.openSecondaryDatabase(null, "c1RelMap", relDb, relByC1IdConfig);
        }
    }

    private void createC2RelMap() throws DatabaseException {
        if (c2RelMap == null) {
            c2ToRelKeyCreator = new C2KeyForRelCreator(relWithPartCoreBinding);
            SecondaryConfig relByC2IdConfig = new SecondaryConfig();
            relByC2IdConfig.setReadOnly(VodbEnv.isReadOnly());
            relByC2IdConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
            relByC2IdConfig.setAllowCreate(!VodbEnv.isReadOnly());
            relByC2IdConfig.setSortedDuplicates(false);
            relByC2IdConfig.setKeyCreator(c2ToRelKeyCreator);
            relByC2IdConfig.setAllowPopulate(true);
            relByC2IdConfig.setTransactional(VodbEnv.isTransactional());
            c2RelMap = env.openSecondaryDatabase(null, "c2RelMap", relDb, relByC2IdConfig);
        }
    }

    public void cleanupSNOMED(I_IntSet relsToIgnore, I_IntSet releases) throws Exception {
        // Update the history records for the relationships...
        AceLog.getAppLog().info("Starting rel history update.");
        Cursor relC = relDb.openCursor(null, null);
        DatabaseEntry relKey = new DatabaseEntry();
        DatabaseEntry relValue = new DatabaseEntry();
        int compressedRels = 0;
        int retiredRels = 0;
        int currentRels = 0;
        int totalRels = 0;
        int retiredNid = LocalVersionedTerminology.get().uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
        while (relC.getNext(relKey, relValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            totalRels++;
            I_RelVersioned vrel = (I_RelVersioned) relWithPartCoreBinding.entryToObject(relValue);
            if (relsToIgnore.contains(vrel.getRelId()) == false) {
                boolean addRetired = vrel.addRetiredRec(releases.getSetValues(), retiredNid);
                boolean removeRedundant = vrel.removeRedundantRecs();
                if (addRetired && removeRedundant) {
                    relWithPartCoreBinding.objectToEntry(vrel, relValue);
                    relC.put(relKey, relValue);
                    retiredRels++;
                    compressedRels++;
                } else if (addRetired) {
                    relWithPartCoreBinding.objectToEntry(vrel, relValue);
                    relC.put(relKey, relValue);
                    retiredRels++;
                } else if (removeRedundant) {
                    relWithPartCoreBinding.objectToEntry(vrel, relValue);
                    relC.put(relKey, relValue);
                    compressedRels++;
                    currentRels++;
                } else {
                    currentRels++;
                }
            }
        }
        relC.close();
        AceLog.getAppLog().info("Total rels: " + totalRels);
        AceLog.getAppLog().info("Compressed rels: " + compressedRels);
        AceLog.getAppLog().info("Retired rels: " + retiredRels);
        AceLog.getAppLog().info("Current rels: " + currentRels);
    }

    public List<I_RelVersioned> getDestRels(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting dest rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        c2ToRelKeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry primaryKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c2RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, primaryKey, foundData, LockMode.DEFAULT);
        List<I_RelVersioned> matches = new ArrayList<I_RelVersioned>();
        while (retVal == OperationStatus.SUCCESS) {
            ThinRelVersioned relFromConceptId = (ThinRelVersioned) relWithPartCoreBinding.entryToObject(foundData);
            if (relFromConceptId.getC2Id() == conceptId) {
                int relId = (Integer) intBinder.entryToObject(primaryKey);
                relFromConceptId.setRelId(relId);
                matches.add(relFromConceptId);
            } else {
                break;
            }
            retVal = mySecCursor.getNext(secondaryKey, primaryKey, foundData, LockMode.DEFAULT);
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "dest rels fetched for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return matches;
    }

    public I_RelVersioned getRel(int relId, int conceptId) throws DatabaseException {
        DatabaseEntry relKey = new DatabaseEntry();
        DatabaseEntry relValue = new DatabaseEntry();
        intBinder.objectToEntry(relId, relKey);
        if (relDb.get(BdbEnv.transaction, relKey, relValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            ThinRelVersioned rel = (ThinRelVersioned) relWithPartCoreBinding.entryToObject(relValue);
            rel.setRelId(relId);
            return rel;
        }
        throw new DatabaseException("Rel: " + relId + " not found.");
    }

    public List<I_RelVersioned> getSrcRels(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting src rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }

        DatabaseEntry secondaryKey = new DatabaseEntry();
        c1ToRelKeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c1RelMap.openSecondaryCursor(null, null);
        DatabaseEntry primaryKey = new DatabaseEntry();
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, primaryKey, foundData, LockMode.DEFAULT);
        List<I_RelVersioned> matches = new ArrayList<I_RelVersioned>();
        while (retVal == OperationStatus.SUCCESS) {
            ThinRelVersioned relFromConceptId = (ThinRelVersioned) relWithPartCoreBinding.entryToObject(foundData);
            if (relFromConceptId.getC1Id() == conceptId) {
                int relId = (Integer) intBinder.entryToObject(primaryKey);
                relFromConceptId.setRelId(relId);
                matches.add(relFromConceptId);
            } else {
                break;
            }
            retVal = mySecCursor.getNext(secondaryKey, primaryKey, foundData, LockMode.DEFAULT);
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "src rels fetched for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return matches;
    }

    public boolean hasDestRel(int conceptId, Set<Integer> destRelTypeIds) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("hasDestRel for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        c2ToRelKeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c2RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relWithPartCoreBinding.entryToObject(foundData);
            if (relFromConceptId.getC2Id() == conceptId) {
                if (destRelTypeIds == null) {
                    mySecCursor.close();
                    return true;
                }
                if (destRelTypeIds.contains(relFromConceptId.getVersions().get(0).getRelTypeId())) {
                    mySecCursor.close();
                    return true;
                }

            } else {
                break;
            }
            retVal = mySecCursor.getNext(secondaryKey, foundData, LockMode.DEFAULT);
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "hasDestRel for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return false;
    }

    public boolean hasDestRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet destRelTypes,
            Set<I_Position> positions) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting dest rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        c2ToRelKeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry primaryKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c2RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, primaryKey, foundData, LockMode.DEFAULT);
        List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
        while (retVal == OperationStatus.SUCCESS) {
            ThinRelVersioned relFromConceptId = (ThinRelVersioned) relWithPartCoreBinding.entryToObject(foundData);
            if (relFromConceptId.getC2Id() == conceptId) {
                relFromConceptId.setRelId((Integer) intBinder.entryToObject(primaryKey));
                relFromConceptId.addTuples(allowedStatus, destRelTypes, positions, returnRels, false);
                if (returnRels.size() > 0) {
                    return true;
                }
            } else {
                break;
            }
            retVal = mySecCursor.getNext(secondaryKey, foundData, LockMode.DEFAULT);
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "dest rels fetched for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return false;
    }

    public boolean hasDestRels(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting dest rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        c2ToRelKeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c2RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relWithPartCoreBinding.entryToObject(foundData);
            if (relFromConceptId.getC2Id() == conceptId) {
                mySecCursor.close();
                return true;
            } else {
                break;
            }
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "dest rels fetched for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return false;
    }

    public boolean hasRel(int relId, int conceptId) throws DatabaseException {
        DatabaseEntry relKey = new DatabaseEntry();
        DatabaseEntry relValue = new DatabaseEntry();
        intBinder.objectToEntry(relId, relKey);
        if (relDb.get(BdbEnv.transaction, relKey, relValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return true;
        }
        return false;
    }

    public boolean hasSrcRel(int conceptId, Set<Integer> srcRelTypeIds) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting src rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }

        DatabaseEntry secondaryKey = new DatabaseEntry();
        c1ToRelKeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c1RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relWithPartCoreBinding.entryToObject(foundData);
            if (relFromConceptId.getC1Id() == conceptId) {
                if (srcRelTypeIds == null) {
                    mySecCursor.close();
                    return true;
                }
                if (srcRelTypeIds.contains(relFromConceptId.getVersions().get(0).getRelTypeId())) {
                    mySecCursor.close();
                    return true;
                }
            } else {
                break;
            }
            retVal = mySecCursor.getNext(secondaryKey, foundData, LockMode.DEFAULT);
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "src rels fetched for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return false;
    }

    public boolean hasSrcRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet sourceRelTypes,
            Set<I_Position> positions) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting src rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }

        DatabaseEntry secondaryKey = new DatabaseEntry();
        c1ToRelKeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry primaryKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c1RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, primaryKey, foundData, LockMode.DEFAULT);
        List<I_RelTuple> tuples = new ArrayList<I_RelTuple>();
        while (retVal == OperationStatus.SUCCESS) {
            ThinRelVersioned relFromConceptId = (ThinRelVersioned) relWithPartCoreBinding.entryToObject(foundData);
            if (relFromConceptId.getC1Id() == conceptId) {
                relFromConceptId.setRelId((Integer) intBinder.entryToObject(primaryKey));
                relFromConceptId.addTuples(allowedStatus, sourceRelTypes, positions, tuples, false);
                if (tuples.size() > 0) {
                    return true;
                }
            } else {
                break;
            }
            retVal = mySecCursor.getNext(secondaryKey, foundData, LockMode.DEFAULT);
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "src rels fetched for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return false;
    }

    public boolean hasSrcRels(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting src rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }

        DatabaseEntry secondaryKey = new DatabaseEntry();
        c1ToRelKeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c1RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relWithPartCoreBinding.entryToObject(foundData);
            if (relFromConceptId.getC1Id() == conceptId) {
                return true;
            } else {
                break;
            }
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "src rels fetched for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return false;
    }

    public void iterateRelationshipEntries(I_ProcessRelationshipEntries processor) throws Exception {
        Cursor relCursor = relDb.openCursor(null, null);
        DatabaseEntry foundKey = processor.getKeyEntry();
        DatabaseEntry foundData = processor.getDataEntry();
        while (relCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            try {
                processor.processRel(foundKey, foundData);
            } catch (Exception e) {
                relCursor.close();
                throw e;
            }
        }
        relCursor.close();
    }

    public I_RelVersioned relEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        ThinRelVersioned rel = (ThinRelVersioned) relWithPartCoreBinding.entryToObject(value);
        rel.setRelId((Integer) intBinder.entryToObject(key));
        return rel;
    }

    public void writeRel(I_RelVersioned rel) throws IOException {
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        intBinder.objectToEntry(rel.getRelId(), key);
        relWithPartCoreBinding.objectToEntry(rel, value);
        try {
            relDb.put(BdbEnv.transaction, key, value);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void close() throws DatabaseException {
        relDb.close();
        c1RelMap.close();
        c2RelMap.close();
        relPartBdb.close();
    }

    public void sync() throws DatabaseException {
        relDb.sync();
        c1RelMap.sync();
        c2RelMap.sync();
        relPartBdb.sync();
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException, IOException {
        if (bean.sourceRels != null) {
            for (I_RelVersioned srcRel : bean.sourceRels) {
                boolean changed = false;
                for (ListIterator<I_RelPart> partItr = srcRel.getVersions().listIterator(); partItr.hasNext();) {
                    I_RelPart part = partItr.next();
                    if (part.getVersion() == Integer.MAX_VALUE) {
                        changed = true;
                        part.setVersion(version);
                        values.add(new TimePathId(version, part.getPathId()));
                    }
                }
                if (changed) {
                    this.writeRel(srcRel);
                }
            }
        }
        if (bean.uncommittedSourceRels != null) {
            for (I_RelVersioned rel : bean.uncommittedSourceRels) {
                ConceptBean destBean = ConceptBean.get(rel.getC2Id());
                destBean.flushDestRels();
                for (I_RelPart p : rel.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        p.setVersion(version);
                        values.add(new TimePathId(version, p.getPathId()));
                    }
                }
                this.writeRel(rel);
                if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                    AceLog.getEditLog().fine("Committing: " + rel);
                }
            }
            if (bean.sourceRels == null) {
                bean.sourceRels = new ArrayList<I_RelVersioned>();
            }
            bean.sourceRels.addAll(bean.uncommittedSourceRels);
            bean.uncommittedSourceRels = null;
            bean.destRels = null;
        }
    }

    public Iterator<I_RelVersioned> getRelationshipIterator() throws IOException {
        return new RelationshipIterator();
    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

}
