package org.ihtsdo.db.bdb.concept.component.refsetmember;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.ConceptConceptConceptMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.ConceptConceptMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.ConceptConceptStringMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.ConceptIntegerMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.ConceptMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.ConceptStringMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.CrossMapForRelMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.CrossMapMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.IntegerMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.LanguageMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.MeasurementMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.MemberMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.MembershipMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.ScopedLanguageMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.StringMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.TemplateForRelMutablePart;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membertype.TemplateMutablePart;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RefsetMember extends ConceptComponent<RefsetMemberMutablePart> 
	implements I_ThinExtByRefVersioned<RefsetMemberMutablePart, RefsetMemberVersion> {
	/**
	 * 
	 * @author kec
	 *
	 */
	protected enum REFSET_MEMBER_TYPE {
	    MEMBER(RefsetAuxiliary.Concept.MEMBERSHIP_EXTENSION.getUids()), 
	    BOOLEAN(RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids()), 
	    CONCEPT(RefsetAuxiliary.Concept.STRING_EXTENSION.getUids()), 
	    CON_INT(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids()),
	    STRING(RefsetAuxiliary.Concept.INT_EXTENSION.getUids()), 
	    INTEGER(RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids()), 
	    MEASUREMENT(RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids()), 
	    LANGUAGE(RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids()), 
	    SCOPED_LANGUAGE(RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids()), 
	    TEMPLATE_FOR_REL(RefsetAuxiliary.Concept.CROSS_MAP_EXTENSION.getUids()),
	    TEMPLATE(RefsetAuxiliary.Concept.CROSS_MAP_REL_EXTENSION.getUids()),
	    CROSS_MAP_FOR_REL(RefsetAuxiliary.Concept.TEMPLATE_EXTENSION.getUids()),
	    CROSS_MAP(RefsetAuxiliary.Concept.TEMPLATE_REL_EXTENSION.getUids()),
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
		for (RefsetMemberMutablePart p: mutableParts) {
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
				mutableParts.add(new MembershipMutablePart(input));
				break;
			case CON_INT:
				mutableParts.add(new ConceptIntegerMutablePart(input));
				break;
			case CONCEPT:
				mutableParts.add(new ConceptMutablePart(input));
				break;
			case CONCEPT_CONCEPT:
				mutableParts.add(new ConceptConceptMutablePart(input));
				break;
			case CONCEPT_CONCEPT_CONCEPT:
				mutableParts.add(new ConceptConceptConceptMutablePart(input));
				break;
			case CONCEPT_CONCEPT_STRING:
				mutableParts.add(new ConceptConceptStringMutablePart(input));
				break;
			case CONCEPT_STRING:
				mutableParts.add(new ConceptStringMutablePart(input));
				break;
			case CROSS_MAP:
				mutableParts.add(new CrossMapMutablePart(input));
				break;
			case CROSS_MAP_FOR_REL:
				mutableParts.add(new CrossMapForRelMutablePart(input));
				break;
			case INTEGER:
				mutableParts.add(new IntegerMutablePart(input));
				break;
			case LANGUAGE:
				mutableParts.add(new LanguageMutablePart(input));
				break;
			case MEASUREMENT:
				mutableParts.add(new MeasurementMutablePart(input));
				break;
			case MEMBER:
				mutableParts.add(new MemberMutablePart(input));
				break;
			case SCOPED_LANGUAGE:
				mutableParts.add(new ScopedLanguageMutablePart(input));
				break;
			case STRING:
				mutableParts.add(new StringMutablePart(input));
				break;
			case TEMPLATE:
				mutableParts.add(new TemplateMutablePart(input));
				break;
			case TEMPLATE_FOR_REL:
				mutableParts.add(new TemplateForRelMutablePart(input));
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
	public void addTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions,
			List<RefsetMemberVersion> returnTuples, boolean addUncommitted) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void addTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions,
			List<RefsetMemberVersion> returnTuples, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void addTuples(List<RefsetMemberVersion> returnTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void addVersion(I_ThinExtByRefPart part) {
		mutableParts.add((RefsetMemberMutablePart) part);
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
	public List<RefsetMemberVersion> getTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions, boolean addUncommitted) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<RefsetMemberVersion> getTuples(I_IntSet allowedStatus,
			PositionSetReadOnly positions, boolean addUncommitted,
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<RefsetMemberVersion> getTuples(boolean addUncommitted,
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

}
