package org.ihtsdo.mojo.maven.sct;

import java.io.Serializable;
import java.util.UUID;

public class Sct1_RefSetRecord implements Comparable<Sct1_RefSetRecord>, Serializable {
    enum ComponentType {
        CONCEPT, DESCRIPTION, IMAGE, MEMBER, RELATIONSHIP, UNKNOWN
    };

    enum ValueType {
        BOOLEAN, CONCEPT, INTEGER, STRING
    };

    private static final long serialVersionUID = 1L;

    long conUuidMsb; // ENVELOP CONCEPTID (eConcept to which this concept belongs)
    long conUuidLsb; // ENVELOP CONCEPTID
    long referencedComponentUuidMsb;
    long referencedComponentUuidLsb;
    ComponentType componentType;

    long refsetUuidMsb;
    long refsetUuidLsb;
    long refsetMemberUuidMsb; // aka primordialComponentUuidMsb
    long refsetMemberUuidLsb; // aka primordialComponentUuidLsb

    boolean valueBoolean;
    long valueConUuidMsb;
    long valueConUuidLsb;
    int valueInt;
    String valueString;
    ValueType valueType;

    int status; // CONCEPTSTATUS
    long revTime;

    int pathIdx;

    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, boolean valueBoolean) {
        super();
        this.conUuidMsb = Long.MAX_VALUE;
        this.conUuidLsb = Long.MAX_VALUE;
        this.referencedComponentUuidMsb = componentUuid.getMostSignificantBits();
        this.referencedComponentUuidLsb = componentUuid.getLeastSignificantBits();
        this.componentType = ComponentType.UNKNOWN;
        this.refsetUuidMsb = refsetUuid.getMostSignificantBits();
        this.refsetUuidLsb = refsetUuid.getLeastSignificantBits();
        this.refsetMemberUuidMsb = memberUuid.getMostSignificantBits();
        this.refsetMemberUuidLsb = memberUuid.getLeastSignificantBits();

        this.valueBoolean = valueBoolean;
        this.valueConUuidMsb = Long.MAX_VALUE;
        this.valueConUuidLsb = Long.MAX_VALUE;
        this.valueInt = Integer.MAX_VALUE;
        this.valueString = null;
        this.valueType = ValueType.BOOLEAN; // BOOLEAN

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
    }

    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, UUID vConcept) {
        super();
        this.conUuidMsb = Long.MAX_VALUE;
        this.conUuidLsb = Long.MAX_VALUE;
        this.referencedComponentUuidMsb = componentUuid.getMostSignificantBits();
        this.referencedComponentUuidLsb = componentUuid.getLeastSignificantBits();
        this.componentType = ComponentType.UNKNOWN;
        this.refsetUuidMsb = refsetUuid.getMostSignificantBits();
        this.refsetUuidLsb = refsetUuid.getLeastSignificantBits();
        this.refsetMemberUuidMsb = memberUuid.getMostSignificantBits();
        this.refsetMemberUuidLsb = memberUuid.getLeastSignificantBits();

        this.valueBoolean = false;
        this.valueConUuidMsb = vConcept.getMostSignificantBits();
        this.valueConUuidLsb = vConcept.getLeastSignificantBits();
        this.valueInt = Integer.MAX_VALUE;
        this.valueString = null;
        this.valueType = ValueType.CONCEPT; // CONCEPT

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
    }

    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, int vInteger) {
        super();
        this.conUuidMsb = Long.MAX_VALUE;
        this.conUuidLsb = Long.MAX_VALUE;
        this.referencedComponentUuidMsb = componentUuid.getMostSignificantBits();
        this.referencedComponentUuidLsb = componentUuid.getLeastSignificantBits();
        this.componentType = ComponentType.UNKNOWN;
        this.refsetUuidMsb = refsetUuid.getMostSignificantBits();
        this.refsetUuidLsb = refsetUuid.getLeastSignificantBits();
        this.refsetMemberUuidMsb = memberUuid.getMostSignificantBits();
        this.refsetMemberUuidLsb = memberUuid.getLeastSignificantBits();

        this.valueBoolean = false;
        this.valueConUuidMsb = Long.MAX_VALUE;
        this.valueConUuidLsb = Long.MAX_VALUE;
        this.valueInt = vInteger;
        this.valueString = null;
        this.valueType = ValueType.INTEGER; // INTEGER

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
    }

    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, String vString) {
        super();
        this.conUuidMsb = Long.MAX_VALUE;
        this.conUuidLsb = Long.MAX_VALUE;
        this.referencedComponentUuidMsb = componentUuid.getMostSignificantBits();
        this.referencedComponentUuidLsb = componentUuid.getLeastSignificantBits();
        this.componentType = ComponentType.UNKNOWN;
        this.refsetUuidMsb = refsetUuid.getMostSignificantBits();
        this.refsetUuidLsb = refsetUuid.getLeastSignificantBits();
        this.refsetMemberUuidMsb = memberUuid.getMostSignificantBits();
        this.refsetMemberUuidLsb = memberUuid.getLeastSignificantBits();

        this.valueBoolean = false;
        this.valueConUuidMsb = Long.MAX_VALUE;
        this.valueConUuidLsb = Long.MAX_VALUE;
        this.valueInt = Integer.MAX_VALUE;
        this.valueString = vString;
        this.valueType = ValueType.STRING; // STRING

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
    }

    public void setEnvelopConUuid(UUID conUuid, ComponentType cType) {
        this.conUuidMsb = conUuid.getMostSignificantBits();
        this.conUuidLsb = conUuid.getLeastSignificantBits();
        this.componentType = cType;
    }

    @Override
    public int compareTo(Sct1_RefSetRecord o) {
        int thisMore = 1;
        int thisLess = -1;
        if (this.referencedComponentUuidMsb < o.referencedComponentUuidMsb) {
            return thisLess; // instance less than received
        } else if (this.referencedComponentUuidMsb > o.referencedComponentUuidMsb) {
            return thisMore; // instance greater than received
        } else {
            if (this.referencedComponentUuidLsb < o.referencedComponentUuidLsb) {
                return thisLess;
            } else if (this.referencedComponentUuidLsb > o.referencedComponentUuidLsb) {
                return thisMore;
            } else {
                if (this.revTime < o.revTime)
                    return thisLess;
                else if (this.revTime > o.revTime)
                    return thisMore;
                else
                    return 0; // instance == received
            }
        }
    }
}
