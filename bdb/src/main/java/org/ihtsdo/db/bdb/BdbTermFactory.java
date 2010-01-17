package org.ihtsdo.db.bdb;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.lucene.search.Hits;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HandleSubversion;
import org.dwfa.ace.api.I_Identify;
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
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.I_WriteDirectToDb;
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
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class BdbTermFactory implements I_TermFactory {

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
		throw new UnsupportedOperationException();
	}

	@Override
	public void addUncommitted(I_ThinExtByRefVersioned extension) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addUncommittedNoChecks(I_GetConceptData concept) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addUncommittedNoChecks(I_ThinExtByRefVersioned extension) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void cancel() throws IOException {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void forget(I_DescriptionVersioned desc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void forget(I_RelVersioned rel) {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ConfigAceFrame getActiveAceFrameConfig()
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int componentId) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int componentId, boolean addUncommitted) throws IOException {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public I_GetConceptData getConcept(UUID... ids)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_GetConceptData getConcept(int nid) throws TerminologyException,
			IOException {
		int cNid = Bdb.getConceptNid(nid);
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
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public I_Identify getId(UUID uid) throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_Identify getId(Collection<UUID> uids) throws TerminologyException,
			IOException {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public I_Path getPath(UUID... ids) throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<I_Path> getPaths() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_Identify getPreviousAuthorityId() throws TerminologyException,
			IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String> getProperties() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProperty(String key) throws IOException {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getTransactional() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<UUID> getUids(int nid) throws TerminologyException,
			IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<I_Transact> getUncommitted() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasConcept(int conceptId) throws IOException {
		throw new UnsupportedOperationException();
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
	public boolean hasId(Collection<UUID> uids) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasId(UUID uid) throws IOException {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void newAceFrame(I_ConfigAceFrame frameConfig) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ConfigAceFrame newAceFrameConfig() throws TerminologyException,
			IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_ShowActivity newActivityPanel(boolean displayInViewer,
			I_ConfigAceFrame aceFrameConfig) {
		throw new UnsupportedOperationException();
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
	public I_GetConceptData newConcept(UUID newConceptId, boolean defined,
			I_ConfigAceFrame aceFrameConfig) throws TerminologyException,
			IOException {
		throw new UnsupportedOperationException();
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
	public I_DescriptionVersioned newDescription(UUID newDescriptionId,
			I_GetConceptData concept, String lang, String text,
			I_ConceptualizeLocally descType, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_DescriptionVersioned newDescription(UUID newDescriptionId,
			I_GetConceptData concept, String lang, String text,
			I_GetConceptData descType, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public I_IntSet newIntSet() {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public I_Position newPosition(I_Path path, int version)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_RelPart newRelPart() {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, I_GetConceptData relType,
			I_GetConceptData relDestination,
			I_GetConceptData relCharacteristic,
			I_GetConceptData relRefinability, I_GetConceptData relStatus,
			int relGroup, I_ConfigAceFrame aceFrameConfig)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
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
	public void setActiveAceFrameConfig(I_ConfigAceFrame activeAceFrameConfig)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setProperty(String key, String value) throws IOException {
		throw new UnsupportedOperationException();
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
	public void writeId(I_Identify versioned) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writePath(I_Path p) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writePathOrigin(I_Path path, I_Position origin)
			throws TerminologyException {
		throw new UnsupportedOperationException();
	}

}
