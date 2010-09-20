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

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.I_StoreConceptAttributes;
import org.dwfa.vodb.I_StoreDescriptions;
import org.dwfa.vodb.I_StoreExtensions;
import org.dwfa.vodb.I_StoreIdentifiers;
import org.dwfa.vodb.I_StoreImages;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.I_StoreMetadata;
import org.dwfa.vodb.I_StorePaths;
import org.dwfa.vodb.I_StorePositions;
import org.dwfa.vodb.I_StoreRelationships;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.I_ProcessConceptAttributeEntries;
import org.dwfa.vodb.types.I_ProcessDescriptionEntries;
import org.dwfa.vodb.types.I_ProcessExtByRefEntries;
import org.dwfa.vodb.types.I_ProcessIdEntries;
import org.dwfa.vodb.types.I_ProcessImageEntries;
import org.dwfa.vodb.types.I_ProcessPathEntries;
import org.dwfa.vodb.types.I_ProcessRelationshipEntries;
import org.dwfa.vodb.types.I_ProcessTimeBranchEntries;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;

import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.utilint.DbLsn;

public class BdbEnv implements I_StoreInBdb, I_StoreConceptAttributes, I_StoreIdentifiers, I_StoreRelationships,
        I_StoreMetadata, I_StoreDescriptions, I_StoreImages, I_StoreExtensions, I_StorePositions, I_StorePaths {

    private static int databaseVersion = 2;

    private Environment env;

    private List<I_StoreInBdb> databases = new ArrayList<I_StoreInBdb>();

    private I_StoreMetadata metaBdb;

    private I_StoreConceptAttributes conAttBdb;

    private I_StoreIdentifiers identifierDb;

    private I_StoreRelationships relBdb;

    private I_StoreDescriptions descBdb;

    private I_StoreImages imageBdb;

    private I_StoreExtensions extensionBdb;

    private I_StorePositions positionBdb;

    private I_StorePaths pathBdb;

    protected static Transaction transaction = null;

    public void startTransaction() throws IOException {
        if (transaction != null) {
            throw new IOException("Transaction already in progress");
        }
        if (VodbEnv.isTransactional() == false) {
            throw new IOException("Database is not transactional");
        }
        TransactionConfig tc = new TransactionConfig();
        tc.setReadUncommitted(true);
        try {
            transaction = env.beginTransaction(null, tc);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void cancelTransaction() throws IOException {
        if (transaction != null) {
            Transaction oldTransaction = transaction;
            transaction = null;
            try {
                oldTransaction.abort();
            } catch (DatabaseException e) {
                throw new ToIoException(e);
            }
        }
    }

    public void commitTransaction() throws IOException {
        if (transaction != null) {
            Transaction oldTransaction = transaction;
            transaction = null;
            try {
                oldTransaction.commit();
            } catch (DatabaseException e) {
                throw new ToIoException(e);
            }
        }
    }

    /**
     * Connect to database and reset any cached objects.
     *
     * @param vodb VodbEnv
     * @param envHome File
     * @param readOnly boolean
     * @param cacheSize Long
     * @param luceneDir File
     * @param dbSetupConfig DatabaseSetupConfig
     *
     * @throws DatabaseException connecting to DB
     * @throws IOException reading DB files
     */
    public BdbEnv(VodbEnv vodb, File envHome, boolean readOnly, Long cacheSize, File luceneDir,
            DatabaseSetupConfig dbSetupConfig) throws DatabaseException, IOException {

        ConceptBean.resetBeanCache();
        ExtensionByReferenceBean.resetBeanCache();

        EnvironmentConfig envConfig = new EnvironmentConfig();

        envConfig.setConfigParam("je.log.faultReadSize", "8124");
        envConfig.setConfigParam("je.log.fileMax", "15000000");
        envConfig.setConfigParam("je.nodeMaxEntries", "1024");
        envConfig.setConfigParam("je.env.sharedLatches", "true");
        envConfig.setConfigParam("je.lock.nLockTables", "5");

        envConfig.setTransactional(VodbEnv.isTransactional());
        VodbEnv.setTransactional(envConfig.getTransactional());

        envConfig.setTxnNoSync(VodbEnv.getTxnNoSync());
        VodbEnv.setTxnNoSync(envConfig.getTxnNoSync());

        envConfig.setTxnTimeout(VodbEnv.getTransactionTimeout());
        VodbEnv.setTransactionTimeout(envConfig.getTxnTimeout());

        envConfig.setReadOnly(readOnly);
        envConfig.setAllowCreate(!readOnly);
        env = new Environment(envHome, envConfig);

        DatabaseConfig metaInfoDbConfig = makeConfig(readOnly, VodbEnv.isTransactional());
        vodb.getActivityFrame().setProgressInfoLower("Opening metaInfo...");

        metaBdb = new MetaBdb(env, metaInfoDbConfig);
        databases.add(metaBdb);

        String versionString = getProperty("dbVersion");
        if (versionString == null) {
            versionString = "2";
            setProperty("dbVersion", "2");
            AceLog.getAppLog().info("Setting db version to 2.");
            versionString = getProperty("dbVersion");

            setProperty("CORE_DB_TYPE", dbSetupConfig.getCoreDbType().name());
            setProperty("ID_DB_TYPE", dbSetupConfig.getIdDbType().name());
            sync();
        } else {
            if (dbSetupConfig == null) {
                dbSetupConfig = new DatabaseSetupConfig();
            }
            String coreDbType = getProperty("CORE_DB_TYPE");
            dbSetupConfig.setCoreDbTypeStr(coreDbType);
            String idDbType = getProperty("ID_DB_TYPE");
            dbSetupConfig.setIdDbTypeStr(idDbType);

        }
        AceLog.getAppLog().info(" db version is: " + Integer.parseInt(versionString));
        AceLog.getAppLog().info("CORE_DB_TYPE is " + dbSetupConfig.getCoreDbType());
        AceLog.getAppLog().info("ID_DB_TYPE is " + dbSetupConfig.getIdDbType());

        vodb.getActivityFrame().setProgressInfoLower("Opening ids...");
        switch (dbSetupConfig.getIdDbType()) {
        case UUID_MAP_PRIMARY:
            identifierDb = new IdentifierBdbWithSecondaryMap(getEnv(), makeConfig(readOnly, VodbEnv.isTransactional()));
            break;
        case UUID_MAP_SECONDARY:
            identifierDb = new IdentifierBdbWithSecondaryMap(getEnv(), makeConfig(readOnly, VodbEnv.isTransactional()));
            break;
        case UUID_MAP_PRIMARY_WITH_CORES:
            identifierDb = new IdWithPartCoresBdb(getEnv(), makeConfig(readOnly, VodbEnv.isTransactional()));
            break;
        }
        databases.add(identifierDb);
        // Reset the authority id so that each time the db starts, it gets a
        // new authorityId.
        resetAuthorityId();

        vodb.getActivityFrame().setProgressInfoLower("Opening core databases...");

        switch (dbSetupConfig.getCoreDbType()) {
        case CON_DESC_REL:
        case CON_DESCMAP_REL:
        case CON_COMPDESC_REL:
            ConDescRelBdb conDescRelBdb = new ConDescRelBdb(env, makeConfig(readOnly, VodbEnv.isTransactional()),
                luceneDir, identifierDb, dbSetupConfig.getCoreDbType());
            conAttBdb = conDescRelBdb;
            relBdb = conDescRelBdb;
            descBdb = conDescRelBdb;
            databases.add(conDescRelBdb);
            break;
        }

        vodb.getActivityFrame().setProgressInfoLower("Opening images...");
        imageBdb = new ImageBdb(env, makeConfig(readOnly, VodbEnv.isTransactional()));
        databases.add(imageBdb);

        vodb.getActivityFrame().setProgressInfoLower("Opening extensions...");
        extensionBdb = new ExtensionBdb(env, makeConfig(readOnly, VodbEnv.isTransactional()));
        databases.add(extensionBdb);

        vodb.getActivityFrame().setProgressInfoLower("Opening time branches...");
        positionBdb = new PositionBdb(env, makeConfig(readOnly, VodbEnv.isTransactional()));
        databases.add(positionBdb);

        vodb.getActivityFrame().setProgressInfoLower("Opening paths...");
        pathBdb = new PathBdb(env, makeConfig(readOnly, VodbEnv.isTransactional()));
        databases.add(pathBdb);

    }

    private I_IdVersioned previousAuthorityId;

    private void resetAuthorityId() throws DatabaseException, IOException {
        try {
            previousAuthorityId = getAuthorityId();
            AceLog.getAppLog().info(
                "Old authority id: " + previousAuthorityId.getTuples().iterator().next().getSourceId());
        } catch (Exception e) {
            AceLog.getAppLog().warning("Unable to get previous authority id.");
        }
        PrimordialId primId = PrimordialId.AUTHORITY_ID;
        I_IdVersioned thinId = new ThinIdVersioned(primId.getNativeId(Integer.MIN_VALUE), 1);
        ThinIdPart idPart = new ThinIdPart();
        idPart.setStatusId(PrimordialId.CURRENT_ID.getNativeId(Integer.MIN_VALUE));
        idPart.setPathId(PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE));
        idPart.setSource(PrimordialId.ACE_AUX_ENCODING_ID.getNativeId(Integer.MIN_VALUE));
        idPart.setSourceId(UUID.randomUUID());
        idPart.setVersion(Integer.MIN_VALUE);
        thinId.addVersion(idPart);
        AceLog.getAppLog().info("New authority id: " + idPart.getSourceId());
        writeId(thinId);
        commitTransaction();
    }

    public I_IdVersioned getAuthorityId() throws IOException {
        return getId(PrimordialId.AUTHORITY_ID.getNativeId(Integer.MIN_VALUE));
    }

    public I_IdVersioned getPreviousAuthorityId() throws TerminologyException, IOException {
        return previousAuthorityId;
    }

    private DatabaseConfig makeConfig(boolean readOnly, boolean transactional) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setReadOnly(readOnly);
        dbConfig.setAllowCreate(!readOnly);
        dbConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
        dbConfig.setSortedDuplicates(false);
        dbConfig.setTransactional(transactional);
        return dbConfig;
    }

    public void close() throws DatabaseException {
        try {
            sync();
            if (env != null) {
                for (I_StoreInBdb store : databases) {
                    store.close();
                }

                env.close();
            }
        } catch (DatabaseException e) {
            AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }

    public void sync() throws DatabaseException {
        if (VodbEnv.isDeferredWrite()) {
            if (env.getConfig().getReadOnly()) {
                AceLog.getAppLog().info("Read only environment requires no sync.");
                return;
            }
            if (!env.getConfig().getReadOnly()) {
                for (I_StoreInBdb store : databases) {
                    AceLog.getAppLog().info("Syncing: " + store.toString());
                    store.sync();
                }
                env.sync();
            }
            AceLog.getAppLog().info("Finished sync.");
        } else {
            CheckpointConfig check = new CheckpointConfig();
            check.setForce(true);
            if (env != null) {
                AceLog.getAppLog().info("Starting checkpoint.");
                env.checkpoint(check);
                AceLog.getAppLog().info("Finished checkpoint.");
            }
        }
    }

    public Map<String, String> getProperties() throws IOException {
        return metaBdb.getProperties();
    }

    public String getProperty(String key) throws IOException {
        return metaBdb.getProperty(key);
    }

    public void setProperty(String key, String value) throws IOException {
        metaBdb.setProperty(key, value);
    }

    protected Environment getEnv() {
        return env;
    }

    public I_ConceptAttributeVersioned getConceptAttributes(int conceptId) throws IOException {
        return conAttBdb.getConceptAttributes(conceptId);
    }

    public Iterator<I_GetConceptData> getConceptIterator() throws IOException {
        return conAttBdb.getConceptIterator();
    }

    public I_IntSet getConceptNids() throws IOException {
        return conAttBdb.getConceptNids();
    }

    public boolean hasConcept(int conceptId) throws DatabaseException {
        return conAttBdb.hasConcept(conceptId);
    }

    public void iterateConceptAttributeEntries(I_ProcessConceptAttributeEntries processor) throws Exception {
        conAttBdb.iterateConceptAttributeEntries(processor);
    }

    public void writeConceptAttributes(I_ConceptAttributeVersioned concept) throws DatabaseException, IOException {
        conAttBdb.writeConceptAttributes(concept);
    }

    public List<I_RelVersioned> getDestRels(int conceptId) throws DatabaseException, IOException {
        return relBdb.getDestRels(conceptId);
    }

    public I_RelVersioned getRel(int relId, int conceptId) throws DatabaseException, IOException {
        return relBdb.getRel(relId, conceptId);
    }

    public List<I_RelVersioned> getSrcRels(int conceptId) throws DatabaseException, IOException {
        return relBdb.getSrcRels(conceptId);
    }

    public boolean hasDestRel(int conceptId, Set<Integer> destRelTypeIds) throws DatabaseException, IOException {
        return relBdb.hasDestRel(conceptId, destRelTypeIds);
    }

    public boolean hasDestRels(int conceptId) throws DatabaseException {
        return relBdb.hasDestRels(conceptId);
    }

    public boolean hasDestRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet destRelTypes,
            Set<I_Position> positions) throws DatabaseException, IOException {
        return relBdb.hasDestRelTuple(conceptId, allowedStatus, destRelTypes, positions);
    }

    public boolean hasRel(int relId, int conceptId) throws DatabaseException, IOException {
        return relBdb.hasRel(relId, conceptId);
    }

    public boolean hasSrcRel(int conceptId, Set<Integer> srcRelTypeIds) throws DatabaseException, IOException {
        return relBdb.hasSrcRel(conceptId, srcRelTypeIds);
    }

    public boolean hasSrcRels(int conceptId) throws DatabaseException, IOException {
        return relBdb.hasSrcRels(conceptId);
    }

    public boolean hasSrcRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet sourceRelTypes,
            Set<I_Position> positions) throws DatabaseException, IOException {
        return relBdb.hasSrcRelTuple(conceptId, allowedStatus, sourceRelTypes, positions);
    }

    public void writeRel(I_RelVersioned rel) throws IOException, DatabaseException {
        relBdb.writeRel(rel);
    }

    public void deleteId(I_IdVersioned id) throws DatabaseException {
        identifierDb.deleteId(id);
    }

    public int getAceAuxillaryNid() {
        return identifierDb.getAceAuxillaryNid();
    }

    public int getCurrentStatusNid() {
        return identifierDb.getCurrentStatusNid();
    }

    public I_IdVersioned getId(Collection<UUID> uids) throws TerminologyException, IOException {
        return identifierDb.getId(uids);
    }

    public I_IdVersioned getId(int nativeId) throws IOException {
        return identifierDb.getId(nativeId);
    }

    public ThinIdVersioned getId(UUID uid) throws TerminologyException, IOException {
        return identifierDb.getId(uid);
    }

    public Collection<I_IdVersioned> getId(String id, int source) throws TerminologyException, IOException {
        return identifierDb.getId(id, source);
    }

    public I_IdVersioned getIdNullOk(int nativeId) throws IOException {
        return identifierDb.getIdNullOk(nativeId);
    }

    public int getMaxId() throws DatabaseException {
        return identifierDb.getMaxId();
    }

    public int getMinId() throws DatabaseException {
        return identifierDb.getMinId();
    }

    public Collection<UUID> getUids(int nativeId) throws TerminologyException, IOException {
        return identifierDb.getUids(nativeId);
    }

    public boolean hasId(Collection<UUID> uids) throws DatabaseException {
        return identifierDb.hasId(uids);
    }

    public boolean hasId(UUID uid) throws DatabaseException {
        return identifierDb.hasId(uid);
    }

    public void iterateIdEntries(I_ProcessIdEntries processor) throws Exception {
        identifierDb.iterateIdEntries(processor);
    }

    public void logIdDbStats() throws DatabaseException {
        identifierDb.logIdDbStats();
    }

    public int nativeGenerationForUuid(UUID uid, int source, int pathId, int version) throws TerminologyException,
            IOException {
        return identifierDb.nativeGenerationForUuid(uid, source, pathId, version);
    }

    public List<UUID> nativeToUuid(int nativeId) throws DatabaseException {
        return identifierDb.nativeToUuid(nativeId);
    }

    public int uuidToNative(Collection<UUID> uids) throws TerminologyException, IOException {
        return identifierDb.uuidToNative(uids);
    }

    public int uuidToNative(UUID uid) throws TerminologyException, IOException {
        return identifierDb.uuidToNative(uid);
    }

    public int uuidToNativeWithGeneration(Collection<UUID> uids, int source, I_Path idPath, int version)
            throws TerminologyException, IOException {
        return identifierDb.uuidToNativeWithGeneration(uids, source, idPath, version);
    }

    public int uuidToNativeWithGeneration(UUID uid, int source, Collection<I_Path> idPaths, int version)
            throws TerminologyException, IOException {
        return identifierDb.uuidToNativeWithGeneration(uid, source, idPaths, version);
    }

    public int uuidToNativeWithGeneration(UUID uid, int source, I_Path idPath, int version)
            throws TerminologyException, IOException {
        return identifierDb.uuidToNativeWithGeneration(uid, source, idPath, version);
    }

    public void writeId(I_IdVersioned id) throws DatabaseException {
        identifierDb.writeId(id);
    }

    public void createLuceneDescriptionIndex() throws IOException {
        descBdb.createLuceneDescriptionIndex();
    }

    public Hits doLuceneSearch(String query) throws IOException, ParseException {
        return descBdb.doLuceneSearch(query);
    }

    public I_DescriptionVersioned getDescription(int descId, int concId) throws IOException, DatabaseException {
        return descBdb.getDescription(descId, concId);
    }

    public Iterator<I_DescriptionVersioned> getDescriptionIterator() throws IOException {
        return descBdb.getDescriptionIterator();
    }

    public List<I_DescriptionVersioned> getDescriptions(int conceptId) throws DatabaseException, IOException {
        return descBdb.getDescriptions(conceptId);
    }

    public boolean hasDescription(int descId, int conId) throws DatabaseException, IOException {
        return descBdb.hasDescription(descId, conId);
    }

    public void iterateDescriptionEntries(I_ProcessDescriptionEntries processor) throws Exception {
        descBdb.iterateDescriptionEntries(processor);
    }

    public CountDownLatch searchLucene(I_TrackContinuation tracker, String query, Collection<LuceneMatch> matches,
            CountDownLatch latch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config,
            LuceneProgressUpdator updater) throws DatabaseException, IOException, ParseException {
        return descBdb.searchLucene(tracker, query, matches, latch, checkList, config, updater);
    }

    public void searchRegex(I_TrackContinuation tracker, Pattern p, Collection<I_DescriptionVersioned> matches,
            CountDownLatch latch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
            throws DatabaseException, IOException {
        descBdb.searchRegex(tracker, p, matches, latch, checkList, config);
    }

    public void writeDescription(I_DescriptionVersioned desc) throws DatabaseException, IOException {
        descBdb.writeDescription(desc);
    }

    public void iterateRelationshipEntries(I_ProcessRelationshipEntries processor) throws Exception {
        relBdb.iterateRelationshipEntries(processor);
    }

    public I_ImageVersioned getImage(int nativeId) throws DatabaseException {
        return imageBdb.getImage(nativeId);
    }

    public List<I_ImageVersioned> getImages(int conceptId) throws DatabaseException {
        return imageBdb.getImages(conceptId);
    }

    public boolean hasImage(int imageId) throws DatabaseException {
        return imageBdb.hasImage(imageId);
    }

    public void iterateImages(I_ProcessImageEntries processor) throws Exception {
        imageBdb.iterateImages(processor);
    }

    public void writeImage(I_ImageVersioned image) throws DatabaseException {
        imageBdb.writeImage(image);
    }

    public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(int componentId) throws IOException {
        return extensionBdb.getAllExtensionsForComponent(componentId);
    }

    public I_ThinExtByRefVersioned getExtension(int memberId) throws IOException {
        return extensionBdb.getExtension(memberId);
    }

    public List<I_GetExtensionData> getExtensionsForComponent(int componentId) throws IOException {
        return extensionBdb.getExtensionsForComponent(componentId);
    }

    public List<ExtensionByReferenceBean> getExtensionsForRefset(int refsetId) throws IOException {
        return extensionBdb.getExtensionsForRefset(refsetId);
    }

    public List<I_ThinExtByRefVersioned> getRefsetExtensionMembers(int refsetId) throws IOException {
        return extensionBdb.getRefsetExtensionMembers(refsetId);
    }

    public boolean hasExtension(int memberId) throws IOException {
        return extensionBdb.hasExtension(memberId);
    }

    public void iterateExtByRefEntries(I_ProcessExtByRefEntries processor) throws Exception {
        extensionBdb.iterateExtByRefEntries(processor);
    }

    public void writeExt(I_ThinExtByRefVersioned ext) throws IOException {
        extensionBdb.writeExt(ext);
    }

    public void addTimeBranchValues(Set<TimePathId> values) throws DatabaseException {
        positionBdb.addTimeBranchValues(values);
    }

    public void iterateTimeBranch(I_ProcessTimeBranchEntries processor) throws Exception {
        positionBdb.iterateTimeBranch(processor);
    }

    public void writeTimePath(TimePathId jarTimePath) throws DatabaseException {
        positionBdb.writeTimePath(jarTimePath);
    }

    public I_Path getPath(int nativeId) throws DatabaseException {
        return pathBdb.getPath(nativeId);
    }

    public boolean hasPath(int nativeId) throws DatabaseException {
        return pathBdb.hasPath(nativeId);
    }

    public void writePath(I_Path p) throws DatabaseException {
        pathBdb.writePath(p);
    }

    public void iteratePaths(I_ProcessPathEntries processor) throws Exception {
        pathBdb.iteratePaths(processor);
    }

    public String getStats() throws ToIoException {
        StatsConfig config = new StatsConfig();
        config.setClear(true);
        try {
            EnvironmentStats s = env.getStats(config);

            DecimalFormat f = new DecimalFormat("###,###,###,###,###,###,###");

            StringBuffer sb = new StringBuffer();

            sb.append("<html><br>Compression stats<br>");
            sb.append("splitBins=").append(f.format(s.getSplitBins())).append("<br>");
            sb.append("dbClosedBins=").append(f.format(s.getDbClosedBins())).append("<br>");
            sb.append("cursorsBins=").append(f.format(s.getCursorsBins())).append("<br>");
            sb.append("nonEmptyBins=").append(f.format(s.getNonEmptyBins())).append("<br>");
            sb.append("processedBins=").append(f.format(s.getProcessedBins())).append("<br>");
            sb.append("inCompQueueSize=").append(f.format(s.getInCompQueueSize())).append("<br>");

            // Evictor
            sb.append("<br>Eviction stats<br>");
            sb.append("nEvictPasses=").append(f.format(s.getNEvictPasses())).append("<br>");
            sb.append("nNodesSelected=").append(f.format(s.getNNodesSelected())).append("<br>");
            sb.append("nNodesScanned=").append(f.format(s.getNNodesScanned())).append("<br>");
            sb.append("nNodesExplicitlyEvicted=").append(f.format(s.getNNodesExplicitlyEvicted())).append("<br>");
            sb.append("nBINsStripped=").append(f.format(s.getNBINsStripped())).append("<br>");
            sb.append("requiredEvictBytes=").append(f.format(s.getRequiredEvictBytes())).append("<br>");

            // Checkpointer
            sb.append("<br>Checkpoint stats<br>");
            sb.append("nCheckpoints=").append(f.format(s.getNCheckpoints())).append("<br>");
            sb.append("lastCheckpointId=").append(f.format(s.getLastCheckpointId())).append("<br>");
            sb.append("nFullINFlush=").append(f.format(s.getNFullINFlush())).append("<br>");
            sb.append("nFullBINFlush=").append(f.format(s.getNFullBINFlush())).append("<br>");
            sb.append("nDeltaINFlush=").append(f.format(s.getNDeltaINFlush())).append("<br>");
            sb.append("lastCheckpointStart=")
                .append(DbLsn.getNoFormatString(s.getLastCheckpointStart()))
                .append("<br>");
            sb.append("lastCheckpointEnd=").append(DbLsn.getNoFormatString(s.getLastCheckpointEnd())).append("<br>");
            sb.append("endOfLog=").append(DbLsn.getNoFormatString(s.getEndOfLog())).append("<br>");

            // Cleaner
            sb.append("<br>Cleaner stats<br>");
            sb.append("cleanerBacklog=").append(f.format(s.getCleanerBacklog())).append("<br>");
            sb.append("nCleanerRuns=").append(f.format(s.getNCleanerRuns())).append("<br>");
            sb.append("nCleanerDeletions=").append(f.format(s.getNCleanerDeletions())).append("<br>");
            sb.append("nINsObsolete=").append(f.format(s.getNINsObsolete())).append("<br>");
            sb.append("nINsCleaned=").append(f.format(s.getNINsCleaned())).append("<br>");
            sb.append("nINsDead=").append(f.format(s.getNINsDead())).append("<br>");
            sb.append("nINsMigrated=").append(f.format(s.getNINsMigrated())).append("<br>");
            sb.append("nLNsObsolete=").append(f.format(s.getNLNsObsolete())).append("<br>");
            sb.append("nLNsCleaned=").append(f.format(s.getNLNsCleaned())).append("<br>");
            sb.append("nLNsDead=").append(f.format(s.getNLNsDead())).append("<br>");
            sb.append("nLNsLocked=").append(f.format(s.getNLNsLocked())).append("<br>");
            sb.append("nLNsMigrated=").append(f.format(s.getNLNsMigrated())).append("<br>");
            sb.append("nLNsMarked=").append(f.format(s.getNLNsMarked())).append("<br>");
            sb.append("nLNQueueHits=").append(f.format(s.getNLNQueueHits())).append("<br>");
            sb.append("nPendingLNsProcessed=").append(f.format(s.getNPendingLNsProcessed())).append("<br>");
            sb.append("nMarkedLNsProcessed=").append(f.format(s.getNMarkedLNsProcessed())).append("<br>");
            sb.append("nToBeCleanedLNsProcessed=").append(f.format(s.getNToBeCleanedLNsProcessed())).append("<br>");
            sb.append("nClusterLNsProcessed=").append(f.format(s.getNClusterLNsProcessed())).append("<br>");
            sb.append("nPendingLNsLocked=").append(f.format(s.getNPendingLNsLocked())).append("<br>");
            sb.append("nCleanerEntriesRead=").append(f.format(s.getNCleanerEntriesRead())).append("<br>");

            // Cache
            sb.append("<br>Cache stats<br>");
            sb.append("nNotResident=").append(f.format(s.getNNotResident())).append("<br>");
            sb.append("nCacheMiss=").append(f.format(s.getNCacheMiss())).append("<br>");
            sb.append("nLogBuffers=").append(f.format(s.getNLogBuffers())).append("<br>");
            sb.append("bufferBytes=").append(f.format(s.getBufferBytes())).append("<br>");
            sb.append("adminBytes=").append(f.format(s.getAdminBytes())).append("<br>");
            sb.append("lockBytes=").append(f.format(s.getLockBytes())).append("<br>");
            sb.append("cacheTotalBytes=").append(f.format(s.getCacheTotalBytes())).append("<br>");

            // Logging
            sb.append("<br>Logging stats<br>");
            sb.append("nFSyncs=").append(f.format(s.getNFSyncs())).append("<br>");
            sb.append("nFSyncRequests=").append(f.format(s.getNFSyncRequests())).append("<br>");
            sb.append("nFSyncTimeouts=").append(f.format(s.getNFSyncTimeouts())).append("<br>");
            sb.append("nRepeatFaultReads=").append(f.format(s.getNRepeatFaultReads())).append("<br>");
            sb.append("nTempBufferWrite=").append(f.format(s.getNTempBufferWrites())).append("<br>");
            sb.append("nRepeatIteratorReads=").append(f.format(s.getNRepeatIteratorReads())).append("<br>");
            sb.append("totalLogSize=").append(f.format(s.getTotalLogSize())).append("<br>");

            return sb.toString();
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public Transaction beginTransaction() throws DatabaseException {
        return env.beginTransaction(null, null);
    }

    public void cleanupSNOMED(I_IntSet relsToIgnore, I_IntSet releases) throws Exception {
        relBdb.cleanupSNOMED(relsToIgnore, releases);
    }

    public I_ConceptAttributeVersioned conAttrEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return conAttBdb.conAttrEntryToObject(key, value);
    }

    public I_DescriptionVersioned descEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return descBdb.descEntryToObject(key, value);
    }

    public I_ThinExtByRefVersioned extEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return extensionBdb.extEntryToObject(key, value);
    }

    public I_IdVersioned idEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return identifierDb.idEntryToObject(key, value);
    }

    public I_ImageVersioned imageEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return imageBdb.imageEntryToObject(key, value);
    }

    public I_Path pathEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return pathBdb.pathEntryToObject(key, value);
    }

    public I_RelVersioned relEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return relBdb.relEntryToObject(key, value);
    }

    public void writeDescriptionNoLuceneUpdate(I_DescriptionVersioned desc) throws DatabaseException, IOException {
        descBdb.writeDescriptionNoLuceneUpdate(desc);
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException, IOException {
        for (I_StoreInBdb db : databases) {
            db.commit(bean, version, values);
        }

    }

    public static int getDatabaseVersion() {
        return databaseVersion;
    }

    public Iterator<I_RelVersioned> getRelationshipIterator() throws IOException {
        return relBdb.getRelationshipIterator();
    }

    public void setupBean(ConceptBean cb) throws IOException {
        for (I_StoreInBdb db : databases) {
            db.setupBean(cb);
        }
    }

    public void compress(int utilization) throws IOException {

        try {

            String lookAheadCacheSize = env.getConfig().getConfigParam("je.cleaner.lookAheadCacheSize");
            env.getConfig().setConfigParam("je.cleaner.lookAheadCacheSize", "81920");

            String cluster = env.getConfig().getConfigParam("je.cleaner.cluster");
            env.getConfig().setConfigParam("je.cleaner.cluster", "true");

            String minFileUtilization = env.getConfig().getConfigParam("je.cleaner.minFileUtilization");
            env.getConfig().setConfigParam("je.cleaner.minFileUtilization", Integer.toString(50));

            String minUtilization = env.getConfig().getConfigParam("je.cleaner.minUtilization");
            env.getConfig().setConfigParam("je.cleaner.minUtilization", Integer.toString(utilization));

            String threads = env.getConfig().getConfigParam("je.cleaner.threads");
            env.getConfig().setConfigParam("je.cleaner.threads", "4");

            boolean anyCleaned = false;
            while (env.cleanLog() > 0) {
                anyCleaned = true;
            }
            if (anyCleaned) {
                CheckpointConfig force = new CheckpointConfig();
                force.setForce(true);
                env.checkpoint(force);
            }

            env.getConfig().setConfigParam("je.cleaner.lookAheadCacheSize", lookAheadCacheSize);
            env.getConfig().setConfigParam("je.cleaner.cluster", cluster);
            env.getConfig().setConfigParam("je.cleaner.minFileUtilization", minFileUtilization);
            env.getConfig().setConfigParam("je.cleaner.minUtilization", minUtilization);
            env.getConfig().setConfigParam("je.cleaner.threads", threads);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public int getConceptCount() throws DatabaseException {
        return conAttBdb.getConceptCount();
    }

    public void searchConcepts(I_TrackContinuation tracker, IntList matches, CountDownLatch latch,
            List<I_TestSearchResults> checkList, I_ConfigAceFrame config) throws DatabaseException, IOException,
            ParseException {
        conAttBdb.searchConcepts(tracker, matches, latch, checkList, config);
    }

    public IdentifierSet getConceptIdSet() throws IOException {
        return conAttBdb.getConceptIdSet();
    }

    public IdentifierSet getEmptyIdSet() throws IOException {
        return conAttBdb.getEmptyIdSet();
    }

    public I_RepresentIdSet getIdSetFromIntCollection(Collection<Integer> ids) throws IOException {
        return conAttBdb.getIdSetFromIntCollection(ids);
    }

    public I_RepresentIdSet getIdSetfromTermCollection(Collection<? extends I_AmTermComponent> components)
            throws IOException {
        return conAttBdb.getIdSetfromTermCollection(components);
    }

    public I_RepresentIdSet getReadOnlyConceptIdSet() throws IOException {
        return conAttBdb.getReadOnlyConceptIdSet();
    }

    public IdentifierSet getRelationshipIdSet() throws IOException {
        return conAttBdb.getRelationshipIdSet();
    }

    public IdentifierSet getDescriptionIdSet() throws IOException {
        return conAttBdb.getDescriptionIdSet();
    }

}
