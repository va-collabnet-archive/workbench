package org.ihtsdo.mojo.maven.sct;

import java.io.Serializable;
import java.util.UUID;

public class SctYRefSetRecord implements Comparable<SctYRefSetRecord>, Serializable {
    enum ComponentType {
        CONCEPT, DESCRIPTION, IMAGE, MEMBER, RELATIONSHIP, UNKNOWN
    };

    enum ValueType {
        BOOLEAN, CONCEPT, INTEGER, STRING
    };

    private static final long serialVersionUID = 1L;

    long conUuidMsb; // ENVELOP CONCEPTID (eConcept to which this concept belongs)
    long conUuidLsb; // ENVELOP CONCEPTID
    long componentUuidMsb;
    long componentUuidLsb;
    ComponentType componentType;

    long refsetUuidMsb;
    long refsetUuidLsb;
    long memberUuidMsb; // aka primordialComponentUuidMsb
    long memberUuidLsb; // aka primordialComponentUuidLsb

    boolean valueBoolean;
    long valueConUuidMsb;
    long valueConUuidLsb;
    int valueInt;
    String valueString;
    ValueType valueType;

    int status; // CONCEPTSTATUS
    int yRevision;
    int yPath;

    public SctYRefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            int yRev, int yPath, boolean valueBoolean) {
        super();
        this.conUuidMsb = Long.MAX_VALUE;
        this.conUuidLsb = Long.MAX_VALUE;
        this.componentUuidMsb = componentUuid.getMostSignificantBits();
        this.componentUuidLsb = componentUuid.getLeastSignificantBits();
        this.componentType = ComponentType.UNKNOWN;
        this.refsetUuidMsb = refsetUuid.getMostSignificantBits();
        this.refsetUuidLsb = refsetUuid.getLeastSignificantBits();
        this.memberUuidMsb = memberUuid.getMostSignificantBits();
        this.memberUuidLsb = memberUuid.getLeastSignificantBits();

        this.valueBoolean = valueBoolean;
        this.valueConUuidMsb = Long.MAX_VALUE;
        this.valueConUuidLsb = Long.MAX_VALUE;
        this.valueInt = Integer.MAX_VALUE;
        this.valueString = null;
        this.valueType = ValueType.BOOLEAN; // BOOLEAN

        this.status = status;
        this.yRevision = yRev;
        this.yPath = yPath;
    }

    public SctYRefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            int yRev, int yPath, UUID vConcept) {
        super();
        this.conUuidMsb = Long.MAX_VALUE;
        this.conUuidLsb = Long.MAX_VALUE;
        this.componentUuidMsb = componentUuid.getMostSignificantBits();
        this.componentUuidLsb = componentUuid.getLeastSignificantBits();
        this.componentType = ComponentType.UNKNOWN;
        this.refsetUuidMsb = refsetUuid.getMostSignificantBits();
        this.refsetUuidLsb = refsetUuid.getLeastSignificantBits();
        this.memberUuidMsb = memberUuid.getMostSignificantBits();
        this.memberUuidLsb = memberUuid.getLeastSignificantBits();

        this.valueBoolean = false;
        this.valueConUuidMsb = vConcept.getMostSignificantBits();
        this.valueConUuidLsb = vConcept.getLeastSignificantBits();
        this.valueInt = Integer.MAX_VALUE;
        this.valueString = null;
        this.valueType = ValueType.CONCEPT; // CONCEPT

        this.status = status;
        this.yRevision = yRev;
        this.yPath = yPath;
    }

    public SctYRefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            int yRev, int yPath, int vInteger) {
        super();
        this.conUuidMsb = Long.MAX_VALUE;
        this.conUuidLsb = Long.MAX_VALUE;
        this.componentUuidMsb = componentUuid.getMostSignificantBits();
        this.componentUuidLsb = componentUuid.getLeastSignificantBits();
        this.componentType = ComponentType.UNKNOWN;
        this.refsetUuidMsb = refsetUuid.getMostSignificantBits();
        this.refsetUuidLsb = refsetUuid.getLeastSignificantBits();
        this.memberUuidMsb = memberUuid.getMostSignificantBits();
        this.memberUuidLsb = memberUuid.getLeastSignificantBits();

        this.valueBoolean = false;
        this.valueConUuidMsb = Long.MAX_VALUE;
        this.valueConUuidLsb = Long.MAX_VALUE;
        this.valueInt = vInteger;
        this.valueString = null;
        this.valueType = ValueType.INTEGER; // INTEGER

        this.status = status;
        this.yRevision = yRev;
        this.yPath = yPath;
    }

    public SctYRefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            int yRev, int yPath, String vString) {
        super();
        this.conUuidMsb = Long.MAX_VALUE;
        this.conUuidLsb = Long.MAX_VALUE;
        this.componentUuidMsb = componentUuid.getMostSignificantBits();
        this.componentUuidLsb = componentUuid.getLeastSignificantBits();
        this.componentType = ComponentType.UNKNOWN;
        this.refsetUuidMsb = refsetUuid.getMostSignificantBits();
        this.refsetUuidLsb = refsetUuid.getLeastSignificantBits();
        this.memberUuidMsb = memberUuid.getMostSignificantBits();
        this.memberUuidLsb = memberUuid.getLeastSignificantBits();

        this.valueBoolean = false;
        this.valueConUuidMsb = Long.MAX_VALUE;
        this.valueConUuidLsb = Long.MAX_VALUE;
        this.valueInt = Integer.MAX_VALUE;
        this.valueString = vString;
        this.valueType = ValueType.STRING; // STRING

        this.status = status;
        this.yRevision = yRev;
        this.yPath = yPath;
    }

    public void setEnvelopConUuid(UUID conUuid, ComponentType cType) {
        this.conUuidMsb = conUuid.getMostSignificantBits();
        this.conUuidLsb = conUuid.getLeastSignificantBits();
        this.componentType = cType;
    }

    @Override
    public int compareTo(SctYRefSetRecord o) {
        int thisMore = 1;
        int thisLess = -1;
        if (this.componentUuidMsb < o.componentUuidMsb) {
            return thisLess; // instance less than received
        } else if (this.componentUuidMsb > o.componentUuidMsb) {
            return thisMore; // instance greater than received
        } else {
            if (this.componentUuidLsb < o.componentUuidLsb) {
                return thisLess;
            } else if (this.componentUuidLsb > o.componentUuidLsb) {
                return thisMore;
            } else {
                return 0; // instance == received
            }
        }
    }
}
