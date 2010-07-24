package org.ihtsdo.tk.concept;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.concept.component.description.TkDescription;
import org.ihtsdo.tk.concept.component.media.TkMedia;
import org.ihtsdo.tk.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.concept.component.refset.Boolean.TkRefsetBooleanMember;
import org.ihtsdo.tk.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.concept.component.refset.cidcid.TkRefsetCidCidMember;
import org.ihtsdo.tk.concept.component.refset.cidcidcid.TkRefsetCidCidCidMember;
import org.ihtsdo.tk.concept.component.refset.cidcidflt.TkRefsetCidFloatMember;
import org.ihtsdo.tk.concept.component.refset.cidcidstr.TkRefsetCidCidStrMember;
import org.ihtsdo.tk.concept.component.refset.cidint.TkRefsetCidIntMember;
import org.ihtsdo.tk.concept.component.refset.cidlong.TkRefsetCidLongMember;
import org.ihtsdo.tk.concept.component.refset.cidstr.TkRefsetCidStrMember;
import org.ihtsdo.tk.concept.component.refset.integer.TkRefsetIntMember;
import org.ihtsdo.tk.concept.component.refset.member.TkRefsetMember;
import org.ihtsdo.tk.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.tk.concept.component.relationship.TkRelationship;


public class TkConcept {
    public static final long serialVersionUID = 1;

    public static final int dataVersion = 3;
    public TkConceptAttributes conceptAttributes;
    public List<TkDescription> descriptions;
    public List<TkRelationship> relationships;
    public List<TkMedia> images;
    public List<TkRefsetAbstractMember<?>> refsetMembers;
    public List<UUID> destRelUuidTypeUuids;
    public List<UUID> refsetUuidMemberUuidForConcept;
    public List<UUID> refsetUuidMemberUuidForDescriptions;
    public List<UUID> refsetUuidMemberUuidForRels;
    public List<UUID> refsetUuidMemberUuidForImages;
    public List<UUID> refsetUuidMemberUuidForRefsetMembers;
    public UUID primordialUuid;
    
    public TkConcept(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        int readDataVersion = in.readInt();
        if (readDataVersion > dataVersion) {
            throw new IOException("Unsupported dataVersion: " + readDataVersion);
        }
        if (readDataVersion == 1) {
            conceptAttributes = new TkConceptAttributes(in, readDataVersion);
            primordialUuid = conceptAttributes.primordialUuid;
        } else {
        	primordialUuid = new UUID(in.readLong(), in.readLong());
        	int attributeCount = in.readByte();
        	if (attributeCount == 1) {
                conceptAttributes = new TkConceptAttributes(in, readDataVersion);
        	}
        }
        int descCount = in.readInt();
        if (descCount > 0) {
            descriptions = new ArrayList<TkDescription>(descCount);
            for (int i = 0; i < descCount; i++) {
                descriptions.add(new TkDescription(in, readDataVersion));
            }
        }
        int relCount = in.readInt();
        if (relCount > 0) {
            relationships = new ArrayList<TkRelationship>(relCount);
            for (int i = 0; i < relCount; i++) {
                relationships.add(new TkRelationship(in, readDataVersion));
            }
        }
        int imgCount = in.readInt();
        if (imgCount > 0) {
            images = new ArrayList<TkMedia>(imgCount);
            for (int i = 0; i < imgCount; i++) {
                images.add(new TkMedia(in, readDataVersion));
            }
        }
        int refsetMemberCount = in.readInt();
        if (refsetMemberCount > 0) {
            refsetMembers = new ArrayList<TkRefsetAbstractMember<?>>(refsetMemberCount);
            for (int i = 0; i < refsetMemberCount; i++) {
                TK_REFSET_TYPE type = TK_REFSET_TYPE.readType(in);
                switch (type) {
                case CID:
                    refsetMembers.add(new TkRefsetCidMember(in, readDataVersion));
                    break;
                case CID_CID:
                    refsetMembers.add(new TkRefsetCidCidMember(in, readDataVersion));
                    break;
                case MEMBER:
                    refsetMembers.add(new TkRefsetMember(in, readDataVersion));
                    break;
                case CID_CID_CID:
                    refsetMembers.add(new TkRefsetCidCidCidMember(in, readDataVersion));
                    break;
                case CID_CID_STR:
                    refsetMembers.add(new TkRefsetCidCidStrMember(in, readDataVersion));
                    break;
                case INT:
                    refsetMembers.add(new TkRefsetIntMember(in, readDataVersion));
                    break;
                case STR:
                    refsetMembers.add(new TkRefsetStrMember(in, readDataVersion));
                    break;
                case CID_INT:
                    refsetMembers.add(new TkRefsetCidIntMember(in, readDataVersion));
                    break;
                case BOOLEAN:
                    refsetMembers.add(new TkRefsetBooleanMember(in, readDataVersion));
                    break;
                case CID_FLOAT:
                    refsetMembers.add(new TkRefsetCidFloatMember(in, readDataVersion));
                    break;
                case CID_LONG:
                    refsetMembers.add(new TkRefsetCidLongMember(in, readDataVersion));
                    break;
                case CID_STR:
                    refsetMembers.add(new TkRefsetCidStrMember(in, readDataVersion));
                    break;
                default:
                    throw new UnsupportedOperationException("Can't handle refset type: " + type);
                }
            }
        }
        int destRelNidTypeNidsCount = in.readInt();
        if (destRelNidTypeNidsCount > 0) {
        	destRelUuidTypeUuids = new ArrayList<UUID>(
        			destRelNidTypeNidsCount);
        	for (int i = 0; i < destRelNidTypeNidsCount; i++) {
        		destRelUuidTypeUuids.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForConceptCount = in.readInt();
        if (refsetUuidMemberUuidForConceptCount > 0) {
        	refsetUuidMemberUuidForConcept = new ArrayList<UUID>(
        			refsetUuidMemberUuidForConceptCount);
        	for (int i = 0; i < refsetUuidMemberUuidForConceptCount; i++) {
        		refsetUuidMemberUuidForConcept.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForDescsCount = in.readInt();
        if (refsetUuidMemberUuidForDescsCount > 0) {
        	refsetUuidMemberUuidForDescriptions = new ArrayList<UUID>(
        			refsetUuidMemberUuidForDescsCount);
        	for (int i = 0; i < refsetUuidMemberUuidForDescsCount; i++) {
        		refsetUuidMemberUuidForDescriptions.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForRelsCount = in.readInt();
        if (refsetUuidMemberUuidForRelsCount > 0) {
        	refsetUuidMemberUuidForRels = new ArrayList<UUID>(
        			refsetUuidMemberUuidForRelsCount);
        	for (int i = 0; i < refsetUuidMemberUuidForRelsCount; i++) {
        		refsetUuidMemberUuidForRels.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForImagesCount = in.readInt();
        if (refsetUuidMemberUuidForImagesCount > 0) {
        	refsetUuidMemberUuidForImages = new ArrayList<UUID>(
        			refsetUuidMemberUuidForImagesCount);
        	for (int i = 0; i < refsetUuidMemberUuidForImagesCount; i++) {
        		refsetUuidMemberUuidForImages.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
        int refsetUuidMemberUuidForRefsetMembersCount = in.readInt();
        if (refsetUuidMemberUuidForRefsetMembersCount > 0) {
        	refsetUuidMemberUuidForRefsetMembers = new ArrayList<UUID>(
        			refsetUuidMemberUuidForRefsetMembersCount);
        	for (int i = 0; i < refsetUuidMemberUuidForRefsetMembersCount; i++) {
        		refsetUuidMemberUuidForRefsetMembers.add(new UUID(in.readLong(), in.readLong()));
        	}
        }
    }

    public void writeExternal(DataOutput out) throws IOException {
        out.writeInt(dataVersion);
        if (primordialUuid == null) {
        	primordialUuid = conceptAttributes.primordialUuid;
        }
    	out.writeLong(primordialUuid.getMostSignificantBits());
    	out.writeLong(primordialUuid.getLeastSignificantBits());
    	if (conceptAttributes == null) {
    		out.writeByte(0);
    	} else {
    		out.writeByte(1);
            conceptAttributes.writeExternal(out);
    	}
        if (descriptions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(descriptions.size());
            for (TkDescription d : descriptions) {
                d.writeExternal(out);
            }
        }
        if (relationships == null) {
            out.writeInt(0);
        } else {
            out.writeInt(relationships.size());
            for (TkRelationship r : relationships) {
                r.writeExternal(out);
            }
        }
        if (images == null) {
            out.writeInt(0);
        } else {
            out.writeInt(images.size());
            for (TkMedia img : images) {
                img.writeExternal(out);
            }
        }
        if (refsetMembers == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetMembers.size());
            for (TkRefsetAbstractMember<?> r : refsetMembers) {
                r.getType().writeType(out);
                r.writeExternal(out);
            }
        }
        if (destRelUuidTypeUuids == null) {
            out.writeInt(0);
        } else {
            out.writeInt(destRelUuidTypeUuids.size());
            assert destRelUuidTypeUuids.size() % 2 == 0: 
            	"Illegal size: " + destRelUuidTypeUuids.size();
            for (UUID uuid : destRelUuidTypeUuids) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForConcept == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForConcept.size());
            assert refsetUuidMemberUuidForConcept.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForConcept.size();
            for (UUID uuid : refsetUuidMemberUuidForConcept) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForDescriptions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForDescriptions.size());
            assert refsetUuidMemberUuidForDescriptions.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForDescriptions.size();
            for (UUID uuid : refsetUuidMemberUuidForDescriptions) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForRels == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForRels.size());
            assert refsetUuidMemberUuidForRels.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForRels.size();
            for (UUID uuid : refsetUuidMemberUuidForRels) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForImages == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForImages.size());
            assert refsetUuidMemberUuidForImages.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForImages.size();
            for (UUID uuid : refsetUuidMemberUuidForImages) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
        if (refsetUuidMemberUuidForRefsetMembers == null) {
            out.writeInt(0);
        } else {
            out.writeInt(refsetUuidMemberUuidForRefsetMembers.size());
            assert refsetUuidMemberUuidForRefsetMembers.size() % 2 == 0: 
            	"Illegal size: " + refsetUuidMemberUuidForRefsetMembers.size();
            for (UUID uuid : refsetUuidMemberUuidForRefsetMembers) {
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
            }
        }
    }

    public List<TkDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<TkDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public List<TkRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<TkRelationship> relationships) {
        this.relationships = relationships;
    }

    public List<TkRefsetAbstractMember<?>> getRefsetMembers() {
        return refsetMembers;
    }

    public void setRefsetMembers(List<TkRefsetAbstractMember<?>> refsetMembers) {
        this.refsetMembers = refsetMembers;
    }
    
    public TkConceptAttributes getConceptAttributes() {
        return conceptAttributes;
    }

    public TkConcept() {
        super();
    }

    public List<TkMedia> getImages() {
        return images;
    }

    public void setImages(List<TkMedia> images) {
        this.images = images;
    }

    public void setConceptAttributes(TkConceptAttributes conceptAttributes) {
        this.conceptAttributes = conceptAttributes;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append("\n   primordial UUID: ");
        buff.append(this.primordialUuid);
        buff.append("\n   ConceptAttributes: \n\t");
        buff.append(this.conceptAttributes);
        buff.append("\n   Descriptions: \n\t");
        buff.append(this.descriptions);
        buff.append("\n   Relationships: \n\t");
        buff.append(this.relationships);
        buff.append("\n   RefsetMembers: \n\t");
        buff.append(this.refsetMembers);
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
        buff.append("\n   refsetUuidMemberUuidForImages: \n\t");
        buff.append(this.refsetUuidMemberUuidForImages);
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
        return this.conceptAttributes.primordialUuid.hashCode();
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
        if (TkConcept.class.isAssignableFrom(obj.getClass())) {
            TkConcept another = (TkConcept) obj;
            
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

	public List<UUID> getRefsetUuidMemberUuidForImages() {
		return refsetUuidMemberUuidForImages;
	}

	public void setRefsetUuidMemberUuidForImages(
			List<UUID> refsetUuidMemberUuidForImages) {
		this.refsetUuidMemberUuidForImages = refsetUuidMemberUuidForImages;
	}

	public List<UUID> getRefsetUuidMemberUuidForRefsetMembers() {
		return refsetUuidMemberUuidForRefsetMembers;
	}

	public void setRefsetUuidMemberUuidForRefsetMembers(
			List<UUID> refsetUuidMemberUuidForRefsetMembers) {
		this.refsetUuidMemberUuidForRefsetMembers = refsetUuidMemberUuidForRefsetMembers;
	}

	public UUID getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(UUID primordialUuid) {
		this.primordialUuid = primordialUuid;
	}



}
