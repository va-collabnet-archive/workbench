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
import org.dwfa.vodb.C1KeyForRelCreator;
import org.dwfa.vodb.C2KeyForRelCreator;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.I_StoreRelationships;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinRelVersionedBinding;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessRelationshipEntries;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.PreloadConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.je.Transaction;

public class RelationshipBdb implements I_StoreInBdb, I_StoreRelationships {

    private class RelationshipIterator implements Iterator<I_RelVersioned> {

        DatabaseEntry foundKey = new DatabaseEntry();

        DatabaseEntry foundData = new DatabaseEntry();

        boolean hasNext;

        private I_RelVersioned desc;

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
                    desc = (I_RelVersioned) relBinding.entryToObject(foundData);
                } else {
                    desc = null;
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
                I_RelVersioned next = desc;
                getNext();
                return next;
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private Environment env;
    private Database relDb;
    private SecondaryDatabase c1RelMap;
    private SecondaryDatabase c2RelMap;
    private ThinRelVersionedBinding relBinding = new ThinRelVersionedBinding();
    private C2KeyForRelCreator c2KeyCreator = new C2KeyForRelCreator(relBinding);
    private C1KeyForRelCreator c1KeyCreator = new C1KeyForRelCreator(relBinding);
    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    private boolean preloadRels = true;

    public RelationshipBdb(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
        super();
        this.env = env;
        relDb = env.openDatabase(null, "relDb", dbConfig);
        createC1RelMap();
        createC2RelMap();

        if (preloadRels) {
            PreloadConfig relPreloadConfig = new PreloadConfig();
            relPreloadConfig.setLoadLNs(false);
            relDb.preload(relPreloadConfig);
        }
        // logStats();
    }

    public void logStats() throws DatabaseException {
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            StatsConfig config = new StatsConfig();
            config.setClear(true);
            config.setFast(false);
            DatabaseStats stats = relDb.getStats(config);
            AceLog.getAppLog().info("relDb stats: " + stats.toString());
        }
    }

    private void createC1RelMap() throws DatabaseException {
        if (c1RelMap == null) {
            C1KeyForRelCreator c1ToRelKeyCreator = new C1KeyForRelCreator(relBinding);
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
            C2KeyForRelCreator c2ToRelKeyCreator = new C2KeyForRelCreator(relBinding);
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

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dwfa.vodb.impl.I_StoreRelationships#writeRel(org.dwfa.ace.api.
     * I_RelVersioned)
     */
    public void writeRel(I_RelVersioned rel) throws IOException {
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        intBinder.objectToEntry(rel.getRelId(), key);
        relBinding.objectToEntry(rel, value);
        try {
            relDb.put(BdbEnv.transaction, key, value);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#hasRel(int)
     */
    public boolean hasRel(int relId, int conceptId) throws DatabaseException {
        DatabaseEntry relKey = new DatabaseEntry();
        DatabaseEntry relValue = new DatabaseEntry();
        intBinder.objectToEntry(relId, relKey);
        if (relDb.get(BdbEnv.transaction, relKey, relValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#getRel(int)
     */
    public I_RelVersioned getRel(int relId, int conceptId) throws DatabaseException {
        DatabaseEntry relKey = new DatabaseEntry();
        DatabaseEntry relValue = new DatabaseEntry();
        intBinder.objectToEntry(relId, relKey);
        if (relDb.get(BdbEnv.transaction, relKey, relValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return (I_RelVersioned) relBinding.entryToObject(relValue);
        }
        throw new DatabaseException("Rel: " + relId + " not found.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#hasSrcRel(int,
     * java.util.Set)
     */
    public boolean hasSrcRel(int conceptId, Set<Integer> srcRelTypeIds) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting src rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }

        DatabaseEntry secondaryKey = new DatabaseEntry();
        c1KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c1RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding.entryToObject(foundData);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#hasSrcRels(int)
     */
    public boolean hasSrcRels(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting src rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }

        DatabaseEntry secondaryKey = new DatabaseEntry();
        c1KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c1RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding.entryToObject(foundData);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#hasSrcRelTuple(int,
     * org.dwfa.ace.api.I_IntSet, org.dwfa.ace.api.I_IntSet, java.util.Set)
     */
    public boolean hasSrcRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet sourceRelTypes,
            Set<I_Position> positions) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting src rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }

        DatabaseEntry secondaryKey = new DatabaseEntry();
        c1KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c1RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        List<I_RelTuple> tuples = new ArrayList<I_RelTuple>();
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding.entryToObject(foundData);
            if (relFromConceptId.getC1Id() == conceptId) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#getSrcRels(int)
     */
    public List<I_RelVersioned> getSrcRels(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting src rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }

        DatabaseEntry secondaryKey = new DatabaseEntry();
        c1KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c1RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        List<I_RelVersioned> matches = new ArrayList<I_RelVersioned>();
        while (retVal == OperationStatus.SUCCESS) {
            ThinRelVersioned relFromConceptId = (ThinRelVersioned) relBinding.entryToObject(foundData);
            if (relFromConceptId.getC1Id() == conceptId) {
                matches.add(relFromConceptId);
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
        return matches;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#hasDestRel(int,
     * java.util.Set)
     */
    public boolean hasDestRel(int conceptId, Set<Integer> destRelTypeIds) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("hasDestRel for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        c2KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c2RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding.entryToObject(foundData);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#hasDestRels(int)
     */
    public boolean hasDestRels(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting dest rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        c2KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c2RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding.entryToObject(foundData);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#getDestRels(int)
     */
    public List<I_RelVersioned> getDestRels(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting dest rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        c2KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c2RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        List<I_RelVersioned> matches = new ArrayList<I_RelVersioned>();
        while (retVal == OperationStatus.SUCCESS) {
            ThinRelVersioned relFromConceptId = (ThinRelVersioned) relBinding.entryToObject(foundData);
            if (relFromConceptId.getC2Id() == conceptId) {
                matches.add(relFromConceptId);
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
        return matches;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreRelationships#hasDestRelTuple(int,
     * org.dwfa.ace.api.I_IntSet, org.dwfa.ace.api.I_IntSet, java.util.Set)
     */
    public boolean hasDestRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet destRelTypes,
            Set<I_Position> positions) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting dest rels for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        c2KeyCreator.createSecondaryKey(Integer.MIN_VALUE, conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = c2RelMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey, foundData, LockMode.DEFAULT);
        List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
        while (retVal == OperationStatus.SUCCESS) {
            I_RelVersioned relFromConceptId = (I_RelVersioned) relBinding.entryToObject(foundData);
            if (relFromConceptId.getC2Id() == conceptId) {
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

    public void close() throws DatabaseException {
        if (relDb != null) {
            relDb.close();
        }
        if (c1RelMap != null) {
            c1RelMap.close();
        }
        if (c2RelMap != null) {
            c2RelMap.close();
        }

    }

    public void sync() throws DatabaseException {
        if (relDb != null) {
            if (!relDb.getConfig().getReadOnly()) {
                relDb.sync();
            }
        }
        if (c1RelMap != null) {
            if (!c1RelMap.getConfig().getReadOnly()) {
                c1RelMap.sync();
            }
        }
        if (c2RelMap != null) {
            if (!c2RelMap.getConfig().getReadOnly()) {
                c2RelMap.sync();
            }
        }
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
            I_RelVersioned vrel = (I_RelVersioned) relBinding.entryToObject(relValue);
            if (relsToIgnore.contains(vrel.getRelId()) == false) {
                boolean addRetired = vrel.addRetiredRec(releases.getSetValues(), retiredNid);
                boolean removeRedundant = vrel.removeRedundantRecs();
                if (addRetired && removeRedundant) {
                    relBinding.objectToEntry(vrel, relValue);
                    relC.put(relKey, relValue);
                    retiredRels++;
                    compressedRels++;
                } else if (addRetired) {
                    relBinding.objectToEntry(vrel, relValue);
                    relC.put(relKey, relValue);
                    retiredRels++;
                } else if (removeRedundant) {
                    relBinding.objectToEntry(vrel, relValue);
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

    public I_RelVersioned relEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return (I_RelVersioned) relBinding.entryToObject(value);
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
