package org.ihtsdo.db.bdb.concept.component.refsetmember;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVariablePartLong;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVariablePartString;
import org.ihtsdo.db.bdb.concept.component.identifier.IdentifierVariablePartUuid;
import org.ihtsdo.db.bdb.concept.component.identifier.Identifier.VARIABLE_PART_TYPES;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RefsetMember extends ConceptComponent<RefsetMemberVariablePart> 
	implements I_ThinExtByRefVersioned<RefsetMemberVariablePart, RefsetMemberVersion> {
	/**
	 * 
	 * @author kec
	 *
	 */
	protected enum REFSET_MEMBER_TYPE {
	    MEMBER(0, RefsetAuxiliary.Concept.MEMBERSHIP_EXTENSION.getUids()), 
	    BOOLEAN(1, RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids()), 
	    CONCEPT(2, RefsetAuxiliary.Concept.STRING_EXTENSION.getUids()), 
	    CON_INT(3, RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids()),
	    STRING(4, RefsetAuxiliary.Concept.INT_EXTENSION.getUids()), 
	    INTEGER(5, RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids()), 
	    MEASUREMENT(6, RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids()), 
	    LANGUAGE(7, RefsetAuxiliary.Concept.SCOPED_LANGUAGE_EXTENSION.getUids()), 
	    SCOPED_LANGUAGE(8, RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids()), 
	    TEMPLATE_FOR_REL(9, RefsetAuxiliary.Concept.CROSS_MAP_EXTENSION.getUids()),
	    TEMPLATE(10, RefsetAuxiliary.Concept.CROSS_MAP_REL_EXTENSION.getUids()),
	    CROSS_MAP_FOR_REL(11, RefsetAuxiliary.Concept.TEMPLATE_EXTENSION.getUids()),
	    CROSS_MAP(12, RefsetAuxiliary.Concept.TEMPLATE_REL_EXTENSION.getUids()),
	    CONCEPT_CONCEPT(13, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.getUids()),
	    CONCEPT_CONCEPT_CONCEPT(14, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.getUids()),
	    CONCEPT_CONCEPT_STRING(15, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.getUids()),
	    CONCEPT_STRING(16, RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.getUids()),
	    	;
	    
	    private int typeNid;
	    private byte typeByte;
	    
	    
	    private REFSET_MEMBER_TYPE(int typeByte, Collection<UUID> uids) throws TerminologyException, IOException {
	    	this.typeByte = (byte) typeByte;
	        this.typeNid = AceConfig.getVodb().uuidToNative(uids);
	    }

	    private static Map<Integer, REFSET_MEMBER_TYPE> typeNidToRefsetMemberTypeMap = 
	    	new HashMap<Integer, REFSET_MEMBER_TYPE>();

			public static REFSET_MEMBER_TYPE readType(TupleInput input) {
				switch (input.readByte()) {
				case 0:
					return MEMBER;
				case 1:
					return BOOLEAN; 
				case 2:
					return CONCEPT; 
				case 3:
					return CON_INT;
				case 4:
					return STRING; 
				case 5:
					return INTEGER; 
				case 6:
					return MEASUREMENT; 
				case 7:
					return LANGUAGE; 
				case 8:
					return SCOPED_LANGUAGE; 
				case 9:
					return TEMPLATE_FOR_REL;
				case 10:
					return TEMPLATE;
				case 11:
					return CROSS_MAP_FOR_REL;
				case 12:
					return CROSS_MAP;
				case 13:
					return CONCEPT_CONCEPT;
				case 14:
					return CONCEPT_CONCEPT_CONCEPT;
				case 15:
					return CONCEPT_CONCEPT_STRING;
				case 16:
					return CONCEPT_STRING;
				}
				throw new UnsupportedOperationException();
			}
	}

	private int refsetNid;
	private int componentNid;
	private int memberTypeNid; 


	public RefsetMember(int nid, int partCount, boolean editable) {
		super(nid, partCount, editable);
	}

	
	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid) {
		refsetNid = conceptNid;
		componentNid = input.readInt();
		memberTypeNid = input.readInt();
		int partsToRead = input.readShort();
		for (int i = 0; i < partsToRead; i++) {
			switch (REFSET_MEMBER_TYPE.readType(input)) {
			case BOOLEAN:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case CON_INT:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case CONCEPT:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case CONCEPT_CONCEPT_CONCEPT:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case CONCEPT_CONCEPT_STRING:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case CONCEPT_STRING:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case CROSS_MAP:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case CROSS_MAP_FOR_REL:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case INTEGER:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case LANGUAGE:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case MEASUREMENT:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case MEMBER:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case SCOPED_LANGUAGE:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case STRING:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case TEMPLATE:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
			case TEMPLATE_FOR_REL:
				variableParts.add(new IdentifierVariablePartLong(input));
				break;
				default:
					throw new UnsupportedOperationException();
			}
	}

	@Override
	public void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		output.writeInt(componentNid);
		
	}

	@Override
	public boolean promote(I_Position viewPosition,
			PathSetReadOnly pomotionPaths, I_IntSet allowedStatus)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		return false;
	}

}
