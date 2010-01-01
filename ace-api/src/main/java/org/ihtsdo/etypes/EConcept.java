package org.ihtsdo.etypes;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
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

public class EConcept extends EComponent implements Externalizable {
	public enum REFSET_TYPES {
		MEMBER(1, RefsetAuxiliary.Concept.MEMBER_TYPE), 
		CID(2, RefsetAuxiliary.Concept.CONCEPT_EXTENSION), 
		CID_CID(3, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION), 
		CID_CID_CID(4, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION), 
		CID_CID_STR(5, RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION),
		STR(6, RefsetAuxiliary.Concept.STRING_EXTENSION),
		INT(7, RefsetAuxiliary.Concept.INT_EXTENSION)
		;

		private int externalizedToken;
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
					nidTypeMap.put(uuidToNid(type.typeConcept.getUids()), type);
				}
			}
			return nidTypeMap.get(nid);
		}
		public void writeType(ObjectOutput output) throws IOException {
			output.writeByte(externalizedToken);
		}

		public static REFSET_TYPES readType(ObjectInput input)
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
			}
			throw new UnsupportedOperationException();
		}
	};

	private static final int dataVersion = 1;
	private EConceptAttributes conceptAttributes;
	private List<EDescription> descriptions;
	private List<ERelationship> relationships;
	private List<EImage> images;
	private List<ERefset> refsetMembers;

	public EConcept(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
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
				default:
					throw new UnsupportedOperationException("Can't handle refset type: " + type);
				}
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
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
	
	public EConcept(I_GetConceptData c) throws IOException, TerminologyException {
		convert(c.getIdentifier());
		conceptAttributes = new EConceptAttributes(c.getConceptAttributes());
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
		Collection<I_ThinExtByRefVersioned> members = getRefsetMembers(c.getNid());
		if (members != null) {
			refsetMembers = new ArrayList<ERefset>(members.size());
			for (I_ThinExtByRefVersioned m: members) {
				ERefset member = ERefset.convert(m);
				if (member != null) {
					refsetMembers.add(ERefset.convert(m));
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
}
