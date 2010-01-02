package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class EConcept  {
	public static final long serialVersionUID = 1;
	/**
	 * CID = Component IDentifier
	 * @author kec
	 *
	 */
	public enum REFSET_TYPES {
		MEMBER(1, RefsetAuxiliary.Concept.MEMBER_TYPE), 
		CID(2, RefsetAuxiliary.Concept.CONCEPT_EXTENSION), 
		CID_CID(3, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION), 
		CID_CID_CID(4, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION), 
		CID_CID_STR(5, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION),
		STR(6, RefsetAuxiliary.Concept.STRING_EXTENSION),
		INT(7, RefsetAuxiliary.Concept.INT_EXTENSION), 
		CID_INT(8, RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION),
		BOOLEAN(9, RefsetAuxiliary.Concept.BOOLEAN_EXTENSION), 
		CID_STR(10, RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION), 
		CID_FLOAT(11, RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION),
		CID_LONG(12, RefsetAuxiliary.Concept.CID_LONG_EXTENSION),
		;

		private int externalizedToken;
		private int typeNid;
		private RefsetAuxiliary.Concept typeConcept;
		private static Map<Integer, REFSET_TYPES> nidTypeMap;

		REFSET_TYPES(int externalizedToken, RefsetAuxiliary.Concept typeConcept) {
			this.externalizedToken = externalizedToken;
			this.typeConcept = typeConcept;
		}

		public static REFSET_TYPES nidToType(int nid) throws TerminologyException, IOException {
			if (nidTypeMap == null) {
				nidTypeMap = new HashMap<Integer, REFSET_TYPES>();
				for (REFSET_TYPES type: REFSET_TYPES.values()) {
					type.typeNid = EComponent.uuidToNid(type.typeConcept.getUids());
					nidTypeMap.put(type.typeNid, type);
				}
			}
			return nidTypeMap.get(nid);
		}
		public void writeType(DataOutput output) throws IOException {
			output.writeByte(externalizedToken);
		}

		public static REFSET_TYPES readType(DataInput input)
				throws IOException {
			switch (input.readByte()) {
			case 1:
				return MEMBER;
			case 2:
				return CID;
			case 3:
				return CID_CID;
			case 4:
				return CID_CID_CID;
			case 5:
				return CID_CID_STR;
			case 6:
				return STR;
			case 7:
				return INT;
			case 8:
				return CID_INT;
			case 9:
				return BOOLEAN;
			case 10:
				return CID_STR;
			case 11:
				return CID_FLOAT;
			case 12:
				return CID_LONG;
			}
			throw new UnsupportedOperationException();
		}

		public int getTypeNid() {
			return typeNid;
		}
	};

	protected static final int dataVersion = 1;
	protected EConceptAttributes conceptAttributes;
	protected List<EDescription> descriptions;
	protected List<ERelationship> relationships;
	protected List<EImage> images;
	protected List<ERefset> refsetMembers;

	public EConcept(DataInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	public void readExternal(DataInput in) throws IOException,
			ClassNotFoundException {
		int readDataVersion = in.readInt();
		if (readDataVersion != dataVersion) {
			throw new IOException("Unsupported dataVersion: " + readDataVersion);
		}
		conceptAttributes = new EConceptAttributes(in);
		int descCount = in.readInt();
		if (descCount > 0) {
			descriptions = new ArrayList<EDescription>(descCount);
			for (int i = 0; i < descCount; i++) {
				descriptions.add(new EDescription(in));
			}
		}
		int relCount = in.readInt();
		if (relCount > 0) {
			relationships = new ArrayList<ERelationship>(relCount);
			for (int i = 0; i < relCount; i++) {
				relationships.add(new ERelationship(in));
			}
		}
		int imgCount = in.readInt();
		if (imgCount > 0) {
			images = new ArrayList<EImage>(imgCount);
			for (int i = 0; i < imgCount; i++) {
				images.add(new EImage(in));
			}
		}
		int refsetMemberCount = in.readInt();
		if (refsetMemberCount > 0) {
			refsetMembers = new ArrayList<ERefset>(refsetMemberCount);
			for (int i = 0; i < refsetMemberCount; i++) {
				REFSET_TYPES type = REFSET_TYPES.readType(in);
				switch (type) {
			case CID:
				refsetMembers.add(new ERefsetCidMember(in));
				break;
			case CID_CID:
				refsetMembers.add(new ERefsetCidCidMember(in));
				break;
			case MEMBER:
				refsetMembers.add(new ERefsetMember(in));
				break;
			case CID_CID_CID:
				refsetMembers.add(new ERefsetCidCidCidMember(in));
				break;
			case CID_CID_STR:
				refsetMembers.add(new ERefsetCidCidStrMember(in));
				break;
			case INT:
				refsetMembers.add(new ERefsetIntMember(in));
				break;
			case STR:
				refsetMembers.add(new ERefsetStrMember(in));
				break;
			case CID_INT:
				refsetMembers.add(new ERefsetCidIntMember(in));
				break;
			case BOOLEAN:
				refsetMembers.add(new ERefsetBooleanMember(in));
				break;
			case CID_FLOAT:
				refsetMembers.add(new ERefsetCidFloatMember(in));
				break;
			case CID_LONG:
				refsetMembers.add(new ERefsetCidLongMember(in));
				break;
			case CID_STR:
				refsetMembers.add(new ERefsetCidStrMember(in));
				break;
				default:
					throw new UnsupportedOperationException("Can't handle refset type: " + type);
				}
			}
		}
	}

	public void writeExternal(DataOutput out) throws IOException {
		out.writeInt(dataVersion);
		conceptAttributes.writeExternal(out);		
		if (descriptions == null) {
			out.writeInt(0);
		} else {
			out.writeInt(descriptions.size());
			for (EDescription d: descriptions) {
				d.writeExternal(out);
			}
		}		
		if (relationships == null) {
			out.writeInt(0);
		} else {
			out.writeInt(relationships.size());
			for (ERelationship r: relationships) {
				r.writeExternal(out);
			}
		}		
		if (images == null) {
			out.writeInt(0);
		} else {
			out.writeInt(images.size());
			for (EImage img: images) {
				img.writeExternal(out);
			}
		}		
		if (refsetMembers == null) {
			out.writeInt(0);
		} else {
			out.writeInt(refsetMembers.size());
			for (ERefset r: refsetMembers) {
				r.getType().writeType(out);
				r.writeExternal(out);
			}
		}		
	}

	public List<EDescription> getDescriptions() {
		return descriptions;
	}

	public List<ERelationship> getRelationships() {
		return relationships;
	}

	public List<ERefset> getRefsetMembers() {
		return refsetMembers;
	}
	
	public EConceptAttributes getConceptAttributes() {
		return conceptAttributes;
	}
	
	
	
	public EConcept() {
		super();
	}

	public EConcept(I_GetConceptData c) throws IOException, TerminologyException {
		conceptAttributes = new EConceptAttributes(c.getConceptAttributes());
		conceptAttributes.convert(c.getIdentifier());
		relationships = new ArrayList<ERelationship>(c.getSourceRels().size());
		for (I_RelVersioned rel: c.getSourceRels()) {
			relationships.add(new ERelationship(rel));
		}
		descriptions = new ArrayList<EDescription>(c.getDescriptions().size());
		for (I_DescriptionVersioned desc: c.getDescriptions()) {
			descriptions.add(new EDescription(desc));
		}
		images = new ArrayList<EImage>(c.getImages().size());
		for (I_ImageVersioned img: c.getImages()) {
			images.add(new EImage(img));
		}
		Collection<I_ThinExtByRefVersioned> members = EComponent.getRefsetMembers(c.getNid());
		if (members != null) {
			refsetMembers = new ArrayList<ERefset>(members.size());
			for (I_ThinExtByRefVersioned m: members) {
				ERefset member = ERefset.convert(m);
				if (member != null) {
					refsetMembers.add(member);
				} else {
					AceLog.getAppLog().severe("Could not convert refset member: " + m +
							"\nfrom refset: " + c);
				}
			}
		}
	}

	public List<EImage> getImages() {
		return images;
	}
	
	public List<EVersion> getExtraVersionsList() {
		return null;
	}

}
