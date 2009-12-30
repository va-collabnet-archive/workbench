package org.ihtsdo.db.bdb.concept.component.refsetmember;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refsetmember.crossMap.CrossMapVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.crossMapForRel.CrossMapForRelVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.floatConcept.MeasurementVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.id.ConceptVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.idId.ConceptConceptVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.idIdId.ConceptConceptConceptVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.idIdString.ConceptConceptStringVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.idInteger.ConceptIntegerVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.idString.ConceptStringVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntegerVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.language.LanguageVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membership.MembershipVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.scopedLanguage.ScopedLanguageVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.string.StringVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.template.TemplateVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.templateForRel.TemplateForRelVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class RefsetMember extends ConceptComponent<RefsetMemberMutablePart, RefsetMember> 
	implements I_ThinExtByRefVersioned {
	/**
	 * 
	 * @author kec
	 *
	 */
	protected enum REFSET_MEMBER_TYPE {
	    MEMBER(RefsetAuxiliary.Concept.MEMBERSHIP_EXTENSION.getUids()), 
	    BOOLEAN(RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids()), 
	    CONCEPT(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids()), 
	    CON_INT(RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids()),
	    STRING(RefsetAuxiliary.Concept.STRING_EXTENSION.getUids()), 
	    INTEGER(RefsetAuxiliary.Concept.INT_EXTENSION.getUids()), 
	    MEASUREMENT(RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids()), 
	    LANGUAGE(RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids()), 
	    SCOPED_LANGUAGE(RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids()), 
	    TEMPLATE_FOR_REL(RefsetAuxiliary.Concept.TEMPLATE_REL_EXTENSION.getUids()),
	    TEMPLATE(RefsetAuxiliary.Concept.TEMPLATE_EXTENSION.getUids()),
	    CROSS_MAP_FOR_REL(RefsetAuxiliary.Concept.CROSS_MAP_REL_EXTENSION.getUids()),
	    CROSS_MAP(RefsetAuxiliary.Concept.CROSS_MAP_EXTENSION.getUids()),
	    CONCEPT_CONCEPT(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.getUids()),
	    CONCEPT_CONCEPT_CONCEPT(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.getUids()),
	    CONCEPT_CONCEPT_STRING(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.getUids()),
	    CONCEPT_STRING(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.getUids()),
	    	;

	    private int typeNid;
	    
	    
	    private REFSET_MEMBER_TYPE(Collection<UUID> uids) {
	        try {
				this.typeNid = AceConfig.getVodb().uuidToNative(uids);
			} catch (TerminologyException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	    }

	    private static Map<Integer, REFSET_MEMBER_TYPE> typeNidToRefsetMemberTypeMap;

			public static REFSET_MEMBER_TYPE readType(TupleInput input) {
				if (typeNidToRefsetMemberTypeMap == null) {
					typeNidToRefsetMemberTypeMap = 
				    	new HashMap<Integer, REFSET_MEMBER_TYPE>();
				    	for (REFSET_MEMBER_TYPE type: REFSET_MEMBER_TYPE.values()) {
				    		typeNidToRefsetMemberTypeMap.put(type.typeNid, type);
				    	}
				}
				return typeNidToRefsetMemberTypeMap.get(input.readInt());
			}
	}

	private int refsetNid;
	private int referencedComponentNid;
	private int memberTypeNid; 


	public RefsetMember(int nid, int partCount, boolean editable) {
		super(nid, partCount, editable);
	}


	@Override
	public void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<RefsetMemberMutablePart> partsToWrite = new ArrayList<RefsetMemberMutablePart>();
		for (RefsetMemberMutablePart p: mutableComponentParts) {
			if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
				partsToWrite.add(p);
			}
		}
		// Start writing
		output.writeInt(nid);
		output.writeShort(partsToWrite.size());
		// refsetNid is the enclosing concept, does not need to be written. 
		output.writeInt(referencedComponentNid);
		output.writeInt(memberTypeNid);

		for (RefsetMemberMutablePart p: partsToWrite) {
			p.writePartToBdb(output);
		}
		
	}

	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid, int listSize) {
		refsetNid = conceptNid;
		
		// nid, list size, and conceptNid are read already by the binder...
		referencedComponentNid = input.readInt();
		REFSET_MEMBER_TYPE memberType = REFSET_MEMBER_TYPE.readType(input);
		memberTypeNid = memberType.typeNid;
		for (int i = 0; i < listSize; i++) {
			switch (memberType) {
			case BOOLEAN:
				mutableComponentParts.add(new MembershipVersion(input));
				break;
			case CON_INT:
				mutableComponentParts.add(new ConceptIntegerVersion(input));
				break;
			case CONCEPT:
				mutableComponentParts.add(new ConceptVersion(input));
				break;
			case CONCEPT_CONCEPT:
				mutableComponentParts.add(new ConceptConceptVersion(input));
				break;
			case CONCEPT_CONCEPT_CONCEPT:
				mutableComponentParts.add(new ConceptConceptConceptVersion(input));
				break;
			case CONCEPT_CONCEPT_STRING:
				mutableComponentParts.add(new ConceptConceptStringVersion(input));
				break;
			case CONCEPT_STRING:
				mutableComponentParts.add(new ConceptStringVersion(input));
				break;
			case CROSS_MAP:
				mutableComponentParts.add(new CrossMapVersion(input));
				break;
			case CROSS_MAP_FOR_REL:
				mutableComponentParts.add(new CrossMapForRelVersion(input));
				break;
			case INTEGER:
				mutableComponentParts.add(new IntegerVersion(input));
				break;
			case LANGUAGE:
				mutableComponentParts.add(new LanguageVersion(input));
				break;
			case MEASUREMENT:
				mutableComponentParts.add(new MeasurementVersion(input));
				break;
			case MEMBER:
				mutableComponentParts.add(new MembershipVersion(input));
				break;
			case SCOPED_LANGUAGE:
				mutableComponentParts.add(new ScopedLanguageVersion(input));
				break;
			case STRING:
				mutableComponentParts.add(new StringVersion(input));
				break;
			case TEMPLATE:
				mutableComponentParts.add(new TemplateVersion(input));
				break;
			case TEMPLATE_FOR_REL:
				mutableComponentParts.add(new TemplateForRelVersion(input));
				break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}

	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void addVersion(I_ThinExtByRefPart part) {
		mutableComponentParts.add((RefsetMemberMutablePart) part);
	}


	@Override
	public int getComponentId() {
		return referencedComponentNid;
	}


	@Override
	public int getMemberId() {
		return nid;
	}


	@Override
	public int getRefsetId() {
		return refsetNid;
	}


	@Override
	public List<I_ThinExtByRefTuple> getTuples(boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getTypeId() {
		return memberTypeNid;
	}


	@Override
	public void setRefsetId(int refsetId) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void setTypeId(int typeId) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions,
			List<I_ThinExtByRefTuple> returnTuples, boolean addUncommitted) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions,
			List<I_ThinExtByRefTuple> returnTuples, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addTuples(List<I_ThinExtByRefTuple> returnTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}


	@Override
	public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus,
			Set<I_Position> positions, boolean addUncommitted) {
		throw new UnsupportedOperationException();
	}


	@Override
	public List<I_ThinExtByRefTuple> getTuples(I_IntSet allowedStatus,
			Set<I_Position> positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public RefsetMember getMutablePart() {
		return this;
	}
}
