package org.ihtsdo.db.bdb.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceConceptAttributes;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.ace.utypes.UniversalAceImage;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesVersion;
import org.ihtsdo.db.bdb.concept.component.description.Description;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionVersion;
import org.ihtsdo.db.bdb.concept.component.image.Image;
import org.ihtsdo.db.bdb.concept.component.image.ImageVersion;
import org.ihtsdo.db.bdb.concept.component.refset.AbstractRefsetMember;
import org.ihtsdo.db.bdb.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipVersion;

public class Concept implements I_Transact, I_GetConceptData {

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

	}

	public static Concept get(UniversalAceBean universal) throws IOException {
		int conceptNid = Bdb.uuidsToNid(universal.getIdentifier().getUIDs());
		
		Concept c = get(conceptNid, true);
		
		UniversalAceConceptAttributes uAttr = universal.getConceptAttributes();
		
		ConceptAttributes attr = new ConceptAttributes(c.nid, uAttr.getVersions().size(), true);
		c.unsubmittedComponents.add(attr);
		for (UniversalAceConceptAttributesPart uPart: uAttr.getVersions()) {
			ConceptAttributesVersion part = 
				new ConceptAttributesVersion(Bdb.uuidsToNid(uPart.getStatusId()), 
						Bdb.uuidsToNid(uPart.getPathId()), 
						uPart.getTime());
			attr.addVersion(part);
		}
			
		UniversalAceIdentification uId = universal.getIdentifier();
		
		for (UniversalAceDescription uDesc: universal.getDescriptions()) {
			Description desc = new Description(uDesc, c.editable);
			c.unsubmittedComponents.add(desc);
		}
		for (UniversalAceRelationship uRel: universal.getSourceRels()) {
			Relationship rel = new Relationship(uRel, c.editable);
			c.unsubmittedComponents.add(rel);
		}
		for (UniversalAceImage uImage: universal.getImages()) {
			Image img = new Image(uImage, c.editable);
			c.unsubmittedComponents.add(img);
		}
		return c;
	}
	
	
	public static Concept get(int nid, boolean editable) throws IOException {
		return new Concept(nid, editable);
	}

	private static I_Transact transactionHandler = new TransactionHandler();

	private int nid;
	private boolean editable;
	private ConceptData data;

	private List<ConceptComponent<?, ?>> unsubmittedComponents;

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
	public List<Relationship> getNativeSourceRels() throws IOException {
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

	public List<ConceptAttributesVersion> getConceptAttributeTuples(
			I_IntSet allowedStatus, Set<I_Position> positions)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<ConceptAttributesVersion> getConceptAttributeTuples(
			I_IntSet allowedStatus, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<ConceptAttributesVersion> getConceptAttributeTuples(
			I_IntSet allowedStatus, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<ConceptAttributesVersion> getConceptAttributeTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public ConceptAttributes getConceptAttributes() throws IOException {
		return data.getConceptAttributes();
	}

	public int getConceptId() {
		return nid;
	}

	public DescriptionVersion getDescTuple(I_IntList typePrefOrder,
			I_IntList langPrefOrder, I_IntSet allowedStatus,
			Set<I_Position> positionSet, LANGUAGE_SORT_PREF sortPref)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public DescriptionVersion getDescTuple(I_IntList descTypePreferenceList,
			I_ConfigAceFrame config) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<DescriptionVersion> getDescriptionTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<DescriptionVersion> getDescriptionTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean returnConflictResolvedLatestState) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<DescriptionVersion> getDescriptionTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public Set<Concept> getDestRelOrigins(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public Set<Concept> getDestRelOrigins(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public Set<Concept> getDestRelOrigins(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<RelationshipVersion> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<RelationshipVersion> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<Relationship> getDestRels() throws IOException {
		return data.getDestRels();
	}

	public List<AbstractRefsetMember> getExtensions() throws IOException,
			TerminologyException {
		return data.getExtensions();
	}

	public List<ImageVersion> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<ImageVersion> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<ImageVersion> getImageTuples(
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<Image> getImages() throws IOException {
		return data.getImages();
	}

	public String getInitialText() throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public I_RepresentIdSet getPossibleKindOfConcepts(I_ConfigAceFrame config)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public Set<Concept> getSourceRelTargets(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public Set<Concept> getSourceRelTargets(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		return getSourceRelTargets(allowedStatus, allowedTypes, positions,
				addUncommitted, returnConflictResolvedLatestState);
	}

	public Set<Concept> getSourceRelTargets(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<RelationshipVersion> getSourceRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<RelationshipVersion> getSourceRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public List<? extends I_RelTuple> getSourceRelTuples(I_IntSet allowedTypes,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		List<I_RelTuple> returnTuples = new ArrayList<I_RelTuple>();
		for (Relationship r: getNativeSourceRels()) {
			r.addTuples(allowedTypes, returnTuples, addUncommitted, 
					returnConflictResolvedLatestState);
		}
		return returnTuples;
	}

	public ConceptAttributes getUncommittedConceptAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<I_DescriptionVersioned> getUncommittedDescriptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<I_Identify> getUncommittedIdVersioned() {
		// TODO Auto-generated method stub
		return null;
	}

	public I_IntSet getUncommittedIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<I_ImageVersioned> getUncommittedImages() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<I_RelVersioned> getUncommittedSourceRels() {
		// TODO Auto-generated method stub
		return null;
	}

	public UniversalAceBean getUniversalAceBean() throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted)
			throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public boolean isParentOf(Concept child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public boolean isParentOf(Concept child, boolean addUncommitted)
			throws IOException, TerminologyException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public boolean isParentOfOrEqualTo(Concept child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException {
		//TODO
		throw new UnsupportedOperationException();
	}

	public boolean isParentOfOrEqualTo(Concept child, boolean addUncommitted)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Relationship getRelationship(int relNid) throws IOException {
		for (Relationship r: getNativeSourceRels()) {
			if (r.getNid() == relNid) {
				return r;
			}
		}
		throw new IOException("no such relid: " + relNid + " in concept: " + nid);
	}


	@Override
	public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(
			I_IntSet allowedStatus, PositionSetReadOnly positions)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(
			I_IntSet allowedStatus, PositionSetReadOnly positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_ConceptAttributeTuple> getConceptAttributeTuples(
			I_IntSet allowedStatus, PositionSetReadOnly positions,
			boolean addUncommitted) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public I_DescriptionTuple getDescTuple(I_IntList typePrefOrder,
			I_IntList langPrefOrder, I_IntSet allowedStatus,
			PositionSetReadOnly positionSet, LANGUAGE_SORT_PREF sortPref)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_DescriptionTuple> getDescriptionTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_DescriptionTuple> getDescriptionTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions,
			boolean returnConflictResolvedLatestState) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<? extends I_GetConceptData> getDestRelOrigins(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public Set<? extends I_GetConceptData> getDestRelOrigins(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_RelTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_ImageTuple> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_ImageTuple> getImageTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<? extends I_GetConceptData> getSourceRelTargets(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public Set<? extends I_GetConceptData> getSourceRelTargets(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_RelTuple> getSourceRelTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public List<? extends I_RelTuple> getSourceRelTuples(
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState) throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, PositionSetReadOnly positions,
			boolean addUncommitted) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isParentOf(I_GetConceptData child, boolean addUncommitted)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isParentOfOrEqualTo(I_GetConceptData child,
			I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, boolean addUncommitted)
			throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isParentOfOrEqualTo(I_GetConceptData child,
			boolean addUncommitted) throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public Object getDenotation(int authorityNid) throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


	@Override
	public I_Identify getIdentifier() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}	
}
