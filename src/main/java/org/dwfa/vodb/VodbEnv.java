package org.dwfa.vodb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
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
import org.dwfa.ace.api.I_SupportClassifier;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.cs.BinaryChangeSetReader;
import org.dwfa.ace.cs.BinaryChangeSetWriter;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.impl.BdbEnv;
import org.dwfa.vodb.jar.PathCollector;
import org.dwfa.vodb.jar.TimePathCollector;
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
import org.dwfa.vodb.types.ThinExtByRefPartConceptInt;
import org.dwfa.vodb.types.ThinExtByRefPartInteger;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.dwfa.vodb.types.ThinExtByRefPartLanguageScoped;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefVersioned;
import org.dwfa.vodb.types.ThinIdVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.JEVersion;
import com.sleepycat.je.Transaction;

/**
 * @author kec
 * 
 */
public class VodbEnv implements I_ImplementTermFactory, I_SupportClassifier {
	private static Logger logger = Logger.getLogger(VodbEnv.class.getName());

	private static boolean readOnly;

	private File luceneDir;

	public VodbEnv() {
		LocalVersionedTerminology.set(this);
	}

	public VodbEnv(boolean stealth) {

	}

	private static boolean transactional = false;

	private static boolean deferredWrite = true;

	private static boolean txnNoSync = false;

	private static long transactionTimeout = 30000; // 30 seconds

	private ActivityPanel activityFrame;

	private File envHome;

	private BdbEnv bdbEnv;

	private static Long cacheSize;

	public void setup(Object envHome, boolean readOnly, Long cacheSize)
			throws ToIoException {
		try {
			setup((File) envHome, readOnly, cacheSize);
		} catch (Exception e) {
			throw new ToIoException(e);
		}
	}

	public void setup(Object envHome, boolean readOnly, Long cacheSize,
			DatabaseSetupConfig dbSetupConfig) throws IOException {
		try {
			setup((File) envHome, readOnly, cacheSize, dbSetupConfig);
		} catch (Exception e) {
			throw new ToIoException(e);
		}
	}

	/**
	 * @throws Exception
	 */
	public void setup(File envHome, boolean readOnly, Long cacheSize)
			throws Exception {
		setup(envHome, readOnly, cacheSize, null);
	}

	public void setup(File envHome, boolean readOnly, Long cacheSize,
			DatabaseSetupConfig dbSetupConfig) throws IOException {
		try {
			if (envHome.exists() == false) {
				if (dbSetupConfig == null) {
					throw new IOException("dbSetupConfig cannot be null for new databases...");
				}
			}
			long startTime = System.currentTimeMillis();
			this.envHome = envHome;
			if (VodbEnv.cacheSize == null) {
				VodbEnv.cacheSize = cacheSize;
			} else {
				cacheSize = VodbEnv.cacheSize;
			}
			activityFrame = new ActivityPanel(true, true);

			AceLog.getAppLog().info("Setting up db: " + envHome);
			activityFrame.setIndeterminate(true);
			activityFrame.setProgressInfoUpper("Loading the terminology");
			activityFrame.setProgressInfoLower("Setting up the environment...");
			activityFrame.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out
							.println("System.exit from activity action listener: "
									+ e.getActionCommand());
					System.exit(0);
				}
			});
			try {
				ActivityViewer.addActivity(activityFrame);
			} catch (Exception e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}

			VodbEnv.readOnly = readOnly;
			if (this != LocalVersionedTerminology.getStealthfactory()) {
				LocalFixedTerminology.setStore(new VodbFixedServer(this));
			}
			envHome.mkdirs();
			luceneDir = new File(envHome, "lucene");

			AceLog.getAppLog().info(
					"Setup transactional: " + transactional + " txnNoSync: "
							+ txnNoSync + " deferredWrite: " + isDeferredWrite()
							+ " transactionTimeout: " + transactionTimeout);
			
			AceLog.getAppLog().info(
					"Berkeley DB info: "
							+ JEVersion.CURRENT_VERSION.getVersionString());
			
			bdbEnv = new BdbEnv(this, envHome, readOnly, cacheSize, luceneDir, dbSetupConfig);
			
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
			bdbEnv.sync();
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	public void close() throws IOException {
		try {
			bdbEnv.close();
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	public Logger getLogger() {
		return logger;
	}


	public I_ConceptAttributeVersioned getConceptAttributes(int conceptId)
			throws IOException {
		return bdbEnv.getConceptAttributes(conceptId);
	}

	public I_DescriptionVersioned getDescription(int descId, int concId) throws IOException {
		try {
			return bdbEnv.getDescription(descId, concId);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
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

	public boolean hasDescription(int descId, int conId) throws DatabaseException, IOException {
		return bdbEnv.hasDescription(descId, conId);
	}

	public boolean hasRel(int relId, int conceptId) throws DatabaseException, IOException {
		return bdbEnv.hasRel(relId, conceptId);
	}

	public I_RelVersioned getRel(int relId, int conceptId) throws DatabaseException, IOException {
		return bdbEnv.getRel(relId, conceptId);
	}

	public I_ThinExtByRefVersioned getExtension(int memberId)
			throws IOException {
		return bdbEnv.getExtension(memberId);
	}

	public boolean hasExtension(int memberId) throws DatabaseException {
		return bdbEnv.hasExtension(memberId);
	}

	public boolean hasConcept(int conceptId) throws DatabaseException {
		return bdbEnv.hasConcept(conceptId);
	}

	public List<I_DescriptionVersioned> getDescriptions(int conceptId)
			throws DatabaseException, IOException {
		return bdbEnv.getDescriptions(conceptId);
	}

	public boolean hasDestRelTuple(int conceptId, I_IntSet allowedStatus,
			I_IntSet destRelTypes, Set<I_Position> positions)
			throws DatabaseException, IOException {
		return bdbEnv.hasDestRelTuple(conceptId, allowedStatus, destRelTypes, positions);
	}

	public List<I_RelVersioned> getDestRels(int conceptId)
			throws DatabaseException, IOException {
		return bdbEnv.getDestRels(conceptId);
	}

	public List<I_GetExtensionData> getExtensionsForComponent(int componentId)
			throws IOException {
		return bdbEnv.getExtensionsForComponent(componentId);
	}

	public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int componentId, boolean addUncommitted) throws IOException {
		List<I_ThinExtByRefVersioned> extensions = getAllExtensionsForComponent(componentId);

		for (I_GetExtensionData wrapper : ExtensionByReferenceBean
				.getNewExtensions(componentId)) {
			extensions.add(wrapper.getExtension());
		}

		return extensions;
	}

	public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int componentId) throws IOException {
		return bdbEnv.getAllExtensionsForComponent(componentId);
	}

	public List<ExtensionByReferenceBean> getExtensionsForRefset(int refsetId)
			throws DatabaseException {
		return bdbEnv.getExtensionsForRefset(refsetId);
	}

	public boolean hasDestRels(int conceptId) throws DatabaseException {
		return bdbEnv.hasDestRels(conceptId);
	}

	public boolean hasDestRel(int conceptId, Set<Integer> destRelTypeIds)
			throws DatabaseException, IOException {
		return bdbEnv.hasDestRel(conceptId, destRelTypeIds);
	}

	public List<I_RelVersioned> getSrcRels(int conceptId)
			throws DatabaseException, IOException {
		return bdbEnv.getSrcRels(conceptId);
	}

	public boolean hasSrcRelTuple(int conceptId, I_IntSet allowedStatus,
			I_IntSet sourceRelTypes, Set<I_Position> positions)
			throws DatabaseException, IOException {
		return bdbEnv.hasSrcRelTuple(conceptId, allowedStatus, sourceRelTypes, positions);
	}

	public boolean hasSrcRels(int conceptId) throws DatabaseException, IOException {
		return bdbEnv.hasSrcRels(conceptId);
	}

	public boolean hasSrcRel(int conceptId, Set<Integer> srcRelTypeIds)
			throws DatabaseException, IOException {
		return bdbEnv.hasSrcRel(conceptId, srcRelTypeIds);
	}


	public int countDescriptions() throws DatabaseException, IOException {
		return bdbEnv.countDescriptions();
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
	public void searchRegex(I_TrackContinuation tracker, Pattern p,
			Collection<I_DescriptionVersioned> matches, CountDownLatch latch,
			List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
			throws DatabaseException, IOException {
		bdbEnv.searchRegex(tracker, p, matches, latch, checkList, config);
	}

	/*
	 * For issues upgrading to lucene 2.x, see this link:
	 * http://www.nabble.com/Lucene-in-Action-examples-complie-problem-tf2418478.html#a6743189
	 */

	public CountDownLatch searchLucene(I_TrackContinuation tracker,
			String query, Collection<LuceneMatch> matches,
			CountDownLatch latch, List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config, LuceneProgressUpdator updater)
			throws DatabaseException, IOException, ParseException {
		return bdbEnv.searchLucene(tracker, query, matches, latch, checkList, config, updater);
	}

	public void createLuceneDescriptionIndex() throws IOException {
		bdbEnv.createLuceneDescriptionIndex();
	}


	public void addPositions(Set<TimePathId> values)
			throws DatabaseException {
		bdbEnv.addTimeBranchValues(values);
	}

	public void populatePositions() throws Exception {
		Set<TimePathId> values = new HashSet<TimePathId>();
		DescChangesProcessor p = new DescChangesProcessor(values);
		iterateDescriptions(p);
		iterateConceptAttributes(p);
		iterateRelationships(p);
		iterateExtByRefs(p);
		addPositions(values);
	}

	private class DescChangesProcessor implements
			I_ProcessDescriptions, I_ProcessConceptAttributes,
			I_ProcessRelationships, I_ProcessExtByRef {
		Set<TimePathId> values;

		public DescChangesProcessor(Set<TimePathId> values) {
			super();
			this.values = values;
		}

		public void processDescription(I_DescriptionVersioned desc)
				throws Exception {
			for (I_DescriptionPart d : desc.getVersions()) {
				TimePathId tb = new TimePathId(d.getVersion(), d.getPathId());
				values.add(tb);
			}
		}

		public void processConceptAttributes(I_ConceptAttributeVersioned conc)
				throws Exception {
			for (I_ConceptAttributePart c : conc.getVersions()) {
				TimePathId tb = new TimePathId(c.getVersion(), c.getPathId());
				values.add(tb);
			}
		}

		public void processRelationship(I_RelVersioned rel) throws Exception {
			for (I_RelPart r : rel.getVersions()) {
				TimePathId tb = new TimePathId(r.getVersion(), r.getPathId());
				values.add(tb);
			}
		}

		public void processExtensionByReference(I_ThinExtByRefVersioned ext)
				throws Exception {
			for (I_ThinExtByRefPart extPart : ext.getVersions()) {
				TimePathId tb = new TimePathId(extPart.getVersion(), extPart.getPathId());
				values.add(tb);
			}
		}

	}

	public void iterateDescriptionEntries(I_ProcessDescriptionEntries processor)
			throws Exception {
		bdbEnv.iterateDescriptionEntries(processor);
	}

	public void iterateRelationshipsEntries(I_ProcessRelationshipEntries processor)
			throws Exception {
		bdbEnv.iterateRelationshipEntries(processor);
	}

	public void iterateExtByRefEntries(I_ProcessExtByRefEntries processor)
			throws Exception {
		bdbEnv.iterateExtByRefEntries(processor);
	}

	public List<I_ThinExtByRefVersioned> getRefsetExtensionMembers(int refsetId)
			throws IOException {
		return bdbEnv.getRefsetExtensionMembers(refsetId);
	}


	public Iterator<I_GetConceptData> getConceptIterator() throws IOException {
		return bdbEnv.getConceptIterator();
	}

	public void iterateConceptAttributeEntries(
			I_ProcessConceptAttributeEntries processor) throws Exception {
		bdbEnv.iterateConceptAttributeEntries(processor);
	}


	public void iterateImages(I_ProcessImageEntries processor) throws Exception {
		bdbEnv.iterateImages(processor);
	}

	public void iteratePaths(I_ProcessPathEntries processor) throws Exception {
		bdbEnv.iteratePaths(processor);
	}

	public void iterateTimeBranch(I_ProcessTimeBranchEntries processor)
			throws Exception {
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


	public void writeImage(I_ImageVersioned image) throws DatabaseException {
		bdbEnv.writeImage(image);
	}

	public void writeConceptAttributes(I_ConceptAttributeVersioned concept)
			throws DatabaseException, IOException {
		bdbEnv.writeConceptAttributes(concept);
	}

	public void writeRel(I_RelVersioned rel) throws IOException {
		try {
			bdbEnv.writeRel(rel);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	public void writeExt(I_ThinExtByRefVersioned ext) throws DatabaseException,
			IOException {
		bdbEnv.writeExt(ext);
	}

	public void writeDescription(I_DescriptionVersioned desc)
			throws DatabaseException, IOException {
		bdbEnv.writeDescription(desc);
	}

	public void writePath(I_Path p) throws DatabaseException {
		bdbEnv.writePath(p);
	}

	public I_Path getPath(int nativeId) throws DatabaseException {
		return bdbEnv.getPath(nativeId);
	}

	public boolean hasPath(int nativeId) throws DatabaseException {
		return bdbEnv.hasPath(nativeId);
	}

	public I_ImageVersioned getImage(UUID uid) throws TerminologyException,
			IOException, DatabaseException {
		return getImage(uuidToNative(uid));
	}

	public boolean hasImage(int imageId) throws DatabaseException {
		return bdbEnv.hasImage(imageId);
	}

	public I_ImageVersioned getImage(int nativeId) throws DatabaseException {
		return bdbEnv.getImage(nativeId);
	}

	public List<I_ImageVersioned> getImages(int conceptId)
			throws DatabaseException {
		return bdbEnv.getImages(conceptId);
	}


	public List<TimePathId> getTimePathList() throws Exception {
		TimePathCollector tpCollector = new TimePathCollector();
		iterateTimeBranch(tpCollector);
		return tpCollector.getTimePathIdList();
	}

	public List<I_Path> getPaths() throws Exception {
		PathCollector collector = new PathCollector();
		iteratePaths(collector);
		return collector.getPaths();
	}


	public void writeTimePath(TimePathId jarTimePath) throws DatabaseException {
		bdbEnv.writeTimePath(jarTimePath);
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

	public I_GetConceptData newConcept(UUID newConceptId, boolean defined,
			I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
			IOException {
		canEdit(aceFrameConfig);
		int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
				.getUids());
		int nid = uuidToNativeWithGeneration(newConceptId, idSource,
				aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
		AceLog.getEditLog().info(
				"Creating new concept: " + newConceptId + " (" + nid
						+ ") defined: " + defined);
		ConceptBean newBean = ConceptBean.get(nid);
		newBean.setPrimordial(true);
		int status = aceFrameConfig.getDefaultStatus().getConceptId();
		ThinConVersioned conceptAttributes = new ThinConVersioned(nid,
				aceFrameConfig.getEditingPathSet().size());
		for (I_Path p : aceFrameConfig.getEditingPathSet()) {
			ThinConPart attributePart = new ThinConPart();
			attributePart.setVersion(Integer.MAX_VALUE);
			attributePart.setDefined(defined);
			attributePart.setPathId(p.getConceptId());
			attributePart.setConceptStatus(status);
			conceptAttributes.addVersion(attributePart);
		}
		newBean.setUncommittedConceptAttributes(conceptAttributes);
		newBean.getUncommittedIds().add(nid);
		ACE.addUncommitted(newBean);
		return newBean;
	}

	public I_DescriptionVersioned newDescription(UUID newDescriptionId,
			I_GetConceptData concept, String lang, String text,
			I_ConceptualizeLocally descType, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		canEdit(aceFrameConfig);
		ACE.addUncommitted((I_Transact) concept);
		int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
				.getUids());
		int descId = uuidToNativeWithGeneration(newDescriptionId, idSource,
				aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine(
					"Creating new description: " + newDescriptionId + " ("
							+ descId + "): " + text);
		}
		ThinDescVersioned desc = new ThinDescVersioned(descId, concept
				.getConceptId(), aceFrameConfig.getEditingPathSet().size());
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
			descPart.setTypeId(descType.getNid());
			desc.addVersion(descPart);
		}
		concept.getUncommittedDescriptions().add(desc);
		concept.getUncommittedIds().add(descId);
		return desc;
	}

	private void canEdit(I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException {
		if (aceFrameConfig.getEditingPathSet().size() == 0) {
			throw new TerminologyException(
					"<br><br>You must select an editing path before editing...<br><br>No editing path selected.");
		}
	}

	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		canEdit(aceFrameConfig);
		if (aceFrameConfig.getHierarchySelection() == null) {
			throw new TerminologyException(
					"<br><br>To create a new relationship, you must<br>select the rel destination in the hierarchy view....");
		}
		int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
				.getUids());
		int relId = uuidToNativeWithGeneration(newRelUid, idSource,
				aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine(
					"Creating new relationship 1: " + newRelUid + " (" + relId
							+ ") from " + concept.getUids() + " to "
							+ aceFrameConfig.getHierarchySelection().getUids());
		}
		ThinRelVersioned rel = new ThinRelVersioned(relId, concept
				.getConceptId(), aceFrameConfig.getHierarchySelection()
				.getConceptId(), 1);
		int status = aceFrameConfig.getDefaultStatus().getConceptId();
		for (I_Path p : aceFrameConfig.getEditingPathSet()) {
			ThinRelPart relPart = new ThinRelPart();
			relPart.setVersion(Integer.MAX_VALUE);
			relPart.setPathId(p.getConceptId());
			relPart.setStatusId(status);
			relPart.setRelTypeId(aceFrameConfig.getDefaultRelationshipType()
					.getConceptId());
			relPart.setCharacteristicId(aceFrameConfig
					.getDefaultRelationshipCharacteristic().getConceptId());
			relPart.setRefinabilityId(aceFrameConfig
					.getDefaultRelationshipRefinability().getConceptId());
			relPart.setGroup(0);
			rel.addVersion(relPart);
		}
		concept.getUncommittedSourceRels().add(rel);
		concept.getUncommittedIds().add(relId);
		ACE.addUncommitted((I_Transact) concept);
		return rel;

	}

	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, I_GetConceptData relType,
			I_GetConceptData relDestination,
			I_GetConceptData relCharacteristic,
			I_GetConceptData relRefinability, I_GetConceptData relStatus,
			int relGroup, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		canEdit(aceFrameConfig);
		int idSource = uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
				.getUids());

		int relId = uuidToNativeWithGeneration(newRelUid, idSource,
				aceFrameConfig.getEditingPathSet(), Integer.MAX_VALUE);
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine(
					"Creating new relationship 2: " + newRelUid + " (" + relId
							+ ") from " + concept.getUids() + " to "
							+ relDestination.getUids());
		}
		ThinRelVersioned rel = new ThinRelVersioned(relId, concept
				.getConceptId(), relDestination.getConceptId(), aceFrameConfig
				.getEditingPathSet().size());

		ThinRelPart relPart = new ThinRelPart();

		rel.addVersion(relPart);

		int status = relStatus.getConceptId();

		for (I_Path p : aceFrameConfig.getEditingPathSet()) {
			relPart.setVersion(Integer.MAX_VALUE);
			relPart.setPathId(p.getConceptId());
			relPart.setStatusId(status);
			relPart.setRelTypeId(relType.getConceptId());
			relPart.setCharacteristicId(relCharacteristic.getConceptId());
			relPart.setRefinabilityId(relRefinability.getConceptId());
			relPart.setGroup(relGroup);
		}
		concept.getUncommittedSourceRels().add(rel);
		concept.getUncommittedIds().add(relId);
		ACE.addUncommitted((I_Transact) concept);
		return rel;

	}

	public I_GetConceptData getConcept(Collection<UUID> ids)
			throws TerminologyException, IOException {
		return ConceptBean.get(ids);
	}

	public I_GetConceptData getConcept(UUID[] ids) throws TerminologyException,
			IOException {
		return ConceptBean.get(Arrays.asList(ids));
	}

	public I_Position newPosition(I_Path path, int version) {
		return new Position(version, path);
	}

	public I_IntSet newIntSet() {
		return new IntSet();
	}

	public void addUncommitted(I_GetConceptData concept) {
		AceLog.getEditLog().fine(
				"Adding uncommitted " + concept + " from vodb: " + this);
		ACE.addUncommitted((I_Transact) concept);
	}

	public void addUncommitted(I_ThinExtByRefVersioned extension) {
		AceLog.getEditLog().fine(
				"Adding uncommitted extension " + extension + " from vodb: "
						+ this);
		ACE.addUncommitted(ExtensionByReferenceBean.make(extension
				.getMemberId(), extension));
	}

	public void loadFromSingleJar(String jarFile, String dataPrefix)
			throws Exception {
		LoadBdb.loadFromSingleJar(jarFile, dataPrefix);
	}

	public void loadFromDirectory(File dataDir) throws Exception {
		LoadBdb.loadFromDirectory(dataDir);
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 * @deprecated use loadFromSingleJar
	 */
	public void loadFromMultipleJars(String[] args) throws Exception {
		LoadBdb.main(args);
	}

	private class ProcessorWrapper implements
			I_ProcessDescriptionEntries, I_ProcessConceptAttributeEntries,
			I_ProcessRelationshipEntries, I_ProcessIdEntries,
			I_ProcessImageEntries, I_ProcessPathEntries,
			I_ProcessExtByRefEntries {

		I_ProcessConceptAttributes conceptAttributeProcessor;

		I_ProcessDescriptions descProcessor;

		I_ProcessRelationships relProcessor;

		I_ProcessIds idProcessor;

		I_ProcessImages imageProcessor;

		I_ProcessPaths pathProcessor;

		I_ProcessExtByRef extProcessor;

		public ProcessorWrapper() {
			super();
		}

		public void processDesc(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_DescriptionVersioned desc = bdbEnv.descEntryToObject(key, value);
			descProcessor.processDescription(desc);
		}

		public void processConceptAttributeEntry(DatabaseEntry key,
				DatabaseEntry value) throws Exception {
			I_ConceptAttributeVersioned conc = bdbEnv.conAttrEntryToObject(key, value);
			conceptAttributeProcessor.processConceptAttributes(conc);
		}

		public void processRel(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
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

		public void setConceptAttributeProcessor(
				I_ProcessConceptAttributes conceptAttributeProcessor) {
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

		public void processId(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_IdVersioned idv = bdbEnv.idEntryToObject(key, value);
			this.idProcessor.processId(idv);
		}

		public I_ProcessImages getImageProcessor() {
			return imageProcessor;
		}

		public void setImageProcessor(I_ProcessImages imageProcessor) {
			this.imageProcessor = imageProcessor;
		}

		public void processImages(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_ImageVersioned imageV = bdbEnv.imageEntryToObject(key, value);
			this.imageProcessor.processImages(imageV);
		}

		public I_ProcessPaths getPathProcessor() {
			return pathProcessor;
		}

		public void setPathProcessor(I_ProcessPaths pathProcessor) {
			this.pathProcessor = pathProcessor;
		}

		public void processPath(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_Path path = bdbEnv.pathEntryToObject(key, value);
			this.pathProcessor.processPath(path);
		}

		public void setExtProcessor(I_ProcessExtByRef extProcessor) {
			this.extProcessor = extProcessor;
		}

		public void processEbr(DatabaseEntry key, DatabaseEntry value)
				throws Exception {
			I_ThinExtByRefVersioned extension = bdbEnv.extEntryToObject(key, value);
			this.extProcessor.processExtensionByReference(extension);
		}

	}

	public class ConceptIteratorWrapper implements
			I_ProcessConceptAttributeEntries {

		I_ProcessConcepts processor;

		public ConceptIteratorWrapper(I_ProcessConcepts processor) {
			super();
			this.processor = processor;
		}

		public void processConceptAttributeEntry(DatabaseEntry key,
				DatabaseEntry value) throws Exception {
			I_ConceptAttributeVersioned conAttrVersioned = bdbEnv.conAttrEntryToObject(key, value);
			ConceptBean bean = ConceptBean.get(conAttrVersioned.getConId());
			processor.processConcept(bean);
		}

		public DatabaseEntry getDataEntry() {
			return new DatabaseEntry();
		}

		public DatabaseEntry getKeyEntry() {
			return new DatabaseEntry();
		}

	}

	public void iterateConceptAttributes(I_ProcessConceptAttributes processor)
			throws Exception {
		Iterator<I_GetConceptData> conItr = bdbEnv.getConceptIterator();
		while (conItr.hasNext()) {
			processor.processConceptAttributes(conItr.next().getConceptAttributes());
		}
	}


	public Iterator<I_DescriptionVersioned> getDescriptionIterator()
			throws IOException {
		return bdbEnv.getDescriptionIterator();
	}

	public void iterateDescriptions(I_ProcessDescriptions processor)
			throws Exception {
		Iterator<I_DescriptionVersioned> descItr = bdbEnv.getDescriptionIterator();
		while (descItr.hasNext()) {
			processor.processDescription(descItr.next());
		}
	}

	public void iterateIds(org.dwfa.ace.api.I_ProcessIds processor)
			throws Exception {
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
		ProcessorWrapper wrapper = new ProcessorWrapper();
		wrapper.setPathProcessor(processor);
		iteratePaths(wrapper);
	}

	public void iterateRelationships(I_ProcessRelationships processor)
			throws Exception {
		Iterator<I_RelVersioned> relItr = bdbEnv.getRelationshipIterator();
		while (relItr.hasNext()) {
			processor.processRelationship(relItr.next());
		}
	}

	public void iterateConcepts(I_ProcessConcepts processor) throws Exception {
		ConceptIteratorWrapper wrapper = new ConceptIteratorWrapper(processor);
		iterateConceptAttributeEntries(wrapper);
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

	public I_GetConceptData getConcept(int nativeId)
			throws TerminologyException, IOException {
		return ConceptBean.get(nativeId);
	}


	public I_Path getPath(Collection<UUID> uids) throws TerminologyException,
			IOException {
		try {
			return getPath(uuidToNative(uids));
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	public I_Path getPath(UUID[] uuids) throws TerminologyException,
			IOException {
		try {
			return getPath(uuidToNative(Arrays.asList(uuids)));
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
	}

	public I_Path newPath(Set<I_Position> origins, I_GetConceptData pathConcept)
			throws TerminologyException, IOException {
		Path newPath = new Path(pathConcept.getConceptId(),
				new ArrayList<I_Position>(origins));
		AceLog.getEditLog().fine("writing new path: \n" + newPath);
		try {
			AceConfig.getVodb().writePath(newPath);
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
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

	public I_WriteChangeSet newBinaryChangeSetWriter(File changeSetFile) {
		File tempChangeSetFile = new File(changeSetFile.getParentFile(),
				changeSetFile.getName() + ".temp");
		BinaryChangeSetWriter bcs = new BinaryChangeSetWriter(changeSetFile,
				tempChangeSetFile);
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

	public I_ConfigAceFrame newAceFrameConfig() throws TerminologyException,
			IOException {
		return new AceFrameConfig();
	}

	I_ConfigAceFrame activeAceFrameConfig;

	public I_ConfigAceFrame getActiveAceFrameConfig()
			throws TerminologyException, IOException {
		return activeAceFrameConfig;
	}

	public void setActiveAceFrameConfig(I_ConfigAceFrame activeAceFrameConfig)
			throws TerminologyException, IOException {
		this.activeAceFrameConfig = activeAceFrameConfig;
	}

	public I_ConfigAceDb newAceDbConfig() {
		return new AceConfig(envHome, readOnly, cacheSize);
	}

	public long convertToThickVersion(int version) {
		return ThinVersionHelper.convert(version);
	}

	public int convertToThinVersion(long time) {
		return ThinVersionHelper.convert(time);
	}

	public int convertToThinVersion(String dateStr)
			throws java.text.ParseException {
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

	public static Long getCacheSize() {
		return cacheSize;
	}

	public static void setCacheSize(Long cacheSize) {
		VodbEnv.cacheSize = cacheSize;
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

	public I_ThinExtByRefPartBoolean newBooleanExtensionPart() {
		return new ThinExtByRefPartBoolean();
	}

	public I_ThinExtByRefPartConcept newConceptExtensionPart() {
		return new ThinExtByRefPartConcept();
	}

	public I_ThinExtByRefVersioned newExtension(int refsetId, int memberId,
			int componentId, int typeId) {
		ThinExtByRefVersioned thinEbr = new ThinExtByRefVersioned(refsetId,
				memberId, componentId, typeId);
		ExtensionByReferenceBean ebrBean = ExtensionByReferenceBean.makeNew(
				memberId, thinEbr);
		ACE.addUncommitted(ebrBean);
		return thinEbr;
	}

	public I_ThinExtByRefVersioned newExtensionBypassCommit(int refsetId,
			int memberId, int componentId, int typeId) {
		ThinExtByRefVersioned thinEbr = new ThinExtByRefVersioned(refsetId,
				memberId, componentId, typeId);
		return thinEbr;
	}

	public I_ThinExtByRefPartInteger newIntegerExtensionPart() {
		return new ThinExtByRefPartInteger();
	}

	public I_ThinExtByRefPartLanguage newLanguageExtensionPart() {
		return new ThinExtByRefPartLanguage();
	}

	public I_ThinExtByRefPartLanguageScoped newLanguageScopedExtensionPart() {
		return new ThinExtByRefPartLanguageScoped();
	}

	public I_ThinExtByRefPartMeasurement newMeasurementExtensionPart() {
		return new ThinExtByRefPartMeasurement();
	}

	public I_ThinExtByRefPartString newStringExtensionPart() {
		return new ThinExtByRefPartString();
	}

	public Set<I_Transact> getUncommitted() {
		return ACE.getUncommitted();
	}

	public I_ThinExtByRefPartConceptInt newConceptIntExtensionPart() {
		return new ThinExtByRefPartConceptInt();
	}

	public I_GetExtensionData getExtensionWrapper(int nid) throws IOException {
		return ExtensionByReferenceBean.get(nid);
	}

	public static boolean isTransactional() {
		return transactional;
	}

	public I_RelVersioned newRelationship(UUID relUuid, int uuidType,
			int conceptNid, int relDestinationNid, int pathNid, int version,
			int relStatusNid, int relTypeNid, int relCharacteristicNid,
			int relRefinabilityNid, int relGroup) throws TerminologyException,
			IOException {

		int relId = nativeGenerationForUuid(relUuid, uuidType, pathNid, version);
		ThinRelVersioned rel = new ThinRelVersioned(relId, conceptNid,
				relDestinationNid, 1);
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

	public I_IdVersioned getId(Collection<UUID> uids)
			throws TerminologyException, IOException {
		return bdbEnv.getId(uids);
	}

	public I_IdVersioned getId(int nativeId) throws IOException {
		return bdbEnv.getId(nativeId);
	}

	public ThinIdVersioned getId(UUID uid) throws TerminologyException,
			IOException {
		return bdbEnv.getId(uid);
	}

	public I_IdVersioned getIdNullOk(int nativeId) throws IOException {
		return bdbEnv.getIdNullOk(nativeId);
	}

	public Collection<UUID> getUids(int nativeId) throws TerminologyException,
			IOException {
		return bdbEnv.getUids(nativeId);
	}

	public boolean hasId(Collection<UUID> uids) throws DatabaseException {
		return bdbEnv.hasId(uids);
	}

	public boolean hasId(UUID uid) throws DatabaseException {
		return bdbEnv.hasId(uid);
	}

	public void iterateIdEntries(I_ProcessIdEntries processor) throws Exception {
		bdbEnv.iterateIdEntries(processor);
	}

	public int nativeGenerationForUuid(UUID uid, int source, int pathId,
			int version) throws TerminologyException, IOException {
		return bdbEnv.nativeGenerationForUuid(uid, source, pathId,
				version);
	}

	public List<UUID> nativeToUuid(int nativeId) throws DatabaseException {
		return bdbEnv.nativeToUuid(nativeId);
	}

	public int uuidToNative(Collection<UUID> uids) throws TerminologyException,
			IOException {
		return bdbEnv.uuidToNative(uids);
	}

	public int uuidToNative(UUID uid) throws TerminologyException, IOException {
		return bdbEnv.uuidToNative(uid);
	}

	public int uuidToNativeWithGeneration(Collection<UUID> uids, int source,
			I_Path idPath, int version) throws TerminologyException,
			IOException {
		return bdbEnv.uuidToNativeWithGeneration(uids, source, idPath,
				version);
	}

	public int uuidToNativeWithGeneration(UUID uid, int source,
			Collection<I_Path> idPaths, int version)
			throws TerminologyException, IOException {
		return bdbEnv.uuidToNativeWithGeneration(uid, source, idPaths,
				version);
	}

	public int uuidToNativeWithGeneration(UUID uid, int source, I_Path idPath,
			int version) throws TerminologyException, IOException {
		return bdbEnv.uuidToNativeWithGeneration(uid, source, idPath,
				version);
	}

	public void writeId(I_IdVersioned id) throws DatabaseException {
		bdbEnv.writeId(id);
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

	public ActivityPanel getActivityFrame() {
		return activityFrame;
	}

	public static void setTransactional(boolean transactional) {
		VodbEnv.transactional = transactional;
	}

	public static boolean getTxnNoSync() {
		return txnNoSync;
	}
	public static void setTxnNoSync(boolean txnNoSync) {
		VodbEnv.txnNoSync =  txnNoSync;
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

	public void cleanupSNOMED(I_IntSet relsToIgnore, int[] releases)
			throws Exception {
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

}
