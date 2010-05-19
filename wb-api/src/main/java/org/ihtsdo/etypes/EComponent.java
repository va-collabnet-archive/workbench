package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.tapi.TerminologyException;

public abstract class EComponent<V extends ERevision> extends ERevision {

    public static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    public enum IDENTIFIER_PART_TYPES {
        LONG(1), STRING(2), UUID(3);

        private int externalPartTypeToken;

        IDENTIFIER_PART_TYPES(int externalPartTypeToken) {
            this.externalPartTypeToken = externalPartTypeToken;
        }

        public void writeType(DataOutput output) throws IOException {
            output.writeByte(externalPartTypeToken);
        }

        public static IDENTIFIER_PART_TYPES getType(Class<?> c) {
            if (Long.class.isAssignableFrom(c)) {
                return LONG;
            } else if (String.class.isAssignableFrom(c)) {
                return STRING;
            } else if (UUID.class.isAssignableFrom(c)) {
                return UUID;
            }
            throw new UnsupportedOperationException();
        }

        public static IDENTIFIER_PART_TYPES readType(DataInput input) throws IOException {
            byte typeByte = input.readByte();
            switch (typeByte) {
            case 1:
                return LONG;
            case 2:
                return STRING;
            case 3:
                return UUID;
            }
            throw new UnsupportedOperationException("Can't find byte: " + typeByte);
        }
    };

    public UUID primordialUuid;

    public List<EIdentifier> additionalIds;

    public List<V> revisions;

    public EComponent() {
        super();
    }

    public EComponent(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        int readDataVersion = in.readInt();
        if (readDataVersion != dataVersion) {
            throw new IOException("Unsupported dataVersion: " + readDataVersion);
        }
        primordialUuid = new UUID(in.readLong(), in.readLong());
        short idVersionCount = in.readShort();
        if (idVersionCount > 0) {
            additionalIds = new ArrayList<EIdentifier>(idVersionCount);
            for (int i = 0; i < idVersionCount; i++) {
                switch (IDENTIFIER_PART_TYPES.readType(in)) {
                case LONG:
                    additionalIds.add(new EIdentifierLong(in));
                    break;
                case STRING:
                    additionalIds.add(new EIdentifierString(in));
                    break;
                case UUID:
                    additionalIds.add(new EIdentifierUuid(in));
                    break;
                default:
                    throw new UnsupportedOperationException();
                }
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(dataVersion);
        out.writeLong(primordialUuid.getMostSignificantBits());
        out.writeLong(primordialUuid.getLeastSignificantBits());
        if (additionalIds == null) {
            out.writeShort(0);
        } else {
            out.writeShort(additionalIds.size());
            for (EIdentifier idv : additionalIds) {
                idv.getIdType().writeType(out);
                idv.writeExternal(out);
            }
        }
    }

    public void convert(I_Identify id) throws TerminologyException, IOException {
        boolean primordialWritten = false;
        int partCount = id.getMutableIdParts().size() - 1;
        if (partCount > 0) {
            additionalIds = new ArrayList<EIdentifier>(partCount);
            for (I_IdPart idp : id.getMutableIdParts()) {
                Object denotation = idp.getDenotation();
                switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
                case LONG:
                    additionalIds.add(new EIdentifierLong(idp));
                    break;
                case STRING:
                    additionalIds.add(new EIdentifierString(idp));
                    break;
                case UUID:
                    if (primordialWritten) {
                        additionalIds.add(new EIdentifierUuid(idp));
                    } else {
                        primordialUuid = (UUID) idp.getDenotation();
                        primordialWritten = true;
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
                }

            }
        } else {
            primordialUuid = (UUID) id.getUUIDs().get(0);
        }
    }

    public int getIdComponentCount() {
        if (additionalIds == null) {
            return 1;
        }
        return additionalIds.size() + 1;
    }

    public List<EIdentifier> getEIdentifiers() {
        List<EIdentifier> ids;
        if (additionalIds != null) {
            ids = new ArrayList<EIdentifier>(additionalIds.size() + 1);
            ids.addAll(additionalIds);
        } else {
            ids = new ArrayList<EIdentifier>(1);
        }
        ids.add(new EIdentifierUuid(this));
        return ids;
    }

    public List<UUID> getUuids() {
        List<UUID> uuids = new ArrayList<UUID>();
        uuids.add(primordialUuid);
        if (additionalIds != null) {
            for (EIdentifier idv : additionalIds) {
                if (EIdentifierUuid.class.isAssignableFrom(idv.getClass())) {
                    uuids.add((UUID) idv.getDenotation());
                }
            }
        }
        return uuids;
    }

    public int getVersionCount() {
        List<? extends ERevision> extraVersions = getRevisionList();
        if (extraVersions == null) {
            return 1;
        }
        return extraVersions.size() + 1;
    }

    public abstract List<? extends ERevision> getRevisionList();

    public UUID getPrimordialComponentUuid() {
        return primordialUuid;
    }

    public List<EIdentifier> getAdditionalIdComponents() {
        return additionalIds;
    }

    public void setAdditionalIdComponents(List<EIdentifier> additionalIdComponents) {
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
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(" primordialComponentUuid:");
        buff.append(this.primordialUuid);
        buff.append(" additionalIdComponents:");
        buff.append(this.additionalIds);
        buff.append(super.toString());
        buff.append(" Revisions:");
        buff.append(this.revisions);
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>EComponent</code>.
     * 
     * @return a hash code value for this <tt>EComponent</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
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
        if (EComponent.class.isAssignableFrom(obj.getClass())) {
            EComponent<?> another = (EComponent<?>) obj;

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
                } else if (another.additionalIds.size() == 0) { // Equal!
                } else {
                    return false;
                }
            } else if (!this.additionalIds.equals(another.additionalIds)) {
                return false;
            }
            // Compare extraVersions
            if (this.revisions == null) {
                if (another.revisions == null) { // Equal!
                } else if (another.revisions.size() == 0) { // Equal!
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

}
