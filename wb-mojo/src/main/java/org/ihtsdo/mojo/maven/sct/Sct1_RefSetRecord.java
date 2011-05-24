package org.ihtsdo.mojo.maven.sct;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    int authorIdx;

    // BOOLEAN
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, boolean valueBoolean, int zAuthIdx) {
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
        this.authorIdx = zAuthIdx;
    }

    // CONCEPT
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, UUID vConcept, int zAuthIdx) {
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
        this.authorIdx = zAuthIdx;
    }

    // INTEGER
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, int vInteger, int zAuthIdx) {
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
        this.authorIdx = zAuthIdx;
    }

    // STRING
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, String vString, int zAuthIdx) {
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
        this.authorIdx = zAuthIdx;
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
                if (this.refsetUuidMsb < o.refsetUuidMsb) {
                    return thisLess; // instance less than received
                } else if (this.refsetUuidMsb > o.refsetUuidMsb) {
                    return thisMore; // instance greater than received
                } else {
                    if (this.refsetUuidLsb < o.refsetUuidLsb) {
                        return thisLess;
                    } else if (this.refsetUuidLsb > o.refsetUuidLsb) {
                        return thisMore;
                    } else {
                        if (this.refsetMemberUuidMsb < o.refsetMemberUuidMsb) {
                            return thisLess; // instance less than received
                        } else if (this.refsetMemberUuidMsb > o.refsetMemberUuidMsb) {
                            return thisMore; // instance greater than received
                        } else {
                            if (this.refsetMemberUuidLsb < o.refsetMemberUuidLsb) {
                                return thisLess;
                            } else if (this.refsetMemberUuidLsb > o.refsetMemberUuidLsb) {
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
           }
        }
    }
        
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("::: REFSET MEMBER RECORD :::");
        sb.append("\r\n::: referencedComponentUuid "
                + new UUID(this.referencedComponentUuidMsb, this.referencedComponentUuidLsb));

        sb.append("\r\n::: (envelop) conUuid " + new UUID(this.conUuidMsb, this.conUuidLsb));

        sb.append("\r\n::: referencedComponentUuid "
                + new UUID(this.referencedComponentUuidMsb, this.referencedComponentUuidLsb));

        sb.append("\r\n::: refsetMemberUuid "
                + new UUID(this.refsetMemberUuidMsb, this.refsetMemberUuidLsb));

        sb.append("\r\n::: status " + this.status);

        Date d = new Date(this.revTime);
        String pattern = "yyyy-MM-dd hh:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.format(d);

        sb.append("\r\n::: revision date " + formatter.format(d).toString());
        
        if (this.valueType == Sct1_RefSetRecord.ValueType.STRING)
            sb.append("\r\n::: value string " + this.valueString);
        else if (this.valueType == Sct1_RefSetRecord.ValueType.BOOLEAN)
            sb.append("\r\n::: value boolean " + this.valueBoolean);
        else if (this.valueType == Sct1_RefSetRecord.ValueType.INTEGER)
            sb.append("\r\n::: value integer " + this.valueInt);
        else if (this.valueType == Sct1_RefSetRecord.ValueType.CONCEPT)
            sb.append("\r\n::: value concept " + new UUID(this.valueConUuidMsb, this.valueConUuidLsb));
        sb.append("\r\n:::\r\n");

        return sb.toString();
    }
    
}
