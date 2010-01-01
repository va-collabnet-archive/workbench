package org.ihtsdo.db.bdb.concept.component.refset;

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
import org.ihtsdo.db.bdb.concept.component.refsetmember.cid.CidVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCid.CidCidVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidCid.CidCidCidVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidCidStr.CidCidStrVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidFloat.CidFloatVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidInt.CidIntVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.cidStr.CidStrVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.integer.IntVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.membership.MembershipVersion;
import org.ihtsdo.db.bdb.concept.component.refsetmember.str.StrVersion;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class AbstractRefsetMember extends ConceptComponent<RefsetMemberMutablePart, AbstractRefsetMember> 
	implements I_ThinExtByRefVersioned {
	/**
	 * CID = Component Identifier
	 * @author kec
	 *
	 */
	public enum REFSET_MEMBER_TYPE {
	    MEMBER(RefsetAuxiliary.Concept.MEMBERSHIP_EXTENSION.getUids()), 
	    BOOLEAN(RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids()), 
	    CID(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids()), 
	    CID_INT(RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids()),
	    CID_LONG(RefsetAuxiliary.Concept.CID_LONG_EXTENSION.getUids()),
	    STRING(RefsetAuxiliary.Concept.STRING_EXTENSION.getUids()), 
	    INTEGER(RefsetAuxiliary.Concept.INT_EXTENSION.getUids()), 
	    CID_FLOAT(RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids()), 
	    CID_CID(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.getUids()),
	    CID_CID_CID(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.getUids()),
	    CID_CID_STRING(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.getUids()),
	    CID_STRING(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.getUids()),
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


	public AbstractRefsetMember(int nid, int partCount, boolean editable) {
		super(nid, partCount, editable);
	}


	@Override
	public void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<RefsetMemberMutablePart> partsToWrite = new ArrayList<RefsetMemberMutablePart>();
		for (RefsetMemberMutablePart p: additionalVersions) {
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
				additionalVersions.add(new MembershipVersion(input));
				break;
			case CID_INT:
				additionalVersions.add(new CidIntVersion(input));
				break;
			case CID:
				additionalVersions.add(new CidVersion(input));
				break;
			case CID_CID:
				additionalVersions.add(new CidCidVersion(input));
				break;
			case CID_CID_CID:
				additionalVersions.add(new CidCidCidVersion(input));
				break;
			case CID_CID_STRING:
				additionalVersions.add(new CidCidStrVersion(input));
				break;
			case CID_STRING:
				additionalVersions.add(new CidStrVersion(input));
				break;
			case INTEGER:
				additionalVersions.add(new IntVersion(input));
				break;
			case CID_FLOAT:
				additionalVersions.add(new CidFloatVersion(input));
				break;
			case MEMBER:
				additionalVersions.add(new MembershipVersion(input));
				break;
			case STRING:
				additionalVersions.add(new StrVersion(input));
				break;
			case CID_LONG:
				additionalVersions.add(new CidLongVersion(input));
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
		additionalVersions.add((RefsetMemberMutablePart) part);
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
	public AbstractRefsetMember getMutablePart() {
		return this;
	}
}
