package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesPart;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesTuple;
import org.ihtsdo.db.bdb.concept.component.description.Description;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionPart;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionTuple;
import org.ihtsdo.db.bdb.concept.component.image.Image;
import org.ihtsdo.db.bdb.concept.component.image.ImagePart;
import org.ihtsdo.db.bdb.concept.component.image.ImageTuple;
import org.ihtsdo.db.bdb.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipPart;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipTuple;

public class Concept
		implements
		I_Transact,
		I_GetConceptData<Relationship, RelationshipPart, RelationshipTuple, 
						 ConceptAttributes, ConceptAttributesPart, ConceptAttributesTuple, 
						 Description, DescriptionPart, DescriptionTuple, 
						 Image, ImagePart, ImageTuple,
						 Concept> {

	private static class TransactionHandler implements I_Transact {

		@Override
		public void abort() throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void commit(int version, Set<TimePathId> values)
				throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public I_Transact getTransactionProxy() throws IOException {
			return this;
		}

	}

	public static Concept get(int nid, boolean editable) throws IOException {
		return new Concept(nid, editable);
	}

	private static I_Transact transactionHandler = new TransactionHandler();

	private int nid;
	private boolean editable;
	private ConceptData data;

	private List<Description> unsubmittedDescriptions;
	private List<Relationship> unsubmittedRelationships;

	protected Concept(int nid, boolean editable) throws IOException {
		super();
		this.nid = nid;
		this.editable = editable;
		data = new ConceptData(nid, editable);
	}

	public int getNid() {
		return nid;
	}

	public List<Description> getDescriptions() throws IOException {
		return data.getDescriptions();
	}

	public List<Relationship> getSourceRels() throws IOException {
		return data.getSourceRels();
	}

	public boolean isEditable() {
		return editable;
	}

	@Override
	public void abort() throws IOException {
		editable = false;
	}

	@Override
	public void commit(int version, Set<TimePathId> values) throws IOException {
		if (editable) {
			try {
				if (ReadWriteDataVersion.get(nid) == data
						.getReadWriteDataVersion()) {

				} else {

				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			} catch (ExecutionException e) {
				throw new IOException(e);
			}
		} else {
			throw new UnsupportedOperationException(
					"Concept is not editable. nid: " + nid);
		}
	}

	@Override
	public I_Transact getTransactionProxy() throws IOException {
		editable = false;
		return transactionHandler;
	}

	@Override
	public boolean equals(Object obj) {
		if (Concept.class.isAssignableFrom(obj.getClass())) {
			Concept another = (Concept) obj;
			return nid == another.nid;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(new int[] { nid });
	}

	public List<UUID> getUids() throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<UUID> getUidsForComponent(int componentNid) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ConceptAttributesTuple> getConceptAttributeTuples(
			I_IntSet allowedStatus, Set<I_Position> positions)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ConceptAttributesTuple> getConceptAttributeTuples(
			I_IntSet allowedStatus, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ConceptAttributesTuple> getConceptAttributeTuples(
			I_IntSet allowedStatus, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ConceptAttributesTuple> getConceptAttributeTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public ConceptAttributes getConceptAttributes() throws IOException {
		return data.getConceptAttributes();
	}

	@Override
	public int getConceptId() {
		return nid;
	}

	@Override
	public DescriptionTuple getDescTuple(I_IntList typePrefOrder,
			I_IntList langPrefOrder, I_IntSet allowedStatus,
			Set<I_Position> positionSet, LANGUAGE_SORT_PREF sortPref)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public DescriptionTuple getDescTuple(I_IntList descTypePreferenceList,
			I_ConfigAceFrame config) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean returnConflictResolvedLatestState) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<DescriptionTuple> getDescriptionTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Concept> getDestRelOrigins(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Concept> getDestRelOrigins(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Concept> getDestRelOrigins(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<RelationshipTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<RelationshipTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<RelationshipTuple> getDestRelTuples(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Relationship> getDestRels() throws IOException {
		return data.getDestRels();
	}

	@Override
	public List<I_ThinExtByRefVersioned> getExtensions() throws IOException,
			TerminologyException {
		return data.getExtensions();
	}

	@Override
	public I_IdVersioned getId() throws IOException {
		return data.getId();
	}

	@Override
	public Object getId(int identifierScheme) throws IOException,
			TerminologyException {
		return data.getId(identifierScheme);
	}

	@Override
	public List<ImageTuple> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ImageTuple> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ImageTuple> getImageTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Image> getImages() throws IOException {
		return data.getImages();
	}

	@Override
	public String getInitialText() throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Concept> getSourceRelTargets(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Concept> getSourceRelTargets(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		return getSourceRelTargets(allowedStatus, allowedTypes, positions,
				addUncommitted, returnConflictResolvedLatestState);
	}

	@Override
	public Set<Concept> getSourceRelTargets(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<RelationshipTuple> getSourceRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<RelationshipTuple> getSourceRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<RelationshipTuple> getSourceRelTuples(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		List<RelationshipTuple> returnTuples = new ArrayList<RelationshipTuple>();
		for (Relationship r: getSourceRels()) {
			r.addTuples(allowedTypes, returnTuples, addUncommitted, 
					returnConflictResolvedLatestState);
		}
		return returnTuples;
	}

	@Override
	public ConceptAttributes getUncommittedConceptAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Description> getUncommittedDescriptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<I_IdVersioned> getUncommittedIdVersioned() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public I_IntSet getUncommittedIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Image> getUncommittedImages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Relationship> getUncommittedSourceRels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UniversalAceBean getUniversalAceBean() throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isParentOf(Concept child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isParentOf(Concept child, boolean addUncommitted)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isParentOfOrEqualTo(Concept child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isParentOfOrEqualTo(Concept child, boolean addUncommitted)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return false;
	}

	public Relationship getRelationship(int relNid) throws IOException {
		for (Relationship r: getSourceRels()) {
			if (r.getNid() == relNid) {
				return r;
			}
		}
		throw new IOException("no such relid: " + relNid + " in concept: " + nid);
	}
}
