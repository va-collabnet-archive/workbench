package org.ihtsdo.tk.dto.concept.component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.identifier.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierLong;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierString;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;
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

public abstract class TkComponent<V extends TkRevision> extends TkRevision {

    private static final long serialVersionUID = 1;

    public UUID primordialUuid;

    public List<TkIdentifier> additionalIds;

    public List<TkRefsetAbstractMember<?>> annotations;

    public List<V> revisions;

    public TkComponent() {
        super();
    }

    public TkComponent(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        primordialUuid = new UUID(in.readLong(), in.readLong());
        short idVersionCount = in.readShort();
        assert idVersionCount < 500: "idVersionCount is: " + idVersionCount;
        if (idVersionCount > 0) {
            additionalIds = new ArrayList<TkIdentifier>(idVersionCount);
            for (int i = 0; i < idVersionCount; i++) {
                switch (IDENTIFIER_PART_TYPES.readType(in)) {
                case LONG:
                    additionalIds.add(new TkIdentifierLong(in, dataVersion));
                    break;
                case STRING:
                    additionalIds.add(new TkIdentifierString(in, dataVersion));
                    break;
                case UUID:
                    additionalIds.add(new TkIdentifierUuid(in, dataVersion));
                    break;
                default:
                    throw new UnsupportedOperationException();
                }
            }
        }
        short annotationCount = in.readShort();
        assert annotationCount < 500: "annotation count is: " + annotationCount;
        if (annotationCount > 0) {
            annotations = new ArrayList<TkRefsetAbstractMember<?>>(annotationCount);
            for (int i = 0; i < annotationCount; i++) {
                TK_REFSET_TYPE type = TK_REFSET_TYPE.readType(in);
                switch (type) {
                    case CID:
                        annotations.add(new TkRefsetCidMember(in, dataVersion));
                        break;
                    case CID_CID:
                        annotations.add(new TkRefsetCidCidMember(in, dataVersion));
                        break;
                    case MEMBER:
                        annotations.add(new TkRefsetMember(in, dataVersion));
                        break;
                    case CID_CID_CID:
                        annotations.add(new TkRefsetCidCidCidMember(in, dataVersion));
                        break;
                    case CID_CID_STR:
                        annotations.add(new TkRefsetCidCidStrMember(in, dataVersion));
                        break;
                    case INT:
                        annotations.add(new TkRefsetIntMember(in, dataVersion));
                        break;
                    case STR:
                        annotations.add(new TkRefsetStrMember(in, dataVersion));
                        break;
                    case CID_INT:
                        annotations.add(new TkRefsetCidIntMember(in, dataVersion));
                        break;
                    case BOOLEAN:
                        annotations.add(new TkRefsetBooleanMember(in, dataVersion));
                        break;
                    case CID_FLOAT:
                        annotations.add(new TkRefsetCidFloatMember(in, dataVersion));
                        break;
                    case CID_LONG:
                        annotations.add(new TkRefsetCidLongMember(in, dataVersion));
                        break;
                    case CID_STR:
                        annotations.add(new TkRefsetCidStrMember(in, dataVersion));
                        break;
                    case LONG:
                        annotations.add(new TkRefsetLongMember(in, dataVersion));
                        break;
                    default:
                        throw new UnsupportedOperationException("Can't handle refset type: " + type);
                }
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(primordialUuid.getMostSignificantBits());
        out.writeLong(primordialUuid.getLeastSignificantBits());
        if (additionalIds == null) {
            out.writeShort(0);
        } else {
            assert additionalIds.size() < 500: "additionalIds is: " + additionalIds.size();
            out.writeShort(additionalIds.size());
            for (TkIdentifier idv : additionalIds) {
                idv.getIdType().writeType(out);
                idv.writeExternal(out);
            }
        }
        if (annotations == null) {
             out.writeShort(0);
        } else {
        assert annotations.size() < 500: "annotation count is: " + annotations.size();
            out.writeShort(annotations.size());
            for (TkRefsetAbstractMember<?> r : annotations) {
                r.getType().writeType(out);
                r.writeExternal(out);
            }
        }
    }

    public int getIdComponentCount() {
        if (additionalIds == null) {
            return 1;
        }
        return additionalIds.size() + 1;
    }

    public List<TkIdentifier> getEIdentifiers() {
        List<TkIdentifier> ids;
        if (additionalIds != null) {
            ids = new ArrayList<TkIdentifier>(additionalIds.size() + 1);
            ids.addAll(additionalIds);
        } else {
            ids = new ArrayList<TkIdentifier>(1);
        }
        ids.add(new TkIdentifierUuid(this.primordialUuid));
        return ids;
    }

    public List<UUID> getUuids() {
        List<UUID> uuids = new ArrayList<UUID>();
        uuids.add(primordialUuid);
        if (additionalIds != null) {
            for (TkIdentifier idv : additionalIds) {
                if (TkIdentifierUuid.class.isAssignableFrom(idv.getClass())) {
                    uuids.add((UUID) idv.getDenotation());
                }
            }
        }
        return uuids;
    }

    public int getVersionCount() {
        List<? extends TkRevision> extraVersions = getRevisionList();
        if (extraVersions == null) {
            return 1;
        }
        return extraVersions.size() + 1;
    }

    public abstract List<? extends TkRevision> getRevisionList();

    public UUID getPrimordialComponentUuid() {
        return primordialUuid;
    }

    public List<TkIdentifier> getAdditionalIdComponents() {
        return additionalIds;
    }

    public void setAdditionalIdComponents(List<TkIdentifier> additionalIdComponents) {
        this.additionalIds = additionalIdComponents;
    }

    public List<V> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<V> revisions) {
        this.revisions = revisions;
    }

    public void setPrimordialComponentUuid(UUID primordialComponentUuid) {
        this.primordialUuid = primordialComponentUuid;
    }

    /**
     * Returns a string representation of the object.
     */
   @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(" primordialComponentUuid:");
        buff.append(this.primordialUuid);
        buff.append(" additionalIdComponents:");
        buff.append(this.additionalIds);
        buff.append(super.toString());
        buff.append(" annotations:");
        buff.append(this.annotations);
        buff.append(" Revisions:");
        buff.append(this.revisions);
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>EComponent</code>.
     *
     * @return a hash code value for this <tt>EComponent</tt>.
     */
   @Override
    public int hashCode() {
        return Arrays.hashCode(new int[] { getPrimordialComponentUuid().hashCode(),
        		statusUuid.hashCode(), pathUuid.hashCode(), (int) time, (int) (time >>> 32) });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EComponent</tt> object, and contains the same values, field by field,
     * as this <tt>EComponent</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkComponent.class.isAssignableFrom(obj.getClass())) {
            TkComponent<?> another = (TkComponent<?>) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare primordialComponentUuid
            if (!this.primordialUuid.equals(another.primordialUuid)) {
                return false;
            }
            // Compare additionalIdComponents
            if (this.additionalIds == null) {
                if (another.additionalIds == null) { // Equal!
                } else if (another.additionalIds.isEmpty()) { // Equal!
                } else {
                    return false;
                }
            } else if (!this.additionalIds.equals(another.additionalIds)) {
                return false;
            }
            // Compare extraVersions
            if (this.revisions == null) {
                if (another.revisions == null) { // Equal!
                } else if (another.revisions.isEmpty()) { // Equal!
                } else {
                    return false;
                }
            } else if (!this.revisions.equals(another.revisions)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }


   public List<TkRefsetAbstractMember<?>> getAnnotations() {
      return annotations;
   }

   public void setAnnotations(List<TkRefsetAbstractMember<?>> annotations) {
      this.annotations = annotations;
   }


}
