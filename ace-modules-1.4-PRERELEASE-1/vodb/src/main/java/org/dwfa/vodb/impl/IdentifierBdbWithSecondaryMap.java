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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.I_StoreIdentifiers;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.UidKeyCreator;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.UuidBinding;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessIdEntries;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;

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
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.StatsConfig;

public class IdentifierBdbWithSecondaryMap implements I_StoreIdentifiers {

    private class NidGenerator {
        private int lastId = Integer.MIN_VALUE;

        private NidGenerator() throws DatabaseException {
            Cursor idCursor = idDb.openCursor(null, null);
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

    private Database idDb;

    private SecondaryDatabase uidToIdMap;

    private UuidBinding uuidBinding = new UuidBinding();

    private ThinIdVersionedBinding idBinding = new ThinIdVersionedBinding();

    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    Semaphore idPutSemaphore = new Semaphore(1);

    private NidGenerator nidGenerator;

    private IdentifierBinding identifierBinding = new IdentifierBinding();

    private SecondaryDatabase index;

    public IdentifierBdbWithSecondaryMap(Environment env, DatabaseConfig mapDbConfig) throws DatabaseException {
        super();
        idDb = env.openDatabase(null, "idDb", mapDbConfig);
        SecondaryConfig uidToIdMapConfig = new SecondaryConfig();
        uidToIdMapConfig.setReadOnly(VodbEnv.isReadOnly());
        uidToIdMapConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
        uidToIdMapConfig.setAllowCreate(!VodbEnv.isReadOnly());
        uidToIdMapConfig.setSortedDuplicates(true);
        uidToIdMapConfig.setMultiKeyCreator(new UidKeyCreator(uuidBinding, idBinding));
        uidToIdMapConfig.setAllowPopulate(true);
        uidToIdMapConfig.setTransactional(VodbEnv.isTransactional());
        uidToIdMap = env.openSecondaryDatabase(null, "uidToIdMap", idDb, uidToIdMapConfig);

        SecondaryConfig indexConfig = new SecondaryConfig();
        indexConfig.setReadOnly(VodbEnv.isReadOnly());
        indexConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
        indexConfig.setAllowCreate(!VodbEnv.isReadOnly());
        indexConfig.setSortedDuplicates(false);
        indexConfig.setMultiKeyCreator(new IdentifierKeyCreator(identifierBinding, idBinding));
        indexConfig.setAllowPopulate(true);
        indexConfig.setTransactional(VodbEnv.isTransactional());
        index = env.openSecondaryDatabase(null, "index", idDb, indexConfig);

        // logIdDbStats();
        nidGenerator = new NidGenerator();

    }

    public void logIdDbStats() throws DatabaseException {
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            StatsConfig config = new StatsConfig();
            config.setClear(true);
            config.setFast(false);
            DatabaseStats stats = idDb.getStats(config);
            AceLog.getAppLog().fine("ID db stats: " + stats.toString());
            stats = uidToIdMap.getStats(config);
            AceLog.getAppLog().fine("uidToIdMap db stats: " + stats.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#sync()
     */
    public void sync() throws DatabaseException {
        if (uidToIdMap != null) {
            if (!uidToIdMap.getConfig().getReadOnly()) {
                uidToIdMap.sync();
            }
        }
        if (idDb != null) {
            if (!idDb.getConfig().getReadOnly()) {
                idDb.sync();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#close()
     */
    public void close() throws DatabaseException {
        if (uidToIdMap != null) {
            uidToIdMap.close();
        }
        if (idDb != null) {
            idDb.close();
        }
        if (index != null) {
            index.close();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#getMinId()
     */
    public int getMinId() throws DatabaseException {
        Cursor idCursor = idDb.openCursor(null, null);
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        int id = Integer.MAX_VALUE;
        if (idCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            id = (Integer) intBinder.entryToObject(foundKey);
        }
        idCursor.close();
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#getMaxId()
     */
    public int getMaxId() throws DatabaseException {
        Cursor idCursor = idDb.openCursor(null, null);
        DatabaseEntry foundKey = new DatabaseEntry();
        DatabaseEntry foundData = new DatabaseEntry();
        int id = Integer.MAX_VALUE;
        if (idCursor.getPrev(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            id = (Integer) intBinder.entryToObject(foundKey);
        }
        idCursor.close();
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#getIdNullOk(int)
     */
    public I_IdVersioned getIdNullOk(int nativeId) throws IOException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINER)) {
            AceLog.getAppLog().finer("Getting id record for : " + nativeId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry idKey = new DatabaseEntry();
        DatabaseEntry idValue = new DatabaseEntry();
        intBinder.objectToEntry(nativeId, idKey);
        try {
            if (idDb.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                if (AceLog.getAppLog().isLoggable(Level.FINER)) {
                    AceLog.getAppLog().finer(
                        "Got id record for: " + nativeId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
                }
                return (I_IdVersioned) idBinding.entryToObject(idValue);
            }
        } catch (DatabaseException e) {
            new ToIoException(e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#nativeToUuid(int)
     */
    public List<UUID> nativeToUuid(int nativeId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINER)) {
            AceLog.getAppLog().finer("Getting id record for : " + nativeId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry idKey = new DatabaseEntry();
        DatabaseEntry idValue = new DatabaseEntry();
        intBinder.objectToEntry(nativeId, idKey);
        if (idDb.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            if (AceLog.getAppLog().isLoggable(Level.FINER)) {
                AceLog.getAppLog().finer(
                    "Got id record for: " + nativeId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
            }
            return ((I_IdVersioned) idBinding.entryToObject(idValue)).getUIDs();
        }
        throw new DatabaseException("List<UUID> not found for: " + nativeId + " (a).");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#getId(int)
     */
    public I_IdVersioned getId(int nativeId) throws IOException {
        I_IdVersioned id = getIdNullOk(nativeId);
        if (id != null) {
            return id;
        }
        throw new ToIoException(new DatabaseException("I_IdVersioned for " + nativeId + " not found (b)."));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#getUids(int)
     */
    public Collection<UUID> getUids(int nativeId) throws TerminologyException, IOException {
        try {
            return nativeToUuid(nativeId);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.I_StoreIdentifiers#writeId(org.dwfa.ace.api.I_IdVersioned)
     */
    public void writeId(I_IdVersioned id) throws DatabaseException {
        DatabaseEntry idKey = new DatabaseEntry();
        DatabaseEntry idValue = new DatabaseEntry();
        intBinder.objectToEntry(id.getNativeId(), idKey);
        idBinding.objectToEntry(id, idValue);
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Writing nativeId : " + id);
            for (I_IdPart p : id.getVersions()) {
                if (UUID.class.isAssignableFrom(p.getSourceId().getClass())) {
                    UUID secondaryId = (UUID) p.getSourceId();
                    try {
                        int nid = uuidToNative(secondaryId);
                        AceLog.getAppLog().fine("Found nid: " + nid + " for : " + secondaryId);
                    } catch (TerminologyException e) {
                        AceLog.getAppLog().fine("No nid for : " + secondaryId);
                    } catch (IOException e) {
                        AceLog.getAppLog().fine("No nid for : " + secondaryId);
                    }
                }
            }
        }
        try {
            idPutSemaphore.acquire();
            nidGenerator.lastId = Math.max(nidGenerator.lastId, id.getNativeId());
            idDb.put(BdbEnv.transaction, idKey, idValue);
            idPutSemaphore.release();
        } catch (InterruptedException e) {
            throw new DatabaseException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.I_StoreIdentifiers#deleteId(org.dwfa.ace.api.I_IdVersioned)
     */
    public void deleteId(I_IdVersioned id) throws DatabaseException {
        DatabaseEntry idKey = new DatabaseEntry();
        DatabaseEntry idValue = new DatabaseEntry();
        intBinder.objectToEntry(id.getNativeId(), idKey);
        idBinding.objectToEntry(id, idValue);
        idDb.delete(null, idKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.I_StoreIdentifiers#nativeGenerationForUuid(java.util.UUID,
     * int, int, int)
     */
    public int nativeGenerationForUuid(UUID uid, int source, int pathId, int version) throws TerminologyException,
            IOException {
        // create a new one...
        try {
            I_IdVersioned newId = new ThinIdVersioned(nidGenerator.nextId(), 0);
            // AceLog.getLog().info("Last id: " + lastId + " NewId: " +
            // newId.getNativeId());
            ThinIdPart idPart = new ThinIdPart();
            idPart.setIdStatus(getCurrentStatusNid());
            idPart.setPathId(pathId);
            idPart.setSource(source);
            idPart.setSourceId(uid);
            idPart.setVersion(version);
            newId.addVersion(idPart);
            writeId(newId);
            return newId.getNativeId();
        } catch (DatabaseException e2) {
            throw new ToIoException(e2);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.I_StoreIdentifiers#uuidToNativeWithGeneration(java.util
     * .Collection,
     * int, org.dwfa.ace.api.I_Path, int)
     */
    public int uuidToNativeWithGeneration(Collection<UUID> uids, int source, I_Path idPath, int version)
            throws TerminologyException, IOException {
        try {
            return uuidToNative(uids);
        } catch (TerminologyException e) {
            // create a new one...
            try {
                I_IdVersioned newId = new ThinIdVersioned(nidGenerator.nextId(), 0);
                // AceLog.getLog().info("Last id: " + lastId + " NewId: " +
                // newId.getNativeId());
                ThinIdPart idPart = new ThinIdPart();
                for (UUID uid : uids) {
                    idPart.setIdStatus(getCurrentStatusNid());
                    idPart.setPathId(idPath.getConceptId());
                    idPart.setSource(source);
                    idPart.setSourceId(uid);
                    idPart.setVersion(version);
                    newId.addVersion(idPart);
                }

                writeId(newId);
                return newId.getNativeId();
            } catch (DatabaseException ex) {
                throw new ToIoException(ex);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.I_StoreIdentifiers#uuidToNativeWithGeneration(java.util
     * .UUID,
     * int, java.util.Collection, int)
     */
    public int uuidToNativeWithGeneration(UUID uid, int source, Collection<I_Path> idPaths, int version)
            throws TerminologyException, IOException {
        try {
            return uuidToNative(uid);
        } catch (NoMappingException e) {
            // create a new one...
            try {
                I_IdVersioned newId = new ThinIdVersioned(nidGenerator.nextId(), 0);
                // AceLog.getLog().info("Last id: " + lastId + " NewId: " +
                // newId.getNativeId());
                ThinIdPart idPart = new ThinIdPart();
                for (I_Path p : idPaths) {
                    idPart.setIdStatus(getCurrentStatusNid());
                    idPart.setPathId(p.getConceptId());
                    idPart.setSource(source);
                    idPart.setSourceId(uid);
                    idPart.setVersion(version);
                    newId.addVersion(idPart);
                }

                writeId(newId);
                return newId.getNativeId();
            } catch (DatabaseException e2) {
                throw new ToIoException(e2);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.I_StoreIdentifiers#iterateIdEntries(org.dwfa.vodb.types
     * .I_ProcessIdEntries)
     */
    public void iterateIdEntries(I_ProcessIdEntries processor) throws Exception {
        Cursor idCursor = idDb.openCursor(null, null);
        DatabaseEntry foundKey = processor.getKeyEntry();
        DatabaseEntry foundData = processor.getDataEntry();
        while (idCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            try {
                processor.processId(foundKey, foundData);
            } catch (Exception e) {
                idCursor.close();
                throw e;
            }
        }
        idCursor.close();
    }

    private ThinIdVersioned getIdCore(UUID uid) throws ToIoException {
        Stopwatch timer = null;

        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting nativeId : " + uid);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry idKey = new DatabaseEntry();
        DatabaseEntry idValue = new DatabaseEntry();
        uuidBinding.objectToEntry(uid, idKey);
        try {
            if (uidToIdMap.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine(
                        "Got nativeId: " + uid + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
                }
                return (ThinIdVersioned) idBinding.entryToObject(idValue);
            }
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#getId(java.util.Collection)
     */
    public I_IdVersioned getId(Collection<UUID> uids) throws TerminologyException, IOException {
        Set<ThinIdVersioned> ids = new HashSet<ThinIdVersioned>(1);
        for (UUID uid : uids) {
            ThinIdVersioned thinId = getId(uid);
            if (thinId != null) {
                ids.add(thinId);
            }
        }
        if (ids.isEmpty()) {
            return null;
        } else if (ids.size() == 1) {
            return ids.iterator().next();
        }
        throw new TerminologyException("UIDs have multiple id records: " + ids + " when getting for: " + uids);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#hasId(java.util.Collection)
     */
    public boolean hasId(Collection<UUID> uids) throws DatabaseException {
        for (UUID uid : uids) {
            if (hasId(uid)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#hasId(java.util.UUID)
     */
    public boolean hasId(UUID uid) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting nativeId : " + uid);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry idKey = new DatabaseEntry();
        DatabaseEntry idValue = new DatabaseEntry();
        uuidBinding.objectToEntry(uid, idKey);
        if (uidToIdMap.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine(
                    "Got nativeId: " + uid + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.I_StoreIdentifiers#uuidToNativeWithGeneration(java.util
     * .UUID,
     * int, org.dwfa.ace.api.I_Path, int)
     */
    public int uuidToNativeWithGeneration(UUID uid, int source, I_Path idPath, int version)
            throws TerminologyException, IOException {
        List<UUID> uids = new ArrayList<UUID>(1);
        uids.add(uid);
        return uuidToNativeWithGeneration(uids, source, idPath, version);
    }

    // TODO remove these after sufficient releases have gone by to ensure that
    // the additional uids are no longer in use.
    public static UUID originalStatedUuid = UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d");
    public static UUID originalInferredUuid = UUID.fromString("d8fb4fb0-18c3-3352-9431-4919193f85bc");

    public static UUID additionalStatedUuid = UUID.fromString("7cf285d6-a3b7-11dc-8314-0800200c9a66");
    public static UUID additionalInferredUuid = UUID.fromString("1ebef994-a3b8-11dc-8314-0800200c9a66");

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#getId(java.util.UUID)
     */
    public ThinIdVersioned getId(UUID uid) throws TerminologyException, IOException {
        ThinIdVersioned returnValue = getIdCore(uid);
        if (returnValue == null) {
            if (uid.equals(originalStatedUuid)) {
                returnValue = getIdCore(additionalStatedUuid);
            } else if (uid.equals(additionalStatedUuid)) {
                returnValue = getIdCore(originalStatedUuid);
            } else if (uid.equals(originalInferredUuid)) {
                returnValue = getIdCore(additionalInferredUuid);
            } else if (uid.equals(additionalInferredUuid)) {
                returnValue = getIdCore(originalInferredUuid);
            }
        }
        return returnValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#uuidToNative(java.util.UUID)
     */
    public int uuidToNative(UUID uid) throws TerminologyException, IOException {
        if (uid.equals(originalStatedUuid) || uid.equals(additionalStatedUuid)) {
            return PrimordialId.STATED_CHARACTERISTIC_ID.getNativeId(Integer.MIN_VALUE);
        }
        if (uid.equals(originalInferredUuid) || uid.equals(additionalInferredUuid)) {
            return PrimordialId.INFERRED_CHARACTERISTIC_ID.getNativeId(Integer.MIN_VALUE);
        }

        I_IdVersioned id = getId(uid);
        if (id != null) {
            return id.getNativeId();
        }
        throw new NoMappingException("No id for: " + uid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.I_StoreIdentifiers#uuidToNative(java.util.Collection)
     */
    public int uuidToNative(Collection<UUID> uids) throws TerminologyException, IOException {
        I_IdVersioned id = getId(uids);
        if (id == null) {
            throw new NoMappingException("No id for: " + uids);
        }
        return id.getNativeId();
    }

    public int getCurrentStatusNid() {
        return PrimordialId.CURRENT_ID.getNativeId(Integer.MIN_VALUE);
    }

    public int getAceAuxillaryNid() {
        return PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE);
    }

    public I_IdVersioned idEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return (I_IdVersioned) idBinding.entryToObject(value);
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException, IOException {
        if (bean.uncommittedIds != null) {
            for (int id : bean.uncommittedIds.getSetValues()) {
                I_IdVersioned idv = AceConfig.getVodb().getId(id);
                for (I_IdPart p : idv.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        p.setVersion(version);
                        values.add(new TimePathId(version, p.getPathId()));
                    }
                }
                this.writeId(idv);
                if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                    AceLog.getEditLog().fine("Committing: " + idv);
                }
            }
        }
    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

    @Override
    public List<I_IdVersioned> getId(String id, int source) throws TerminologyException, IOException {
        Cursor idCursor = null;
        List<I_IdVersioned> result = new ArrayList<I_IdVersioned>();
        try {

            idCursor = index.openCursor(BdbEnv.transaction, null);
            DatabaseEntry idKey = new DatabaseEntry();
            DatabaseEntry idValue = new DatabaseEntry();
            identifierBinding.objectToEntry(new Identifier(id, source), idKey);

            while (idCursor.getNext(idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                result.add((ThinIdVersioned) idBinding.entryToObject(idValue));
            }
        } catch (DatabaseException e) {
            throw new TerminologyException("Database exception looking up '" + id + "' in scheme '" + source + "'", e);
        } finally {
            if (idCursor != null) {
                try {
                    idCursor.close();
                } catch (DatabaseException e) {
                    throw new TerminologyException("Database exception looking up '" + id + "' in scheme '" + source
                        + "'", e);
                }
            }
        }

        return result;
    }

}
