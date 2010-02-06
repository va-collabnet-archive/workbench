package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.apache.lucene.search.Hits;
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
import org.dwfa.ace.api.I_Identify;
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
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_TrackContinuation;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.Terms;
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
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.I_Search;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.PathManager;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.impl.CheckAndProcessRegexMatch;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.I_ProcessConceptData;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesRevision;
import org.ihtsdo.db.bdb.concept.component.description.Description;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionRevision;
import org.ihtsdo.db.bdb.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipRevision;
import org.ihtsdo.db.runner.WorkbenchRunner;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseException;

public class BdbTermFactory implements I_TermFactory, I_ImplementTermFactory,
		I_Search {

	private BdbPathManager pathManager;

	private I_ShowActivity activityFrame;

	private File envHome;

	public static void canEdit(I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException {
		if (aceFrameConfig.getEditingPathSet().size() == 0) {
			throw new TerminologyException(
					"<br><br>You must select an editing path before editing...<br><br>No editing path selected.");
		}
	}

	private TupleBinding<Integer> intBinder = TupleBinding
			.getPrimitiveBinding(Integer.class);

	private boolean closed = false;

	public static boolean isHeadless() {
		return DwfaEnv.isHeadless();
	}

	public static void setHeadless(Boolean headless) {
		DwfaEnv.setHeadless(headless);
	}

	private class ShutdownThread extends Thread {

		public ShutdownThread() {
			super("BdbTermFactory Shutdown Thread");
		}

		public void run() {
			try {
				if (!closed) {
					Bdb.close();
					closed = true;
				}
			} catch (InterruptedException e) {
				AceLog.getEditLog().alertAndLogException(e);
			} catch (ExecutionException e) {
				AceLog.getEditLog().alertAndLogException(e);
			}
		}
	}

	public void close() throws IOException {
		try {
			Bdb.close();
			closed = true;
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void addUncommitted(I_GetConceptData concept) {
		BdbCommitManager.addUncommitted(concept);
	}

	@Override
	public void addUncommitted(I_ThinExtByRefVersioned extension) {
		BdbCommitManager.addUncommitted(extension);
	}

	@Override
	public void addUncommittedNoChecks(I_GetConceptData concept) {
		BdbCommitManager.addUncommittedNoChecks(concept);
	}

	@Override
	public void addUncommittedNoChecks(I_ThinExtByRefVersioned extension) {
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
	public void commitTransaction() throws IOException {
		throw new UnsupportedOperationException();
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
	public Hits doLuceneSearch(String query) throws IOException,
			org.apache.lucene.queryParser.ParseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void forget(I_GetConceptData concept) {
		BdbCommitManager.forget(concept);
	}

	@Override
	public void forget(I_DescriptionVersioned desc) {
		BdbCommitManager.forget(desc);
	}

	@Override
	public void forget(I_RelVersioned rel) {
		BdbCommitManager.forget(rel);
	}

	I_ConfigAceFrame activeAceFrameConfig;

	@Override
	public I_ConfigAceFrame getActiveAceFrameConfig()
			throws TerminologyException, IOException {
		return activeAceFrameConfig;
	}

	@Override
	public void setActiveAceFrameConfig(I_ConfigAceFrame activeAceFrameConfig)
			throws TerminologyException, IOException {
		this.activeAceFrameConfig = activeAceFrameConfig;
	}

	@Override
	public List<? extends I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int nid) throws IOException {
		Concept c = Bdb.getConceptDb().getConcept(
				Bdb.getNidCNidMap().getCNid(nid));
		return c.getExtensionsForComponent(nid);
	}

	@Override
	@Deprecated
	public List<? extends I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int nid, boolean addUncommitted) throws IOException {
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
		throw new UnsupportedOperationException();
	}

	@Override
	public I_GetConceptData getConcept(Collection<UUID> ids)
			throws TerminologyException, IOException {
		return Bdb.getConceptDb().getConcept(Bdb.uuidsToNid(ids));
	}

	@Override
	public I_GetConceptData getConcept(UUID... ids)
			throws TerminologyException, IOException {
		return Bdb.getConceptDb().getConcept(Bdb.uuidToNid(ids));
	}

	@Override
	public I_GetConceptData getConcept(int nid) throws TerminologyException,
			IOException {
		assert nid != Integer.MAX_VALUE;
		int cNid = Bdb.getConceptNid(nid);
		assert cNid != Integer.MAX_VALUE : "nid: " + nid + " cNid: " + cNid
				+ " concept: " + Bdb.getConceptDb().getConcept(nid) + " uuid:"
				+ Bdb.getUuidsToNidMap().getUuidsForNid(nid);
		return Bdb.getConceptDb().getConcept(cNid);
	}

	@Override
	public I_GetConceptData getConcept(String conceptId, int sourceId)
			throws TerminologyException,
			org.apache.lucene.queryParser.ParseException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<I_GetConceptData> getConcept(String conceptId)
			throws TerminologyException,
			org.apache.lucene.queryParser.ParseException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getConceptCount() throws IOException {
		return (int) Bdb.getConceptDb().getCount();
	}

	@Override
	public I_RepresentIdSet getConceptIdSet() throws IOException {
		throw new UnsupportedOperationException();
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
	public I_DescriptionVersioned getDescription(int dnid, int cnid)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_DescriptionVersioned getDescription(String descriptionId)
			throws TerminologyException,
			org.apache.lucene.queryParser.ParseException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_RepresentIdSet getDescriptionIdSet() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<I_DescriptionVersioned> getDescriptionIterator()
			throws IOException {
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
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefVersioned getExtension(int memberId)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_GetExtensionData getExtensionWrapper(int memberId)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<I_GetExtensionData> getExtensionsForComponent(int componentId)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_Identify getId(int nid) throws TerminologyException, IOException {
		return Bdb.getConceptForComponent(nid).getComponent(nid);
	}

	@Override
	public I_Identify getId(UUID uuid) throws TerminologyException, IOException {
		return getId(Bdb.uuidToNid(uuid));
	}

	@Override
	public I_Identify getId(Collection<UUID> uids) throws TerminologyException,
			IOException {
		return getId(Bdb.uuidsToNid(uids));
	}

	@Override
	public I_RepresentIdSet getIdSetFromIntCollection(Collection<Integer> ids)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_RepresentIdSet getIdSetfromTermCollection(
			Collection<? extends I_AmTermComponent> components)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_Path getPath(Collection<UUID> uids) throws TerminologyException,
			IOException {
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
	public I_Identify getPreviousAuthorityId() throws TerminologyException,
			IOException {
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
	public List<I_ThinExtByRefVersioned> getRefsetExtensionMembers(int refsetId)
			throws IOException {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<UUID> getUids(int nid) throws TerminologyException,
			IOException {
		return getId(nid).getUUIDs();
	}

	@Override
	public Set<? extends I_Transact> getUncommitted() {
		return BdbCommitManager.getUncommitted();
	}

	@Override
	public boolean hasConcept(int conceptId) throws IOException {
		if (Bdb.getNidCNidMap().getCNid(conceptId) == conceptId) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasDescription(int descId, int conceptId) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasExtension(int memberId) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasId(Collection<UUID> uuids) throws IOException {
		return Bdb.uuidsToNid(uuids) != Integer.MAX_VALUE;
	}

	@Override
	public boolean hasId(UUID uuid) throws IOException {
		return Bdb.uuidToNid(uuid) != Integer.MAX_VALUE;
	}

	@Override
	public boolean hasImage(int imageId) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPath(int nid) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasRel(int relId, int conceptId) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void iterateConceptAttributes(I_ProcessConceptAttributes processor)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void iterateConcepts(I_ProcessConcepts procesor) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void iterateDescriptions(I_ProcessDescriptions processor)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void iterateExtByRefs(I_ProcessExtByRef processor) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void iterateIds(I_ProcessIds processor) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void iterateImages(I_ProcessImages processor) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void iteratePaths(I_ProcessPaths processor) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void iterateRelationships(I_ProcessRelationships processor)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadFromDirectory(File dataDir, String encoding)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadFromMultipleJars(String[] args) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadFromSingleJar(String jarFile, String dataPrefix)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public TransferHandler makeTerminologyTransferHandler(
			JComponent thisComponent) {
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
                AceFrame newFrame = new AceFrame(WorkbenchRunner.args, 
                		WorkbenchRunner.lc, frameConfig, false);
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
	public I_ConfigAceFrame newAceFrameConfig() throws TerminologyException,
			IOException {
		return new AceFrameConfig();
	}

	@Override
	public I_ShowActivity newActivityPanel(boolean displayInViewer,
			I_ConfigAceFrame aceFrameConfig) {
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
	public I_ReadChangeSet newBinaryChangeSetReader(File changeSetFile)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_WriteChangeSet newBinaryChangeSetWriter(File changeSetFile)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartBoolean newBooleanExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_GetConceptData newConcept(UUID newConceptUuid, boolean defined,
			I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
			IOException {
        canEdit(aceFrameConfig);
        int cNid = Bdb.uuidToNid(newConceptUuid);
        Concept newC = Concept.get(cNid);
        ConceptAttributes a = new ConceptAttributes();
        a.enclosingConcept = newC;
        newC.setConceptAttributes(a);
        a.setDefined(defined);
        a.primordialUNid = Bdb.getUuidsToNidMap().getUNid(newConceptUuid);
        a.primordialSapNid = Integer.MIN_VALUE;
		int statusNid = aceFrameConfig.getDefaultStatus().getNid();
		for (I_Path p: aceFrameConfig.getEditingPathSet()) {
			if (a.primordialSapNid == Integer.MIN_VALUE) {
				a.primordialSapNid = 
					Bdb.getSapDb().getSapNid(statusNid, 
							p.getConceptId(), Long.MAX_VALUE);
			} else {
				if (a.revisions == null) {
					a.revisions = new ArrayList<ConceptAttributesRevision>(
							aceFrameConfig.getEditingPathSet().size() - 1);
				}
				a.revisions.add((ConceptAttributesRevision) a.makeAnalog(statusNid, 
						p.getConceptId(), Long.MAX_VALUE));
			}
		}
        return newC;
	}

	@Override
	public I_ConceptAttributePart newConceptAttributePart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartConceptConceptConcept newConceptConceptConceptExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartConceptConcept newConceptConceptExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartConceptConceptString newConceptConceptStringExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartConcept newConceptExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartConceptInt newConceptIntExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartConceptString newConceptStringExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Description newDescription(UUID newDescriptionId,
			I_GetConceptData concept, String lang, String text,
			I_ConceptualizeLocally descType, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		return newDescription(newDescriptionId, concept, lang, text, Terms
				.get().getConcept(descType.getNid()), aceFrameConfig);
	}

	@Override
	public Description newDescription(UUID newDescriptionId,
			I_GetConceptData concept, String lang, String text,
			I_GetConceptData descType, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		canEdit(aceFrameConfig);
		Concept c = (Concept) concept;
		c.makeWritable();
		Description d = new Description();
		Bdb.gVersion.incrementAndGet();
		d.enclosingConcept = c;
		d.nid = Bdb.uuidToNid(newDescriptionId);
		d.primordialUNid = Bdb.getUuidsToNidMap().getUNid(newDescriptionId);
		d.setLang(lang);
		d.setText(text);
		d.setInitialCaseSignificant(false);
		d.setTypeId(descType.getNid());
		d.primordialSapNid = Integer.MIN_VALUE;
		int statusNid = aceFrameConfig.getDefaultStatus().getNid();
		for (I_Path p: aceFrameConfig.getEditingPathSet()) {
			if (d.primordialSapNid == Integer.MIN_VALUE) {
				d.primordialSapNid = 
					Bdb.getSapDb().getSapNid(statusNid, 
							p.getConceptId(), Long.MAX_VALUE);
			} else {
				if (d.revisions == null) {
					d.revisions = new ArrayList<DescriptionRevision>(
							aceFrameConfig.getEditingPathSet().size() - 1);
				}
				d.revisions.add(d.makeAnalog(statusNid, 
						p.getConceptId(), Long.MAX_VALUE));
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
	public I_ThinExtByRefVersioned newExtension(int refsetId, int memberId,
			int componentId, int typeId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefVersioned newExtension(int refsetId, int memberId,
			int componentId, Class<? extends I_ThinExtByRefPart> partType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefVersioned newExtensionNoChecks(int refsetId,
			int memberId, int componentId, int typeId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends I_ThinExtByRefPart> T newExtensionPart(Class<T> t) {
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
	public I_ThinExtByRefPartInteger newIntegerExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartLanguage newLanguageExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartLanguageScoped newLanguageScopedExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ThinExtByRefPartMeasurement newMeasurementExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_Path newPath(Set<I_Position> origins, I_GetConceptData pathConcept)
			throws TerminologyException, IOException {
		ArrayList<I_Position> originList = new ArrayList<I_Position>();
		if (origins != null) {
			originList.addAll(origins);
		}
		Path newPath = new Path(pathConcept.getConceptId(), originList);
		AceLog.getEditLog().fine("writing new path: \n" + newPath);
		new PathManager().write(newPath);
		return newPath;
	}

	@Override
	public I_Position newPosition(I_Path path, int version)
			throws TerminologyException, IOException {
		return new Position(version, path);
	}

	@Override
	public I_RelPart newRelPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		return newRelationship(newRelUid, concept, 
				aceFrameConfig.getDefaultRelationshipType(), 
				Terms.get().getConcept(aceFrameConfig.getRoots().getSetValues()[0]), 
				aceFrameConfig.getDefaultRelationshipCharacteristic(), 
				aceFrameConfig.getDefaultRelationshipRefinability(), 
				aceFrameConfig.getDefaultStatus(), 
				0, 
				aceFrameConfig);
	}

	@Override
	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, 
			I_GetConceptData relType,
			I_GetConceptData relDestination,
			I_GetConceptData relCharacteristic,
			I_GetConceptData relRefinability, I_GetConceptData relStatus,
			int relGroup, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		canEdit(aceFrameConfig);
        if (concept == null) {
            AceLog.getAppLog().alertAndLogException(
                    new Exception("Cannot add a relationship while the component viewer is empty..."));
            return null;
        }
		Concept c = (Concept) concept;
		c.makeWritable();
		Relationship r = new Relationship();
		Bdb.gVersion.incrementAndGet();
		r.enclosingConcept = c;
		r.nid = Bdb.uuidToNid(newRelUid);
		r.primordialUNid = Bdb.getUuidsToNidMap().getUNid(newRelUid);
		int parentId = Integer.MIN_VALUE;
        if (aceFrameConfig.getHierarchySelection() != null) {
            parentId = aceFrameConfig.getHierarchySelection().getConceptId();
        } else {
            parentId = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.localize().getNid();
        }
        r.setC2Id(parentId);
        r.setTypeId(relType.getNid());
        r.setRefinabilityId(relRefinability.getNid());
        r.setCharacteristicId(relCharacteristic.getNid());
		r.primordialSapNid = Integer.MIN_VALUE;
		r.setGroup(0);
		int statusNid = relStatus.getNid();
		for (I_Path p: aceFrameConfig.getEditingPathSet()) {
			if (r.primordialSapNid == Integer.MIN_VALUE) {
				r.primordialSapNid = 
					Bdb.getSapDb().getSapNid(statusNid, 
							p.getConceptId(), Long.MAX_VALUE);
			} else {
				if (r.revisions == null) {
					r.revisions = new ArrayList<RelationshipRevision>(
							aceFrameConfig.getEditingPathSet().size() - 1);
				}
				r.revisions.add((RelationshipRevision) r.makeAnalog(statusNid, 
						p.getConceptId(), Long.MAX_VALUE));
			}
		}
		c.getSourceRels().add(r);
		Terms.get().addUncommitted(c);
		return r;
	}

	@Override
	public I_ThinExtByRefPartString newStringExtensionPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeChangeSetReader(I_ReadChangeSet reader) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeChangeSetWriter(I_WriteChangeSet writer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeFromCacheAndRollbackTransaction(int memberId)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resumeChangeSetWriters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProperty(String key, String value) throws IOException {
		Bdb.setProperty(key, value);
	}

	@Override
	public void startTransaction() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void suspendChangeSetWriters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int uuidToNative(UUID uid) throws TerminologyException, IOException {
		return Bdb.uuidToNid(uid);
	}

	@Override
	public int uuidToNative(Collection<UUID> uids) throws TerminologyException,
			IOException {
		return Bdb.uuidsToNid(uids);
	}

	@Override
	@Deprecated
	public int uuidToNativeWithGeneration(Collection<UUID> uids, int source,
			I_Path idPath, int version) throws TerminologyException,
			IOException {
		return Bdb.uuidsToNid(uids);
	}

	@Override
	@Deprecated
	public int uuidToNativeWithGeneration(UUID uid, int source,
			Collection<I_Path> idPaths, int version)
			throws TerminologyException, IOException {
		return Bdb.uuidToNid(uid);
	}

	@Override
	@Deprecated
	public int uuidToNativeWithGeneration(UUID uid, int source, I_Path idPath,
			int version) throws TerminologyException, IOException {
		return Bdb.uuidToNid(uid);
	}

	@Override
	@Deprecated
	public void writeId(I_Identify versioned) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writePath(I_Path p) throws IOException {
		try {
			pathManager.write(p);
		} catch (TerminologyException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void writePathOrigin(I_Path path, I_Position origin)
			throws TerminologyException {
		pathManager.writeOrigin(path, origin);
	}

	public void setPathManager(BdbPathManager pathManager) {
		this.pathManager = pathManager;
	}

	@Override
	public List<UUID> nativeToUuid(int nid) throws IOException {
		return Bdb.getConceptForComponent(nid).getUidsForComponent(nid);
	}

	@Override
	public I_ImageVersioned getImage(UUID uuid) throws IOException {
		return getImage(Bdb.uuidToNid(uuid));
	}

	@Override
	public I_ImageVersioned getImage(int nid) throws IOException {
		return (I_ImageVersioned) Bdb.getConceptForComponent(nid).getComponent(
				nid);
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
	public void setup(Object envHome, boolean readOnly, Long cacheSize)
			throws IOException {
		// nothing to do...
	}

	@Override
	public void setup(Object envHome, boolean readOnly, Long cacheSize,
			DatabaseSetupConfig databaseSetupConfig) throws IOException {
		// nothing to do...
	}

	@Override
	public void searchConcepts(I_TrackContinuation tracker,
			I_RepresentIdSet matches, CountDownLatch latch,
			List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
			throws DatabaseException, IOException,
			org.apache.lucene.queryParser.ParseException {
		throw new UnsupportedOperationException();

	}

	@Override
	public CountDownLatch searchLucene(I_TrackContinuation tracker,
			String query, Collection<LuceneMatch> matches,
			CountDownLatch latch, List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config, LuceneProgressUpdator updater)
			throws DatabaseException, IOException,
			org.apache.lucene.queryParser.ParseException {
		throw new UnsupportedOperationException();
	}

	private static class RegexSearcher implements I_ProcessConceptData {

		private CountDownLatch conceptLatch;
		private I_TrackContinuation tracker;
		private Semaphore checkSemaphore;
		private List<I_TestSearchResults> checkList;
		private I_ConfigAceFrame config;
		private Pattern p;
		Collection<I_DescriptionVersioned> matches;

		public RegexSearcher(CountDownLatch conceptLatch,
				I_TrackContinuation tracker, Semaphore checkSemaphore,
				List<I_TestSearchResults> checkList, I_ConfigAceFrame config,
				Pattern p, Collection<I_DescriptionVersioned> matches) {
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
				List<? extends I_DescriptionVersioned> descriptions = concept
						.getDescriptions();
				CountDownLatch descriptionLatch = new CountDownLatch(
						descriptions.size());
				for (I_DescriptionVersioned descV : descriptions) {
					try {
						checkSemaphore.acquire();
					} catch (InterruptedException e) {
						AceLog.getAppLog().log(Level.WARNING,
								e.getLocalizedMessage(), e);
					}
					ACE.threadPool.execute(new CheckAndProcessRegexMatch(
							descriptionLatch, checkSemaphore, p, matches,
							descV, checkList, config));
				}
				try {
					descriptionLatch.await();
				} catch (InterruptedException e) {
					AceLog.getAppLog().log(Level.WARNING,
							e.getLocalizedMessage(), e);
				}
				conceptLatch.countDown();
			} else {
				while (conceptLatch.getCount() > 0) {
					conceptLatch.countDown();
				}
			}
		}
	}

	@Override
	public void searchRegex(I_TrackContinuation tracker, Pattern p,
			Collection<I_DescriptionVersioned> matches,
			CountDownLatch conceptLatch, List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config) throws DatabaseException, IOException {
		Stopwatch timer = null;
		if (AceLog.getAppLog().isLoggable(Level.INFO)) {
			timer = new Stopwatch();
			timer.start();
		}

		Semaphore checkSemaphore = new Semaphore(15);
		RegexSearcher searcher = new RegexSearcher(conceptLatch, tracker,
				checkSemaphore, checkList, config, p, matches);
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
				AceLog.getAppLog().info(
						"Regex Search time: " + timer.getElapsedTime());
			} else {
				AceLog.getAppLog().info(
						"Canceled. Elapsed time: " + timer.getElapsedTime());
			}
			timer.stop();
		}
	}
}
