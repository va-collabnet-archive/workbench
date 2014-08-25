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

        BOOLEAN, CONCEPT, INTEGER, STRING, C_FLOAT, STRING_STRING
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
    float valueFloat;
    String valueString1;
    String valueString2;
    ValueType valueType;
    int status; // CONCEPTSTATUS
    long revTime;
    int pathIdx;
    int authorIdx;
    int moduleIdx;

    // BOOLEAN
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, int zAuthIdx, int zModuleIdx,
            boolean valueBoolean) {
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
        this.valueFloat = Float.MAX_VALUE;
        this.valueString1 = null;
        this.valueString2 = null;
        this.valueType = ValueType.BOOLEAN; // BOOLEAN

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
        this.authorIdx = zAuthIdx;
        this.moduleIdx = zModuleIdx;
    }

    // CONCEPT
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, int zAuthIdx, int zModuleIdx,
            UUID vConcept) {
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
        this.valueFloat = Float.MAX_VALUE;
        this.valueString1 = null;
        this.valueString2 = null;
        this.valueType = ValueType.CONCEPT; // CONCEPT

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
        this.authorIdx = zAuthIdx;
        this.moduleIdx = zModuleIdx;
    }

    // INTEGER
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, int zAuthIdx, int zModuleIdx,
            int vInteger) {
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
        this.valueFloat = Float.MAX_VALUE;
        this.valueString1 = null;
        this.valueString2 = null;
        this.valueType = ValueType.INTEGER; // INTEGER

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
        this.authorIdx = zAuthIdx;
        this.moduleIdx = zModuleIdx;
    }
    
    // FLOAT
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, int zAuthIdx, int zModuleIdx, UUID vConcept,
            float vFloat) {
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
        this.valueFloat = vFloat;
        this.valueString1 = null;
        this.valueString2 = null;
        this.valueType = ValueType.C_FLOAT; // FLOAT

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
        this.authorIdx = zAuthIdx;
        this.moduleIdx = zModuleIdx;
    }

    // STRING
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, int zAuthIdx, int zModuleIdx,
            String vString) {
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
        this.valueFloat = Float.MAX_VALUE;
        this.valueString1 = vString;
        this.valueString2 = null;
        this.valueType = ValueType.STRING; // STRING

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
        this.authorIdx = zAuthIdx;
        this.moduleIdx = zModuleIdx;
    }
    
    // STRING_STRING
    public Sct1_RefSetRecord(UUID refsetUuid, UUID memberUuid, UUID componentUuid, int status,
            long zRevTime, int zPathIdx, int zAuthIdx, int zModuleIdx,
            String vString1, String vString2) {
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
        this.valueFloat = Float.MAX_VALUE;
        this.valueString1 = vString1;
        this.valueString2 = vString2;
        this.valueType = ValueType.STRING_STRING; // STRING_STRING

        this.status = status;
        this.revTime = zRevTime;
        this.pathIdx = zPathIdx;
        this.authorIdx = zAuthIdx;
        this.moduleIdx = zModuleIdx;
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
                                if (this.revTime < o.revTime) {
                                    return thisLess;
                                } else if (this.revTime > o.revTime) {
                                    return thisMore;
                                } else {
                                    return 0; // instance == received
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("::: REFSET MEMBER RECORD :::");
        sb.append("\r\n::: referencedComponentUuid ");
        sb.append(new UUID(this.referencedComponentUuidMsb, this.referencedComponentUuidLsb));

        sb.append("\r\n::: (envelop) conUuid ");
        sb.append(new UUID(this.conUuidMsb, this.conUuidLsb));

        sb.append("\r\n::: referencedComponentUuid ");
        sb.append(new UUID(this.referencedComponentUuidMsb, this.referencedComponentUuidLsb));

        sb.append("\r\n::: refsetMemberUuid ");
        sb.append(new UUID(this.refsetMemberUuidMsb, this.refsetMemberUuidLsb));

        sb.append("\r\n::: status ").append(this.status);

        Date d = new Date(this.revTime);
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.format(d);

        sb.append("\r\n::: revision date ").append(formatter.format(d).toString());

        if (this.valueType == Sct1_RefSetRecord.ValueType.STRING) {
            sb.append("\r\n::: value string ").append(this.valueString1);
        }else if (this.valueType == Sct1_RefSetRecord.ValueType.STRING_STRING) {
            sb.append("\r\n::: value string1 ").append(this.valueString1);
            sb.append("\r\n::: value string2 ").append(this.valueString2);
        } else if (this.valueType == Sct1_RefSetRecord.ValueType.BOOLEAN) {
            sb.append("\r\n::: value boolean ").append(this.valueBoolean);
        } else if (this.valueType == Sct1_RefSetRecord.ValueType.INTEGER) {
            sb.append("\r\n::: value integer ").append(this.valueInt);
        } else if (this.valueType == Sct1_RefSetRecord.ValueType.C_FLOAT) {
            sb.append("\r\n::: value float ").append(this.valueFloat);
        } else if (this.valueType == Sct1_RefSetRecord.ValueType.CONCEPT) {
            sb.append("\r\n::: value concept ");
            sb.append(new UUID(this.valueConUuidMsb, this.valueConUuidLsb));
        }
        sb.append("\r\n:::\r\n");

        return sb.toString();
    }
}
