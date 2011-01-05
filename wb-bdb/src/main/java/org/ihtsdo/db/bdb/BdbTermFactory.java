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
import org.apache.lucene.search.Query;
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
import org.dwfa.ace.api.I_HelpMarkedParentRefsets;
import org.dwfa.ace.api.I_HelpMemberRefsets;
import org.dwfa.ace.api.I_HelpMemberRefsetsCalculateConflicts;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
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
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
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
import org.dwfa.ace.task.refset.spec.compute.RefsetQueryFactory;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.tapi.ComputationCanceled;
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
import org.ihtsdo.cs.econcept.EConceptChangeSetReader;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.bdb.computer.refset.MarkedParentRefsetHelper;
import org.ihtsdo.db.bdb.computer.refset.MemberRefsetConflictCalculator;
import org.ihtsdo.db.bdb.computer.refset.MemberRefsetHelper;
import org.ihtsdo.db.bdb.computer.refset.RefsetComputer;
import org.ihtsdo.db.bdb.computer.refset.RefsetHelper;
import org.ihtsdo.db.bdb.computer.refset.SpecRefsetHelper;
import org.ihtsdo.db.runner.WorkbenchRunner;
import org.ihtsdo.db.util.NidPairForRefset;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.lucene.CheckAndProcessLuceneMatch;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

import com.sleepycat.je.DatabaseException;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.tk.api.refset.RefsetMemberChronicleBI;

public class BdbTermFactory implements I_TermFactory, I_ImplementTermFactory, I_Search {

    private BdbPathManager pathManager;

    private File envHome;

    public static void canEdit(final I_ConfigAceFrame aceFrameConfig) throws TerminologyException {
        if (aceFrameConfig.getEditingPathSet().isEmpty()) {
            throw new TerminologyException(
                "<br><br>You must select an editing path before editing...<br><br>No editing path selected.");
        }
    }

    public static boolean isHeadless() {
        return DwfaEnv.isHeadless();
    }

    public static void setHeadless(final Boolean headless) {
        DwfaEnv.setHeadless(headless);
    }

    @Override
    public void close() throws IOException {
        try {
            Bdb.close();
            Terms.close(this);
        } catch (final DatabaseException e) {
            throw new IOException(e);
        } catch (final InterruptedException e) {
            throw new IOException(e);
        } catch (final ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void addChangeSetReader(final I_ReadChangeSet reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addChangeSetWriter(String key, ChangeSetGeneratorBI writer) {
        ChangeSetWriterHandler.addWriter(key, writer);
    }

    @Override
    public void addUncommitted(final I_GetConceptData concept) {
        BdbCommitManager.addUncommitted(concept);
    }

    @Override
    public void addUncommitted(final I_ExtendByRef extension) {
        BdbCommitManager.addUncommitted(extension);
    }

    @Override
    public void addUncommittedNoChecks(final I_GetConceptData concept) {
        BdbCommitManager.addUncommittedNoChecks(concept);
    }

    @Override
    public void addUncommittedNoChecks(final I_ExtendByRef extension) {
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
    public void commit(final ChangeSetPolicy changeSetPolicy, final ChangeSetWriterThreading changeSetWriterThreading)
    throws Exception {
        BdbCommitManager.commit(changeSetPolicy, changeSetWriterThreading);
    }

    @Override
    public void commitTransaction() throws IOException {
        // legacy operation, nothing to do...
    }

    @Override
    public long convertToThickVersion(final int version) {
        return ThinVersionHelper.convert(version);
    }

    @Override
    public int convertToThinVersion(final long time) {
        return ThinVersionHelper.convert(time);
    }

    @Override
    public int convertToThinVersion(final String dateStr) throws ParseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SearchResult doLuceneSearch(String query) throws IOException, org.apache.lucene.queryParser.ParseException {
        Query q =
                new QueryParser(LuceneManager.version, "desc", new StandardAnalyzer(LuceneManager.version))
                    .parse(query);
        return LuceneManager.search(q);
    }

    @Override
    public void forget(final I_GetConceptData concept) throws IOException {
        BdbCommitManager.forget(concept);
    }

    @Override
    public void forget(final I_DescriptionVersioned desc) throws IOException {
        BdbCommitManager.forget(desc);
    }

    @Override
    public void forget(final I_RelVersioned rel) throws IOException {
        BdbCommitManager.forget(rel);
    }

    @Override
    public void forget(final I_ExtendByRef extension) throws IOException {
        BdbCommitManager.forget(extension);
    }

    @Override
    public void forget(final I_ConceptAttributeVersioned attr) throws IOException {
        BdbCommitManager.forget(attr);
    }

    I_ConfigAceFrame activeAceFrameConfig;

    @Override
    public I_ConfigAceFrame getActiveAceFrameConfig() throws IOException {
        return this.activeAceFrameConfig;
    }

    @Override
    public void setActiveAceFrameConfig(final I_ConfigAceFrame activeAceFrameConfig) throws TerminologyException, IOException {
        this.activeAceFrameConfig = activeAceFrameConfig;
    }

    @Override
    public List<? extends I_ExtendByRef> getAllExtensionsForComponent(int nid) throws IOException {
        List<NidPairForRefset> pairs = Bdb.getRefsetPairs(nid);
        if (pairs == null || pairs.isEmpty()) {
            return new ArrayList<I_ExtendByRef>(0);
        }
        List<I_ExtendByRef> returnValues = new ArrayList<I_ExtendByRef>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<Integer>();
        for (NidPairForRefset pair : pairs) {
            I_ExtendByRef ext = (I_ExtendByRef) Bdb.getComponent(pair.getMemberNid());
            if (ext != null && !addedMembers.contains(ext.getNid())) {
            	addedMembers.add(ext.getNid());
                returnValues.add(ext);
            }
        }
        
        ComponentBI component = 
                Bdb.getComponent(nid);
        if (component instanceof Concept) {
            component = ((Concept) component).getConceptAttributes();
        }
        ComponentChroncileBI<?> cc = (ComponentChroncileBI<?>) component;
        for (RefsetMemberChronicleBI annotation: cc.getAnnotations()) {
            returnValues.add((I_ExtendByRef) annotation);
        }
        return returnValues;
    }

    @Override
    public List<? extends I_ExtendByRef> getRefsetExtensionsForComponent(int refsetNid, int nid) throws IOException {
        List<NidPairForRefset> pairs = Bdb.getRefsetPairs(nid);
        if (pairs == null || pairs.isEmpty()) {
            return new ArrayList<I_ExtendByRef>(0);
        }
        List<I_ExtendByRef> returnValues = new ArrayList<I_ExtendByRef>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<Integer>();
        for (NidPairForRefset pair : pairs) {
            if (pair.getRefsetNid() == refsetNid) {
                I_ExtendByRef ext = (I_ExtendByRef) Bdb.getComponent(pair.getMemberNid());
                if (ext != null && !addedMembers.contains(ext.getNid())) {
                	addedMembers.add(ext.getNid());
                    returnValues.add(ext);
                }
            }
        }
        return returnValues;
    }

    @Override
    @Deprecated
    public List<? extends I_ExtendByRef> getAllExtensionsForComponent(final int nid, final boolean addUncommitted)
            throws IOException {
        return this.getAllExtensionsForComponent(nid);
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
    public I_GetConceptData getConcept(final Collection<UUID> ids) throws TerminologyException, IOException {
        return Bdb.getConceptDb().getConcept(Bdb.uuidsToNid(ids));
    }

    @Override
    public I_GetConceptData getConcept(final UUID... ids) throws TerminologyException, IOException {
        return Bdb.getConceptDb().getConcept(Bdb.uuidToNid(ids));
    }

    @Override
    public I_GetConceptData getConcept(final int nid) throws TerminologyException, IOException {
        assert nid != Integer.MAX_VALUE;
        return Bdb.getConceptDb().getConcept(nid);
    }

    @Override
    public I_GetConceptData getConcept(final String conceptId, final int sourceId) throws TerminologyException,
    org.apache.lucene.queryParser.ParseException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<I_GetConceptData> getConcept(final String conceptIdStr) throws TerminologyException,
            org.apache.lucene.queryParser.ParseException, IOException {
        Set<I_GetConceptData> matchingConcepts = new HashSet<I_GetConceptData>();
        Query q = new QueryParser(LuceneManager.version,
                "desc", new StandardAnalyzer(LuceneManager.version)).parse(conceptIdStr);
        SearchResult result = LuceneManager.search(q);

        for (int i = 0; i < result.topDocs.totalHits; i++) {
            Document doc = result.searcher.doc(result.topDocs.scoreDocs[i].doc);
            int cnid = Integer.parseInt(doc.get("cnid"));
            matchingConcepts.add(Concept.get(cnid));
        }
        return matchingConcepts;
    }

    @Override
    public int getConceptCount() throws IOException {
        return Bdb.getConceptDb().getCount();
    }

    @Override
    public I_RepresentIdSet getConceptNidSet() throws IOException {
        return Bdb.getConceptDb().getConceptNidSet();
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
    public I_DescriptionVersioned getDescription(final int dNid, final int cNid) throws TerminologyException, IOException {
        if (this.hasConcept(cNid)) {
            return Bdb.getConcept(cNid).getDescription(dNid);
        }
        return null;
    }

    @Override
    public I_DescriptionVersioned getDescription(final int dNid) throws TerminologyException, IOException {
        return this.getDescription(dNid, Bdb.getConceptNid(dNid));
    }

    @Override
    public I_RelVersioned getRelationship(final int rNid) throws IOException {
        if (Bdb.getNidCNidMap().hasMap(rNid)) {
            return Bdb.getConcept(Bdb.getConceptNid(rNid)).getSourceRel(rNid);
        }
        return null;
    }

    @Override
    public I_RepresentIdSet getDescriptionIdSet() throws IOException {
        I_RepresentIdSet descriptionIdSet = new IdentifierSet();
        NidBitSetItrBI iterator = getConceptNidSet().iterator();
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
    public I_ExtendByRef getExtension(final int memberId) throws IOException {
        return (I_ExtendByRef) Bdb.getConceptForComponent(memberId).getComponent(memberId);
    }

    @Override
    public I_Identify getId(final int nid) throws TerminologyException, IOException {
        final Concept concept = Bdb.getConceptForComponent(nid);
        if (concept != null) {
            return (I_Identify) concept.getComponent(nid);
        }

        return null;
    }

    @Override
    public I_Identify getId(final UUID uuid) throws TerminologyException, IOException {
        return this.getId(Bdb.uuidToNid(uuid));
    }

    @Override
    public I_Identify getId(final Collection<UUID> uids) throws TerminologyException, IOException {
        return this.getId(Bdb.uuidsToNid(uids));
    }

    @Override
    public I_RepresentIdSet getIdSetFromIntCollection(final Collection<Integer> ids) throws IOException {
        final I_RepresentIdSet newSet = this.getEmptyIdSet();
        for (final int nid : ids) {
            newSet.setMember(nid);
        }
        return newSet;
    }

    @Override
    public I_RepresentIdSet getIdSetfromTermCollection(final Collection<? extends I_AmTermComponent> components)
    throws IOException {
        final I_RepresentIdSet newSet = this.getEmptyIdSet();
        for (final I_AmTermComponent component : components) {
            newSet.setMember(component.getNid());
        }
        return newSet;
    }

    @Override
    public PathBI getPath(final Collection<UUID> uids) throws TerminologyException, IOException {
        return pathManager.get(uuidToNative(uids));
    }

    @Override
    public PathBI getPath(final UUID... ids) throws TerminologyException, IOException {
        return pathManager.get(Bdb.uuidToNid(ids));
    }

    @Override
    public PathBI getPath(final int nid) throws TerminologyException, IOException {
        return pathManager.get(nid);
    }

    @Override
    public List<PathBI> getPaths() throws Exception {
        return new ArrayList<PathBI>(pathManager.getAll());
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
    public String getProperty(final String key) throws IOException {
        return Bdb.getProperty(key);
    }

    @Override
    public I_RepresentIdSet getReadOnlyConceptIdSet() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<RefsetMember<?, ?>> getRefsetExtensionMembers(final int refsetId) throws IOException {
        return Concept.get(refsetId).getExtensions();
    }

    @Override
    public I_RepresentIdSet getRelationshipIdSet() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStats() throws IOException {
        return Bdb.getStats();
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
    public Collection<UUID> getUids(final int nid) throws TerminologyException, IOException {
        return this.getId(nid).getUUIDs();
    }

    @Override
    public Set<? extends I_Transact> getUncommitted() {
        return BdbCommitManager.getUncommitted();
    }

    @Override
    public boolean hasConcept(final int conceptId) throws IOException {
        if (conceptId == Integer.MIN_VALUE || conceptId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        if (Bdb.getNidCNidMap().hasConcept(conceptId)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasDescription(final int descId, final int conceptId) throws IOException {
        if (descId == Integer.MIN_VALUE || descId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        final ComponentBI c = Bdb.getComponent(descId);
        if (c == null) {
            return false;
        }
        return Description.class.isAssignableFrom(c.getClass());
    }

    @Override
    public boolean hasExtension(final int memberId) throws IOException {
        if (memberId == Integer.MIN_VALUE || memberId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        final ComponentBI c = Bdb.getComponent(memberId);
        if (c == null) {
            return false;
        }
        return RefsetMember.class.isAssignableFrom(c.getClass());
    }

    @Override
    public boolean hasId(final Collection<UUID> uuids) throws IOException {
        for (final UUID uuid : uuids) {
            if (Bdb.hasUuid(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasId(final UUID uuid) throws IOException {
        return Bdb.hasUuid(uuid);
    }

    @Override
    public boolean hasImage(final int imageId) throws IOException {
        if (imageId == Integer.MIN_VALUE || imageId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        final ComponentBI c = Bdb.getComponent(imageId);
        if (c == null) {
            return false;
        }
        return Image.class.isAssignableFrom(c.getClass());
    }

    @Override
    public boolean hasPath(int nid) throws IOException {
        try {
            return pathManager.hasPath(nid);
        } catch (final PathNotExistsException e) {
            return false;
        } catch (final TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return false;
    }

    @Override
    public boolean hasRel(final int relId, final int conceptId) throws IOException {
        if (relId == Integer.MIN_VALUE || relId > Bdb.getUuidsToNidMap().getCurrentMaxNid()) {
            return false;
        }
        final ComponentBI c = Bdb.getComponent(relId);
        if (c == null) {
            return false;
        }
        return Relationship.class.isAssignableFrom(c.getClass());
    }

    private class IterateConceptsAdaptor implements I_ProcessConceptData {
        private final I_ProcessConcepts processor;

        public IterateConceptsAdaptor(final I_ProcessConcepts procesor) {
            super();
            this.processor = procesor;
        }

        @Override
        public void processConceptData(final Concept concept) throws Exception {
            this.processor.processConcept(concept);
        }

        @Override
        public boolean continueWork() {
            return true;
        }
		@Override
		public NidBitSetBI getNidSet() throws IOException {
			return Bdb.getConceptDb().getReadOnlyConceptIdSet();
		}       
    }

    @Override
    public void iterateConcepts(final I_ProcessConcepts procesor) throws Exception {
        Bdb.getConceptDb().iterateConceptDataInSequence(new IterateConceptsAdaptor(procesor));
    }

    @Override
    public void iterateDescriptions(final I_ProcessDescriptions processor) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void iterateExtByRefs(final I_ProcessExtByRef processor) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFromDirectory(final File dataDir, final String encoding) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFromSingleJar(final String jarFile, final String dataPrefix) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransferHandler makeTerminologyTransferHandler(final JComponent thisComponent) {
        return new TerminologyTransferHandler(thisComponent);
    }

    public static class MakeNewAceFrame implements Runnable {
        I_ConfigAceFrame frameConfig;
        Exception ex;

        public MakeNewAceFrame(final I_ConfigAceFrame frameConfig) {
            super();
            this.frameConfig = frameConfig;
        }

        public void run() {
            try {
                final AceFrame newFrame = new AceFrame(WorkbenchRunner.args, WorkbenchRunner.lc, this.frameConfig, false);
                newFrame.setVisible(true);
                final AceFrameConfig nativeConfig = (AceFrameConfig) this.frameConfig;
                nativeConfig.setAceFrame(newFrame);
            } catch (final Exception e) {
                this.ex = e;
            }
        }

        public void check() throws Exception {
            if (this.ex != null) {
                throw this.ex;
            }
        }
    }

    @Override
    public void newAceFrame(final I_ConfigAceFrame frameConfig) throws Exception {
        final MakeNewAceFrame maker = new MakeNewAceFrame(frameConfig);
        SwingUtilities.invokeAndWait(maker);
        maker.check();
    }

    @Override
    public I_ConfigAceFrame newAceFrameConfig() throws TerminologyException, IOException {
        return new AceFrameConfig();
    }

    @Override
    public I_ShowActivity newActivityPanel(final boolean displayInViewer, final I_ConfigAceFrame aceFrameConfig,
            final String firstUpperInfo, final boolean showStop) {
        if (isHeadless()) {
            return new UpperInfoOnlyConsoleMonitor();
        } else {
            final ActivityPanel ap = new ActivityPanel(aceFrameConfig, showStop);
            ap.setIndeterminate(true);
            ap.setProgressInfoUpper(firstUpperInfo);
            ap.setProgressInfoLower("");
            if (displayInViewer) {
                try {
                    ActivityViewer.addActivity(ap);
                } catch (final Exception e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                }
            }
            return ap;
        }
    }

    @Override
    public I_ReadChangeSet newBinaryChangeSetReader(final File changeSetFile) throws IOException {
        final EConceptChangeSetReader ecr = new EConceptChangeSetReader();
        ecr.setChangeSetFile(changeSetFile);
        return ecr;
    }

    @Override
    public I_WriteChangeSet newBinaryChangeSetWriter(final File changeSetFile) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public I_GetConceptData newConcept(final UUID newConceptUuid, final boolean defined, final I_ConfigAceFrame aceFrameConfig)
    throws TerminologyException, IOException {
        final int statusNid = aceFrameConfig.getDefaultStatus().getNid();
        return this.newConcept(newConceptUuid, defined, aceFrameConfig, statusNid);
    }

    @Override
    public I_GetConceptData newConcept(final UUID newConceptUuid, final boolean defined, final I_ConfigAceFrame aceFrameConfig,
            final int statusNid) throws TerminologyException, IOException {
        return this.newConcept(newConceptUuid, defined, aceFrameConfig, statusNid, Long.MAX_VALUE);
    }

    @Override
    public I_GetConceptData newConcept(final UUID newConceptUuid, final boolean isDefined, final I_ConfigAceFrame aceFrameConfig,
            final int statusNid, final long time) throws TerminologyException, IOException {
        canEdit(aceFrameConfig);
        final int cNid = Bdb.uuidToNid(newConceptUuid);
        Bdb.getNidCNidMap().setCNidForNid(cNid, cNid);
        final Concept newC = Concept.get(cNid);
        final ConceptAttributes a = new ConceptAttributes();
        a.nid = cNid;
        a.enclosingConceptNid = cNid;
        newC.setConceptAttributes(a);
        a.setDefined(isDefined);
        a.primordialUNid = Bdb.getUuidsToNidMap().getUNid(newConceptUuid);
        a.primordialSapNid = Integer.MIN_VALUE;

        for (final PathBI p : aceFrameConfig.getEditingPathSet()) {
            if (a.primordialSapNid == Integer.MIN_VALUE) {
                a.primordialSapNid =
                        Bdb.getSapDb().getSapNid(statusNid, getUserNid(aceFrameConfig), p.getConceptNid(), time);
            } else {
                if (a.revisions == null) {
                    a.revisions = new CopyOnWriteArrayList<ConceptAttributesRevision>();
                }
                a.revisions.add((ConceptAttributesRevision) a.makeAnalog(statusNid, p.getConceptNid(), time));
            }
        }
        return newC;
    }

    private static int getUserNid(I_ConfigAceFrame aceFrameConfig) {
        int userNid = ReferenceConcepts.USER.getNid();
        if (aceFrameConfig.getDbConfig() != null && aceFrameConfig.getDbConfig().getUserConcept() != null) {
            userNid = aceFrameConfig.getDbConfig().getUserConcept().getConceptNid();
        }
        return userNid;
    }

    @Override
    public I_ConceptAttributePart newConceptAttributePart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Description newDescription(final UUID newDescriptionId, final I_GetConceptData concept, final String lang, final String text,
            final I_ConceptualizeLocally descType, final I_ConfigAceFrame aceFrameConfig) throws TerminologyException, IOException {
        return this.newDescription(newDescriptionId, concept, lang, text, Terms.get().getConcept(descType.getNid()),
                aceFrameConfig);
    }

    @Override
    public Description newDescription(final UUID descUuid, final I_GetConceptData concept, final String lang, final String text,
            final I_GetConceptData descType, final I_ConfigAceFrame aceFrameConfig) throws TerminologyException, IOException {

        final int statusNid = aceFrameConfig.getDefaultStatus().getNid();
        return this.newDescription(descUuid, concept, lang, text, descType, aceFrameConfig, statusNid);
    }

    public Description newDescription(final UUID descUuid, final I_GetConceptData concept, final String lang, final String text,
            final I_GetConceptData descriptionType, final I_ConfigAceFrame aceFrameConfig, final int statusNid)
    throws TerminologyException, IOException {
        return this.newDescription(descUuid, concept, lang, text, descriptionType, aceFrameConfig,
                Bdb.getConcept(statusNid), Long.MAX_VALUE);
    }

    @Override
    public Description newDescription(final UUID descUuid, final I_GetConceptData concept, final String lang, final String text,
            final I_GetConceptData descType, final I_ConfigAceFrame aceFrameConfig, final I_GetConceptData status, final long effectiveDate)
    throws TerminologyException, IOException {

        canEdit(aceFrameConfig);
        final Concept c = (Concept) concept;
        final Description d = new Description();
        Bdb.gVersion.incrementAndGet();
        d.enclosingConceptNid = c.getNid();
        d.nid = Bdb.uuidToNid(descUuid);
        Bdb.getNidCNidMap().setCNidForNid(c.getNid(), d.nid);
        d.primordialUNid = Bdb.getUuidsToNidMap().getUNid(descUuid);
        d.setLang(lang);
        d.setText(text);
        d.setInitialCaseSignificant(false);
        d.setTypeNid(descType.getNid());
        d.primordialSapNid = Integer.MIN_VALUE;
        for (final PathBI p : aceFrameConfig.getEditingPathSet()) {
            if (d.primordialSapNid == Integer.MIN_VALUE) {
                d.primordialSapNid =
                        Bdb.getSapDb().getSapNid(status.getNid(), getUserNid(aceFrameConfig), p.getConceptNid(),
                            effectiveDate);
            } else {
                if (d.revisions == null) {
                    d.revisions = new CopyOnWriteArrayList<DescriptionRevision>();
                }
                d.revisions.add(d.makeAnalog(status.getNid(), p.getConceptNid(), effectiveDate));
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
    public I_ExtendByRef newExtension(final int refsetId, final int memberId, final int componentId,
            final Class<? extends I_ExtendByRefPart> partType) throws IOException {
        final Concept refsetConcept = Concept.get(refsetId);
        final I_ConfigAceFrame config = this.getActiveAceFrameConfig();
        Bdb.getNidCNidMap().setCNidForNid(refsetId, memberId);

        final RefsetMember<?, ?> member =
            createMember(memberId, componentId, EConcept.REFSET_TYPES.classToType(partType), refsetConcept, config);
        this.addUncommitted(refsetConcept);
        return member;
    }

    @Override
    public I_ExtendByRef newExtension(final int refsetId, final UUID memberPrimUuid, final int componentId, final int typeId)
    throws IOException {
        final Concept refsetConcept = Concept.get(refsetId);
        final I_ConfigAceFrame config = this.getActiveAceFrameConfig();
        final RefsetMember<?, ?> member =
            createMember(memberPrimUuid, componentId, EConcept.REFSET_TYPES.nidToType(typeId), refsetConcept,
                    config, new RefsetPropertyMap());
        this.addUncommitted(refsetConcept);
        return member;
    }

    @Override
    public I_ExtendByRef newExtension(final int refsetId, final UUID memberPrimUuid, final int componentId,
            final Class<? extends I_ExtendByRefPart> partType) throws IOException {
        final Concept refsetConcept = Concept.get(refsetId);
        final I_ConfigAceFrame config = this.getActiveAceFrameConfig();
        final RefsetMember<?, ?> member =
            createMember(memberPrimUuid, componentId, EConcept.REFSET_TYPES.classToType(partType), refsetConcept,
                    config, new RefsetPropertyMap());
        this.addUncommitted(refsetConcept);
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
    public static RefsetMember<?, ?> createMember(final int memberId, final int referencedComponentNid, final EConcept.REFSET_TYPES type,
            final Concept refsetConcept, final I_ConfigAceFrame config) throws IOException {
        final UUID primordialUuid = Bdb.getUuidsToNidMap().getUuidsForNid(memberId).iterator().next();
        return createMember(primordialUuid, referencedComponentNid, type, refsetConcept, config,
                new RefsetPropertyMap());
    }

    public static RefsetMember<?, ?> createMember(final UUID primordialUuid, final int referencedComponentNid,
            final EConcept.REFSET_TYPES type, final Concept refsetConcept, final I_ConfigAceFrame config, final RefsetPropertyMap propMap)
            throws IOException {
        assert referencedComponentNid != 0 : " invalid referencedComponentNid for refset: " + refsetConcept;
        RefsetMember<?, ?> member = null;
        AceLog.getEditLog().info("BdbTermFactory createMember type = "+type);
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

    private static RefsetMember<?, ?> setupNewMember(final UUID primordialUuid, final int referencedComponentNid,
            final Concept refsetConcept, final I_ConfigAceFrame config, final RefsetMember<?, ?> member, final RefsetPropertyMap propMap)
            throws IOException {

        assert config != null : "Config cannot be null.";
        assert config.getEditingPathSet() != null : "Config edit path set cannot be null.";
        member.enclosingConceptNid = refsetConcept.getNid();
        member.nid = Bdb.uuidToNid(primordialUuid);
        member.refsetNid = refsetConcept.getNid();
        Bdb.getNidCNidMap().setCNidForNid(refsetConcept.getNid(), member.nid);
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
        for (final PathBI p : config.getEditingPathSet()) {
            if (member.primordialSapNid == Integer.MIN_VALUE) {
                member.primordialSapNid =
                        Bdb.getSapDb().getSapNid(statusNid, getUserNid(config), p.getConceptNid(), time);
                propMap.setPropertiesExceptSap((I_ExtendByRefPart) member);
            } else {
                final I_ExtendByRefPart revision = (I_ExtendByRefPart) member.makeAnalog(statusNid, p.getConceptNid(), time);
                propMap.setProperties(revision);
                member.addVersion(revision);
            }
        }
        if (refsetConcept.isAnnotationStyleRefset()) {
            ConceptComponent<?,?> referencedComponent = (ConceptComponent<?, ?>) Bdb.getComponent(referencedComponentNid);
            referencedComponent.addAnnotation(member);
        } else {
            refsetConcept.getExtensions().add(member);
        }
        return member;
    }

    @Override
    public I_ExtendByRef newExtensionNoChecks(final int refsetId, final int memberId, final int componentId, final int typeId)
    throws IOException {
        final Concept refsetConcept = Concept.get(refsetId);
        final I_ConfigAceFrame config = this.getActiveAceFrameConfig();
        final RefsetMember<?, ?> member =
            createMember(memberId, componentId, REFSET_TYPES.nidToType(typeId), refsetConcept, config);
        this.addUncommittedNoChecks(refsetConcept);
        return member;
    }

    @Override
    public <T extends I_ExtendByRefPart> T newExtensionPart(final Class<T> t) {
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
    @Deprecated
    public PathBI newPath(final Set<PositionBI> origins, final I_GetConceptData pathConcept) throws TerminologyException,
            IOException {
        return newPath(origins, pathConcept, getActiveAceFrameConfig());
    }

    @Override
    public PathBI newPath(final Collection<? extends PositionBI> origins, final I_GetConceptData pathConcept,
            I_ConfigAceFrame commitConfig) throws TerminologyException, IOException {
    	AceLog.getEditLog().info("BdbTermFactory newPath Called");
        assert pathConcept != null && pathConcept.getConceptNid() != 0;
        AceLog.getEditLog().info("BdbTermFactory newPath pathConcept != null && pathConcept.getConceptNid() != 0");
        final ArrayList<PositionBI> originList = new ArrayList<PositionBI>();
        if (origins != null) {
        	AceLog.getEditLog().info("BdbTermFactory newPath origins != null");
            originList.addAll(origins);
        }
        AceLog.getEditLog().info("BdbTermFactory newPath creating new path");
        final Path newPath = new Path(pathConcept.getConceptNid(), originList);
        AceLog.getEditLog().info("BdbTermFactory writing new path: \n" + newPath);
        this.pathManager.write(newPath, commitConfig);
        AceLog.getEditLog().info("BdbTermFactory newPath about to return new Path");
        return newPath;
    }

    @Override
    public Position newPosition(final PathBI path, final int version) throws TerminologyException, IOException {
        return new Position(version, path);
    }

    @Override
    public I_RelPart newRelPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Relationship newRelationship(final UUID newRelUid, final I_GetConceptData concept, final I_ConfigAceFrame aceFrameConfig)
    throws TerminologyException, IOException {
        return this.newRelationship(newRelUid, concept, aceFrameConfig.getDefaultRelationshipType(), aceFrameConfig
                .getHierarchySelection(), aceFrameConfig.getDefaultRelationshipCharacteristic(), aceFrameConfig
                .getDefaultRelationshipRefinability(), aceFrameConfig.getDefaultStatus(), 0, aceFrameConfig);
    }

    @Override
    public Relationship newRelationship(final UUID newRelUid, final I_GetConceptData concept, final I_GetConceptData relType,
            final I_GetConceptData relDestination, final I_GetConceptData relCharacteristic, final I_GetConceptData relRefinability,
            final I_GetConceptData relStatus, final int relGroup, final I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
            IOException {
        return this.newRelationship(newRelUid, concept, relType, relDestination, relCharacteristic, relRefinability,
                relStatus, relGroup, aceFrameConfig, Long.MAX_VALUE);
    }

    @Override
    public Relationship newRelationship(final UUID newRelUid, final I_GetConceptData concept, final I_GetConceptData relType,
            final I_GetConceptData relDestination, final I_GetConceptData relCharacteristic, final I_GetConceptData relRefinability,
            final I_GetConceptData relStatus, final int group, final I_ConfigAceFrame aceFrameConfig, final long effectiveDate)
    throws TerminologyException, IOException {
        canEdit(aceFrameConfig);
        if (concept == null) {
            AceLog.getAppLog().alertAndLogException(
                    new Exception("Cannot add a relationship while the component viewer is empty..."));
            return null;
        }
        final Concept c = (Concept) concept;
        final Relationship r = new Relationship();
        Bdb.gVersion.incrementAndGet();
        r.enclosingConceptNid = c.getNid();
        r.nid = Bdb.uuidToNid(newRelUid);
        Bdb.getNidCNidMap().setCNidForNid(c.getNid(), r.nid);
        r.primordialUNid = Bdb.getUuidsToNidMap().getUNid(newRelUid);
        final int parentId = relDestination.getNid();
        r.setC2Id(parentId);
        r.setTypeId(relType.getNid());
        r.setRefinabilityId(relRefinability.getNid());
        r.setCharacteristicId(relCharacteristic.getNid());
        r.primordialSapNid = Integer.MIN_VALUE;
        r.setGroup(group);
        final int statusNid = relStatus.getNid();
        for (final PathBI p : aceFrameConfig.getEditingPathSet()) {
            if (r.primordialSapNid == Integer.MIN_VALUE) {
                r.primordialSapNid =
                        Bdb.getSapDb().getSapNid(statusNid, getUserNid(aceFrameConfig), p.getConceptNid(),
                            effectiveDate);
            } else {
                if (r.revisions == null) {
                    r.revisions = new CopyOnWriteArrayList<RelationshipRevision>();
                }
                r.revisions.add((RelationshipRevision) r.makeAnalog(statusNid, p.getConceptNid(), effectiveDate));
            }
        }
        c.getSourceRels().add(r);
        // AceLog.getAppLog().info("BDBTF newRelationship r ="+r );
        // AceLog.getAppLog().info("BDBTF newRelationship c ="+c.toLongString() );
        return r;
    }

    /**
     * newRelationshipNoCheck for use with classifier write-back to classifier output path.<br>
     * <br>
     * This call does not check the write-back path as path checking is done
     * as part of the classifier setup.
     */
    @Override
    public Relationship newRelationshipNoCheck(final UUID newRelUid, final I_GetConceptData concept, final int relTypeNid, final int c2Nid,
            final int relCharacteristicNid, final int relRefinabilityNid, final int relStatusNid, final int group, final int pathNid,
            final long effectiveDate) throws TerminologyException, IOException {
        // NO CHECK PERFORMED
        final Concept c = (Concept) concept;
        final Relationship r = new Relationship();
        Bdb.gVersion.incrementAndGet();
        r.enclosingConceptNid = c.getNid();
        r.nid = Bdb.uuidToNid(newRelUid);
        Bdb.getNidCNidMap().setCNidForNid(c.getNid(), r.nid);
        r.primordialUNid = Bdb.getUuidsToNidMap().getUNid(newRelUid);
        r.setC2Id(c2Nid);
        r.setTypeId(relTypeNid);
        r.setRefinabilityId(relRefinabilityNid);
        r.setCharacteristicId(relCharacteristicNid);
        r.setGroup(group);
        r.primordialSapNid =
                Bdb.getSapDb().getSapNid(relStatusNid,
                    getActiveAceFrameConfig().getDbConfig().getUserConcept().getConceptNid(), pathNid, effectiveDate);
        c.getSourceRels().add(r);
        return r;
    }

    @Override
    public I_RelVersioned newRelationshipNoCheck(UUID newRelUid, I_GetConceptData concept, int relTypeNid, int c2Nid,
            int relCharacteristicNid, int relRefinabilityNid, int group, int relStatusNid, int authorNid, int pathNid,
            long effectiveDate) throws TerminologyException, IOException {
        // NO CHECK PERFORMED
        Concept c = (Concept) concept;
        Relationship r = new Relationship();
        Bdb.gVersion.incrementAndGet();
        r.enclosingConceptNid = c.getNid();
        r.nid = Bdb.uuidToNid(newRelUid);
        Bdb.getNidCNidMap().setCNidForNid(c.getNid(), r.nid);
        r.primordialUNid = Bdb.getUuidsToNidMap().getUNid(newRelUid);
        r.setC2Id(c2Nid);
        r.setTypeId(relTypeNid);
        r.setRefinabilityId(relRefinabilityNid);
        r.setCharacteristicId(relCharacteristicNid);
        r.setGroup(group);
        r.primordialSapNid = Bdb.getSapDb().getSapNid(relStatusNid, authorNid, pathNid, effectiveDate);
        c.getSourceRels().add(r);
        return r;
    }

    @Override
    public void removeChangeSetReader(final I_ReadChangeSet reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeChangeSetWriter(String key) {
        ChangeSetWriterHandler.removeWriter(key);
    }

    @Override
    public void removeFromCacheAndRollbackTransaction(final int memberId) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resumeChangeSetWriters() {
        BdbCommitManager.resumeChangeSetWriters();
    }

    @Override
    public void setProperty(final String key, final String value) throws IOException {
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
    public int uuidToNative(final UUID... uid) throws TerminologyException, IOException {
        return Bdb.uuidToNid(uid);
    }

    @Override
    public int uuidToNative(final Collection<UUID> uids) throws TerminologyException, IOException {
        return Bdb.uuidsToNid(uids);
    }

    @Override
    @Deprecated
    public void writeId(final I_Identify versioned) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writePath(final PathBI p, final I_ConfigAceFrame config) throws IOException {
        try {
            pathManager.write(p, config);
        } catch (TerminologyException e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<Path> getPathChildren(final int nid) throws TerminologyException {
        return this.pathManager.getPathChildren(nid);
    }

    @Override
    public void writePathOrigin(final PathBI path, final PositionBI origin, final I_ConfigAceFrame config) throws TerminologyException {
        pathManager.writeOrigin(path, origin, config);
    }

    public void setPathManager(final BdbPathManager pathManager) {
        this.pathManager = pathManager;
    }

    @Override
    public List<UUID> nativeToUuid(final int nid) throws IOException {
        final Concept concept = Bdb.getConceptForComponent(nid);
        if (concept != null && concept.isCanceled() == false) {
            return concept.getUidsForComponent(nid);
        }
        return null;
    }

    public UUID nidToUuid(int nid) throws IOException {
        Concept c = Bdb.getConceptForComponent(nid);
        if (c == null) {
            return Bdb.getUuidsToNidMap().getUuidsForNid(nid).get(0);
        }
        ComponentChroncileBI<?> component = c.getComponent(nid);
        assert component != null : "No component in concept for nid: " + nid + "\n\n\n" + c.toLongString();
        return component.getPrimUuid();
    }

    @Override
    public I_ImageVersioned getImage(final UUID uuid) throws IOException {
        return this.getImage(Bdb.uuidToNid(uuid));
    }

    @Override
    public I_ImageVersioned getImage(final int nid) throws IOException {
        return (I_ImageVersioned) Bdb.getConceptForComponent(nid).getComponent(nid);
    }

    @Override
    public void checkpoint() throws IOException {
        try {
            Bdb.sync();
        } catch (final InterruptedException e) {
            throw new IOException(e);
        } catch (final ExecutionException e) {
            throw new IOException(e);
        } catch (final TerminologyException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void compress(final int minUtilization) throws IOException {
        Bdb.compress(minUtilization);
    }

    @Override
    public I_ConfigAceDb newAceDbConfig() {
        return new AceConfig(this.envHome, false);
    }

    @Override
    public void setup(final Object envHome, final boolean readOnly, final Long cacheSize) throws IOException {
        final File homeFile = (File) envHome;
        Bdb.setup(homeFile.getAbsolutePath());
    }

    @Override
    public void setup(final Object envHome, final boolean readOnly, final Long cacheSize, final DatabaseSetupConfig databaseSetupConfig)
    throws IOException {
        final File homeFile = (File) envHome;
        Bdb.setup(homeFile.getAbsolutePath());
    }

    private static class ConceptSearcher implements I_ProcessConceptData {

        private CountDownLatch conceptLatch;
        private I_TrackContinuation tracker;
        private List<I_TestSearchResults> checkList;
        private I_ConfigAceFrame config;
        private I_RepresentIdSet matches;
    	private NidBitSetBI nidSet;
    	
    	public NidBitSetBI getNidSet() {
    		return nidSet;
    	}

        public ConceptSearcher(final CountDownLatch conceptLatch, final I_TrackContinuation tracker,
                final List<I_TestSearchResults> checkList, final I_ConfigAceFrame config, final I_RepresentIdSet matches) throws IOException {
            super();
            this.conceptLatch = conceptLatch;
            this.tracker = tracker;
            this.checkList = checkList;
            this.config = config;
            this.matches = matches;
            this.nidSet = Bdb.getConceptDb().getConceptNidSet();
        }

        @Override
        public void processConceptData(final Concept concept) throws Exception {

            if (tracker.continueWork()) {
                boolean failed = false;
                for (I_TestSearchResults test : checkList) {
                    if (test.test(concept, config) == false) {
                        failed = true;
                        break;
                    }
                }
                if (!failed) {
                    matches.setMember(concept.getNid());
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
    public void searchConcepts(I_TrackContinuation tracker, I_RepresentIdSet matches, CountDownLatch latch,
            List<I_TestSearchResults> checkList, I_ConfigAceFrame config) throws DatabaseException, IOException,
            org.apache.lucene.queryParser.ParseException {
        final ConceptSearcher searcher = new ConceptSearcher(latch, tracker, checkList, config, matches);
        try {
            Bdb.getConceptDb().iterateConceptDataInParallel(searcher);
        } catch (final Exception e) {
            throw new IOException();
        }

    }

    @Override
    public CountDownLatch searchLucene(final I_TrackContinuation tracker, final String query, final Collection<LuceneMatch> matches,
            final CountDownLatch latch, final List<I_TestSearchResults> checkList, final I_ConfigAceFrame config,
            final LuceneProgressUpdator updater) throws DatabaseException, IOException,
            org.apache.lucene.queryParser.ParseException {
        final Stopwatch timer = new Stopwatch();
        ;
        timer.start();
        try {
            Query q =
                    new QueryParser(LuceneManager.version, "desc", new StandardAnalyzer(LuceneManager.version))
                        .parse(query);
            if (LuceneManager.indexExists() == false) {
                updater.setProgressInfo("Making lucene index -- this may take a while...");
                LuceneManager.createLuceneDescriptionIndex();
            }
            updater.setIndeterminate(true);
            updater.setProgressInfo("Starting StandardAnalyzer lucene query...");
            final long startTime = System.currentTimeMillis();
            updater.setProgressInfo("Query complete in " + Long.toString(System.currentTimeMillis() - startTime)
                    + " ms.");
            SearchResult result = LuceneManager.search(q);

            if (result.topDocs.totalHits > 0) {
                AceLog.getAppLog().info("StandardAnalyzer query returned " + result.topDocs.totalHits + " hits");
            } else {
                updater.setProgressInfo("Starting WhitespaceAnalyzer lucene query...");
                AceLog.getAppLog().info(
                    "StandardAnalyzer query returned no results. Now trying WhitespaceAnalyzer query");
                q = new QueryParser(LuceneManager.version, "desc", new WhitespaceAnalyzer()).parse(query);
                result = LuceneManager.search(q);
            }

            updater.setProgressInfo("Query complete in " + Long.toString(System.currentTimeMillis() - startTime)
                + " ms. Hits: " + result.topDocs.totalHits);

            final CountDownLatch hitLatch = new CountDownLatch(result.topDocs.totalHits);
            updater.setHits(result.topDocs.totalHits);
            updater.setIndeterminate(false);

            for (int i = 0; i < result.topDocs.totalHits; i++) {
                final Document doc = result.searcher.doc(result.topDocs.scoreDocs[i].doc);
                final float score = result.topDocs.scoreDocs[i].score;
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
        } catch (final ParseException pe) {
            AceLog.getAppLog().alertAndLogException(pe);
            timer.stop();
            updater.setProgressInfo("Query malformed: " + query);
            updater.setIndeterminate(false);
            updater.setHits(0);
            return new CountDownLatch(0);
        } catch (final Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            timer.stop();
            updater.setProgressInfo("Exception during query: " + e.getLocalizedMessage());
            updater.setIndeterminate(false);
            updater.setHits(0);
            return new CountDownLatch(0);
        }
    }

    private static class RegexSearcher implements I_ProcessConceptData {

        private final CountDownLatch conceptLatch;
        private final I_TrackContinuation tracker;
        private final Semaphore checkSemaphore;
        private final List<I_TestSearchResults> checkList;
        private final I_ConfigAceFrame config;
        private final Pattern p;
        Collection<I_DescriptionVersioned> matches;
    	private NidBitSetBI nidSet;
    	
    	public NidBitSetBI getNidSet() {
    		return nidSet;
    	}

        public RegexSearcher(final CountDownLatch conceptLatch, final I_TrackContinuation tracker, final Semaphore checkSemaphore,
                final List<I_TestSearchResults> checkList, final I_ConfigAceFrame config, final Pattern p,
                final Collection<I_DescriptionVersioned> matches) throws IOException {
            super();
            this.conceptLatch = conceptLatch;
            this.tracker = tracker;
            this.checkSemaphore = checkSemaphore;
            this.checkList = checkList;
            this.config = config;
            this.p = p;
            this.matches = matches;
            this.nidSet = Bdb.getConceptDb().getConceptNidSet();
        }

        @Override
        public void processConceptData(final Concept concept) throws Exception {

            if (this.tracker.continueWork()) {
                final Collection<? extends I_DescriptionVersioned> descriptions = concept.getDescriptions();
                final CountDownLatch descriptionLatch = new CountDownLatch(descriptions.size());
                for (final I_DescriptionVersioned descV : descriptions) {
                    try {
                        this.checkSemaphore.acquire();
                    } catch (final InterruptedException e) {
                        AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
                    }
                    ACE.threadPool.execute(new CheckAndProcessRegexMatch(descriptionLatch, this.checkSemaphore, this.p, this.matches,
                            descV, this.checkList, this.config));
                }
                try {
                    descriptionLatch.await();
                } catch (final InterruptedException e) {
                    AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
                }
                this.conceptLatch.countDown();
            } else {
                while (this.conceptLatch.getCount() > 0) {
                    this.conceptLatch.countDown();
                }
            }
        }

        @Override
        public boolean continueWork() {
            return this.tracker.continueWork();
        }
    }

    @Override
    public void searchRegex(final I_TrackContinuation tracker, final Pattern p, final Collection<I_DescriptionVersioned> matches,
            final CountDownLatch conceptLatch, final List<I_TestSearchResults> checkList, final I_ConfigAceFrame config)
    throws DatabaseException, IOException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            timer = new Stopwatch();
            timer.start();
        }

        final Semaphore checkSemaphore = new Semaphore(15);
        final RegexSearcher searcher =
            new RegexSearcher(conceptLatch, tracker, checkSemaphore, checkList, config, p, matches);
        assert conceptLatch.getCount() == Bdb.getConceptDb().getCount() : " counts do not match. Latch count: "
            + conceptLatch.getCount() + " Db count: " + Bdb.getConceptDb().getCount();
        try {
            Bdb.getConceptDb().iterateConceptDataInParallel(searcher);
            conceptLatch.await();
        } catch (final Exception e1) {
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
    public void addToWatchList(final I_GetConceptData c) {
        Bdb.addToWatchList(c);
    }

    @Override
    public void removeFromWatchList(final I_GetConceptData c) {
        Bdb.removeFromWatchList(c);
    }

    SpecRefsetHelper specRefsetHelper;

    @Override
    public I_HelpSpecRefset getSpecRefsetHelper(final I_ConfigAceFrame config) throws Exception {
        if (this.specRefsetHelper == null || this.specRefsetHelper.getConfig() != config) {
            this.specRefsetHelper = new SpecRefsetHelper(config);
        }
        return this.specRefsetHelper;
    }

    @Override
    public I_HelpMarkedParentRefsets getMarkedParentRefsetHelper(final I_ConfigAceFrame config, final int memberRefsetId,
            final int memberTypeId) throws Exception {
        return new MarkedParentRefsetHelper(config, memberRefsetId, memberTypeId);
    }

    @Override
    public I_HelpMemberRefsetsCalculateConflicts getMemberRefsetConflictCalculator(final I_ConfigAceFrame config)
    throws Exception {
        return new MemberRefsetConflictCalculator(config);
    }

    @Override
    public I_HelpMemberRefsets getMemberRefsetHelper(final I_ConfigAceFrame config, final int memberRefsetId, final int memberTypeId)
    throws Exception {
        return new MemberRefsetHelper(config, memberRefsetId, memberTypeId);
    }

    @Override
    public I_HelpRefsets getRefsetHelper(final I_ConfigAceFrame config) {
        return new RefsetHelper(config);
    }

    @Override
    public boolean pathExists(final int pathConceptId) throws TerminologyException, IOException {
        return this.pathManager.exists(pathConceptId);
    }

    @Override
    public boolean pathExistsFast(final int pathConceptId) throws TerminologyException, IOException {
        return pathManager.existsFast(pathConceptId);
    }

    @Override
    public Object getComponent(final int nid) throws TerminologyException, IOException {
        return Bdb.getComponent(nid);
    }

    @SuppressWarnings("unchecked")
	@Override
    public Condition computeRefset(final int refsetNid, final RefsetSpecQuery query, final I_ConfigAceFrame frameConfig) throws Exception {
        AceLog.getAppLog().info(">>>>>>>>>> Computing RefsetSpecQuery: " + query);
        final List<String> dangleWarnings = RefsetQueryFactory.removeDangles(query);
        for (final String warning : dangleWarnings) {
            AceLog.getAppLog().info(warning + "\nClause removed from computation: ");
        }

        final Concept refsetConcept = Concept.get(refsetNid);
        final RefsetSpec specHelper = new RefsetSpec(refsetConcept, true, frameConfig);

        final Collection<RefsetMember<?, ?>> members = refsetConcept.getData().getRefsetMembers();

        final HashSet<Integer> currentMembersList = new HashSet<Integer>();
        NidSetBI allowedStatus = frameConfig.getAllowedStatus();
        int cidTypeNid = REFSET_TYPES.CID.getTypeNid();
        int normalMemberNid = ReferenceConcepts.NORMAL_MEMBER.getNid();
        for (final RefsetMember<?, ?> m : members) {
            for (RefsetMember.Version v : m.getVersions(frameConfig.getCoordinate())) {
                if (allowedStatus.contains(v.getStatusNid()) && v.getTypeNid() == cidTypeNid) {
                    if (((I_ExtendByRefPartCid) v).getC1id() == normalMemberNid) {
                        if (Terms.get().hasConcept(m.getComponentId())) {
                            currentMembersList.add(m.getComponentId());
                        } else { // assume it is a description member
                            final I_DescriptionVersioned desc = Terms.get().getDescription(m.getComponentId());
                            if (desc != null) {
                                currentMembersList.add(desc.getConceptNid());
                            }
                        }
                    }
                }
            }
        }

        final HashSet<I_ShowActivity> activities = new HashSet<I_ShowActivity>();
        RefsetComputer computer;
        try {
            I_RepresentIdSet possibleIds;
            if (specHelper.isConceptComputeType()) {
                AceLog.getAppLog().info(">>>>>>>>>> Computing possible concepts for concept spec: " + query);
                possibleIds = query.getPossibleConceptsInterruptable(null, activities);

            } else if (specHelper.isDescriptionComputeType()) {
                AceLog.getAppLog().info(">>>>>>>>>> Computing possible concepts for description spec: " + query);
                possibleIds = query.getPossibleDescriptionsInterruptable(null, activities);
            } else {
                throw new Exception("Relationship compute type not supported.");
            }

            // add the current members to the list of possible concepts to check (in case some need to be retired)
            possibleIds.or(this.getIdSetFromIntCollection(currentMembersList));
            AceLog.getAppLog().info(">>>>>>>>>> Search space (concept count): " + possibleIds.cardinality());

            computer = new RefsetComputer(refsetNid, query, frameConfig, possibleIds, activities);
            if (possibleIds.cardinality() > 500) {
                AceLog.getAppLog().info(">>>>>>>>> Iterating concepts in parallel.");
                Bdb.getConceptDb().iterateConceptDataInParallel(computer);
            } else {
                AceLog.getAppLog().info(">>>>>>>>> Iterating concepts in sequence.");
                final NidBitSetItrBI possibleItr = possibleIds.iterator();
                final ConceptFetcher fetcher = new ConceptFetcher();
                while (possibleItr.next()) {
                    fetcher.setConcept(Concept.get(possibleItr.nid()));
                    computer.processUnfetchedConceptData(possibleItr.nid(), fetcher);
                }
            }

            if (!computer.continueWork()) {
                throw new ComputationCanceled("Computation cancelled");
            }

            AceLog.getAppLog().info(">>>>>>>>> Finished computing spec - adding uncommitted.");
            specHelper.setLastComputeTime(System.currentTimeMillis());

            computer.addUncommitted();
            if (frameConfig.getDbConfig().getRefsetChangesChangeSetPolicy() == null) {
                frameConfig.getDbConfig().setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
                frameConfig.getDbConfig().setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
            }

            if (!computer.continueWork()) {
                throw new ComputationCanceled("Computation cancelled");
            }

            AceLog.getAppLog().info(">>>>>>>>> Finished computing spec - committing.");
            BdbCommitManager.commit(frameConfig.getDbConfig().getRefsetChangesChangeSetPolicy(), frameConfig
                .getDbConfig().getChangeSetWriterThreading());
            if (!computer.continueWork()) {
                for (final I_ShowActivity a : activities) {
                    a.cancel();
                    a.setProgressInfoLower("Cancelled.");
                }
                return Condition.ITEM_CANCELED;
            } else {
                return Condition.ITEM_COMPLETE;
            }
        } catch (final ComputationCanceled e) {
            for (final I_ShowActivity a : activities) {
                a.cancel();
                a.setProgressInfoLower("Cancelled.");
            }
        } catch (final InterruptedException e) {
            for (final I_ShowActivity a : activities) {
                a.cancel();
                a.setProgressInfoLower("Cancelled.");
            }
        } catch (final ExecutionException e) {
            for (final I_ShowActivity a : activities) {
                a.cancel();
                a.setProgressInfoLower("Cancelled.");
            }
            if (getRootCause(e) instanceof TerminologyException) {
                throw new TerminologyException(e.getMessage());
            } else if (getRootCause(e) instanceof IOException) {
                throw new IOException(e.getMessage());
            } else if (getRootCause(e) instanceof ComputationCanceled) {
                // Nothing to do
            } else if (getRootCause(e) instanceof InterruptedException) {
                // Nothing to do
            } else {

                e.printStackTrace();
                throw new TerminologyException(e);
            }
        }
        // Clean up any sub-activities...

        return Condition.ITEM_CANCELED;
    }

    private Throwable getRootCause(final Exception e) {
        Throwable prevCause = e;
        Throwable rootCause = e.getCause();
        while (rootCause != null) {
            prevCause = rootCause;
            rootCause = rootCause.getCause();
        }

        return prevCause;
    }

    private static class ConceptFetcher implements I_FetchConceptFromCursor {
        Concept concept;

        public void setConcept(final Concept concept) {
            this.concept = concept;
        }

        @Override
        public Concept fetch() {
            return this.concept;
        }

    }

    @Override
    public I_ImageVersioned newImage(final UUID imageUuid, final int conceptNid, final int typeNid, final byte[] image, final String textDescription,
            final String format, final I_ConfigAceFrame aceConfig) throws IOException, TerminologyException {

        final Concept c = Concept.get(conceptNid);
        final Image img = new Image();
        img.nid = this.uuidToNative(imageUuid);
        img.primordialUNid = Bdb.getUuidsToNidMap().getUNid(imageUuid);
        img.enclosingConceptNid = c.getNid();
        Bdb.getNidCNidMap().setCNidForNid(conceptNid, img.nid);
        img.setImage(image);
        img.setFormat(format);
        img.setTextDescription(textDescription);
        img.setTypeNid(typeNid);
        img.primordialSapNid = Integer.MIN_VALUE;
        final int statusNid = aceConfig.getDefaultStatus().getNid();
        for (final PathBI p : aceConfig.getEditingPathSet()) {
            if (img.primordialSapNid == Integer.MIN_VALUE) {
                img.primordialSapNid =
                        Bdb.getSapDb().getSapNid(statusNid, aceConfig.getDbConfig().getUserConcept().getNid(),
                            p.getConceptNid(), Long.MAX_VALUE);
            } else {
                if (img.revisions == null) {
                    img.revisions = new CopyOnWriteArrayList<ImageRevision>();
                }
                img.revisions.add(img.makeAnalog(statusNid, p.getConceptNid(), Long.MAX_VALUE));
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
    public void setCheckCreationDataEnabled(final boolean enabled) {
        BdbCommitManager.setCheckCreationDataEnabled(enabled);
    }

    @Override
    public void setCheckCommitDataEnabled(final boolean enabled) {
        BdbCommitManager.setCheckCommitDataEnabled(enabled);
    }

    public void resetViewPositions() {
        Bdb.getSapDb().clearMapperCache();
    }

    public void setCacheSize(final String cacheSize) {
        Bdb.setCacheSize(cacheSize);
    }

    public long getCacheSize() {
        return Bdb.getCacheSize();
    }

    public void setCachePercent(final String cachePercent) {
        Bdb.setCachePercent(cachePercent);
    }

    public int getCachePercent() {
        return Bdb.getCachePercent();
    }

    @Override
    public void removeOrigin(final PathBI path, final I_Position origin, final I_ConfigAceFrame config) throws TerminologyException {
        pathManager.removeOrigin(path, origin, config);
    }

    @Override
    public I_GetConceptData getConceptForNid(final int componentNid) throws IOException {
        return Bdb.getConceptForComponent(componentNid);
    }

    private int authorNid = Integer.MAX_VALUE;

    @Override
    public int getAuthorNid() {
        if (authorNid == Integer.MAX_VALUE) {
            try {
                if (getActiveAceFrameConfig() != null && getActiveAceFrameConfig().getDbConfig() != null
                    && getActiveAceFrameConfig().getDbConfig().getUserConcept() != null) {
                    authorNid = getActiveAceFrameConfig().getDbConfig().getUserConcept().getConceptNid();
                } else {
                    authorNid = uuidToNative(TkRevision.unspecifiedUserUuid);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return authorNid;
    }

    public ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName, File changeSetTempFileName,
            ChangeSetGenerationPolicy policy) {
        return new EConceptChangeSetWriter(changeSetFileName, changeSetTempFileName, policy, true);
    }

}
