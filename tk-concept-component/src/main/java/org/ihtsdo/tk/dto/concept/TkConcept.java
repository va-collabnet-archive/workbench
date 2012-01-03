package org.ihtsdo.tk.dto.concept;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.refset.Boolean.TkRefsetBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcid.TkRefsetCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidcid.TkRefsetCidCidCidMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidcidstr.TkRefsetCidCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidint.TkRefsetCidIntMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongMember;
import org.ihtsdo.tk.dto.concept.component.refset.cidstr.TkRefsetCidStrMember;
import org.ihtsdo.tk.dto.concept.component.refset.integer.TkRefsetIntMember;
import org.ihtsdo.tk.dto.concept.component.refset.member.TkRefsetMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_cnid.RefexCnidCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_str.RefexCnidCnidStrVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_float.RefexCnidFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_int.RefexCnidIntVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_long.RefexCnidLongVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_str.RefexCnidStrVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_member.RefexMemberVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refset.array.bytearray.TkRefsetArrayOfBytearrayMember;

public class TkConcept {

    public static final String PADDING = "     ";
    public static final int dataVersion = 7;
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public boolean annotationStyleRefex = false;
    public TkConceptAttributes conceptAttributes;
    public List<TkDescription> descriptions;
    public List<TkMedia> media;
    public UUID primordialUuid;
    public List<TkRefsetAbstractMember<?>> refsetMembers;
    public List<TkRelationship> relationships;

    //~--- constructors --------------------------------------------------------
    public TkConcept() {
        super();
    }

    public TkConcept(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public TkConcept(ConceptChronicleBI c) throws IOException {
        conceptAttributes = new TkConceptAttributes(c.getConAttrs());
        relationships = new ArrayList<TkRelationship>(c.getRelsOutgoing().size());

        for (RelationshipChronicleBI rel : c.getRelsOutgoing()) {
            relationships.add(new TkRelationship(rel));
        }

        descriptions = new ArrayList<TkDescription>(c.getDescs().size());

        for (DescriptionChronicleBI desc : c.getDescs()) {
            descriptions.add(new TkDescription(desc));
        }

        media = new ArrayList<TkMedia>(c.getMedia().size());

        for (MediaChronicleBI mediaChronicle : c.getMedia()) {
            TkMedia tkMedia = new TkMedia(mediaChronicle);
            media.add(tkMedia);
        }

        if (!c.isAnnotationStyleRefex()) {
            Collection<? extends RefexChronicleBI> members = c.getRefsetMembers();

            if (members != null) {
                refsetMembers = new ArrayList<TkRefsetAbstractMember<?>>(members.size());

                for (RefexChronicleBI m : members) {
                    TkRefsetAbstractMember<?> member = convertRefex(m);

                    if (member != null) {
                        refsetMembers.add(member);
                    } else {
                        throw new IOException("Could not convert refset member: " + m + "\nfrom refset: " + c);
                    }
                }
            }
        }
    }

    public static TkRefsetAbstractMember<?> convertRefex(RefexChronicleBI<?> m) throws IOException {
        if (m.getPrimordialVersion() instanceof RefexCnidCnidCnidVersionBI) {
            return new TkRefsetCidCidCidMember((RefexCnidCnidCnidVersionBI) m);
        } else if (m.getPrimordialVersion() instanceof RefexCnidCnidStrVersionBI) {
            return new TkRefsetCidCidStrMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexCnidCnidVersionBI) {
            return new TkRefsetCidCidMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexCnidFloatVersionBI) {
            return new TkRefsetCidFloatMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexCnidIntVersionBI) {
            return new TkRefsetCidIntMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexCnidLongVersionBI) {
            return new TkRefsetCidLongMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexCnidStrVersionBI) {
            return new TkRefsetCidStrMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexCnidVersionBI) {
            return new TkRefsetCidMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexIntVersionBI) {
            return new TkRefsetIntMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexStrVersionBI) {
            return new TkRefsetStrMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexLongVersionBI) {
            return new TkRefsetLongMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexBooleanVersionBI) {
            return new TkRefsetBooleanMember(m);
        } else if (m.getPrimordialVersion() instanceof RefexMemberVersionBI) {
            return new TkRefsetMember(m);
        } else {
            throw new UnsupportedOperationException("Cannot handle: " + m);
        }
    }

    public TkConcept(TkConcept another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        super();
        this.annotationStyleRefex = another.annotationStyleRefex;

        if (another.conceptAttributes != null) {
            this.conceptAttributes = another.conceptAttributes.makeConversion(conversionMap, offset, mapAll);
        }

        if (another.descriptions != null) {
            this.descriptions = new ArrayList<TkDescription>(another.descriptions.size());

            for (TkDescription d : another.descriptions) {
                this.descriptions.add(d.makeConversion(conversionMap, offset, mapAll));
            }
        }

        if (another.media != null) {
            this.media = new ArrayList<TkMedia>(another.media.size());

            for (TkMedia d : another.media) {
                this.media.add(d.makeConversion(conversionMap, offset, mapAll));
            }
        }

        this.primordialUuid = conversionMap.get(another.primordialUuid);

        if (another.refsetMembers != null) {
            this.refsetMembers = new ArrayList<TkRefsetAbstractMember<?>>(another.refsetMembers.size());

            for (TkRefsetAbstractMember<?> d : another.refsetMembers) {
                this.refsetMembers.add((TkRefsetAbstractMember<?>) d.makeConversion(conversionMap, offset,
                        mapAll));
            }
        }

        if (another.relationships != null) {
            this.relationships = new ArrayList<TkRelationship>(another.relationships.size());

            for (TkRelationship d : another.relationships) {
                this.relationships.add(d.makeConversion(conversionMap, offset, mapAll));
            }
        }
    }

    public TkConcept(ConceptVersionBI another, NidBitSetBI exclusions, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate vc)
            throws IOException, ContradictionException {
        this(another, exclusions, conversionMap,
                offset, mapAll, vc, vc, vc);
    }

    public TkConcept(ConceptVersionBI another, NidBitSetBI exclusions, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll,
            ViewCoordinate conceptVc,
            ViewCoordinate descVc,
            ViewCoordinate relVc)
            throws IOException, ContradictionException {
        super();
        this.primordialUuid = conversionMap.get(another.getPrimUuid());
        this.annotationStyleRefex = another.isAnnotationStyleRefex();
        this.conceptAttributes = new TkConceptAttributes(another.getConAttrsActive(), exclusions,
                conversionMap, offset, mapAll, conceptVc);

        Collection<? extends DescriptionVersionBI> activeDescriptions =
                another.getChronicle().getVersion(descVc).getDescsActive();

        if (activeDescriptions != null) {
            this.descriptions = new ArrayList<TkDescription>(activeDescriptions.size());
            nextDescription:
            for (DescriptionVersionBI d : activeDescriptions) {
                for (int nid : d.getAllNidsForVersion()) {
                    if (exclusions.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                        continue nextDescription;
                    } else if (Ts.get().getComponent(nid).getVersions(descVc).isEmpty()) {
                        continue nextDescription;
                    }
                }

                this.descriptions.add(new TkDescription(d, exclusions, conversionMap, offset, mapAll, descVc));
            }
        }

        Collection<? extends MediaVersionBI> activeMedia = another.getMediaActive();

        if (activeMedia != null) {
            this.media = new ArrayList<TkMedia>(activeMedia.size());
            nextMedia:
            for (MediaVersionBI m : activeMedia) {
                for (int nid : m.getAllNidsForVersion()) {
                    if (exclusions.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                        continue nextMedia;
                    } else if (Ts.get().getComponent(nid).getVersions(conceptVc).isEmpty()) {
                        continue nextMedia;
                    }
                }

                this.media.add(new TkMedia(m, exclusions, conversionMap, offset, mapAll, conceptVc));
            }
        }

        Collection<? extends RefexVersionBI<?>> activeRefsetMembers = another.getRefsetMembersActive();

        if (activeRefsetMembers != null) {
            this.refsetMembers = new ArrayList<TkRefsetAbstractMember<?>>(activeRefsetMembers.size());
            nextRefsetMember:
            for (RefexVersionBI rxm : activeRefsetMembers) {
                for (int nid : rxm.getAllNidsForVersion()) {
                    if (exclusions.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                        continue nextRefsetMember;
                    } else if (Ts.get().getComponent(nid).getVersions(conceptVc).isEmpty()) {
                        continue nextRefsetMember;
                    }
                }

                this.refsetMembers.add(rxm.getTkRefsetMemberActiveOnly(conceptVc, exclusions, conversionMap));
            }
        }

        Collection<? extends RelationshipVersionBI> rels = another.getChronicle().getVersion(relVc).getRelsOutgoingActive();

        if (rels != null) {
            this.relationships = new ArrayList<TkRelationship>(rels.size());
            nextRel:
            for (RelationshipVersionBI rel : rels) {
                int destNid = rel.getDestinationNid();
                for (int nid : rel.getAllNidsForVersion()) {
                   if (exclusions.isMember(nid) || (Ts.get().getComponent(nid) == null)) {
                        continue nextRel;
                    } else if (Ts.get().getComponent(nid).getVersions(relVc).isEmpty()) {
                        if (nid == destNid) {
                            if (Ts.get().getComponent(destNid).getVersions(conceptVc).isEmpty()) {
                                continue nextRel;
                            }
                        } else {
                            continue nextRel;
                        }
                    }
                }
                this.relationships.add(new TkRelationship(rel, exclusions, conversionMap, offset, mapAll, relVc));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>EConcept</tt> object, and contains the same values, field by field, as
     * this <tt>EConcept</tt>.
     *
     * @param obj the object to compare with.
     * @return
     * <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkConcept.class.isAssignableFrom(obj.getClass())) {
            TkConcept another = (TkConcept) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare ConceptAttributes
            if (this.conceptAttributes == null) {
                if (this.conceptAttributes != another.conceptAttributes) {
                    return false;
                }
            } else if (!this.conceptAttributes.equals(another.conceptAttributes)) {
                return false;
            }

            // Compare Descriptions
            if (this.descriptions == null) {
                if (another.descriptions == null) {              // Equal!
                } else if (another.descriptions.isEmpty()) {     // Equal!
                } else {
                    return false;
                }
            } else if (!this.descriptions.equals(another.descriptions)) {
                return false;
            }

            // Compare Relationships
            if (this.relationships == null) {
                if (another.relationships == null) {             // Equal!
                } else if (another.relationships.isEmpty()) {    // Equal!
                } else {
                    return false;
                }
            } else if (!this.relationships.equals(another.relationships)) {
                return false;
            }

            // Compare Images
            if (this.media == null) {
                if (another.media == null) {                     // Equal!
                } else if (another.media.isEmpty()) {            // Equal!
                } else {
                    return false;
                }
            } else if (!this.media.equals(another.media)) {
                return false;
            }

            // Compare Refset Members
            if (this.refsetMembers == null) {
                if (another.refsetMembers == null) {             // Equal!
                } else if (another.refsetMembers.isEmpty()) {    // Equal!
                } else {
                    return false;
                }
            } else if (!this.refsetMembers.equals(another.refsetMembers)) {
                return false;
            }

            // If none of the previous comparisons fail, the objects must be equal
            return true;
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>EConcept</code>.
     *
     * @return a hash code value for this <tt>EConcept</tt>.
     */
    @Override
    public int hashCode() {
        return this.conceptAttributes.primordialUuid.hashCode();
    }

    public final void readExternal(DataInput in) throws IOException, ClassNotFoundException {
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
            media = new ArrayList<TkMedia>(imgCount);

            for (int i = 0; i < imgCount; i++) {
                media.add(new TkMedia(in, readDataVersion));
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

                    case LONG:
                        refsetMembers.add(new TkRefsetLongMember(in, readDataVersion));

                        break;
                    case ARRAY_BYTEARRAY:
                        refsetMembers.add(new TkRefsetArrayOfBytearrayMember(in, dataVersion));
                        break;
                    default:
                        throw new UnsupportedOperationException("Can't handle refset type: " + type);
                }
            }
        }

        if (readDataVersion < 4) {
            in.readInt();    // destRelNidTypeNidsCount
            in.readInt();    // refsetUuidMemberUuidForConceptCount
            in.readInt();    // refsetUuidMemberUuidForDescsCount
            in.readInt();    // refsetUuidMemberUuidForRelsCount
            in.readInt();    // refsetUuidMemberUuidForImagesCount
            in.readInt();    // refsetUuidMemberUuidForRefsetMembersCount
        }

        if (readDataVersion >= 5) {
            annotationStyleRefex = in.readBoolean();
        } else {
            annotationStyleRefex = false;
        }
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName());
        buff.append(": \n   primordial UUID: ");
        buff.append(TkRevision.informAboutUuid(this.primordialUuid));
        buff.append("\n   ConceptAttributes: \n");
        buff.append(PADDING);

        if (this.conceptAttributes == null) {
            buff.append(PADDING + "none\n");
        } else {
            buff.append(this.conceptAttributes);
            buff.append("\n");
        }

        buff.append("\n   Descriptions: \n");

        if (this.descriptions == null) {
            buff.append(PADDING + "none\n");
        } else {
            for (TkDescription d : this.descriptions) {
                buff.append(PADDING);
                buff.append(d);
                buff.append("\n");
            }
        }

        buff.append("\n   Relationships: \n");

        if (this.relationships == null) {
            buff.append(PADDING + "none\n");
        } else {
            for (TkRelationship r : this.relationships) {
                buff.append(PADDING);
                buff.append(r);
                buff.append("\n");
            }
        }

        buff.append("\n   RefsetMembers: \n");

        if (this.refsetMembers == null) {
            buff.append(PADDING + "none\n");
        } else {
            for (TkRefsetAbstractMember<?> r : this.refsetMembers) {
                buff.append(PADDING);
                buff.append(r);
                buff.append("\n");
            }
        }

        buff.append("\n   Media: \n");

        if (this.media == null) {
            buff.append(PADDING + "none");
        } else {
            for (TkMedia m : this.media) {
                buff.append(PADDING);
                buff.append(m);
                buff.append("\n");
            }
        }

        return buff.toString();
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

        if (media == null) {
            out.writeInt(0);
        } else {
            out.writeInt(media.size());

            for (TkMedia img : media) {
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

        out.writeBoolean(annotationStyleRefex);
    }

    //~--- get methods ---------------------------------------------------------
    public TkConceptAttributes getConceptAttributes() {
        return conceptAttributes;
    }

    public List<TkDescription> getDescriptions() {
        return descriptions;
    }

    public List<TkMedia> getImages() {
        return media;
    }

    public UUID getPrimordialUuid() {
        return primordialUuid;
    }

    public List<TkRefsetAbstractMember<?>> getRefsetMembers() {
        return refsetMembers;
    }

    public List<TkRelationship> getRelationships() {
        return relationships;
    }

    public boolean isAnnotationStyleRefex() {
        return annotationStyleRefex;
    }

    //~--- set methods ---------------------------------------------------------
    public void setAnnotationStyleRefex(boolean annotationStyleRefex) {
        this.annotationStyleRefex = annotationStyleRefex;
    }

    public void setConceptAttributes(TkConceptAttributes conceptAttributes) {
        this.conceptAttributes = conceptAttributes;
    }

    public void setDescriptions(List<TkDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public void setImages(List<TkMedia> images) {
        this.media = images;
    }

    public void setPrimordialUuid(UUID primordialUuid) {
        this.primordialUuid = primordialUuid;
    }

    public void setRefsetMembers(List<TkRefsetAbstractMember<?>> refsetMembers) {
        this.refsetMembers = refsetMembers;
    }

    public void setRelationships(List<TkRelationship> relationships) {
        this.relationships = relationships;
    }
}
