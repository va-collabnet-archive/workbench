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
package org.dwfa.vodb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.JEVersion;
import com.sleepycat.je.Transaction;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.activity.UpperInfoOnlyConsoleMonitor;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HandleSubversion;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConceptAttributes;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_ProcessDescriptions;
import org.dwfa.ace.api.I_ProcessExtByRef;
import org.dwfa.ace.api.I_ProcessIds;
import org.dwfa.ace.api.I_ProcessImages;
import org.dwfa.ace.api.I_ProcessPaths;
import org.dwfa.ace.api.I_ProcessRelationships;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_SupportClassifier;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.api.process.I_ProcessQueue;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.config.AceRunner;
import org.dwfa.ace.cs.BinaryChangeSetReader;
import org.dwfa.ace.cs.BinaryChangeSetWriter;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.app.DwfaEnv;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.svn.Svn;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.SuppressDataChecks;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.impl.BdbEnv;
import org.dwfa.vodb.jar.TimePathCollector;
import org.dwfa.vodb.process.ProcessQueue;
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
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinExtByRefPartBoolean;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConceptConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConceptString;
import org.dwfa.vodb.types.ThinExtByRefPartConceptString;
import org.dwfa.vodb.types.ThinExtByRefPartInteger;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.dwfa.vodb.types.ThinExtByRefPartLanguageScoped;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefVersioned;
import org.dwfa.vodb.types.ThinIdPart;
import org.dwfa.vodb.types.ThinIdVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author kec
 *
 */
public class VodbEnv implements I_ImplementTermFactory, I_SupportClassifier, I_WriteDirectToDb {
    private static Logger logger = Logger.getLogger(VodbEnv.class.getName());

    private static boolean readOnly;

    private File luceneDir;

    private boolean stealth = false;

    public VodbEnv() {
    }

    public VodbEnv(boolean stealth) {
        this.stealth = stealth;
    }

    private static boolean transactional = false;

    private static boolean deferredWrite = true;

    private static boolean txnNoSync = false;

    private static long transactionTimeout = 0;

    private I_ShowActivity activityFrame;

    private File envHome;

    private BdbEnv bdbEnv;

    private TupleBinding<Integer> intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    public static boolean isHeadless() {
        return DwfaEnv.isHeadless();
    }

    public static void setHeadless(Boolean headless) {
        DwfaEnv.setHeadless(headless);
    }

    public void setup(Object envHome, boolean readOnly, Long cacheSize) throws ToIoException {
        try {
            setup((File) envHome, readOnly, cacheSize);
        } catch (Exception e) {
            throw new ToIoException(e);
        }
    }

    public void setup(Object envHome, boolean readOnly, Long cacheSize, DatabaseSetupConfig dbSetupConfig)
            throws IOException {
        try {
            setup((File) envHome, readOnly, cacheSize, dbSetupConfig);
        } catch (Exception e) {
            throw new ToIoException(e);
        }
    }

    private class ShutdownThread extends Thread {

        public ShutdownThread() {
            super("Vodb Shutdown Thread");
        }

        public void run() {
            try {
                bdbEnv.cancelTransaction();
                if (!closed) {
                    sync();
                }
            } catch (IOException e) {
                AceLog.getEditLog().alertAndLogException(e);
            }
        }
    }

    /**
     * @throws Exception
     */
    public void setup(File envHome, boolean readOnly, Long cacheSize) throws Exception {
        setup(envHome, readOnly, cacheSize, null);
    }

    public void setup(File envHome, boolean readOnly) throws Exception {
        setup(envHome, readOnly, null, null);
    }

    public void setup(File envHome, boolean readOnly, Long cacheSize, DatabaseSetupConfig dbSetupConfig)
            throws IOException {
        try {
            if (envHome.exists() == false) {
                if (dbSetupConfig == null) {
                    throw new IOException("dbSetupConfig cannot be null for new databases...");
                }
            }
            Runtime.getRuntime().addShutdownHook(new ShutdownThread());
            long startTime = System.currentTimeMillis();
            this.envHome = envHome;
            if (isHeadless()) {
                activityFrame = new UpperInfoOnlyConsoleMonitor();
            } else {
                activityFrame = new ActivityPanel(true, null, null);
            }

            AceLog.getAppLog().info("Setting up db: " + envHome);
            activityFrame.setIndeterminate(true);
            activityFrame.setProgressInfoUpper("Loading the terminology");
            activityFrame.setProgressInfoLower("Setting up the environment...");
            activityFrame.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("System.exit from activity action listener: " + e.getActionCommand());
                    System.exit(0);
                }
            });
            try {
                ActivityViewer.addActivity(activityFrame);
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }

            VodbEnv.readOnly = readOnly;
            envHome.mkdirs();
            luceneDir = new File(envHome, "lucene");

            AceLog.getAppLog().info(
                "Setup transactional: " + transactional + " txnNoSync: " + txnNoSync + " deferredWrite: "
                    + isDeferredWrite() + " transactionTimeout: " + transactionTimeout);

            AceLog.getAppLog().info("Berkeley DB info: " + JEVersion.CURRENT_VERSION.getVersionString());

            bdbEnv = new BdbEnv(this, envHome, readOnly, null, luceneDir, dbSetupConfig);
            if (this.stealth == false) {
                LocalVersionedTerminology.set(this);
            }
            LocalFixedTerminology.setStore(new VodbFixedServer(this));

            activityFrame.setProgressInfoLower("complete");
            activityFrame.complete();
            long loadTime = System.currentTimeMillis() - startTime;
            logger.info("### Load time: " + loadTime + " ms");
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void sync() throws IOException {
        try {
            if (bdbEnv != null) {
                bdbEnv.sync();
            }
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    boolean closed = false;

    public void close() throws IOException {
        try {
            bdbEnv.close();
            closed = true;
            ConceptBean.purge();
            ExtensionByReferenceBean.purge();
            LocalVersionedTerminology.close(this);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public I_ConceptAttributeVersioned getConceptAttributes(int conceptId) throws IOException {
        return bdbEnv.getConceptAttributes(conceptId);
    }

    public I_DescriptionVersioned getDescription(int descId, int concId) throws IOException {
        try {
            return bdbEnv.getDescription(descId, concId);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    /**
     * @see org.dwfa.ace.api.I_TermFactory#getDescription(java.lang.String)
     */
    // TODO needs to fixed for the refactored id database.
    @SuppressWarnings("deprecation")
    public I_DescriptionVersioned getDescription(String descriptionId) throws TerminologyException, ParseException,
            IOException {

        Hits hits = doLuceneSearch(descriptionId);

        if (hits == null || hits.length() == 0) {
            throw new TerminologyException("Search produced no results");
        }

        // ensure only one match.
        if (hits.length() == 1) {
            Document doc = hits.doc(0);
            int dnid = Integer.parseInt(doc.get("dnid"));
            int cnid = Integer.parseInt(doc.get("cnid"));

            I_DescriptionVersioned description = getDescription(dnid, cnid);
            return description;
        }

        throw new TerminologyException("More that one description matched the id " + descriptionId);
    }

    public String getProperty(String key) throws IOException {
        return bdbEnv.getProperty(key);
    }

    public Map<String, String> getProperties() throws IOException {
        return bdbEnv.getProperties();
    }

    public void setProperty(String key, String value) throws IOException {
        bdbEnv.setProperty(key, value);
    }

    public boolean hasDescription(int descId, int conId) throws IOException {
        try {
            return bdbEnv.hasDescription(descId, conId);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public boolean hasRel(int relId, int conceptId) throws IOException {
        try {
            return bdbEnv.hasRel(relId, conceptId);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public I_RelVersioned getRel(int relId, int conceptId) throws DatabaseException, IOException {
        return bdbEnv.getRel(relId, conceptId);
    }

    public I_ThinExtByRefVersioned getExtension(int memberId) throws IOException {
        return bdbEnv.getExtension(memberId);
    }

    public void removeFromCacheAndRollbackTransaction(final int memberId) throws IOException {
        removeFromExtensionCache(memberId);
        cancel();
    }

    private void removeFromExtensionCache(final int memberId) throws IOException {
        ((ExtensionByReferenceBean)getExtensionWrapper(memberId)).removeFromCache();
    }

    public boolean hasExtension(int memberId) throws IOException {
        return bdbEnv.hasExtension(memberId);
    }

    public boolean hasConcept(int conceptId) throws IOException {
        try {
            return bdbEnv.hasConcept(conceptId);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public List<I_DescriptionVersioned> getDescriptions(int conceptId) throws DatabaseException, IOException {
        return bdbEnv.getDescriptions(conceptId);
    }

    public boolean hasDestRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet destRelTypes,
            Set<I_Position> positions) throws DatabaseException, IOException {
        return bdbEnv.hasDestRelTuple(conceptId, allowedStatus, destRelTypes, positions);
    }

    public List<I_RelVersioned> getDestRels(int conceptId) throws DatabaseException, IOException {
        return bdbEnv.getDestRels(conceptId);
    }

    public List<I_GetExtensionData> getExtensionsForComponent(int componentId) throws IOException {
        return bdbEnv.getExtensionsForComponent(componentId);
    }

    public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(int componentId, boolean addUncommitted)
            throws IOException {
        List<I_ThinExtByRefVersioned> extensions = getAllExtensionsForComponent(componentId);

        if (addUncommitted) {
            for (I_GetExtensionData wrapper : ExtensionByReferenceBean.getNewExtensions(componentId)) {
                extensions.add(wrapper.getExtension());
            }
        }
        return extensions;
    }

    public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(int componentId) throws IOException {
        return bdbEnv.getAllExtensionsForComponent(componentId);
    }

    public List<ExtensionByReferenceBean> getExtensionsForRefset(int refsetId) throws IOException {
        return bdbEnv.getExtensionsForRefset(refsetId);
    }

    public boolean hasDestRels(int conceptId) throws DatabaseException {
        return bdbEnv.hasDestRels(conceptId);
    }

    public boolean hasDestRel(int conceptId, Set<Integer> destRelTypeIds) throws DatabaseException, IOException {
        return bdbEnv.hasDestRel(conceptId, destRelTypeIds);
    }

    public List<I_RelVersioned> getSrcRels(int conceptId) throws DatabaseException, IOException {
        return bdbEnv.getSrcRels(conceptId);
    }

    public boolean hasSrcRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet sourceRelTypes,
            Set<I_Position> positions) throws DatabaseException, IOException {
        return bdbEnv.hasSrcRelTuple(conceptId, allowedStatus, sourceRelTypes, positions);
    }

    public boolean hasSrcRels(int conceptId) throws DatabaseException, IOException {
        return bdbEnv.hasSrcRels(conceptId);
    }

    public boolean hasSrcRel(int conceptId, Set<Integer> srcRelTypeIds) throws DatabaseException, IOException {
        return bdbEnv.hasSrcRel(conceptId, srcRelTypeIds);
    }

    /**
     * This method is multithreaded hot.
     *
     * @param continueWork
     * @param p
     * @param matches
     * @param latch
     * @throws DatabaseException
     * @throws IOException
     */
    public void searchRegex(I_TrackContinuation tracker, Pattern p, Collection<I_DescriptionVersioned> matches,
            CountDownLatch latch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
            throws DatabaseException, IOException {
        bdbEnv.searchRegex(tracker, p, matches, latch, checkList, config);
    }

    /*
     * For issues upgrading to lucene 2.x, see this link:
     * http://www.nabble.com/Lucene-in-Action-examples-complie-problem-tf2418478.html#a6743189
     */

    public CountDownLatch searchLucene(I_TrackContinuation tracker, String query, Collection<LuceneMatch> matches,
            CountDownLatch latch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config,
            LuceneProgressUpdator updater) throws DatabaseException, IOException, ParseException {
        return bdbEnv.searchLucene(tracker, query, matches, latch, checkList, config, updater);
    }

    public void createLuceneDescriptionIndex() throws IOException {
        bdbEnv.createLuceneDescriptionIndex();
    }

    public void addPositions(Set<TimePathId> values) throws DatabaseException {
        bdbEnv.addTimeBranchValues(values);
    }

    public void populatePositions() throws Exception {
        Set<TimePathId> values = new HashSet<TimePathId>();
        final DescChangesProcessor p = new DescChangesProcessor(values);
        final CountDownLatch latch = new CountDownLatch(4);
        new Thread() {
            public void run() {
                try {
                    iterateDescriptions(p);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                latch.countDown();
            }
        }.start();
        new Thread() {
            public void run() {
                try {
                    iterateConceptAttributes(p);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                latch.countDown();
            }
        }.start();
        new Thread() {
            public void run() {
                try {
                    iterateRelationships(p);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                latch.countDown();
            }
        }.start();
        new Thread() {
            public void run() {
                try {
                    iterateExtByRefs(p);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                latch.countDown();
            }
        }.start();
        latch.await();
        addPositions(values);
    }

    private class DescChangesProcessor implements I_ProcessDescriptions, I_ProcessConceptAttributes,
            I_ProcessRelationships, I_ProcessExtByRef {
        Set<TimePathId> values;

        public DescChangesProcessor(Set<TimePathId> values) {
            super();
            this.values = values;
        }

        public void processDescription(I_DescriptionVersioned desc) throws Exception {
            for (I_DescriptionPart d : desc.getVersions()) {
                TimePathId tb = new TimePathId(d.getVersion(), d.getPathId());
                synchronized (values) {
                    values.add(tb);
                }
            }
        }

        public void processConceptAttributes(I_ConceptAttributeVersioned conc) throws Exception {
            if (conc != null) {
                if (conc.getVersions() != null) {
                    for (I_ConceptAttributePart c : conc.getVersions()) {
                        TimePathId tb = new TimePathId(c.getVersion(), c.getPathId());
                        synchronized (values) {
                            values.add(tb);
                        }
                    }
                } else {
                    AceLog.getAppLog().warning("null concept versions for: " + ConceptBean.get(conc.getConId()));
                }
            } else {
                throw new Exception("concept is null");
            }
        }

        public void processRelationship(I_RelVersioned rel) throws Exception {
            for (I_RelPart r : rel.getVersions()) {
                TimePathId tb = new TimePathId(r.getVersion(), r.getPathId());
                synchronized (values) {
                    values.add(tb);
                }
            }
        }

        public void processExtensionByReference(I_ThinExtByRefVersioned ext) throws Exception {
            for (I_ThinExtByRefPart extPart : ext.getVersions()) {
                TimePathId tb = new TimePathId(extPart.getVersion(), extPart.getPathId());
                synchronized (values) {
                    values.add(tb);
                }
            }
        }

    }

    public void iterateDescriptionEntries(I_ProcessDescriptionEntries processor) throws Exception {
        bdbEnv.iterateDescriptionEntries(processor);
    }

    public void iterateRelationshipsEntries(I_ProcessRelationshipEntries processor) throws Exception {
        bdbEnv.iterateRelationshipEntries(processor);
    }

    public void iterateExtByRefEntries(I_ProcessExtByRefEntries processor) throws Exception {
        bdbEnv.iterateExtByRefEntries(processor);
    }

    public List<I_ThinExtByRefVersioned> getRefsetExtensionMembers(int refsetId) throws IOException {
        return bdbEnv.getRefsetExtensionMembers(refsetId);
    }

    public Iterator<I_GetConceptData> getConceptIterator() throws IOException {
        return bdbEnv.getConceptIterator();
    }

    public void iterateConceptAttributeEntries(I_ProcessConceptAttributeEntries processor) throws Exception {
        bdbEnv.iterateConceptAttributeEntries(processor);
    }

    public void iterateImages(I_ProcessImageEntries processor) throws Exception {
        bdbEnv.iterateImages(processor);
    }

    public void iteratePaths(I_ProcessPathEntries processor) throws Exception {
        bdbEnv.iteratePaths(processor);
    }

    public void iterateTimeBranch(I_ProcessTimeBranchEntries processor) throws Exception {
        bdbEnv.iterateTimeBranch(processor);
    }

    public int getCurrentStatusNid() {
        return PrimordialId.CURRENT_ID.getNativeId(Integer.MIN_VALUE);
    }

    public int getAceAuxillaryNid() {
        return PrimordialId.ACE_AUXILIARY_ID.getNativeId(Integer.MIN_VALUE);
    }

    public Class<Integer> getNativeIdClass() {
        return Integer.class;
    }

    public void writeImage(I_ImageVersioned image) throws IOException {
        try {
            bdbEnv.writeImage(image);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void writeConceptAttributes(I_ConceptAttributeVersioned concept) throws IOException {
        try {
            bdbEnv.writeConceptAttributes(concept);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void writeRel(I_RelVersioned rel) throws IOException {
        ACE.commitSequence++;
        try {
            bdbEnv.writeRel(rel);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void writeExt(I_ThinExtByRefVersioned ext) throws IOException {
        bdbEnv.writeExt(ext);
    }

    public void writeDescription(I_DescriptionVersioned desc) throws IOException {
        try {
            bdbEnv.writeDescription(desc);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void writePath(I_Path p) throws IOException {
        try {
            new PathManager().write(p);
        } catch (Exception e) {
            throw new ToIoException(e);
        }
    }

    public void writePathOrigin(I_Path path, I_Position origin) throws TerminologyException {
        new PathManager().writeOrigin(path, origin);
    }

    public I_Path getPath(int nativeId) throws TerminologyException {
        return new PathManager().get(nativeId);
    }

    public boolean hasPath(int nativeId) throws IOException {
        try {
            return new PathManager().exists(nativeId);
        } catch (Exception e) {
            throw new ToIoException(e);
        }
    }

    public I_ImageVersioned getImage(UUID uid) throws TerminologyException, IOException, DatabaseException {
        return getImage(uuidToNative(uid));
    }

    public boolean hasImage(int imageId) throws IOException {
        try {
            return bdbEnv.hasImage(imageId);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public I_ImageVersioned getImage(int nativeId) throws DatabaseException {
        return bdbEnv.getImage(nativeId);
    }

    public List<I_ImageVersioned> getImages(int conceptId) throws DatabaseException {
        return bdbEnv.getImages(conceptId);
    }

    public List<TimePathId> getTimePathList() throws Exception {
        TimePathCollector tpCollector = new TimePathCollector();
        iterateTimeBranch(tpCollector);
        return tpCollector.getTimePathIdList();
    }

    public List<I_Path> getPaths() throws Exception {
        ArrayList<I_Path> paths = new ArrayList<I_Path>();
        paths.addAll(new PathManager().getAll());
        return paths;
    }

    public void writeTimePath(TimePathId jarTimePath) throws IOException {
        try {
            bdbEnv.writeTimePath(jarTimePath);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void forget(I_GetConceptData concept) {
        try {
            AceLog.getEditLog().info("Forgetting: " + concept.getUids());
        } catch (IOException e) {
            AceLog.getEditLog().alertAndLogException(e);
        }
        ACE.removeUncommitted((I_Transact) concept);
    }

    public void forget(I_DescriptionVersioned desc) {
        throw new UnsupportedOperationException();

    }

    public void forget(I_RelVersioned rel) {
        throw new UnsupportedOperationException();

    }

    public LogWithAlerts getEditLog() {
        return AceLog.getEditLog();
    }

    public I_GetConceptData newConceptBypassCommit(int conceptNid) throws IOException {
        ConceptBean newBean = ConceptBean.get(conceptNid);
        newBean.setPrimordial(true);
        return newBean;
    }

    public I_GetConceptData newConcept(UUID newConceptId, boolean defined, I_ConfigAceFrame aceFrameConfig)
            throws TerminologyException, IOException {
        canEdit(aceFrameConfig);
        int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
        int nid =
                uuidToNativeWithGeneration(newConceptId, idSource, aceFrameConfig.getEditingPathSet(),
                    Integer.MAX_VALUE);
        AceLog.getEditLog().info("Creating new concept: " + newConceptId + " (" + nid + ") defined: " + defined);
        ConceptBean newBean = ConceptBean.get(nid);
        newBean.setPrimordial(true);
        int status = aceFrameConfig.getDefaultStatus().getConceptId();
        ThinConVersioned conceptAttributes = new ThinConVersioned(nid, aceFrameConfig.getEditingPathSet().size());
        for (I_Path p : aceFrameConfig.getEditingPathSet()) {
            ThinConPart attributePart = new ThinConPart();
            attributePart.setVersion(Integer.MAX_VALUE);
            attributePart.setDefined(defined);
            attributePart.setPathId(p.getConceptId());
            attributePart.setStatusId(status);
            conceptAttributes.addVersion(attributePart);
        }
        newBean.setUncommittedConceptAttributes(conceptAttributes);
        newBean.getUncommittedIds().add(nid);
        addUncommitted((I_Transact) newBean);
        return newBean;
    }

    public I_DescriptionVersioned newDescriptionBypassCommit(int descriptionNid, int conceptNid) throws IOException {
        return new ThinDescVersioned(descriptionNid, conceptNid, 1);
    }

    public I_DescriptionVersioned newDescription(UUID newDescriptionId, I_GetConceptData concept, String lang,
            String text, I_GetConceptData descType, I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
            IOException {
        canEdit(aceFrameConfig);
        addUncommitted((I_Transact) concept);
        int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
        int descId =
                uuidToNativeWithGeneration(newDescriptionId, idSource, aceFrameConfig.getEditingPathSet(),
                    Integer.MAX_VALUE);
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("Creating new description: " + newDescriptionId + " (" + descId + "): " + text);
        }
        ThinDescVersioned desc =
                new ThinDescVersioned(descId, concept.getConceptId(), aceFrameConfig.getEditingPathSet().size());
        boolean capStatus = false;
        int status = aceFrameConfig.getDefaultStatus().getConceptId();
        for (I_Path p : aceFrameConfig.getEditingPathSet()) {
            ThinDescPart descPart = new ThinDescPart();
            descPart.setVersion(Integer.MAX_VALUE);
            descPart.setPathId(p.getConceptId());
            descPart.setInitialCaseSignificant(capStatus);
            descPart.setLang(lang);
            descPart.setStatusId(status);
            descPart.setText(text);
            descPart.setTypeId(descType.getConceptId());
            desc.addVersion(descPart);
        }
        concept.getUncommittedDescriptions().add(desc);
        concept.getUncommittedIds().add(descId);
        return desc;
    }

    public I_DescriptionVersioned newDescription(UUID newDescriptionId, I_GetConceptData concept, String lang,
            String text, I_ConceptualizeLocally descType, I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
            IOException {
        return newDescription(newDescriptionId, concept, lang, text, ConceptBean.get(descType.getNid()), aceFrameConfig);
    }

    private void canEdit(I_ConfigAceFrame aceFrameConfig) throws TerminologyException {
        if (aceFrameConfig.getEditingPathSet().size() == 0) {
            throw new TerminologyException(
                "<br><br>You must select an editing path before editing...<br><br>No editing path selected.");
        }
    }

    public I_RelVersioned newRelationship(UUID newRelUid, I_GetConceptData concept, I_ConfigAceFrame aceFrameConfig)
            throws TerminologyException, IOException {
        canEdit(aceFrameConfig);
        if (aceFrameConfig.getHierarchySelection() == null) {
            throw new TerminologyException(
                "<br><br>To create a new relationship, you must<br>select the rel destination in the hierarchy view....");
        }
        ACE.commitSequence++;
        int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
        int relId =
                uuidToNativeWithGeneration(newRelUid, idSource, aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine(
                "Creating new relationship 1: " + newRelUid + " (" + relId + ") from " + concept.getUids() + " to "
                    + aceFrameConfig.getHierarchySelection().getUids());
        }
        ThinRelVersioned rel =
                new ThinRelVersioned(relId, concept.getConceptId(), aceFrameConfig.getHierarchySelection()
                    .getConceptId(), 1);
        int status = aceFrameConfig.getDefaultStatus().getConceptId();
        for (I_Path p : aceFrameConfig.getEditingPathSet()) {
            ThinRelPart relPart = new ThinRelPart();
            relPart.setVersion(Integer.MAX_VALUE);
            relPart.setPathId(p.getConceptId());
            relPart.setStatusId(status);
            relPart.setTypeId(aceFrameConfig.getDefaultRelationshipType().getConceptId());
            relPart.setCharacteristicId(aceFrameConfig.getDefaultRelationshipCharacteristic().getConceptId());
            relPart.setRefinabilityId(aceFrameConfig.getDefaultRelationshipRefinability().getConceptId());
            relPart.setGroup(0);
            rel.addVersion(relPart);
        }
        concept.getUncommittedSourceRels().add(rel);
        concept.getUncommittedIds().add(relId);
        addUncommitted((I_Transact) concept);
        return rel;

    }

    public I_RelVersioned newRelationship(UUID newRelUid, I_GetConceptData concept, I_GetConceptData relType,
            I_GetConceptData relDestination, I_GetConceptData relCharacteristic, I_GetConceptData relRefinability,
            I_GetConceptData relStatus, int relGroup, I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
            IOException {
        canEdit(aceFrameConfig);
        ACE.commitSequence++;
        int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());

        int relId =
                uuidToNativeWithGeneration(newRelUid, idSource, aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine(
                "Creating new relationship 2: " + newRelUid + " (" + relId + ") from " + concept.getUids() + " to "
                    + relDestination.getUids());
        }
        ThinRelVersioned rel =
                new ThinRelVersioned(relId, concept.getConceptId(), relDestination.getConceptId(), aceFrameConfig
                    .getEditingPathSet().size());

        ThinRelPart relPart = new ThinRelPart();

        rel.addVersion(relPart);

        int status = relStatus.getConceptId();

        for (I_Path p : aceFrameConfig.getEditingPathSet()) {
            relPart.setVersion(Integer.MAX_VALUE);
            relPart.setPathId(p.getConceptId());
            relPart.setStatusId(status);
            relPart.setTypeId(relType.getConceptId());
            relPart.setCharacteristicId(relCharacteristic.getConceptId());
            relPart.setRefinabilityId(relRefinability.getConceptId());
            relPart.setGroup(relGroup);
        }
        concept.getUncommittedSourceRels().add(rel);
        concept.getUncommittedIds().add(relId);
        addUncommitted((I_Transact) concept);
        return rel;

    }

    public I_GetConceptData getConcept(Collection<UUID> ids) throws TerminologyException, IOException {
        return ConceptBean.get(ids);
    }

    public I_GetConceptData getConcept(UUID... ids) throws TerminologyException, IOException {
        return ConceptBean.get(Arrays.asList(ids));
    }

    public I_Position newPosition(I_Path path, int version) {
        return new Position(version, path);
    }

    public I_IntSet newIntSet() {
        return new IntSet();
    }

    public void addUncommitted(I_GetConceptData concept) {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("Adding uncommitted " + concept + " from vodb: " + this);
        }
        ACE.addUncommitted((I_Transact) concept);
    }

    public void addUncommitted(I_ThinExtByRefVersioned extension) {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("Adding uncommitted extension " + extension + " from vodb: " + this);
        }
        ACE.addUncommitted(ExtensionByReferenceBean.make(extension.getMemberId(), extension));
    }

    public void addUncommittedNoChecks(I_GetConceptData concept) {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("Adding uncommittedNoChecks " + concept + " from vodb: " + this);
        }
        ACE.addUncommittedNoChecks((I_Transact) concept);
    }

    public void addUncommittedNoChecks(I_ThinExtByRefVersioned extension) {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("Adding uncommittedNoChecks extension " + extension + " from vodb: " + this);
        }
        ACE.addUncommittedNoChecks(ExtensionByReferenceBean.make(extension.getMemberId(), extension));
    }

    public void loadFromDirectory(File dataDir, String encoding) throws Exception {
        LoadBdb.loadFromDirectory(dataDir, encoding);
    }

    /**
     *
     * @param args
     * @throws Exception
     * @deprecated use loadFromSingleJar
     */
    @Override public void loadFromMultipleJars(final String[] args, final File unjaringDir) throws Exception {
        LoadBdb.main(args, unjaringDir);
    }

    private class ProcessorWrapper implements I_ProcessDescriptionEntries, I_ProcessConceptAttributeEntries,
            I_ProcessRelationshipEntries, I_ProcessIdEntries, I_ProcessImageEntries, I_ProcessExtByRefEntries {

        I_ProcessConceptAttributes conceptAttributeProcessor;

        I_ProcessDescriptions descProcessor;

        I_ProcessRelationships relProcessor;

        I_ProcessIds idProcessor;

        I_ProcessImages imageProcessor;


        I_ProcessExtByRef extProcessor;

        public ProcessorWrapper() {
            super();
        }

        public void processDesc(DatabaseEntry key, DatabaseEntry value) throws Exception {
            I_DescriptionVersioned desc = bdbEnv.descEntryToObject(key, value);
            descProcessor.processDescription(desc);
        }

        public void processConceptAttributeEntry(DatabaseEntry key, DatabaseEntry value) throws Exception {
            I_ConceptAttributeVersioned conc = bdbEnv.conAttrEntryToObject(key, value);
            conceptAttributeProcessor.processConceptAttributes(conc);
        }

        public void processRel(DatabaseEntry key, DatabaseEntry value) throws Exception {
            I_RelVersioned rel = bdbEnv.relEntryToObject(key, value);
            relProcessor.processRelationship(rel);
        }

        public DatabaseEntry getDataEntry() {
            return new DatabaseEntry();
        }

        public DatabaseEntry getKeyEntry() {
            return new DatabaseEntry();
        }

        public I_ProcessConceptAttributes getConceptAttributeProcessor() {
            return conceptAttributeProcessor;
        }

        public void setConceptAttributeProcessor(I_ProcessConceptAttributes conceptAttributeProcessor) {
            this.conceptAttributeProcessor = conceptAttributeProcessor;
        }

        public I_ProcessDescriptions getDescProcessor() {
            return descProcessor;
        }

        public void setDescProcessor(I_ProcessDescriptions descProcessor) {
            this.descProcessor = descProcessor;
        }

        public I_ProcessRelationships getRelProcessor() {
            return relProcessor;
        }

        public void setRelProcessor(I_ProcessRelationships relProcessor) {
            this.relProcessor = relProcessor;
        }

        public org.dwfa.ace.api.I_ProcessIds getIdProcessor() {
            return idProcessor;
        }

        public void setIdProcessor(org.dwfa.ace.api.I_ProcessIds processor) {
            this.idProcessor = processor;
        }

        public void processId(DatabaseEntry key, DatabaseEntry value) throws Exception {
            I_IdVersioned idv = bdbEnv.idEntryToObject(key, value);
            this.idProcessor.processId(idv);
        }

        public I_ProcessImages getImageProcessor() {
            return imageProcessor;
        }

        public void setImageProcessor(I_ProcessImages imageProcessor) {
            this.imageProcessor = imageProcessor;
        }

        public void processImages(DatabaseEntry key, DatabaseEntry value) throws Exception {
            I_ImageVersioned imageV = bdbEnv.imageEntryToObject(key, value);
            this.imageProcessor.processImages(imageV);



        }

        public void setExtProcessor(I_ProcessExtByRef extProcessor) {
            this.extProcessor = extProcessor;
        }

        public void processEbr(DatabaseEntry key, DatabaseEntry value) throws Exception {
            I_ThinExtByRefVersioned extension = bdbEnv.extEntryToObject(key, value);
            this.extProcessor.processExtensionByReference(extension);
        }
    }

    /**
     * This is a double pass processor. The intention is to ensure the concept attribute
     * cursor is closed before modifying any concepts (the provided processor may change
     * concept attributes).
     *
     * First pass picks up all the concept ids from the concept attributes.
     * Second pass passes off each concept to another processor.
     */
    public class ConceptIteratorWrapper implements I_ProcessConceptAttributeEntries {

        private ArrayList<Integer> conceptIds;

        public ConceptIteratorWrapper() throws Exception {
            conceptIds = new ArrayList<Integer>(getConceptCount());
        }

        /** Pass one */
        public void processConceptAttributeEntry(DatabaseEntry key, DatabaseEntry value) throws Exception {
           conceptIds.add(intBinder.entryToObject(key));
        }

        /** Pass two - to be called explicitly */
        public void iterate(I_ProcessConcepts processor) throws Exception {
            for (Integer id  : conceptIds) {
                processor.processConcept(ConceptBean.get(id));
            }
        }

        public DatabaseEntry getDataEntry() {
            return new DatabaseEntry();
        }

        public DatabaseEntry getKeyEntry() {
            return new DatabaseEntry();
        }

    }

    public void iterateConceptAttributes(I_ProcessConceptAttributes processor) throws Exception {
        Iterator<I_GetConceptData> conItr = bdbEnv.getConceptIterator();
        while (conItr.hasNext()) {
            I_GetConceptData con = conItr.next();
            I_ConceptAttributeVersioned cav = con.getConceptAttributes();
            if (cav != null) {
                processor.processConceptAttributes(cav);
            } else {
                AceLog.getAppLog().warning("null concept versions for: " + ConceptBean.get(con.getConceptId()));
            }
        }
    }

    public Iterator<I_DescriptionVersioned> getDescriptionIterator() throws IOException {
        return bdbEnv.getDescriptionIterator();
    }

    public void iterateDescriptions(I_ProcessDescriptions processor) throws Exception {
        Iterator<I_DescriptionVersioned> descItr = bdbEnv.getDescriptionIterator();
        while (descItr.hasNext()) {
            processor.processDescription(descItr.next());
        }
    }

    public void iterateIds(org.dwfa.ace.api.I_ProcessIds processor) throws Exception {
        ProcessorWrapper wrapper = new ProcessorWrapper();
        wrapper.setIdProcessor(processor);
        iterateIdEntries(wrapper);
    }

    public void iterateImages(I_ProcessImages processor) throws Exception {
        ProcessorWrapper wrapper = new ProcessorWrapper();
        wrapper.setImageProcessor(processor);
        iterateImages(wrapper);

    }

    public void iteratePaths(I_ProcessPaths processor) throws Exception {
        for (I_Path path : new PathManager().getAll()) {
            processor.processPath(path);
        }
    }

    public void iterateRelationships(I_ProcessRelationships processor) throws Exception {
        Iterator<I_RelVersioned> relItr = bdbEnv.getRelationshipIterator();
        while (relItr.hasNext()) {
            processor.processRelationship(relItr.next());
        }
    }

    public void iterateConcepts(I_ProcessConcepts processor) throws Exception {
        ConceptIteratorWrapper wrapper = new ConceptIteratorWrapper();
        iterateConceptAttributeEntries(wrapper);
        wrapper.iterate(processor);
    }

    public void iterateExtByRefs(I_ProcessExtByRef processor) throws Exception {
        ProcessorWrapper wrapper = new ProcessorWrapper();
        wrapper.setExtProcessor(processor);
        iterateExtByRefEntries(wrapper);
    }

    public void checkpoint() throws IOException {
        this.sync();
    }

    public Hits doLuceneSearch(String query) throws IOException, ParseException {
        return bdbEnv.doLuceneSearch(query);
    }

    public I_GetConceptData getConcept(int nativeId) throws TerminologyException, IOException {
        return ConceptBean.get(nativeId);
    }

    public I_Path getPath(Collection<UUID> uids) throws TerminologyException, IOException {
        return getPath(uuidToNative(uids));
    }

    public I_Path getPath(UUID... uuids) throws TerminologyException, IOException {
        return getPath(uuidToNative(Arrays.asList(uuids)));
    }

    public I_Path newPath(Set<I_Position> origins, I_GetConceptData pathConcept) throws TerminologyException, IOException {
        ArrayList<I_Position> originList = new ArrayList<I_Position>();
        if (origins != null) {
            originList.addAll(origins);
        }
        Path newPath = new Path(pathConcept.getConceptId(), originList);
        AceLog.getEditLog().fine("writing new path: \n" + newPath);
        new PathManager().write(newPath);
        return newPath;
    }

    public void commit() throws Exception {
        ACE.commit();
    }

    public void cancel() throws IOException {
        ACE.abort();
    }

    public void addChangeSetWriter(I_WriteChangeSet csw) {
        ACE.getCsWriters().add(csw);

    }

    public I_ReadChangeSet newBinaryChangeSetReader(File changeSetFile) {
        BinaryChangeSetReader bcs = new BinaryChangeSetReader();
        bcs.setChangeSetFile(changeSetFile);
        return bcs;
    }

    @Override public void loadFromSingleJar(final String jarFile, final String dataPrefix, final File extractDir)
            throws Exception {
        LoadBdb.loadFromSingleJar(jarFile, dataPrefix, extractDir);
    }

    public I_WriteChangeSet newBinaryChangeSetWriter(File changeSetFile) {
        File tempChangeSetFile = new File(changeSetFile.getParentFile(), changeSetFile.getName() + ".temp");
        BinaryChangeSetWriter bcs = new BinaryChangeSetWriter(changeSetFile, tempChangeSetFile);
        return bcs;
    }

    public void removeChangeSetWriter(I_WriteChangeSet csw) {
        ACE.getCsWriters().remove(csw);
    }

    public void closeChangeSets() throws IOException {
        for (I_WriteChangeSet cs : ACE.getCsWriters()) {
            cs.commit();
        }
        ACE.getCsWriters().clear();
    }

    public I_ConfigAceFrame newAceFrameConfig() throws TerminologyException, IOException {
        return new AceFrameConfig();
    }

    public static class MakeNewAceFrame implements Runnable {
        I_ConfigAceFrame frameConfig;
        Exception ex;

        public MakeNewAceFrame(I_ConfigAceFrame frameConfig) {
            super();
            this.frameConfig = frameConfig;
        }

        public void run() {
            try {
                AceFrame newFrame = new AceFrame(AceRunner.args, AceRunner.lc, frameConfig, false);
                newFrame.setVisible(true);
                AceFrameConfig nativeConfig = (AceFrameConfig) frameConfig;
                nativeConfig.setAceFrame(newFrame);

            } catch (Exception e) {
                ex = e;
            }
        }

        public void check() throws Exception {
            if (ex != null) {
                throw ex;
            }
        }
    }

    public void newAceFrame(final I_ConfigAceFrame frameConfig) throws Exception {
        MakeNewAceFrame maker = new MakeNewAceFrame(frameConfig);
        SwingUtilities.invokeAndWait(maker);
        maker.check();
    }

    I_ConfigAceFrame activeAceFrameConfig;

    public I_ConfigAceFrame getActiveAceFrameConfig() throws TerminologyException, IOException {
        return activeAceFrameConfig;
    }

    public void setActiveAceFrameConfig(I_ConfigAceFrame activeAceFrameConfig) throws TerminologyException, IOException {
        this.activeAceFrameConfig = activeAceFrameConfig;
    }

    public I_ConfigAceDb newAceDbConfig() {
        return new AceConfig(envHome, readOnly);
    }

    public long convertToThickVersion(int version) {
        return ThinVersionHelper.convert(version);
    }

    public int convertToThinVersion(long time) {
        return ThinVersionHelper.convert(time);
    }

    public int convertToThinVersion(String dateStr) throws java.text.ParseException {
        return ThinVersionHelper.convert(dateStr);
    }

    public I_IntList newIntList() {
        return new IntList();
    }

    public void resumeChangeSetWriters() {
        ACE.resumeChangeSetWriters();
    }

    public void suspendChangeSetWriters() {
        ACE.suspendChangeSetWriters();
    }

    public I_ConceptAttributePart newConceptAttributePart() {
        return new ThinConPart();
    }

    public I_DescriptionPart newDescriptionPart() {
        return new ThinDescPart();
    }

    public I_RelPart newRelPart() {
        return new ThinRelPart();
    }


    public <T extends I_ThinExtByRefPart> T newExtensionPart(Class<T> t) {
        return VodbTypeFactory.create(t);
    }

    public I_ThinExtByRefVersioned newExtension(int refsetId, int memberId, int componentId, int typeId) {
        ThinExtByRefVersioned thinEbr = new ThinExtByRefVersioned(refsetId, memberId, componentId, typeId);
        ExtensionByReferenceBean ebrBean = ExtensionByReferenceBean.makeNew(memberId, thinEbr);
        addUncommitted(ebrBean);
        return thinEbr;
    }

    public I_ThinExtByRefVersioned newExtension(int refsetId, int memberId, int componentId,
            Class<? extends I_ThinExtByRefPart> partType) {
        int typeId = ThinExtBinder.getExtensionType(partType).getNid();
        return newExtension(refsetId, memberId, componentId, typeId);
    }
    @Deprecated
    public I_ThinExtByRefVersioned newExtensionNoChecks(int refsetId, int memberId, int componentId, int typeId) {

        ThinExtByRefVersioned thinEbr = new ThinExtByRefVersioned(refsetId, memberId, componentId, typeId);

        ExtensionByReferenceBean ebrBean = ExtensionByReferenceBean.makeNew(memberId, thinEbr);

        ACE.addUncommittedNoChecks(ebrBean);
        return thinEbr;
    }

    public I_ThinExtByRefVersioned newExtensionBypassCommit(int refsetId, int memberId, int componentId, int typeId) {
        ThinExtByRefVersioned thinEbr = new ThinExtByRefVersioned(refsetId, memberId, componentId, typeId);
        return thinEbr;
    }

    @Deprecated
    public I_ThinExtByRefPartInteger newIntegerExtensionPart() {
        return newExtensionPart(ThinExtByRefPartInteger.class);
    }

    @Deprecated
    public I_ThinExtByRefPartLanguage newLanguageExtensionPart() {
        return newExtensionPart(ThinExtByRefPartLanguage.class);
    }

    @Deprecated
    public I_ThinExtByRefPartLanguageScoped newLanguageScopedExtensionPart() {
        return newExtensionPart(ThinExtByRefPartLanguageScoped.class);
    }

    @Deprecated
    public I_ThinExtByRefPartMeasurement newMeasurementExtensionPart() {
        return newExtensionPart(ThinExtByRefPartMeasurement.class);
    }

    @Deprecated
    public I_ThinExtByRefPartString newStringExtensionPart() {
        return newExtensionPart(ThinExtByRefPartString.class);
    }

    @Deprecated
    public I_ThinExtByRefPartConceptInt newConceptIntExtensionPart() {
        return newExtensionPart(I_ThinExtByRefPartConceptInt.class);
    }

    @Deprecated
    public I_ThinExtByRefPartBoolean newBooleanExtensionPart() {
        return newExtensionPart(ThinExtByRefPartBoolean.class);
    }

    @Deprecated
    public I_ThinExtByRefPartConcept newConceptExtensionPart() {
        return newExtensionPart(ThinExtByRefPartConcept.class);
    }

    @Deprecated
    public I_ThinExtByRefPartConceptConceptConcept newConceptConceptConceptExtensionPart() {
        return new ThinExtByRefPartConceptConceptConcept();
    }

    @Deprecated
    public I_ThinExtByRefPartConceptConcept newConceptConceptExtensionPart() {
        return new ThinExtByRefPartConceptConcept();
    }

    @Deprecated
    public I_ThinExtByRefPartConceptConceptString newConceptConceptStringExtensionPart() {
        return new ThinExtByRefPartConceptConceptString();
    }

    @Deprecated
    public I_ThinExtByRefPartConceptString newConceptStringExtensionPart() {
        return new ThinExtByRefPartConceptString();
    }
    public List<I_Transact> getUncommitted() {
        return ACE.getUncommitted();
    }

    public I_GetExtensionData getExtensionWrapper(int nid) throws IOException {
        return ExtensionByReferenceBean.get(nid);
    }

    public static boolean isTransactional() {
        return transactional;
    }

    public boolean getTransactional() {
        return transactional;
    }

    public I_RelVersioned newRelationship(UUID relUuid, int uuidType, int conceptNid, int relDestinationNid,
            int pathNid, int version, int relStatusNid, int relTypeNid, int relCharacteristicNid,
            int relRefinabilityNid, int relGroup) throws TerminologyException, IOException {

        ACE.commitSequence++;
        int relId = nativeGenerationForUuid(relUuid, uuidType, pathNid, version);
        ThinRelVersioned rel = new ThinRelVersioned(relId, conceptNid, relDestinationNid, 1);
        ThinRelPart part = new ThinRelPart();
        part.setCharacteristicId(relCharacteristicNid);
        part.setGroup(relGroup);
        part.setPathId(pathNid);
        part.setRefinabilityId(relRefinabilityNid);
        part.setRelTypeId(relTypeNid);
        part.setStatusId(relStatusNid);
        part.setVersion(version);
        rel.addVersion(part);
        return rel;
    }

    public I_RelVersioned newRelationshipBypassCommit(int relNid, int conceptNid, int relDestinationNid)
            throws IOException {
        ACE.commitSequence++;
        return new ThinRelVersioned(relNid, conceptNid, relDestinationNid, 1);
    }

    public static boolean isReadOnly() {
        return readOnly;
    }

    public static void setDeferredWrite(boolean deferredWrite) {
        VodbEnv.deferredWrite = deferredWrite;
    }

    public static boolean isDeferredWrite() {
        return deferredWrite;
    }

    public void deleteId(I_IdVersioned id) throws DatabaseException {
        bdbEnv.deleteId(id);
    }

    public I_IdVersioned getId(Collection<UUID> uids) throws TerminologyException, IOException {
        return bdbEnv.getId(uids);
    }

    public I_IdVersioned getId(int nativeId) throws IOException {
        return bdbEnv.getId(nativeId);
    }

    public ThinIdVersioned getId(UUID uid) throws TerminologyException, IOException {
        return bdbEnv.getId(uid);
    }

    /**
     * Gets a collection of matching I_IdVersioned given an ID and an ID scheme -
     * if no matches are found an empty collection is returned.
     *
     * Usually only one match will be returned, however given the data structure
     * it is possible for more than one I_IdVersioned to have the same
     * ID/ID scheme combination.
     *
     * @param id identifier to find
     * @param scheme native id of the provided identifier's scheme
     * @return Collection of matching I_IdVersioned objects, or an empty collection
     * if none are found
     * @throws TerminologyException
     * @throws IOException
     */
    public Collection<I_IdVersioned> getId(String id, int scheme) throws TerminologyException, IOException {
        return bdbEnv.getId(id, scheme);
    }

    public I_IdVersioned getAuthorityId() throws TerminologyException, IOException {
        return bdbEnv.getAuthorityId();
    }

    public I_IdVersioned getPreviousAuthorityId() throws TerminologyException, IOException {
        return bdbEnv.getPreviousAuthorityId();
    }

    public I_IdVersioned getIdNullOk(int nativeId) throws IOException {
        return bdbEnv.getIdNullOk(nativeId);
    }

    public Collection<UUID> getUids(int nativeId) throws TerminologyException, IOException {
        return bdbEnv.getUids(nativeId);
    }

    public boolean hasId(Collection<UUID> uids) throws IOException {
        try {
            return bdbEnv.hasId(uids);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public boolean hasId(UUID uid) throws IOException {
        try {
            return bdbEnv.hasId(uid);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public void iterateIdEntries(I_ProcessIdEntries processor) throws Exception {
        bdbEnv.iterateIdEntries(processor);
    }

    public int nativeGenerationForUuid(UUID uid, int source, int pathId, int version) throws TerminologyException,
            IOException {
        return bdbEnv.nativeGenerationForUuid(uid, source, pathId, version);
    }

    public List<UUID> nativeToUuid(int nativeId) throws DatabaseException {
        return bdbEnv.nativeToUuid(nativeId);
    }

    public int uuidToNative(Collection<UUID> uids) throws TerminologyException, IOException {
        return bdbEnv.uuidToNative(uids);
    }

    public int uuidToNative(UUID uid) throws TerminologyException, IOException {
        return bdbEnv.uuidToNative(uid);
    }

    public int uuidToNativeDirectWithGeneration(Collection<UUID> uids, int source, I_Path idPath, int version)
            throws TerminologyException, IOException {
        return bdbEnv.uuidToNativeWithGeneration(uids, source, idPath, version);
    }

    public int uuidToNativeWithGeneration(Collection<UUID> uids, int source, I_Path idPath, int version)
            throws TerminologyException, IOException {
        return bdbEnv.uuidToNativeWithGeneration(uids, source, idPath, version);
    }

    public int uuidToNativeWithGeneration(UUID uid, int source, Collection<I_Path> idPaths, int version)
            throws TerminologyException, IOException {
        return bdbEnv.uuidToNativeWithGeneration(uid, source, idPaths, version);
    }

    public int uuidToNativeWithGeneration(UUID uid, int source, I_Path idPath, int version)
            throws TerminologyException, IOException {
        return bdbEnv.uuidToNativeWithGeneration(uid, source, idPath, version);
    }

    public void writeId(I_IdVersioned id) throws IOException {
        try {
            bdbEnv.writeId(id);
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    public int getMaxId() throws DatabaseException {
        return bdbEnv.getMaxId();
    }

    public int getMinId() throws DatabaseException {
        return bdbEnv.getMinId();
    }

    public void logIdDbStats() throws DatabaseException {
        bdbEnv.logIdDbStats();
    }

    public String getStats() throws ToIoException {
        return bdbEnv.getStats();
    }

    public I_ShowActivity getActivityFrame() {
        return activityFrame;
    }

    public static void setTransactional(boolean transactional) {
        VodbEnv.transactional = transactional;
    }

    public static boolean getTxnNoSync() {
        return txnNoSync;
    }

    public static void setTxnNoSync(boolean txnNoSync) {
        VodbEnv.txnNoSync = txnNoSync;
    }

    public static long getTransactionTimeout() {
        return transactionTimeout;
    }

    public static void setTransactionTimeout(long transactionTimeout) {
        VodbEnv.transactionTimeout = transactionTimeout;
    }

    public Transaction beginTransaction() throws DatabaseException {
        return bdbEnv.beginTransaction();
    }

    public void cleanupSNOMED(I_IntSet relsToIgnore, I_IntSet releases) throws Exception {
        bdbEnv.cleanupSNOMED(relsToIgnore, releases);
    }

    public void writeDescriptionNoLuceneUpdate(I_DescriptionVersioned vdesc) throws DatabaseException, IOException {
        bdbEnv.writeDescriptionNoLuceneUpdate(vdesc);
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException, IOException {
        bdbEnv.commit(bean, version, values);
    }

    public int getDatabaseVersion() {
        return BdbEnv.getDatabaseVersion();
    }

    public void setupBean(ConceptBean cb) throws IOException {
        bdbEnv.setupBean(cb);
    }

    public I_WriteDirectToDb getDirectInterface() {
        return this;
    }

    public void compress(int minUtilization) throws IOException {
        bdbEnv.compress(minUtilization);
    }

    public I_IdVersioned newIdVersionedBypassCommit(int nid) {
        return new ThinIdVersioned(nid, 1);
    }

    public I_IdPart newIdPart() {
        return new ThinIdPart();
    }

    public void startTransaction() throws IOException {
        bdbEnv.startTransaction();
    }

    public void cancelTransaction() throws IOException {
        bdbEnv.cancelTransaction();
    }

    public void commitTransaction() throws IOException {
        bdbEnv.commitTransaction();
    }

    public I_ShowActivity newActivityPanel(boolean displayInViewer, I_ConfigAceFrame aceFrameConfig) {
        if (isHeadless()) {
            return new UpperInfoOnlyConsoleMonitor();
        } else {
            ActivityPanel ap = new ActivityPanel(true, null, aceFrameConfig);
            ap.setIndeterminate(true);
            ap.setProgressInfoUpper("New activity");
            ap.setProgressInfoLower("");
            if (displayInViewer) {
                try {
                    ActivityViewer.addActivity(ap);
                } catch (Exception e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                }
            }
            return ap;
        }
    }

    public Collection<I_WriteChangeSet> getChangeSetWriters() {
        return ACE.getCsWriters();
    }

    public void addChangeSetReader(I_ReadChangeSet reader) {
        ACE.getCsReaders().add(reader);
    }

    public Collection<I_ReadChangeSet> getChangeSetReaders() {
        return ACE.getCsReaders();
    }

    public void removeChangeSetReader(I_ReadChangeSet reader) {
        ACE.getCsReaders().remove(reader);
    }

    public I_HandleSubversion getSvnHandler() {
        return new Svn();
    }

    /**
     * @see org.dwfa.ace.api.I_TermFactory#getConcept(java.lang.String, int)
     */
    @SuppressWarnings("deprecation")
    public I_GetConceptData getConcept(String conceptId, int sourceId) throws TerminologyException, ParseException,
            IOException {

        Hits hits = doLuceneSearch(conceptId);

        if (hits == null || hits.length() == 0) {
            throw new TerminologyException("Search for conceptId '" + conceptId + "' in scheme '" + sourceId
                + "' which is '" + getConcept(sourceId) + "' produced no results");
        }

        // Find the hit that actually has our concept id in it
        // and has a matching source (the identifier scheme)

        for (int i = 0; i < hits.length(); i++) {
            Document doc = hits.doc(i);
            int cnid = Integer.parseInt(doc.get("cnid"));
            I_GetConceptData concept = getConcept(cnid);
            for (I_IdPart version : concept.getId().getVersions()) {
                if (conceptId.equals(version.getSourceId().toString()) && (sourceId == version.getSource())) {
                    return concept;
                }
            }
        }

        throw new TerminologyException("Unable to locate a matching concept for id '" + conceptId + "' source '" + sourceId + "' from hits " + hits);
    }

    /**
     * @see org.dwfa.ace.api.I_TermFactory#getConcept(java.lang.String)
     */
    @SuppressWarnings("deprecation")
    public Set<I_GetConceptData> getConcept(String conceptId) throws TerminologyException, ParseException, IOException {

        Set<I_GetConceptData> results = new HashSet<I_GetConceptData>();

        Hits hits = doLuceneSearch(conceptId);

        if (hits == null || hits.length() == 0) {
            throw new TerminologyException("Search produced no results");
        }

        // Find the hit that actually has our concept id in it

        for (int i = 0; i < hits.length(); i++) {
            Document doc = hits.doc(i);
            int cnid = Integer.parseInt(doc.get("cnid"));
            I_GetConceptData concept = getConcept(cnid);
            for (I_IdPart version : concept.getId().getVersions()) {
                if (conceptId.equals(version.getSourceId().toString())) {
                    results.add(concept);
                }
            }
        }

        if (results.size() == 0) {
            throw new TerminologyException("Unable to locate a matching concept");
        } else {
            return results;
        }
    }

    public int getConceptCount() throws IOException {
        try {
            return bdbEnv.getConceptCount();
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

    private void addUncommitted(I_Transact to) {
        if (isDataChecksSuppressed()) {
            ACE.addUncommittedNoChecks(to);
        } else {
            ACE.addUncommitted(to);
        }
    }

    /**
     * If a class that is calling a method in this class defines the {@link AllowDataCheckSuppression} annotation then
     * we will traverse back up the the call stack for a method that declares the {@link SuppressDataChecks} annotation.
     * If found returns true.
     */
    private boolean isDataChecksSuppressed() {
        boolean suppressAllowed = false;

        for (StackTraceElement e : new Throwable().getStackTrace()) {
            Class<?> elementClass;
            try {
                elementClass = Class.forName(e.getClassName());
            } catch (ClassNotFoundException ex) {
                // We have encountered a class on the call stack which is not
                // available to our classloader
                // so we will stop here and assert we can check no further.
                return false;
            }

            // Skip this class
            if (elementClass.equals(this.getClass())) {
                continue;
            }

            // Verify the immediate calling class (first time only) allows suppression
            if (!suppressAllowed) {
                AllowDataCheckSuppression allowAnnotation = elementClass.getAnnotation(AllowDataCheckSuppression.class);

                if (allowAnnotation != null) {
                    suppressAllowed = true;
                } else {
                    return false;
                }
            }

            do {
                for (Method m : elementClass.getDeclaredMethods()) {
                    if (m.getName().equals(e.getMethodName())) {
                        SuppressDataChecks annotation = m.getAnnotation(SuppressDataChecks.class);
                        if (annotation != null) {
                            return true;
                        }
                    }
                }
                // Check the superclass, if it exists, in case the method in inherited
                elementClass = elementClass.getSuperclass();
            } while (elementClass != null);

        }

        return false;
    }

    public void searchConcepts(I_TrackContinuation tracker, org.apache.commons.collections.primitives.IntList matches,
            CountDownLatch latch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
            throws DatabaseException, IOException, ParseException {
        bdbEnv.searchConcepts(tracker, matches, latch, checkList, config);
    }

    public List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings() {
        return ACE.getCommitErrorsAndWarnings();
    }

    public TransferHandler makeTerminologyTransferHandler(JComponent thisComponent) {
        return new TerminologyTransferHandler(thisComponent);
    }

    public I_IntSet getConceptNids() throws IOException {
        return bdbEnv.getConceptNids();
    }

    public I_RepresentIdSet getConceptIdSet() throws IOException {
        return bdbEnv.getConceptIdSet();
    }

    public I_RepresentIdSet getEmptyIdSet() throws IOException {
        return bdbEnv.getEmptyIdSet();
    }

    public I_RepresentIdSet getIdSetFromIntCollection(Collection<Integer> ids) throws IOException {
        return bdbEnv.getIdSetFromIntCollection(ids);
    }

    public I_RepresentIdSet getIdSetfromTermCollection(Collection<? extends I_AmTermComponent> components)
            throws IOException {
        return bdbEnv.getIdSetfromTermCollection(components);
    }

    public I_RepresentIdSet getReadOnlyConceptIdSet() throws IOException {
        return bdbEnv.getReadOnlyConceptIdSet();
    }

    @Override
    public I_ProcessQueue newProcessQueue(int threadCount) {
        return new ProcessQueue(threadCount);
    }

    @Override
    public I_ProcessQueue newProcessQueue(String name, int threadCount) {
        return new ProcessQueue(name, threadCount);
    }

    public <T extends I_ThinExtByRefPart> int getRefsetTypeIdByExtensionType(Class<T> extType) {
        return ThinExtBinder.getExtensionTypeNid(ThinExtBinder.getExtensionType(extType));
    }

    public I_RepresentIdSet getDescriptionIdSet() throws IOException {
        return bdbEnv.getDescriptionIdSet();
    }

    public I_RepresentIdSet getRelationshipIdSet() throws IOException {
        return bdbEnv.getRelationshipIdSet();
    }
}
