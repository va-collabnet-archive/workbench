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

public abstract class EComponent<V extends EVersion> extends EVersion {

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

    public UUID primordialComponentUuid;

    public List<EIdentifierVersion> additionalIdComponents;

    public List<V> extraVersions;

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
        primordialComponentUuid = new UUID(in.readLong(), in.readLong());
        short idVersionCount = in.readShort();
        if (idVersionCount > 0) {
            additionalIdComponents = new ArrayList<EIdentifierVersion>(idVersionCount);
            for (int i = 0; i < idVersionCount; i++) {
                switch (IDENTIFIER_PART_TYPES.readType(in)) {
                case LONG:
                    additionalIdComponents.add(new EIdentifierVersionLong(in));
                    break;
                case STRING:
                    additionalIdComponents.add(new EIdentifierVersionString(in));
                    break;
                case UUID:
                    additionalIdComponents.add(new EIdentifierVersionUuid(in));
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
        out.writeLong(primordialComponentUuid.getMostSignificantBits());
        out.writeLong(primordialComponentUuid.getLeastSignificantBits());
        if (additionalIdComponents == null) {
            out.writeShort(0);
        } else {
            out.writeShort(additionalIdComponents.size());
            for (EIdentifierVersion idv : additionalIdComponents) {
                idv.getIdType().writeType(out);
                idv.writeExternal(out);
            }
        }
    }

    public void convert(I_Identify id) throws TerminologyException, IOException {
        boolean primordialWritten = false;
        int partCount = id.getMutableIdParts().size() - 1;
        if (partCount > 0) {
            additionalIdComponents = new ArrayList<EIdentifierVersion>(partCount);
            for (I_IdPart idp : id.getMutableIdParts()) {
                Object denotation = idp.getDenotation();
                switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
                case LONG:
                    additionalIdComponents.add(new EIdentifierVersionLong(idp));
                    break;
                case STRING:
                    additionalIdComponents.add(new EIdentifierVersionString(idp));
                    break;
                case UUID:
                    if (primordialWritten) {
                        additionalIdComponents.add(new EIdentifierVersionUuid(idp));
                    } else {
                        primordialComponentUuid = (UUID) idp.getDenotation();
                        primordialWritten = true;
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
                }

            }
        } else {
            primordialComponentUuid = (UUID) id.getUUIDs().get(0);
        }
    }

    public int getIdComponentCount() {
        if (additionalIdComponents == null) {
            return 1;
        }
        return additionalIdComponents.size() + 1;
    }

    public List<EIdentifierVersion> getEIdentifiers() {
        List<EIdentifierVersion> ids;
        if (additionalIdComponents != null) {
            ids = new ArrayList<EIdentifierVersion>(additionalIdComponents.size() + 1);
            ids.addAll(additionalIdComponents);
        } else {
            ids = new ArrayList<EIdentifierVersion>(1);
        }
        ids.add(new EIdentifierVersionUuid(this));
        return ids;
    }

    public List<UUID> getUuids() {
        List<UUID> uuids = new ArrayList<UUID>();
        uuids.add(primordialComponentUuid);
        if (additionalIdComponents != null) {
            for (EIdentifierVersion idv : additionalIdComponents) {
                if (EIdentifierVersionUuid.class.isAssignableFrom(idv.getClass())) {
                    uuids.add((UUID) idv.getDenotation());
                }
            }
        }
        return uuids;
    }

    public int getVersionCount() {
        List<? extends EVersion> extraVersions = getExtraVersionsList();
        if (extraVersions == null) {
            return 1;
        }
        return extraVersions.size() + 1;
    }

    public abstract List<? extends EVersion> getExtraVersionsList();

    public UUID getPrimordialComponentUuid() {
        return primordialComponentUuid;
    }

    public List<EIdentifierVersion> getAdditionalIdComponents() {
        return additionalIdComponents;
    }

    public void setAdditionalIdComponents(List<EIdentifierVersion> additionalIdComponents) {
        this.additionalIdComponents = additionalIdComponents;
    }

    public List<V> getExtraVersions() {
        return extraVersions;
    }

    public void setExtraVersions(List<V> extraVersions) {
        this.extraVersions = extraVersions;
    }

    public void setPrimordialComponentUuid(UUID primordialComponentUuid) {
        this.primordialComponentUuid = primordialComponentUuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(" primordialComponentUuid:");
        buff.append(this.primordialComponentUuid);
        buff.append(" additionalIdComponents:");
        buff.append(this.additionalIdComponents);
        buff.append(" extraVersions:");
        buff.append(this.extraVersions);
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>EComponent</code>.
     * 
     * @return a hash code value for this <tt>EComponent</tt>.
     */
    public int hashCode() {
        return this.primordialComponentUuid.hashCode();
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
            if (!this.primordialComponentUuid.equals(another.primordialComponentUuid)) {
                return false;
            }
            // Compare additionalIdComponents
            if (this.additionalIdComponents == null) {
                if (another.additionalIdComponents == null) { // Equal!
                } else if (another.additionalIdComponents.size() == 0) { // Equal!
                } else {
                    return false;
                }
            } else if (!this.additionalIdComponents.equals(another.additionalIdComponents)) {
                return false;
            }
            // Compare extraVersions
            if (this.extraVersions == null) {
                if (another.extraVersions == null) { // Equal!
                } else if (another.extraVersions.size() == 0) { // Equal!
                } else {
                    return false;
                }
            } else if (!this.extraVersions.equals(another.extraVersions)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

}
