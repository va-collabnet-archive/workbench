package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.activity.UpperInfoOnlyConsoleMonitor;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HandleSubversion;
import org.dwfa.ace.api.I_HelpMarkedParentRefsets;
import org.dwfa.ace.api.I_HelpMemberRefsets;
import org.dwfa.ace.api.I_HelpMemberRefsetsCalculateConflicts;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_ProcessDescriptions;
import org.dwfa.ace.api.I_ProcessExtByRef;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_TrackContinuation;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.search.I_Search;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.impl.CheckAndProcessRegexMatch;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_FetchConceptFromCursor;
import org.ihtsdo.concept.I_ProcessConceptData;
import org.ihtsdo.concept.component.ComponentList;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.attributes.ConceptAttributesRevision;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.description.DescriptionRevision;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.image.ImageRevision;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refsetmember.Boolean.BooleanMember;
import org.ihtsdo.concept.component.refsetmember.Long.LongMember;
import org.ihtsdo.concept.component.refsetmember.cid.CidMember;
import org.ihtsdo.concept.component.refsetmember.cidCid.CidCidMember;
import org.ihtsdo.concept.component.refsetmember.cidCidCid.CidCidCidMember;
import org.ihtsdo.concept.component.refsetmember.cidCidStr.CidCidStrMember;
import org.ihtsdo.concept.component.refsetmember.cidFloat.CidFloatMember;
import org.ihtsdo.concept.component.refsetmember.cidInt.CidIntMember;
import org.ihtsdo.concept.component.refsetmember.cidLong.CidLongMember;
import org.ihtsdo.concept.component.refsetmember.cidStr.CidStrMember;
import org.ihtsdo.concept.component.refsetmember.integer.IntMember;
import org.ihtsdo.concept.component.refsetmember.membership.MembershipMember;
import org.ihtsdo.concept.component.refsetmember.str.StrMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.concept.component.relationship.RelationshipRevision;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.bdb.computer.refset.MarkedParentRefsetHelper;
import org.ihtsdo.db.bdb.computer.refset.MemberRefsetConflictCalculator;
import org.ihtsdo.db.bdb.computer.refset.MemberRefsetHelper;
import org.ihtsdo.db.bdb.computer.refset.RefsetComputer;
import org.ihtsdo.db.bdb.computer.refset.RefsetHelper;
import org.ihtsdo.db.bdb.computer.refset.SpecRefsetHelper;
import org.ihtsdo.db.runner.WorkbenchRunner;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.lucene.CheckAndProcessLuceneMatch;
import org.ihtsdo.lucene.LuceneManager;

import com.sleepycat.je.DatabaseException;

public class BdbTermFactory implements I_TermFactory, I_ImplementTermFactory, I_Search {

    private BdbPathManager pathManager;

    private File envHome;

    public static void canEdit(I_ConfigAceFrame aceFrameConfig) throws TerminologyException {
        if (aceFrameConfig.getEditingPathSet().size() == 0) {
            throw new TerminologyException(
                "<br><br>You must select an editing path before editing...<br><br>No editing path selected.");
        }
    }

    public static boolean isHeadless() {
        return DwfaEnv.isHeadless();
    }

    public static void setHeadless(Boolean headless) {
        DwfaEnv.setHeadless(headless);
    }

    @Override
    public void close() throws IOException {
        try {
            Bdb.close();
            Terms.close(this);
        } catch (DatabaseException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            throw new IOException(e);
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void addChangeSetReader(I_ReadChangeSet reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addChangeSetWriter(I_WriteChangeSet writer) {
        ChangeSetWriterHandler.addWriter(writer);
    }

    @Override
    public void addUncommitted(I_GetConceptData concept) {
        BdbCommitManager.addUncommitted(concept);
    }

    @Override
    public void addUncommitted(I_ExtendByRef extension) {
        BdbCommitManager.addUncommitted(extension);
    }

    @Override
    public void addUncommittedNoChecks(I_GetConceptData concept) {
        BdbCommitManager.addUncommittedNoChecks(concept);
    }

    @Override
    public void addUncommittedNoChecks(I_ExtendByRef extension) {
        BdbCommitManager.addUncommittedNoChecks(extension);
    }

    @Override
    public void cancel() throws IOException {
        BdbCommitManager.cancel();
    }

    @Override
    public void cancelTransaction() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void closeChangeSets() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void commit() throws Exception {
        BdbCommitManager.commit();
    }

    @Override
    public void commit(ChangeSetPolicy changeSetPolicy,
            ChangeSetWriterThreading changeSetWriterThreading) {
        BdbCommitManager.commit(changeSetPolicy, changeSetWriterThreading);
    }
    
    
    @Override
    public void commitTransaction() throws IOException {
        // legacy operation, nothing to do...
    }

    @Override
    public long convertToThickVersion(int version) {
        return ThinVersionHelper.convert(version);
    }

    @Override
    public int convertToThinVersion(long time) {
        return ThinVersionHelper.convert(time);
    }

    @Override
    public int convertToThinVersion(String dateStr) throws ParseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Hits doLuceneSearch(String query) throws IOException, org.apache.lucene.queryParser.ParseException {
        Query q = new QueryParser("desc", new StandardAnalyzer()).parse(query);
        return LuceneManager.search(q);
    }

    @Override
    public void forget(I_GetConceptData concept) throws IOException {
        BdbCommitManager.forget(concept);
    }

    @Override
    public void forget(I_DescriptionVersioned desc) throws IOException {
        BdbCommitManager.forget(desc);
    }


    @Override
    public void forget(I_DescriptionVersioned desc, I_DescriptionPart part) throws IOException {
        if (part.getTime() == Long.MAX_VALUE) {
            if (part == desc) {
                forget(desc);
            } else {
                Description d = (Description) desc;
                if (Description.Version.class.isAssignableFrom(part.getClass())) {
                    Description.Version dv = (Description.Version) part;
                    if (dv.getRevision() == null) {
                        forget(d);
                    } else {
                        d.removeRevision((DescriptionRevision) dv.getRevision());
                    }
                } else {
                    d.removeRevision((DescriptionRevision) part);
                }
            }
        } 
    }

    @Override
    public void forget(I_RelVersioned rel) throws IOException {
        BdbCommitManager.forget(rel);
    }

    public void forget(I_ExtendByRef extension) throws IOException {
        BdbCommitManager.forget(extension);
    }

    I_ConfigAceFrame activeAceFrameConfig;

    @Override
    public I_ConfigAceFrame getActiveAceFrameConfig() throws IOException {
        return activeAceFrameConfig;
    }

    @Override
    public void setActiveAceFrameConfig(I_ConfigAceFrame activeAceFrameConfig) throws TerminologyException, IOException {
        this.activeAceFrameConfig = activeAceFrameConfig;
    }

    @Override
    public List<? extends I_ExtendByRef> getAllExtensionsForComponent(int nid) throws IOException {
        int cNid = Bdb.getNidCNidMap().getCNid(nid);
        if (cNid == Integer.MAX_VALUE) {
            return new ArrayList<I_ExtendByRef>(0);
        }
        Concept c = Bdb.getConceptDb().getConcept(cNid);
        return c.getExtensionsForComponent(nid);
    }

    @Override
    @Deprecated
    public List<? extends I_ExtendByRef> getAllExtensionsForComponent(int nid, boolean addUncommitted)
            throws IOException {
        return getAllExtensionsForComponent(nid);
    }

    @Override
    public I_Identify getAuthorityId() throws TerminologyException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<I_ReadChangeSet> getChangeSetReaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<I_WriteChangeSet> getChangeSetWriters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AlertToDataConstraintFailure> getCommitErrorsAndWarnings() {
        return BdbCommitManager.getCommitErrorsAndWarnings();
    }

    @Override
    public I_GetConceptData getConcept(Collection<UUID> ids) throws TerminologyException, IOException {
        return Bdb.getConceptDb().getConcept(Bdb.uuidsToNid(ids));
    }

    @Override
    public I_GetConceptData getConcept(UUID... ids) throws TerminologyException, IOException {
        return Bdb.getConceptDb().getConcept(Bdb.uuidToNid(ids));
    }

    @Override
    public I_GetConceptData getConcept(int nid) throws TerminologyException, IOException {
        assert nid != Integer.MAX_VALUE;
        return Bdb.getConceptDb().getConcept(nid);
    }

    @Override
    public I_GetConceptData getConcept(String conceptId, int sourceId) throws TerminologyException,
            org.apache.lucene.queryParser.ParseException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<I_GetConceptData> getConcept(String conceptId) throws TerminologyException,
            org.apache.lucene.queryParser.ParseException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getConceptCount() throws IOException {
        return (int) Bdb.getConceptDb().getCount();
    }

    @Override
    public I_RepresentIdSet getConceptIdSet() throws IOException {
        return Bdb.getConceptDb().getConceptIdSet();
    }

    @Override
    public Iterator<I_GetConceptData> getConceptIterator() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_IntSet getConceptNids() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_DescriptionVersioned getDescription(int dNid, int cNid) throws TerminologyException, IOException {
        if (hasConcept(cNid)) {
            return Bdb.getConcept(cNid).getDescription(dNid);
        }
        return null;
    }

    @Override
    public I_DescriptionVersioned getDescription(int dNid) throws TerminologyException, IOException {
        return getDescription(dNid, Bdb.getConceptNid(dNid));
    }


    @Override
    public I_RelVersioned getRelationship(int rNid) throws IOException {
        if (Bdb.getNidCNidMap().hasMap(rNid)) {
            return Bdb.getConcept(Bdb.getConceptNid(rNid)).getSourceRel(rNid);
        } 
        return null;
    }

    @Override
    public I_RepresentIdSet getDescriptionIdSet() throws IOException {
        I_RepresentIdSet descriptionIdSet = new IdentifierSet();
        I_IterateIds iterator = getConceptIdSet().iterator();
        while (iterator.next()) {
            Concept concept = Bdb.getConcept(iterator.nid());
            for (Description description : concept.getDescriptions()) {
                descriptionIdSet.setMember(description.getDescId());
            }
        }
        return descriptionIdSet;
    }

    @Override
    public Iterator<I_DescriptionVersioned> getDescriptionIterator() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_WriteDirectToDb getDirectInterface() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LogWithAlerts getEditLog() {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_RepresentIdSet getEmptyIdSet() throws IOException {
        return Bdb.getConceptDb().getEmptyIdSet();
    }

    @Override
    public I_ExtendByRef getExtension(int memberId) throws IOException {
        return (I_ExtendByRef) Bdb.getConceptForComponent(memberId).getComponent(memberId);
    }

    @Override
    public I_Identify getId(int nid) throws TerminologyException, IOException {
        Concept concept = Bdb.getConceptForComponent(nid);
        if (concept != null) {
            return concept.getComponent(nid);
        }

        return null;
    }

    @Override
    public I_Identify getId(UUID uuid) throws TerminologyException, IOException {
        return getId(Bdb.uuidToNid(uuid));
    }

    @Override
    public I_Identify getId(Collection<UUID> uids) throws TerminologyException, IOException {
        return getId(Bdb.uuidsToNid(uids));
    }

    @Override
    public I_RepresentIdSet getIdSetFromIntCollection(Collection<Integer> ids) throws IOException {
        I_RepresentIdSet newSet = getEmptyIdSet();
        for (int nid : ids) {
            newSet.setMember(nid);
        }
        return newSet;
    }

    @Override
    public I_RepresentIdSet getIdSetfromTermCollection(Collection<? extends I_AmTermComponent> components)
            throws IOException {
        I_RepresentIdSet newSet = getEmptyIdSet();
        for (I_AmTermComponent component : components) {
            newSet.setMember(component.getNid());
        }
        return newSet;
    }

    @Override
    public I_Path getPath(Collection<UUID> uids) throws TerminologyException, IOException {
        return pathManager.get(uuidToNative(uids));
    }

    @Override
    public I_Path getPath(UUID... ids) throws TerminologyException, IOException {
        return pathManager.get(Bdb.uuidToNid(ids));
    }

    @Override
    public List<I_Path> getPaths() throws Exception {
        return new ArrayList<I_Path>(pathManager.getAll());
    }

    @Override
    public I_Identify getPreviousAuthorityId() throws TerminologyException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getProperties() throws IOException {
        return Bdb.getProperties();
    }

    @Override
    public String getProperty(String key) throws IOException {
        return Bdb.getProperty(key);
    }

    @Override
    public I_RepresentIdSet getReadOnlyConceptIdSet() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<RefsetMember<?, ?>> getRefsetExtensionMembers(int refsetId) throws IOException {
        return Concept.get(refsetId).getExtensions();
    }

    @Override
    public I_RepresentIdSet getRelationshipIdSet() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStats() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_HandleSubversion getSvnHandler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TimePathId> getTimePathList() throws Exception {
        return Bdb.getSapDb().getTimePathList();
    }

    @Override
    public boolean getTransactional() {
        return false;
    }

    @Override
    public Collection<UUID> getUids(int nid) throws TerminologyException, IOException {
        return getId(nid).getUUIDs();
    }

    @Override
    public Set<? extends I_Transact> getUncommitted() {
        return BdbCommitManager.getUncommitted();
    }

    @Override
    public boolean hasConcept(int conceptId) throws IOException {
        if (conceptId == Integer.MIN_VALUE || conceptId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        if (Bdb.getNidCNidMap().hasConcept(conceptId)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasDescription(int descId, int conceptId) throws IOException {
        if (descId == Integer.MIN_VALUE || descId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        I_AmTermComponent c = Bdb.getComponent(descId);
        if (c == null) {
            return false;
        }
        return Description.class.isAssignableFrom(c.getClass());
    }

    @Override
    public boolean hasExtension(int memberId) throws IOException {
        if (memberId == Integer.MIN_VALUE || memberId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        I_AmTermComponent c = Bdb.getComponent(memberId);
        if (c == null) {
            return false;
        }
        return RefsetMember.class.isAssignableFrom(c.getClass());
    }

    @Override
    public boolean hasId(Collection<UUID> uuids) throws IOException {
        for (UUID uuid: uuids) {
            if (Bdb.hasUuid(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasId(UUID uuid) throws IOException {
        return Bdb.hasUuid(uuid);
    }

    @Override
    public boolean hasImage(int imageId) throws IOException {
        if (imageId == Integer.MIN_VALUE || imageId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        I_AmTermComponent c = Bdb.getComponent(imageId);
        if (c == null) {
            return false;
        }
        return Image.class.isAssignableFrom(c.getClass());
    }

    @Override
    public boolean hasPath(int nid) throws IOException {
        try {
            return pathManager.get(nid) != null;
        } catch (PathNotExistsException e) {
            return false;
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return false;
    }

    @Override
    public boolean hasRel(int relId, int conceptId) throws IOException {
        if (relId == Integer.MIN_VALUE || relId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        I_AmTermComponent c = Bdb.getComponent(relId);
        if (c == null) {
            return false;
        }
        return Relationship.class.isAssignableFrom(c.getClass());
    }

    private class IterateConceptsAdaptor implements I_ProcessConceptData {
        private I_ProcessConcepts processor;

        public IterateConceptsAdaptor(I_ProcessConcepts procesor) {
            super();
            this.processor = procesor;
        }

        @Override
        public void processConceptData(Concept concept) throws Exception {
            processor.processConcept(concept);
        }

        @Override
        public boolean continueWork() {
            return true;
        }
    }
    
    @Override
    public void iterateConcepts(I_ProcessConcepts procesor) throws Exception {
        Bdb.getConceptDb().iterateConceptDataInSequence(new IterateConceptsAdaptor(procesor));
    }

    @Override
    public void iterateDescriptions(I_ProcessDescriptions processor) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void iterateExtByRefs(I_ProcessExtByRef processor) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFromDirectory(File dataDir, String encoding) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFromSingleJar(String jarFile, String dataPrefix) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransferHandler makeTerminologyTransferHandler(JComponent thisComponent) {
        return new TerminologyTransferHandler(thisComponent);
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
                AceFrame newFrame = new AceFrame(WorkbenchRunner.args, WorkbenchRunner.lc, frameConfig, false);
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

    @Override
    public void newAceFrame(I_ConfigAceFrame frameConfig) throws Exception {
        MakeNewAceFrame maker = new MakeNewAceFrame(frameConfig);
        SwingUtilities.invokeAndWait(maker);
        maker.check();
    }

    @Override
    public I_ConfigAceFrame newAceFrameConfig() throws TerminologyException, IOException {
        return new AceFrameConfig();
    }

    @Override
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

    @Override
    public I_ReadChangeSet newBinaryChangeSetReader(File changeSetFile) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_WriteChangeSet newBinaryChangeSetWriter(File changeSetFile) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_GetConceptData newConcept(UUID newConceptUuid, boolean defined, I_ConfigAceFrame aceFrameConfig)
            throws TerminologyException, IOException {
        int statusNid = aceFrameConfig.getDefaultStatus().getNid();
        return newConcept(newConceptUuid, defined, aceFrameConfig, statusNid);
    }

    @Override
    public I_GetConceptData newConcept(UUID newConceptUuid, boolean defined, I_ConfigAceFrame aceFrameConfig,
            int statusNid) throws TerminologyException, IOException {
        return newConcept(newConceptUuid, defined, aceFrameConfig, statusNid,
            Long.MAX_VALUE);
    }


    @Override
    public I_GetConceptData newConcept(UUID newConceptUuid, boolean isDefined, I_ConfigAceFrame aceFrameConfig, int statusNid,
            long time) throws TerminologyException, IOException {
        canEdit(aceFrameConfig);
        int cNid = Bdb.uuidToNid(newConceptUuid);
        Bdb.getNidCNidMap().setCidForNid(cNid, cNid);
        Concept newC = Concept.get(cNid);
        ConceptAttributes a = new ConceptAttributes();
        a.nid = cNid;
        a.enclosingConceptNid = cNid;
        newC.setConceptAttributes(a);
        a.setDefined(isDefined);
        a.primordialUNid = Bdb.getUuidsToNidMap().getUNid(newConceptUuid);
        a.primordialSapNid = Integer.MIN_VALUE;
        for (I_Path p : aceFrameConfig.getEditingPathSet()) {
            if (a.primordialSapNid == Integer.MIN_VALUE) {
                a.primordialSapNid = Bdb.getSapDb().getSapNid(statusNid, p.getConceptId(), time);
            } else {
                if (a.revisions == null) {
                    a.revisions =
                            new CopyOnWriteArrayList<ConceptAttributesRevision>();
                }
                a.revisions.add((ConceptAttributesRevision) a.makeAnalog(statusNid, p.getConceptId(), time));
            }
        }
        return newC;
    }

    @Override
    public I_ConceptAttributePart newConceptAttributePart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Description newDescription(UUID newDescriptionId, I_GetConceptData concept, String lang, String text,
            I_ConceptualizeLocally descType, I_ConfigAceFrame aceFrameConfig) throws TerminologyException, IOException {
        return newDescription(newDescriptionId, concept, lang, text, Terms.get().getConcept(descType.getNid()),
            aceFrameConfig);
    }

    @Override
    public Description newDescription(UUID descUuid, I_GetConceptData concept, String lang, String text,
            I_GetConceptData descType, I_ConfigAceFrame aceFrameConfig) throws TerminologyException, IOException {

        int statusNid = aceFrameConfig.getDefaultStatus().getNid();
        return newDescription(descUuid, concept, lang, text, descType, aceFrameConfig, statusNid);
    }

    public Description newDescription(UUID descUuid, I_GetConceptData concept, String lang, String text,
            I_GetConceptData descriptionType, I_ConfigAceFrame aceFrameConfig, int statusNid)
            throws TerminologyException, IOException {
        return newDescription(descUuid, concept, lang,
            text, descriptionType, aceFrameConfig, Bdb.getConcept(statusNid),
            Long.MAX_VALUE);
    }


    @Override
    public Description newDescription(UUID descUuid, I_GetConceptData concept, String lang,
            String text, I_GetConceptData descType, I_ConfigAceFrame aceFrameConfig, I_GetConceptData status,
            long effectiveDate) throws TerminologyException, IOException {

        canEdit(aceFrameConfig);
        Concept c = (Concept) concept;
        Description d = new Description();
        Bdb.gVersion.incrementAndGet();
        d.enclosingConceptNid = c.getNid();
        d.nid = Bdb.uuidToNid(descUuid);
        Bdb.getNidCNidMap().setCidForNid(c.getNid(), d.nid);
        d.primordialUNid = Bdb.getUuidsToNidMap().getUNid(descUuid);
        d.setLang(lang);
        d.setText(text);
        d.setInitialCaseSignificant(false);
        d.setTypeId(descType.getNid());
        d.primordialSapNid = Integer.MIN_VALUE;
        for (I_Path p : aceFrameConfig.getEditingPathSet()) {
            if (d.primordialSapNid == Integer.MIN_VALUE) {
                d.primordialSapNid = Bdb.getSapDb().getSapNid(status.getNid(), p.getConceptId(), effectiveDate);
            } else {
                if (d.revisions == null) {
                    d.revisions = new CopyOnWriteArrayList<DescriptionRevision>();
                }
                d.revisions.add(d.makeAnalog(status.getNid(), p.getConceptId(), effectiveDate));
            }
        }
        c.getDescriptions().add(d);
        return d;
    }

    @Override
    public I_DescriptionPart newDescriptionPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public I_ExtendByRef newExtension(int refsetId, int memberId, int componentId,
            Class<? extends I_ExtendByRefPart> partType) throws IOException {
        Concept refsetConcept = Concept.get(refsetId);
        I_ConfigAceFrame config = getActiveAceFrameConfig();
        Bdb.getNidCNidMap().setCidForNid(refsetId, memberId);

        RefsetMember<?, ?> member =
                createMember(memberId, componentId, EConcept.REFSET_TYPES.classToType(partType), refsetConcept, config);
        addUncommitted(refsetConcept);
        return member;
    }

    @Override
    public I_ExtendByRef newExtension(int refsetId, UUID memberPrimUuid, int componentId, int typeId)
            throws IOException {
        Concept refsetConcept = Concept.get(refsetId);
        I_ConfigAceFrame config = getActiveAceFrameConfig();
        RefsetMember<?, ?> member =
                createMember(memberPrimUuid, componentId, EConcept.REFSET_TYPES.nidToType(typeId), refsetConcept,
                    config, new RefsetPropertyMap());
        addUncommitted(refsetConcept);
        return member;
    }

    @Override
    public I_ExtendByRef newExtension(int refsetId, UUID memberPrimUuid, int componentId,
            Class<? extends I_ExtendByRefPart> partType) throws IOException {
        Concept refsetConcept = Concept.get(refsetId);
        I_ConfigAceFrame config = getActiveAceFrameConfig();
        RefsetMember<?, ?> member =
                createMember(memberPrimUuid, componentId, EConcept.REFSET_TYPES.classToType(partType), refsetConcept,
                    config, new RefsetPropertyMap());
        addUncommitted(refsetConcept);
        return member;
    }

    /**
     * @deprecated inefficient. Use <code>createMember(UUID primordialUuid...</code>
     * @param memberId
     * @param type
     * @param refsetConcept
     * @param config
     * @return
     * @throws IOException
     */
    @Deprecated
    public static RefsetMember<?, ?> createMember(int memberId, int referencedComponentNid, EConcept.REFSET_TYPES type,
            Concept refsetConcept, I_ConfigAceFrame config) throws IOException {
        UUID primordialUuid = Bdb.getUuidsToNidMap().getUuidsForNid(memberId).iterator().next();
        return createMember(primordialUuid, referencedComponentNid, type, refsetConcept, config,
            new RefsetPropertyMap());
    }

    public static RefsetMember<?, ?> createMember(UUID primordialUuid, int referencedComponentNid,
            EConcept.REFSET_TYPES type, Concept refsetConcept, I_ConfigAceFrame config, RefsetPropertyMap propMap)
            throws IOException {
        assert referencedComponentNid != 0 : " invalid referencedComponentNid for refset: " + refsetConcept;
        RefsetMember<?, ?> member = null;
        switch (type) {
        case BOOLEAN:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new BooleanMember(),
                        propMap);
            break;
        case CID:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new CidMember(),
                        propMap);
            break;
        case CID_CID:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new CidCidMember(),
                        propMap);
            break;
        case CID_CID_CID:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config,
                        new CidCidCidMember(), propMap);
            break;
        case CID_CID_STR:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config,
                        new CidCidStrMember(), propMap);
            break;
        case CID_FLOAT:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new CidFloatMember(),
                        propMap);
            break;
        case CID_INT:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new CidIntMember(),
                        propMap);
            break;
        case CID_LONG:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new CidLongMember(),
                        propMap);
            break;
        case CID_STR:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new CidStrMember(),
                        propMap);
            break;
        case INT:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new IntMember(),
                        propMap);
            break;
        case LONG:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new LongMember(),
                        propMap);
            break;
        case MEMBER:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config,
                        new MembershipMember(), propMap);
            break;
        case STR:
            member =
                    setupNewMember(primordialUuid, referencedComponentNid, refsetConcept, config, new StrMember(),
                        propMap);
            break;
        default:
            throw new UnsupportedOperationException("Can't handle refset type: " + type);
        }
        return member;
    }

    private static RefsetMember<?, ?> setupNewMember(UUID primordialUuid, int referencedComponentNid,
            Concept refsetConcept, I_ConfigAceFrame config, RefsetMember<?, ?> member, RefsetPropertyMap propMap)
            throws IOException {

        assert config != null : "Config cannot be null.";
        assert config.getEditingPathSet() != null : "Config edit path set cannot be null.";
        member.enclosingConceptNid = refsetConcept.getNid();
        member.nid = Bdb.uuidToNid(primordialUuid);
        Bdb.getNidCNidMap().setCidForNid(refsetConcept.getNid(), member.nid);
        member.referencedComponentNid = referencedComponentNid;
        member.primordialUNid = Bdb.getUuidsToNidMap().getUNid(primordialUuid);
        member.primordialSapNid = Integer.MIN_VALUE;
        int statusNid = ReferenceConcepts.CURRENT.getNid();
        long time = Long.MAX_VALUE;
        if (propMap.containsKey(REFSET_PROPERTY.TIME)) {
            time = propMap.getLong(REFSET_PROPERTY.TIME);
        }
        if (propMap.containsKey(REFSET_PROPERTY.STATUS)) {
            statusNid = propMap.getInt(REFSET_PROPERTY.STATUS);
        }
        assert config.getEditingPathSet().size() > 0 : "Empty editing path set. Must have at least one editing path.";
        for (I_Path p : config.getEditingPathSet()) {
            if (member.primordialSapNid == Integer.MIN_VALUE) {
                member.primordialSapNid = Bdb.getSapDb().getSapNid(statusNid, p.getConceptId(), time);
                propMap.setProperties((I_ExtendByRefPart) member);
            } else {
                I_ExtendByRefPart revision = (I_ExtendByRefPart) member.makeAnalog(statusNid, p.getConceptId(), time);
                propMap.setProperties(revision);
                member.addVersion(revision);
            }
        }
        refsetConcept.getExtensions().add(member);
        return member;
    }

    @Override
    public I_ExtendByRef newExtensionNoChecks(int refsetId, int memberId, int componentId, int typeId)
            throws IOException {
        Concept refsetConcept = Concept.get(refsetId);
        I_ConfigAceFrame config = getActiveAceFrameConfig();
        RefsetMember<?, ?> member =
                createMember(memberId, componentId, REFSET_TYPES.nidToType(typeId), refsetConcept, config);
        addUncommittedNoChecks(refsetConcept);
        return member;
    }

    @Override
    public <T extends I_ExtendByRefPart> T newExtensionPart(Class<T> t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_IntList newIntList() {
        return new IntList();
    }

    @Override
    public I_IntSet newIntSet() {
        return new IntSet();
    }

    @Override
    public I_Path newPath(Set<I_Position> origins, I_GetConceptData pathConcept) throws TerminologyException,
            IOException {
        return newPath(origins, pathConcept, getActiveAceFrameConfig());
    }

    @Override
    public I_Path newPath(Set<I_Position> origins, I_GetConceptData pathConcept, I_ConfigAceFrame commitConfig)
            throws TerminologyException, IOException {
        assert pathConcept != null && pathConcept.getConceptId() != 0;
        ArrayList<I_Position> originList = new ArrayList<I_Position>();
        if (origins != null) {
            originList.addAll(origins);
        }
        Path newPath = new Path(pathConcept.getConceptId(), originList);
        AceLog.getEditLog().fine("writing new path: \n" + newPath);
        pathManager.write(newPath, commitConfig);
        return newPath;
    }

    @Override
    public I_Position newPosition(I_Path path, int version) throws TerminologyException, IOException {
        return new Position(version, path);
    }

    @Override
    public I_RelPart newRelPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Relationship newRelationship(UUID newRelUid, I_GetConceptData concept, I_ConfigAceFrame aceFrameConfig)
            throws TerminologyException, IOException {
        return newRelationship(newRelUid, concept, aceFrameConfig.getDefaultRelationshipType(), aceFrameConfig
            .getHierarchySelection(), aceFrameConfig.getDefaultRelationshipCharacteristic(), aceFrameConfig
            .getDefaultRelationshipRefinability(), aceFrameConfig.getDefaultStatus(), 0, aceFrameConfig);
    }

    @Override
    public Relationship newRelationship(UUID newRelUid, I_GetConceptData concept, I_GetConceptData relType,
            I_GetConceptData relDestination, I_GetConceptData relCharacteristic, I_GetConceptData relRefinability,
            I_GetConceptData relStatus, int relGroup, I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
            IOException {
        return  newRelationship(newRelUid, concept, relType,
             relDestination,  relCharacteristic,  relRefinability,  relStatus,
             relGroup, aceFrameConfig, Long.MAX_VALUE);
    }


    @Override
    public Relationship newRelationship(UUID newRelUid, I_GetConceptData concept, I_GetConceptData relType,
            I_GetConceptData relDestination, I_GetConceptData relCharacteristic, I_GetConceptData relRefinability, I_GetConceptData relStatus,
            int group, I_ConfigAceFrame aceFrameConfig, long effectiveDate) throws TerminologyException, IOException {
        canEdit(aceFrameConfig);
        if (concept == null) {
            AceLog.getAppLog().alertAndLogException(
                new Exception("Cannot add a relationship while the component viewer is empty..."));
            return null;
        }
        Concept c = (Concept) concept;
        Relationship r = new Relationship();
        Bdb.gVersion.incrementAndGet();
        r.enclosingConceptNid = c.getNid();
        r.nid = Bdb.uuidToNid(newRelUid);
        Bdb.getNidCNidMap().setCidForNid(c.getNid(), r.nid);
        r.primordialUNid = Bdb.getUuidsToNidMap().getUNid(newRelUid);
        int parentId = relDestination.getNid();
        r.setC2Id(parentId);
        r.setTypeId(relType.getNid());
        r.setRefinabilityId(relRefinability.getNid()); 
        r.setCharacteristicId(relCharacteristic.getNid());
        r.primordialSapNid = Integer.MIN_VALUE;
        r.setGroup(0);
        int statusNid = relStatus.getNid();
        for (I_Path p : aceFrameConfig.getEditingPathSet()) {
            if (r.primordialSapNid == Integer.MIN_VALUE) {
                r.primordialSapNid = Bdb.getSapDb().getSapNid(statusNid, p.getConceptId(), effectiveDate);
            } else {
                if (r.revisions == null) {
                    r.revisions = new CopyOnWriteArrayList<RelationshipRevision>();
                }
                r.revisions.add((RelationshipRevision) r.makeAnalog(statusNid, p.getConceptId(), effectiveDate));
            }
        }
        c.getSourceRels().add(r);
        return r;    
    }

    @Override
    public void removeChangeSetReader(I_ReadChangeSet reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeChangeSetWriter(I_WriteChangeSet writer) {
        ChangeSetWriterHandler.removeWriter(writer);
    }

    @Override
    public void removeFromCacheAndRollbackTransaction(int memberId) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resumeChangeSetWriters() {
        BdbCommitManager.resumeChangeSetWriters();
    }

    @Override
    public void setProperty(String key, String value) throws IOException {
        Bdb.setProperty(key, value);
    }

    @Override
    public void startTransaction() throws IOException {
        // legacy operation, nothing to do. ;
    }

    @Override
    public void suspendChangeSetWriters() {
        BdbCommitManager.suspendChangeSetWriters();
    }

    @Override
    public int uuidToNative(UUID uid) throws TerminologyException, IOException {
        return Bdb.uuidToNid(uid);
    }

    @Override
    public int uuidToNative(Collection<UUID> uids) throws TerminologyException, IOException {
        return Bdb.uuidsToNid(uids);
    }

    @Override
    @Deprecated
    public void writeId(I_Identify versioned) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writePath(I_Path p, I_ConfigAceFrame config) throws IOException {
        try {
            pathManager.write(p, config);
        } catch (TerminologyException e) {
            throw new IOException(e);
        }
    }

    public List<I_Position> getPathChildren(int nid) throws TerminologyException {
        return pathManager.getPathChildren(nid);
    }

    @Override
    public void writePathOrigin(I_Path path, I_Position origin, I_ConfigAceFrame config) throws TerminologyException {
        pathManager.writeOrigin(path, origin, config);
    }

    public void setPathManager(BdbPathManager pathManager) {
        this.pathManager = pathManager;
    }

    @Override
    public List<UUID> nativeToUuid(int nid) throws IOException {
        Concept concept = Bdb.getConceptForComponent(nid);
        if (concept != null) {
            return concept.getUidsForComponent(nid);
        }
        return null;
    }

    @Override
    public I_ImageVersioned getImage(UUID uuid) throws IOException {
        return getImage(Bdb.uuidToNid(uuid));
    }

    @Override
    public I_ImageVersioned getImage(int nid) throws IOException {
        return (I_ImageVersioned) Bdb.getConceptForComponent(nid).getComponent(nid);
    }

    @Override
    public void checkpoint() throws IOException {
        try {
            Bdb.sync();
        } catch (InterruptedException e) {
            throw new IOException(e);
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void compress(int minUtilization) throws IOException {
        Bdb.compress(minUtilization);
    }

    @Override
    public I_ConfigAceDb newAceDbConfig() {
        return new AceConfig(envHome, false);
    }

    @Override
    public void setup(Object envHome, boolean readOnly, Long cacheSize) throws IOException {
        File homeFile = (File) envHome;
        Bdb.setup(homeFile.getAbsolutePath());
    }

    @Override
    public void setup(Object envHome, boolean readOnly, Long cacheSize, DatabaseSetupConfig databaseSetupConfig)
            throws IOException {
        File homeFile = (File) envHome;
        Bdb.setup(homeFile.getAbsolutePath());
    }

    @Override
    public void searchConcepts(I_TrackContinuation tracker, I_RepresentIdSet matches, CountDownLatch latch,
            List<I_TestSearchResults> checkList, I_ConfigAceFrame config) throws DatabaseException, IOException,
            org.apache.lucene.queryParser.ParseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CountDownLatch searchLucene(I_TrackContinuation tracker, String query, Collection<LuceneMatch> matches,
            CountDownLatch latch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config,
            LuceneProgressUpdator updater) throws DatabaseException, IOException,
            org.apache.lucene.queryParser.ParseException {
        Stopwatch timer = new Stopwatch();
        ;
        timer.start();
        try {
            Query q = new QueryParser("desc", new StandardAnalyzer(Version.LUCENE_29)).parse(query);
            if (LuceneManager.indexExists() == false) {
                updater.setProgressInfo("Making lucene index -- this may take a while...");
                LuceneManager.createLuceneDescriptionIndex();
            }
            updater.setIndeterminate(true);
            updater.setProgressInfo("Starting StandardAnalyzer lucene query...");
            long startTime = System.currentTimeMillis();
            updater.setProgressInfo("Query complete in " + Long.toString(System.currentTimeMillis() - startTime)
                + " ms.");
            Hits hits = LuceneManager.search(q);

            if (hits.length() > 0) {
                AceLog.getAppLog().info("StandardAnalyzer query returned " + hits.length() + " hits");
            } else {
                updater.setProgressInfo("Starting WhitespaceAnalyzer lucene query...");
                AceLog.getAppLog().info(
                    "StandardAnalyzer query returned no results. Now trying WhitespaceAnalyzer query");
                q = new QueryParser("desc", new WhitespaceAnalyzer()).parse(query);
                hits = LuceneManager.search(q);
            }

            updater.setProgressInfo("Query complete in " + Long.toString(System.currentTimeMillis() - startTime)
                + " ms. Hits: " + hits.length());

            CountDownLatch hitLatch = new CountDownLatch(hits.length());
            updater.setHits(hits.length());
            updater.setIndeterminate(false);

            for (int i = 0; i < hits.length(); i++) {
                Document doc = hits.doc(i);
                float score = hits.score(i);
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("Hit: " + doc + " Score: " + score);
                }

                ACE.threadPool.execute(new CheckAndProcessLuceneMatch(hitLatch, updater, doc, score, matches,
                    checkList, config));
            }
            if (AceLog.getAppLog().isLoggable(Level.INFO)) {
                if (tracker.continueWork()) {
                    AceLog.getAppLog().info("Search time 1: " + timer.getElapsedTime());
                } else {
                    AceLog.getAppLog().info("Search 1 Canceled. Elapsed time: " + timer.getElapsedTime());
                }
            }
            timer.stop();
            return hitLatch;
        } catch (ParseException pe) {
            AceLog.getAppLog().alertAndLogException(pe);
            timer.stop();
            updater.setProgressInfo("Query malformed: " + query);
            updater.setIndeterminate(false);
            updater.setHits(0);
            return new CountDownLatch(0);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            timer.stop();
            updater.setProgressInfo("Exception during query: " + e.getLocalizedMessage());
            updater.setIndeterminate(false);
            updater.setHits(0);
            return new CountDownLatch(0);
        }
    }

    private static class RegexSearcher implements I_ProcessConceptData {

        private CountDownLatch conceptLatch;
        private I_TrackContinuation tracker;
        private Semaphore checkSemaphore;
        private List<I_TestSearchResults> checkList;
        private I_ConfigAceFrame config;
        private Pattern p;
        Collection<I_DescriptionVersioned> matches;

        public RegexSearcher(CountDownLatch conceptLatch, I_TrackContinuation tracker, Semaphore checkSemaphore,
                List<I_TestSearchResults> checkList, I_ConfigAceFrame config, Pattern p,
                Collection<I_DescriptionVersioned> matches) {
            super();
            this.conceptLatch = conceptLatch;
            this.tracker = tracker;
            this.checkSemaphore = checkSemaphore;
            this.checkList = checkList;
            this.config = config;
            this.p = p;
            this.matches = matches;
        }

        @Override
        public void processConceptData(Concept concept) throws Exception {

            if (tracker.continueWork()) {
                Collection<? extends I_DescriptionVersioned> descriptions = concept.getDescriptions();
                CountDownLatch descriptionLatch = new CountDownLatch(descriptions.size());
                for (I_DescriptionVersioned descV : descriptions) {
                    try {
                        checkSemaphore.acquire();
                    } catch (InterruptedException e) {
                        AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
                    }
                    ACE.threadPool.execute(new CheckAndProcessRegexMatch(descriptionLatch, checkSemaphore, p, matches,
                        descV, checkList, config));
                }
                try {
                    descriptionLatch.await();
                } catch (InterruptedException e) {
                    AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
                }
                conceptLatch.countDown();
            } else {
                while (conceptLatch.getCount() > 0) {
                    conceptLatch.countDown();
                }
            }
        }

        @Override
        public boolean continueWork() {
            return tracker.continueWork();
        }
    }

    @Override
    public void searchRegex(I_TrackContinuation tracker, Pattern p, Collection<I_DescriptionVersioned> matches,
            CountDownLatch conceptLatch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
            throws DatabaseException, IOException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            timer = new Stopwatch();
            timer.start();
        }

        Semaphore checkSemaphore = new Semaphore(15);
        RegexSearcher searcher =
                new RegexSearcher(conceptLatch, tracker, checkSemaphore, checkList, config, p, matches);
        assert conceptLatch.getCount() == Bdb.getConceptDb().getCount() : " counts do not match. Latch count: "
            + conceptLatch.getCount() + " Db count: " + Bdb.getConceptDb().getCount();
        try {
            Bdb.getConceptDb().iterateConceptDataInParallel(searcher);
            conceptLatch.await();
        } catch (Exception e1) {
            AceLog.getAppLog().log(Level.WARNING, e1.getLocalizedMessage(), e1);
            while (conceptLatch.getCount() > 0) {
                conceptLatch.countDown();
            }
        }
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            if (tracker.continueWork()) {
                AceLog.getAppLog().info("Regex Search time: " + timer.getElapsedTime());
            } else {
                AceLog.getAppLog().info("Canceled. Elapsed time: " + timer.getElapsedTime());
            }
            timer.stop();
        }
    }

    @Override
    public void addToWatchList(I_GetConceptData c) {
        Bdb.addToWatchList(c);
    }

    @Override
    public void removeFromWatchList(I_GetConceptData c) {
        Bdb.removeFromWatchList(c);
    }

    SpecRefsetHelper specRefsetHelper;

    @Override
    public I_HelpSpecRefset getSpecRefsetHelper(I_ConfigAceFrame config) throws Exception {
        if (specRefsetHelper == null || specRefsetHelper.getConfig() != config) {
            specRefsetHelper = new SpecRefsetHelper(config);
        }
        return specRefsetHelper;
    }

    @Override
    public I_HelpMarkedParentRefsets getMarkedParentRefsetHelper(I_ConfigAceFrame config, int memberRefsetId,
            int memberTypeId) throws Exception {
        return new MarkedParentRefsetHelper(config, memberRefsetId, memberTypeId);
    }

    @Override
    public I_HelpMemberRefsetsCalculateConflicts getMemberRefsetConflictCalculator(I_ConfigAceFrame config)
            throws Exception {
        return new MemberRefsetConflictCalculator(config);
    }

    @Override
    public I_HelpMemberRefsets getMemberRefsetHelper(I_ConfigAceFrame config, int memberRefsetId, int memberTypeId)
            throws Exception {
        return new MemberRefsetHelper(config, memberRefsetId, memberTypeId);
    }

    @Override
    public I_HelpRefsets getRefsetHelper(I_ConfigAceFrame config) {
        return new RefsetHelper(config);
    }

    @Override
    public boolean pathExists(int pathConceptId) throws TerminologyException {
        return pathManager.exists(pathConceptId);
    }

    @Override
    public Object getComponent(int nid) throws TerminologyException, IOException {
        return Bdb.getComponent(nid);
    }

    @Override
    public void computeRefset(int refsetNid, RefsetSpecQuery query, I_ConfigAceFrame frameConfig) throws Exception {
        SpecRefsetHelper refsetHelper = new SpecRefsetHelper(frameConfig);
        Concept refsetConcept = Concept.get(refsetNid);
        RefsetSpec specHelper = new RefsetSpec(refsetConcept, true);
        ComponentList<RefsetMember<?, ?>> members = refsetConcept.getData().getRefsetMembers();

        HashSet<Integer> currentMembersList = new HashSet<Integer>();
        for (RefsetMember<?, ?> v : members) {
            if (refsetHelper.hasCurrentRefsetExtension(refsetNid, v.getComponentId(), ReferenceConcepts.NORMAL_MEMBER
                .getNid())) {
                if (Terms.get().hasConcept(v.getComponentId())) {
                    currentMembersList.add(v.getComponentId());
                } else { // assume it is a description member
                    I_DescriptionVersioned desc = Terms.get().getDescription(v.getComponentId());
                    if (desc != null) {
                        currentMembersList.add(desc.getConceptId());
                    }
                }
            }
            }

        I_RepresentIdSet possibleIds;
        if (specHelper.isConceptComputeType()) {
            possibleIds = query.getPossibleConcepts(frameConfig, null);
        } else if (specHelper.isDescriptionComputeType()) {
            possibleIds = query.getPossibleDescriptions(frameConfig, null);
        } else {
            throw new Exception("Relationship compute type not supported.");
        }

        // add the current members to the list of possible concepts to check (in case some need to be retired)
        possibleIds.or(getIdSetFromIntCollection(currentMembersList));
        AceLog.getAppLog().info(">>>>>>>>>> Search space (concept count): " + possibleIds.cardinality());

        RefsetComputer computer = new RefsetComputer(refsetNid, query, frameConfig, possibleIds);
        if (possibleIds.cardinality() > 500) {
            Bdb.getConceptDb().iterateConceptDataInParallel(computer);
        } else {
            I_IterateIds possibleItr = possibleIds.iterator();
            ConceptFetcher fetcher = new ConceptFetcher();
            while (possibleItr.next()) {
                 fetcher.setConcept(Concept.get(possibleItr.nid()));
                computer.processUnfetchedConceptData(possibleItr.nid(), fetcher);
            }
        }
        computer.addUncommitted();
        BdbCommitManager.commit(frameConfig.getDbConfig().getRefsetChangesChangeSetPolicy(), 
            frameConfig.getDbConfig().getChangeSetWriterThreading());
    }
    
    private static class ConceptFetcher implements I_FetchConceptFromCursor {
        Concept concept;

        public void setConcept(Concept concept) {
            this.concept = concept;
        }

        @Override
        public Concept fetch() {
            return concept;
        }
        
    }

    @Override
    public I_ImageVersioned newImage(UUID imageUuid, int conceptNid, int typeNid, byte[] image, String textDescription,
            String format, I_ConfigAceFrame aceConfig) throws IOException {

        Concept c = Concept.get(conceptNid);
        Image img = new Image();
        img.setImage(image);
        img.setFormat(format);
        img.setTextDescription(textDescription);
        img.setTypeNid(typeNid);
        img.primordialSapNid = Integer.MIN_VALUE;
        int statusNid = aceConfig.getDefaultStatus().getNid();
        for (I_Path p : aceConfig.getEditingPathSet()) {
            if (img.primordialSapNid == Integer.MIN_VALUE) {
                img.primordialSapNid = Bdb.getSapDb().getSapNid(statusNid, p.getConceptId(), Long.MAX_VALUE);
            } else {
                if (img.revisions == null) {
                    img.revisions = new CopyOnWriteArrayList<ImageRevision>();
                }
                img.revisions.add(img.makeAnalog(statusNid, p.getConceptId(), Long.MAX_VALUE));
            }
        }
        c.getImages().add(img);
        Terms.get().addUncommitted(c);
        return img;
    }

    @Override
    public boolean isCheckCreationDataEnabled() {
        return BdbCommitManager.isCheckCreationDataEnabled();
    }

    @Override
    public boolean isCheckCommitDataEnabled() {
        return BdbCommitManager.isCheckCommitDataEnabled();
    }

    @Override
    public void setCheckCreationDataEnabled(boolean enabled) {
        BdbCommitManager.setCheckCreationDataEnabled(enabled);
    }

    @Override
    public void setCheckCommitDataEnabled(boolean enabled) {
        BdbCommitManager.setCheckCommitDataEnabled(enabled);
    }
    
    public void resetViewPositions() {
        Bdb.getSapDb().clearMapperCache();
    }

}
