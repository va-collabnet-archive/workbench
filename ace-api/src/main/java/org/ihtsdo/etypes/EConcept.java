package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class EConcept {
    public static final long serialVersionUID = 1;

    /**
     * CID = Component IDentifier
     * 
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
        LONG(13, RefsetAuxiliary.Concept.LONG_EXTENSION);

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
                for (REFSET_TYPES type : REFSET_TYPES.values()) {
                    try {
                        type.typeNid = EComponent.uuidToNid(type.typeConcept.getUids());
                        nidTypeMap.put(type.typeNid, type);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (nidTypeMap.containsKey(nid)) {
                return nidTypeMap.get(nid);
            } else {
            	I_GetConceptData typeConcept = LocalVersionedTerminology.get().getConcept(nid);
            	throw new TerminologyException("Unknown refset type: " + nid + 
            			" concept: " + typeConcept);
            }
        }

        public void writeType(DataOutput output) throws IOException {
            output.writeByte(externalizedToken);
        }

        public static REFSET_TYPES readType(DataInput input) throws IOException {
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
    protected List<ERefset<?>> refsetMembers;
    protected List<UUID> destRelUuidTypeUuids;
    protected List<UUID> refsetUuidMemberUuidForConcept;
    protected List<UUID> refsetUuidMemberUuidForDescriptions;
    protected List<UUID> refsetUuidMemberUuidForRels;

    public EConcept(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
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
            refsetMembers = new ArrayList<ERefset<?>>(refsetMemberCount);
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
        int destRelNidTypeNidsCount = in.readInt();
        if (destRelNidTypeNidsCount > 0) {
        	destRelUuidTypeUuids = new ArrayList<UUID>(destRelNidTypeNidsCount);
        	for (int i = 0; i < destRelNidTypeNidsCount; i++) {
        		destRelUuidTypeUuids.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForConceptCount = in.readInt();
        if (refsetUuidMemberUuidForConceptCount > 0) {
        	refsetUuidMemberUuidForConcept = new ArrayList<UUID>(refsetUuidMemberUuidForConceptCount);
        	for (int i = 0; i < refsetUuidMemberUuidForConceptCount; i++) {
        		refsetUuidMemberUuidForConcept.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForDescsCount = in.readInt();
        if (refsetUuidMemberUuidForDescsCount > 0) {
        	refsetUuidMemberUuidForDescriptions = new ArrayList<UUID>(refsetUuidMemberUuidForDescsCount);
        	for (int i = 0; i < refsetUuidMemberUuidForDescsCount; i++) {
        		refsetUuidMemberUuidForDescriptions.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForRelsCount = in.readInt();
        if (refsetUuidMemberUuidForRelsCount > 0) {
        	refsetUuidMemberUuidForRels = new ArrayList<UUID>(refsetUuidMemberUuidForRelsCount);
        	for (int i = 0; i < refsetUuidMemberUuidForRelsCount; i++) {
        		refsetUuidMemberUuidForRels.add(new UUID(in.readLong(), in.readLong()));
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
            for (EDescription d : descriptions) {
                d.writeExternal(out);
            }
        }
        if (relationships == null) {
            out.writeInt(0);
        } else {
            out.writeInt(relationships.size());
            for (ERelationship r : relationships) {
                r.writeExternal(out);
            }
        }
        if (images == null) {
            out.writeInt(0);
        } else {
            out.writeInt(images.size());
            for (EImage img : images) {
                img.writeExternal(out);
            }
        }
        if (refsetMembers == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetMembers.size());
            for (ERefset<?> r : refsetMembers) {
                r.getType().writeType(out);
                r.writeExternal(out);
            }
        }
        if (destRelUuidTypeUuids == null) {
            out.writeInt(0);
        } else {
            out.writeInt(destRelUuidTypeUuids.size());
            for (UUID uuid : destRelUuidTypeUuids) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForConcept == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForConcept.size());
            for (UUID uuid : refsetUuidMemberUuidForConcept) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForDescriptions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForDescriptions.size());
            for (UUID uuid : refsetUuidMemberUuidForDescriptions) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForRels == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForRels.size());
            for (UUID uuid : refsetUuidMemberUuidForRels) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
    }

    public List<EDescription> getDescriptions() {
        return descriptions;
    }

    public List<ERelationship> getRelationships() {
        return relationships;
    }

    public List<ERefset<?>> getRefsetMembers() {
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
        for (I_RelVersioned rel : c.getSourceRels()) {
            relationships.add(new ERelationship(rel));
        }
        descriptions = new ArrayList<EDescription>(c.getDescriptions().size());
        for (I_DescriptionVersioned desc : c.getDescriptions()) {
            descriptions.add(new EDescription(desc));
        }
        images = new ArrayList<EImage>(c.getImages().size());
        for (I_ImageVersioned img : c.getImages()) {
            EImage eImage = new EImage(img);
            if (eImage.time == Long.MIN_VALUE) {
                eImage.time = this.conceptAttributes.time;
                // Fixup for a data issue.
            }
            images.add(eImage);
        }
        Collection<I_ThinExtByRefVersioned> members = EComponent.getRefsetMembers(c.getNid());
        if (members != null) {
            refsetMembers = new ArrayList<ERefset<?>>(members.size());
            for (I_ThinExtByRefVersioned m : members) {
                ERefset<?> member = ERefset.convert(m);
                if (member != null) {
                    refsetMembers.add(member);
                } else {
                    AceLog.getAppLog().severe("Could not convert refset member: " + m + "\nfrom refset: " + c);
                }
            }
        }

        Collection<I_ThinExtByRefVersioned> conceptMembers = EComponent.getRefsetMembersForComponent(c.getNid());
        if (conceptMembers != null) {
        	ArrayList<ERefset<?>> refsetMemberForComponent = new ArrayList<ERefset<?>>(conceptMembers.size());
        	refsetUuidMemberUuidForConcept = new ArrayList<UUID>(refsetMemberForComponent.size() * 2);
        	for (I_ThinExtByRefVersioned m : members) {
                UUID refsetUuid = EVersion.nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForConcept.add(refsetUuid);
                UUID memberUuid = EVersion.nidToUuid(m.getNid());
                refsetUuidMemberUuidForConcept.add(memberUuid);
            }
        }

        Collection<I_ThinExtByRefVersioned> descriptionMembers = new ArrayList<I_ThinExtByRefVersioned>();
        for (I_DescriptionVersioned desc: c.getDescriptions()) {
        	Collection<I_ThinExtByRefVersioned> componentMembers = EComponent.getRefsetMembersForComponent(desc.getNid());
        	if (componentMembers != null) {
        		descriptionMembers.addAll(componentMembers);
        	}
        }
        if (descriptionMembers.size() > 0) {
        	refsetUuidMemberUuidForDescriptions = new ArrayList<UUID>(descriptionMembers.size() * 2);
        	for (I_ThinExtByRefVersioned m : descriptionMembers) {
                UUID refsetUuid = EVersion.nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForDescriptions.add(refsetUuid);
                UUID memberUuid = EVersion.nidToUuid(m.getNid());
                refsetUuidMemberUuidForDescriptions.add(memberUuid);
            }
        }

        Collection<I_ThinExtByRefVersioned> relMembers = new ArrayList<I_ThinExtByRefVersioned>();
        for (I_RelVersioned r: c.getSourceRels()) {
        	Collection<I_ThinExtByRefVersioned> componentMembers = EComponent.getRefsetMembersForComponent(r.getNid());
        	if (componentMembers != null) {
        		relMembers.addAll(componentMembers);
        	}
        }
        if (relMembers.size() > 0) {
        	refsetUuidMemberUuidForRels = new ArrayList<UUID>(relMembers.size() * 2);
        	for (I_ThinExtByRefVersioned m : relMembers) {
                UUID refsetUuid = EVersion.nidToUuid(m.getRefsetId());
                refsetUuidMemberUuidForRels.add(refsetUuid);
                UUID memberUuid = EVersion.nidToUuid(m.getNid());
                refsetUuidMemberUuidForRels.add(memberUuid);
            }
        }
        
        destRelUuidTypeUuids = new ArrayList<UUID>();
        for (I_RelVersioned r: c.getDestRels()) {
            UUID relUuid = EVersion.nidToUuid(r.getNid());
            HashSet<UUID> typesAdded = new HashSet<UUID>();
            for (I_RelPart p: r.getMutableParts()) {
                UUID typeUuid = EVersion.nidToUuid(p.getTypeId());
                if (!typesAdded.contains(typeUuid)) {
                    destRelUuidTypeUuids.add(relUuid);            	
                    destRelUuidTypeUuids.add(typeUuid);
                    typesAdded.add(typeUuid);
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

    public void setConceptAttributes(EConceptAttributes conceptAttributes) {
        this.conceptAttributes = conceptAttributes;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName());
        buff.append(": ");
        buff.append("\n   Descriptions: \n\t");
        buff.append(this.descriptions);
        buff.append("\n   Relationships: \n\t");
        buff.append(this.relationships);
        buff.append("\n   RefsetMembers: \n\t");
        buff.append(this.refsetMembers);
        buff.append("\n   ConceptAttributes: \n\t");
        buff.append(this.conceptAttributes);
        buff.append("\n   Images: \n\t");
        buff.append(this.images);
        buff.append("\n   destRelUuidTypeUuids: \n\t");
        buff.append(this.destRelUuidTypeUuids);
        buff.append("\n   refsetUuidMemberUuidForConcept: \n\t");
        buff.append(this.refsetUuidMemberUuidForConcept);
        buff.append("\n   refsetUuidMemberUuidForDescriptions: \n\t");
        buff.append(this.refsetUuidMemberUuidForDescriptions);
        buff.append("\n   refsetUuidMemberUuidForRels: \n\t");
        buff.append(this.refsetUuidMemberUuidForRels);
        return buff.toString();
    }

	public List<UUID> getDestRelUuidTypeUuids() {
		return destRelUuidTypeUuids;
	}

	public void setDestRelUuidTypeUuids(List<UUID> destRelOriginUuidTypeUuids) {
		this.destRelUuidTypeUuids = destRelOriginUuidTypeUuids;
	}

	public List<UUID> getRefsetUuidMemberUuidForConcept() {
		return refsetUuidMemberUuidForConcept;
	}

	public void setRefsetUuidMemberUuidForConcept(
			List<UUID> refsetUuidMemberUuidForConcept) {
		this.refsetUuidMemberUuidForConcept = refsetUuidMemberUuidForConcept;
	}

	public List<UUID> getRefsetUuidMemberUuidForDescriptions() {
		return refsetUuidMemberUuidForDescriptions;
	}

	public void setRefsetUuidMemberUuidForDescriptions(
			List<UUID> refsetUuidMemberUuidForDescriptions) {
		this.refsetUuidMemberUuidForDescriptions = refsetUuidMemberUuidForDescriptions;
	}

	public List<UUID> getRefsetUuidMemberUuidForRels() {
		return refsetUuidMemberUuidForRels;
	}

	public void setRefsetUuidMemberUuidForRels(
			List<UUID> refsetUuidMemberUuidForRels) {
		this.refsetUuidMemberUuidForRels = refsetUuidMemberUuidForRels;
	}
	
    /**
     * Returns a hash code for this <code>EConcept</code>.
     * 
     * @return a hash code value for this <tt>EConcept</tt>.
     */
    public int hashCode() {
        return this.conceptAttributes.primordialComponentUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EConcept</tt> object, and contains the same values, field by field, 
     * as this <tt>EConcept</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (EConcept.class.isAssignableFrom(obj.getClass())) {
            EConcept another = (EConcept) obj;
            
            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare ConceptAttributes
            if (this.conceptAttributes == null) {
                if (this.conceptAttributes != another.conceptAttributes)
                    return false;
            } else if (!this.conceptAttributes.equals(another.conceptAttributes)) {
                return false;
            }
            // Compare Descriptions
            if (this.descriptions == null) {
                if (another.descriptions == null) { // Equal!
                } else if (another.descriptions.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.descriptions.equals(another.descriptions)) {
                return false;
            }
            // Compare Relationships
            if (this.relationships == null) {
                if (another.relationships == null) { // Equal!
                } else if (another.relationships.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.relationships.equals(another.relationships)) {
                return false;
            }
            // Compare Images
            if (this.images == null) {
                if (another.images == null) { // Equal!
                } else if (another.images.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.images.equals(another.images)) {
                return false;
            }
            // Compare Refset Members
            if (this.refsetMembers == null) {
                if (another.refsetMembers == null) { // Equal!
                } else if (another.refsetMembers.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.refsetMembers.equals(another.refsetMembers)) {
                return false;
            }
            // Compare destRelUuidTypeUuids
            if (this.destRelUuidTypeUuids == null) {
                if (another.destRelUuidTypeUuids == null) { // Equal!
                } else if (another.destRelUuidTypeUuids.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.destRelUuidTypeUuids.equals(another.destRelUuidTypeUuids)) {
                return false;
            }
            // Compare refsetUuidMemberUuidForConcept
            if (this.refsetUuidMemberUuidForConcept == null) {
                if (another.refsetUuidMemberUuidForConcept == null) { // Equal!
                } else if (another.refsetUuidMemberUuidForConcept.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.refsetUuidMemberUuidForConcept.equals(another.refsetUuidMemberUuidForConcept)) {
                return false;
            }
            // Compare refsetUuidMemberUuidForDescriptions
            if (this.refsetUuidMemberUuidForDescriptions == null) {
                if (another.refsetUuidMemberUuidForDescriptions == null) { // Equal!
                } else if (another.refsetUuidMemberUuidForDescriptions.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.refsetUuidMemberUuidForDescriptions.equals(another.refsetUuidMemberUuidForDescriptions)) {
                return false;
            }
            // Compare refsetUuidMemberUuidForRels
            if (this.refsetUuidMemberUuidForRels == null) {
                if (another.refsetUuidMemberUuidForRels == null) { // Equal!
                } else if (another.refsetUuidMemberUuidForRels.size() == 0) { // Equal!
                } else
                    return false;
            } else if (!this.refsetUuidMemberUuidForRels.equals(another.refsetUuidMemberUuidForRels)) {
                return false;
            }

            // If none of the previous comparisons fail, the objects must be equal
            return true;
        }
        return false;
    }

}
