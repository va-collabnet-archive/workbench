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

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
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
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.LeastRecentlyUsedCache;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.I_StoreIdentifiers;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.UuidBinding;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessIdEntries;
import org.dwfa.vodb.types.ThinIdIntegerPartWithCoreDelegate;
import org.dwfa.vodb.types.ThinIdLongPartWithCoreDelegate;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdPartCore;
import org.dwfa.vodb.types.ThinIdPartWithCoreDelegate;
import org.dwfa.vodb.types.ThinIdUuidPartWithCoreDelegate;
import org.dwfa.vodb.types.ThinIdVersioned;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

public class IdWithPartCoresBdb implements I_StoreIdentifiers {

    private class NidGenerator {
        private int lastId = Integer.MIN_VALUE;

        private NidGenerator() throws DatabaseException {
            Cursor idCursor = idCoreDb.openCursor(null, null);
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

    public class ThinIdCoreVersionedBinding extends TupleBinding<I_IdVersioned> {

        private static final byte INTEGER_ID = 1;
        private static final byte LONG_ID = 2;
        private static final byte UUID_ID = 3;
        private static final byte STRING_ID = 4;

        public I_IdVersioned entryToObject(TupleInput ti) {
            short size = ti.readShort();
            I_IdVersioned versioned = new ThinIdVersioned(Integer.MIN_VALUE, size);
            for (int x = 0; x < size; x++) {
                short partId = ti.readShort();
                ThinIdPartCore core;
                try {
                    core = idPartCoreBdb.getIdPartCore(partId);
                } catch (DatabaseException e) {
                    throw new RuntimeException(e);
                }
                byte idType = ti.readByte();
                I_IdPart idPart;
                switch (idType) {
                case INTEGER_ID:
                    idPart = new ThinIdIntegerPartWithCoreDelegate(ti.readInt(), core);
                    assert versioned.getVersions().contains(idPart) == false;
                    versioned.addVersion(idPart);
                    break;
                case LONG_ID:
                    idPart = new ThinIdLongPartWithCoreDelegate(ti.readLong(), core);
                    assert versioned.getVersions().contains(idPart) == false;
                    versioned.addVersion(idPart);
                    break;
                case UUID_ID:
                    idPart = new ThinIdUuidPartWithCoreDelegate(ti.readLong(), ti.readLong(), core);
                    assert versioned.getVersions().contains(idPart) == false;
                    versioned.addVersion(idPart);
                    break;
                case STRING_ID:
                    idPart = new ThinIdPartWithCoreDelegate(ti.readString(), core);
                    assert versioned.getVersions().contains(idPart) == false;
                    versioned.addVersion(idPart);
                    break;
                }
            }
            return versioned;
        }

        public void objectToEntry(I_IdVersioned versioned, TupleOutput to) {
            to.writeShort(versioned.getVersions().size());
            for (I_IdPart id : versioned.getVersions()) {
                try {
                    to.writeShort(idPartCoreBdb.getIdPartCoreId(id));
                } catch (DatabaseException e) {
                    throw new RuntimeException();
                }
                if (Integer.class.isAssignableFrom(id.getSourceId().getClass())) {
                    to.writeByte(INTEGER_ID);
                    Integer sourceId = (Integer) id.getSourceId();
                    to.writeInt(sourceId);
                } else if (Long.class.isAssignableFrom(id.getSourceId().getClass())) {
                    to.writeByte(LONG_ID);
                    Long sourceId = (Long) id.getSourceId();
                    to.writeLong(sourceId);
                } else if (UUID.class.isAssignableFrom(id.getSourceId().getClass())) {
                    to.writeByte(UUID_ID);
                    UUID sourceId = (UUID) id.getSourceId();
                    to.writeLong(sourceId.getMostSignificantBits());
                    to.writeLong(sourceId.getLeastSignificantBits());
                } else if (String.class.isAssignableFrom(id.getSourceId().getClass())) {
                    to.writeByte(STRING_ID);
                    String sourceId = (String) id.getSourceId();
                    to.writeString(sourceId);
                }
            }
        }
    }

    private Database idCoreDb;

    private Database uuidToNidDb;

    private UuidBinding uuidBinding = new UuidBinding();

    private ThinIdCoreVersionedBinding idBinding = new ThinIdCoreVersionedBinding();

    private TupleBinding<Integer> intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    private Semaphore idPutSemaphore = new Semaphore(1);
    private Semaphore uuidToNidDbPutSemaphore = new Semaphore(1);

    private Map<UUID, Integer> uuidNidMapCache = Collections.synchronizedMap(new LeastRecentlyUsedCache<UUID, Integer>(
        2000));

    private NidGenerator nidGenerator;

    private IdPartCoreBdb idPartCoreBdb;

    private IdentifierBinding identifierBinding = new IdentifierBinding();

    private SecondaryDatabase index;

    public IdWithPartCoresBdb(Environment env, DatabaseConfig mapDbConfig) throws DatabaseException {
        super();
        idCoreDb = env.openDatabase(null, "idCoreDb", mapDbConfig);
        uuidToNidDb = env.openDatabase(null, "uuidToNidDb", mapDbConfig);
        SecondaryConfig indexConfig = new SecondaryConfig();
        nidGenerator = new NidGenerator();
        idPartCoreBdb = new IdPartCoreBdb(env, mapDbConfig);

        indexConfig.setReadOnly(VodbEnv.isReadOnly());
        indexConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
        indexConfig.setAllowCreate(!VodbEnv.isReadOnly());
        indexConfig.setSortedDuplicates(true);
        indexConfig.setMultiKeyCreator(new IdentifierKeyCreator(identifierBinding, idBinding));
        indexConfig.setAllowPopulate(true);
        indexConfig.setTransactional(VodbEnv.isTransactional());
        index = env.openSecondaryDatabase(null, "index", idCoreDb, indexConfig);
        // PreloadConfig preloadConfig = new PreloadConfig();
        // preloadConfig.setLoadLNs(true);
        // uuidToNidDb.preload(preloadConfig);
        // logIdDbStats();
    }

    public void logIdDbStats() throws DatabaseException {
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            StatsConfig config = new StatsConfig();
            config.setClear(true);
            config.setFast(false);
            DatabaseStats stats = idCoreDb.getStats(config);
            AceLog.getAppLog().fine("ID db stats: " + stats.toString());
            stats = uuidToNidDb.getStats(config);
            AceLog.getAppLog().fine("uuidToNidDb db stats: " + stats.toString());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.I_StoreIdentifiers#sync()
     */
    public void sync() throws DatabaseException {
        if (uuidToNidDb != null) {
            if (!uuidToNidDb.getConfig().getReadOnly()) {
                uuidToNidDb.sync();
            }
        }
        if (idCoreDb != null) {
            if (!idCoreDb.getConfig().getReadOnly()) {
                idCoreDb.sync();
            }
        }

        idPartCoreBdb.sync();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.I_StoreIdentifiers#close()
     */
    public void close() throws DatabaseException {
        if (uuidToNidDb != null) {
            uuidToNidDb.close();
        }
        if (idCoreDb != null) {
            idCoreDb.close();
        }

        if (index != null) {
            index.close();
        }

        idPartCoreBdb.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.I_StoreIdentifiers#getMinId()
     */
    public int getMinId() throws DatabaseException {
        Cursor idCursor = idCoreDb.openCursor(null, null);
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
        Cursor idCursor = idCoreDb.openCursor(null, null);
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
            if (idCoreDb.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                if (AceLog.getAppLog().isLoggable(Level.FINER)) {
                    AceLog.getAppLog().finer(
                        "Got id record for: " + nativeId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
                }
                I_IdVersioned theId = (I_IdVersioned) idBinding.entryToObject(idValue);
                theId.setNativeId(nativeId);
                return theId;
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
        if (idCoreDb.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            if (AceLog.getAppLog().isLoggable(Level.FINER)) {
                AceLog.getAppLog().finer(
                    "Got id record for: " + nativeId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
            }
            return ((I_IdVersioned) idBinding.entryToObject(idValue)).getUIDs();
        }
        throw new DatabaseException("Concept: " + nativeId + " not found.");
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
        throw new ToIoException(new DatabaseException("Concept: " + nativeId + " not found."));
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

            OperationStatus retVal = idCursor.getSearchKey(idKey, idValue, LockMode.DEFAULT);
            while (retVal == OperationStatus.SUCCESS) {
                result.add((ThinIdVersioned) idBinding.entryToObject(idValue));
                retVal = idCursor.getNextDup(idKey, idValue, LockMode.DEFAULT);
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

        if (result.isEmpty()) {
            AceLog.getAppLog().warning("No id database entry found for id '" + id + "' source '" + source + "'");
        }

        return result;
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
            idCoreDb.put(BdbEnv.transaction, idKey, idValue);
            idPutSemaphore.release();
            uuidToNidDbPutSemaphore.acquire();
            for (I_IdPart p : id.getVersions()) {
                if (UUID.class.isAssignableFrom(p.getSourceId().getClass())) {
                    UUID secondaryId = (UUID) p.getSourceId();
                    intBinder.objectToEntry(id.getNativeId(), idValue);
                    uuidBinding.objectToEntry(secondaryId, idKey);
                    uuidToNidDb.put(BdbEnv.transaction, idKey, idValue);
                }
            }
            uuidToNidDbPutSemaphore.release();
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
        intBinder.objectToEntry(id.getNativeId(), idKey);
        idCoreDb.delete(null, idKey);
        for (UUID uuid : id.getUIDs()) {
            uuidBinding.objectToEntry(uuid, idKey);
            uuidToNidDb.delete(null, idKey);
        }
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
            idPart.setStatusId(getCurrentStatusNid());
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

        int rv = uuidToNativeCore(uids);

        if (rv == 0) {
            // create a new one...
            try {
                I_IdVersioned newId = new ThinIdVersioned(nidGenerator.nextId(), 0);
                // AceLog.getLog().info("Last id: " + lastId + " NewId: " +
                // newId.getNativeId());
                ThinIdPart idPart = new ThinIdPart();
                for (UUID uid : uids) {
                    idPart.setStatusId(getCurrentStatusNid());
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
        } else {
            return rv;
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
                I_IdVersioned newId = new ThinIdVersioned(nidGenerator.nextId(), idPaths.size());
                //AceLog.getAppLog().info(" NewId: " + newId.getNativeId() + ", paths: " + idPaths);
                ThinIdPart idPart = new ThinIdPart();
                for (I_Path p : idPaths) {
                    idPart.setStatusId(getCurrentStatusNid());
                    idPart.setPathId(p.getConceptId());
                    idPart.setSource(source);
                    idPart.setSourceId(uid);
                    idPart.setVersion(version);
                    //AceLog.getAppLog().info(" adding version: " + idPart);
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
        Cursor idCursor = idCoreDb.openCursor(null, null);
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
        if (uuidToNidDb.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
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

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.I_StoreIdentifiers#uuidToNative(java.util.UUID)
     */
    public int uuidToNative(UUID uid) throws TerminologyException, IOException {
        int returnValue = uuidToNativeCore(uid);
        if (returnValue != 0) {
            return returnValue;
        }
        throw new NoMappingException("No id for: " + uid);
    }

    private int uuidToNativeCore(Collection<UUID> uuids) throws ToIoException {
        for (UUID uuid : uuids) {
            int returnValue = uuidToNativeCore(uuid);
            if (returnValue != 0) {
                return returnValue;
            }
        }
        return 0;
    }

    private int uuidToNativeCore(UUID uid) throws ToIoException {
        Integer nid = uuidNidMapCache.get(uid);
        if (nid != null) {
            return nid;
        }
        DatabaseEntry idKey = new DatabaseEntry();
        DatabaseEntry idValue = new DatabaseEntry();
        uuidBinding.objectToEntry(uid, idKey);
        try {
            if (uuidToNidDb.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                nid = (Integer) intBinder.entryToObject(idValue);
                uuidNidMapCache.put(uid, nid);
                return nid;

            }
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.I_StoreIdentifiers#uuidToNative(java.util.Collection)
     */
    public int uuidToNative(Collection<UUID> uids) throws TerminologyException, IOException {
        for (UUID uuid : uids) {
            int returnValue = uuidToNativeCore(uuid);
            if (returnValue != 0) {
                return returnValue;
            }
        }
        throw new NoMappingException("No id for: " + uids);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.I_StoreIdentifiers#getId(java.util.UUID)
     */
    public ThinIdVersioned getId(UUID uid) throws TerminologyException, IOException {
        ThinIdVersioned returnValue = getIdCore(uid);
        return returnValue;
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
            if (uuidToNidDb.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine(
                        "Got nativeId: " + uid + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
                }
                int nid = (Integer) intBinder.entryToObject(idValue);
                intBinder.objectToEntry(nid, idKey);
                if (idCoreDb.get(BdbEnv.transaction, idKey, idValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine(
                            "Got nativeId: " + uid + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
                    }
                    ThinIdVersioned theId = (ThinIdVersioned) idBinding.entryToObject(idValue);
                    theId.setNativeId(nid);
                    return theId;
                }
            }
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
        return null;
    }

    public int getCurrentStatusNid() {
        return PrimordialId.CURRENT_ID.getNativeId(Integer.MIN_VALUE);
    }

    public int getAceAuxillaryNid() {
        return PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE);
    }

    public I_IdVersioned idEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        ThinIdVersioned theId = (ThinIdVersioned) idBinding.entryToObject(value);
        int nid = (Integer) intBinder.entryToObject(key);
        theId.setNativeId(nid);
        return theId;
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException, IOException {
        if (bean.uncommittedIds != null) {
            for (int id : bean.uncommittedIds.getSetValues()) {
                I_IdVersioned idv = AceConfig.getVodb().getId(id);
                List<I_IdPart> partsToRemove = new ArrayList<I_IdPart>();
                List<I_IdPart> partsToAdd = new ArrayList<I_IdPart>();
                for (I_IdPart p : idv.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        I_IdPart newPart = p.duplicate();
                        newPart.setVersion(version);
                        assert partsToAdd.contains(newPart) == false;
                        partsToAdd.add(newPart);
                        partsToRemove.add(p);
                        values.add(new TimePathId(version, p.getPathId()));
                    }
                }
                idv.getVersions().removeAll(partsToRemove);
                idv.getVersions().addAll(partsToAdd);
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

}
