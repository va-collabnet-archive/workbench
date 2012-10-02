/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api.blueprint;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayAnalogBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanAnalogBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntAnalogBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongAnalogBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidAnalogBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidAnalogBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidAnalogBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.uuid.UuidT5Generator;

// TODO: Auto-generated Javadoc
/**
 * The Class RefexCAB.
 *
 * @author kec
 */
public class RefexCAB extends CreateOrAmendBlueprint {

    /** The Constant refexSpecNamespace. */
    public static final UUID refexSpecNamespace =
            UUID.fromString("c44bc030-1166-11e0-ac64-0800200c9a66");
    
    /** The member type. */
    private TK_REFEX_TYPE memberType;

    /**
     * Compute member component uuid.
     *
     * @return the uuid
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     */
    public UUID computeMemberComponentUuid() throws IOException, InvalidCAB {
        try {
            UUID memberComponentUuid = UuidT5Generator.get(refexSpecNamespace,
                    memberType.name()
                    + getPrimordialUuidStringForNidProp(RefexProperty.COLLECTION_NID)
                    + getReferencedComponentUuid().toString());
            properties.put(RefexProperty.MEMBER_UUID, memberComponentUuid);
            return memberComponentUuid;
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets the member content uuid.
     *
     * @return the uuid
     * @throws InvalidCAB the invalid cab
     * @throws IOException signals that an I/O exception has occurred.
     */
    public UUID setMemberContentUuid() throws InvalidCAB, IOException {
        UUID memberContentUuid = computeMemberContentUuid();
        properties.put(RefexProperty.MEMBER_UUID, memberContentUuid);
        return memberContentUuid;
    }

    /**
     * Use when the 1-1 relationship between a refex and a referenced component
     * does not apply.
     *
     * @return A
     * <code>UUID</code> based on a Type 5 generator that uses the content
     * fields of the refex.
     * @throws InvalidCAB the invalid cab
     * @throws IOException signals that an I/O exception has occurred.
     */
    public UUID computeMemberContentUuid() throws InvalidCAB, IOException {
        try {
            StringBuilder sb = new StringBuilder();
            for (RefexProperty prop : RefexProperty.values()) {
                switch (prop) {
                    case MEMBER_UUID:
                    case STATUS_NID:
                    case COLLECTION_NID:
                    case RC_UUID:
                        break;
                    default:
                        if (properties.get(prop) != null) {
                            sb.append(properties.get(prop).toString());
                        }
                }
            }
            return UuidT5Generator.get(refexSpecNamespace,
                    memberType.name()
                    + getPrimordialUuidStringForNidProp(RefexProperty.COLLECTION_NID)
                    + getPrimordialUuidStringForNidProp(RefexProperty.RC_UUID)
                    + sb.toString());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#recomputeUuid()
     */
    @Override
    /**
     * For a refex the componentUuid methods from CreateOrAmendBlueprint
     * refer to the member component uuid.
     */
    public void recomputeUuid() throws InvalidCAB, IOException, ContradictionException {
        setComponentUuid(computeMemberComponentUuid());
        for (RefexCAB annotBp : getAnnotationBlueprints()) {
            annotBp.setReferencedComponentUuid(getComponentUuid());
            annotBp.recomputeUuid();
        }
    }

    /**
     * Gets the primordial uuid string for nid prop.
     *
     * @param prop the prop
     * @return the primordial uuid string for nid prop
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     */
    private String getPrimordialUuidStringForNidProp(RefexProperty prop)
            throws IOException, InvalidCAB {
        Object idObj = properties.get(prop);
        if (idObj == null) {
            throw new InvalidCAB(
                    "No data for: " + prop);
        }
        if (idObj instanceof UUID) {
            return ((UUID) idObj).toString();
        }
        int nid = (Integer) idObj;
        ComponentBI component = Ts.get().getComponent(nid);
        if (component != null) {
            return component.getPrimUuid().toString();
        }
        List<UUID> uuids = Ts.get().getUuidsForNid(nid);
        if (uuids.size() == 1) {
            return uuids.get(0).toString();
        }
        throw new InvalidCAB("Can't find nid for: " + prop
                + " props: " + this.properties);
    }
    
    /** The properties. */
    protected EnumMap<RefexProperty, Object> properties =
            new EnumMap<RefexProperty, Object>(RefexProperty.class);

    /**
     * This constructor creates a MEMBER_UUID that is computed from a type 5
     * UUID generator that uses a hash of the
     * <code>memberType</code>,
     * <code>referencedComponentNid</code>, and
     * <code>collectionNid</code>. This member ID is suitable for all refex
     * collections where there should be no more than one refex per referenced
     * component.
     *
     * @param memberType the member type
     * @param referencedComponentNid the referenced component nid
     * @param collectionNid the collection nid
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public RefexCAB(TK_REFEX_TYPE memberType,
            int referencedComponentNid, int collectionNid) throws IOException, InvalidCAB, ContradictionException {
        this(memberType, Ts.get().getUuidPrimordialForNid(referencedComponentNid), collectionNid, null, null, null);

        this.properties.put(RefexProperty.MEMBER_UUID,
                computeMemberComponentUuid());
    }

    /**
     * Instantiates a new refex cab.
     *
     * @param memberType the member type
     * @param referencedComponentUuid the referenced component uuid
     * @param collectionNid the collection nid
     * @param refexVersion the refex version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public RefexCAB(TK_REFEX_TYPE memberType,
            UUID referencedComponentUuid, int collectionNid, RefexVersionBI refexVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        this(memberType, referencedComponentUuid, collectionNid, null, refexVersion, viewCoordinate);

        this.properties.put(RefexProperty.MEMBER_UUID,
                computeMemberComponentUuid());
    }

    /**
     * Instantiates a new refex cab.
     *
     * @param memberType the member type
     * @param referencedComponentUuid the referenced component uuid
     * @param collectionNid the collection nid
     * @param memberUuid the member uuid
     * @param refexVersion the refex version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public RefexCAB(TK_REFEX_TYPE memberType,
            UUID referencedComponentUuid, int collectionNid,
            UUID memberUuid, RefexVersionBI refexVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        super(memberUuid, refexVersion, viewCoordinate);
        this.memberType = memberType;
        this.properties.put(RefexProperty.RC_UUID, referencedComponentUuid);
        this.properties.put(RefexProperty.COLLECTION_NID, collectionNid);
        this.properties.put(RefexProperty.STATUS_NID,
                SnomedMetadataRfx.getSTATUS_CURRENT_NID());
        if (getMemberUUID() != null) {
            this.properties.put(RefexProperty.MEMBER_UUID, memberUuid);
        }
        if (this.properties.get(RefexProperty.STATUS_NID) != null) {
            setStatusUuid(Ts.get().getComponent(
                    (Integer) this.properties.get(RefexProperty.STATUS_NID)).getPrimUuid());
        }
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#setCurrent()
     */
    @Override
    public void setCurrent() {
        super.setCurrent();
        try {
            this.properties.put(RefexProperty.STATUS_NID,
                    Ts.get().getNidForUuids(super.getStatusUuid()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#setRetired()
     */
    @Override
    public void setRetired() {
        super.setRetired();
        try {
            this.properties.put(RefexProperty.STATUS_NID,
                    Ts.get().getNidForUuids(super.getStatusUuid()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets the referenced component uuid.
     *
     * @param referencedComponentUuid the new referenced component uuid
     */
    protected void setReferencedComponentUuid(UUID referencedComponentUuid) {
        this.properties.put(RefexProperty.RC_UUID, referencedComponentUuid);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#getStatusUuid()
     */
    @Override
    public UUID getStatusUuid() {
        try {
            super.setStatusUuid(Ts.get().getComponent(
                    (Integer) this.properties.get(RefexProperty.STATUS_NID)).getPrimUuid());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return super.getStatusUuid();
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#setStatusUuid(java.util.UUID)
     */
    @Override
    public void setStatusUuid(UUID statusUuid) {
        super.setStatusUuid(statusUuid);
        if (this.properties.get(RefexProperty.STATUS_NID) != null) {
            try {
                this.properties.put(RefexProperty.STATUS_NID,
                        Ts.get().getNidForUuids(statusUuid));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * The Enum RefexProperty.
     */
    public enum RefexProperty {

        /** The member uuid. */
        MEMBER_UUID,
        
        /** The collection nid. */
        COLLECTION_NID,
        
        /** The rc uuid. */
        RC_UUID,
        
        /** The status nid. */
        STATUS_NID,
        
        /** The CNI d1. */
        CNID1,
        
        /** The CNI d2. */
        CNID2,
        
        /** The CNI d3. */
        CNID3,
        
        /** The UUI d1. */
        UUID1,
        
        /** The UUI d2. */
        UUID2,
        
        /** The UUI d3. */
        UUID3,
        
        /** The BOOLEA n1. */
        BOOLEAN1,
        
        /** The INTEGE r1. */
        INTEGER1,
        
        /** The STRIN g1. */
        STRING1,
        
        /** The LON g1. */
        LONG1,
        
        /** The FLOA t1. */
        FLOAT1,
        
        /** The array bytearray. */
        ARRAY_BYTEARRAY,
    }

    /**
     * Sets the member uuid.
     *
     * @param memberUuid the new member uuid
     */
    public void setMemberUuid(UUID memberUuid) {
        setComponentUuid(memberUuid);
        properties.put(RefexProperty.MEMBER_UUID, memberUuid);
    }

    /**
     * Contains key.
     *
     * @param key the key
     * @return <code>true</code>, if successful
     */
    public boolean containsKey(RefexProperty key) {
        return properties.containsKey(key);
    }

    /**
     * Entry set.
     *
     * @return the sets the
     */
    public Set<Entry<RefexProperty, Object>> entrySet() {
        return properties.entrySet();
    }

    /**
     * Key set.
     *
     * @return the sets the
     */
    public Set<RefexProperty> keySet() {
        return properties.keySet();
    }

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the object
     */
    public Object put(RefexProperty key, Number value) {
        return properties.put(key, value);
    }

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the object
     */
    public Object put(RefexProperty key, String value) {
        assert key == RefexProperty.STRING1;
        return properties.put(key, value);
    }

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the object
     */
    public Object put(RefexProperty key, Boolean value) {
        assert key == RefexProperty.BOOLEAN1;
        return properties.put(key, value);
    }

    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the object
     */
    public Object put(RefexProperty key, UUID value) {
        assert key == RefexProperty.MEMBER_UUID ||
                key == RefexProperty.UUID1 ||
                key == RefexProperty.UUID2 ||
                key == RefexProperty.UUID3;
        return properties.put(key,
                value);
    }
    
    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     * @return the object
     */
    public Object put(RefexProperty key, byte[][] value){
        assert key == RefexProperty.ARRAY_BYTEARRAY;
        return properties.put(RefexProperty.ARRAY_BYTEARRAY,
                value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " "
                + memberType + " " + properties;
    }

    /**
     * With.
     *
     * @param key the key
     * @param value the value
     * @return the refex cab
     */
    public RefexCAB with(RefexProperty key, Number value) {
        put(key, value);
        return this;
    }

    /**
     * With.
     *
     * @param key the key
     * @param value the value
     * @return the refex cab
     */
    public RefexCAB with(RefexProperty key, String value) {
        assert key == RefexProperty.STRING1;
        properties.put(key, value);
        return this;
    }

    /**
     * With.
     *
     * @param key the key
     * @param value the value
     * @return the refex cab
     */
    public RefexCAB with(RefexProperty key, Boolean value) {
        assert key == RefexProperty.BOOLEAN1;
        properties.put(key, value);
        return this;
    }
    
    /**
     * With.
     *
     * @param key the key
     * @param arrayOfByteArray the array of byte array
     * @return the refex cab
     */
    public RefexCAB with(RefexProperty key, byte[][] arrayOfByteArray) {
        assert key == RefexProperty.ARRAY_BYTEARRAY;
        properties.put(key, arrayOfByteArray);
        return this;
    }
    
    

    /**
     * Checks for property.
     *
     * @param key the key
     * @return <code>true</code>, if successful
     */
    public boolean hasProperty(RefexProperty key) {
        return properties.containsKey(key);
    }

    /**
     * Write to.
     *
     * @param refexAnalog the refex analog
     * @throws PropertyVetoException the property veto exception
     * @throws IOException signals that an I/O exception has occurred.
     */
    public void writeTo(RefexAnalogBI<?> refexAnalog) throws PropertyVetoException, IOException {
        setProperties(refexAnalog);
    }

    /**
     * Sets the properties.
     *
     * @param refexAnalog the new properties
     * @throws PropertyVetoException the property veto exception
     * @throws IOException signals that an I/O exception has occurred.
     */
    public void setProperties(RefexAnalogBI<?> refexAnalog) throws PropertyVetoException, IOException {
        for (Entry<RefexProperty, Object> entry : properties.entrySet()) {
            switch (entry.getKey()) {
                case MEMBER_UUID:
                    try {
                        refexAnalog.setNid(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case RC_UUID:
                    refexAnalog.setReferencedComponentNid(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    break;
                case BOOLEAN1:
                    RefexBooleanAnalogBI<?> booleanPart = (RefexBooleanAnalogBI<?>) refexAnalog;
                    booleanPart.setBoolean1((Boolean) entry.getValue());
                    break;
                case CNID1:
                    RefexNidAnalogBI<?> c1v = (RefexNidAnalogBI<?>) refexAnalog;
                    c1v.setNid1((Integer) entry.getValue());
                    break;
                case CNID3:
                    RefexNidNidNidAnalogBI<?> c3part = (RefexNidNidNidAnalogBI<?>) refexAnalog;
                    c3part.setNid3((Integer) entry.getValue());
                    break;
                case CNID2:
                    RefexNidNidAnalogBI<?> c2part = (RefexNidNidAnalogBI<?>) refexAnalog;
                    c2part.setNid2((Integer) entry.getValue());
                    break;
                case UUID1:
                    RefexNidAnalogBI<?> cv1part = (RefexNidAnalogBI<?>) refexAnalog;
                    cv1part.setNid1(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    break;
                case UUID2:
                    RefexNidNidAnalogBI<?> cv2part = (RefexNidNidAnalogBI<?>) refexAnalog;
                    cv2part.setNid2(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    break;
                case UUID3:
                    RefexNidNidNidAnalogBI<?> cv3part = (RefexNidNidNidAnalogBI<?>) refexAnalog;
                    cv3part.setNid3(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    break;
                case INTEGER1:
                    RefexIntAnalogBI<?> intPart = (RefexIntAnalogBI<?>) refexAnalog;
                    intPart.setInt1((Integer) entry.getValue());
                    break;
                case LONG1:
                    RefexLongAnalogBI<?> longPart = (RefexLongAnalogBI<?>) refexAnalog;
                    longPart.setLong1((Long) entry.getValue());
                    break;
                case STATUS_NID:
                    ((AnalogBI) refexAnalog).setStatusNid((Integer) entry.getValue());
                    break;
                case STRING1:
                    RefexStringAnalogBI<?> strPart = (RefexStringAnalogBI<?>) refexAnalog;
                    strPart.setString1((String) entry.getValue());
                    break;

                default:
                    throw new RuntimeException("Can't handle: " + entry.getKey());
            }
        }
    }

    /**
     * Sets the properties except sap.
     *
     * @param refexAnalog the new properties except sap
     * @throws PropertyVetoException the property veto exception
     * @throws IOException signals that an I/O exception has occurred.
     */
    public void setPropertiesExceptSap(RefexAnalogBI<?> refexAnalog) throws PropertyVetoException, IOException {
        for (Entry<RefexProperty, Object> entry : properties.entrySet()) {
            switch (entry.getKey()) {
                case MEMBER_UUID:
                    try {
                        int nid = Ts.get().getNidForUuids((UUID) entry.getValue());
                        if (refexAnalog.getNid() != nid) {
                            refexAnalog.setNid(nid);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case BOOLEAN1:
                    RefexBooleanAnalogBI<?> booleanPart = (RefexBooleanAnalogBI<?>) refexAnalog;
                    booleanPart.setBoolean1((Boolean) entry.getValue());
                    break;
                case COLLECTION_NID:
                    refexAnalog.setCollectionNid((Integer) entry.getValue());
                    break;
                case RC_UUID:
                    refexAnalog.setReferencedComponentNid(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    break;
                case CNID1:
                    RefexNidAnalogBI<?> c1part = (RefexNidAnalogBI<?>) refexAnalog;
                    c1part.setNid1((Integer) entry.getValue());
                    break;
                case CNID3:
                    RefexNidNidNidAnalogBI<?> c3part = (RefexNidNidNidAnalogBI<?>) refexAnalog;
                    c3part.setNid3((Integer) entry.getValue());
                    break;
                case CNID2:
                    RefexNidNidAnalogBI<?> c2part = (RefexNidNidAnalogBI<?>) refexAnalog;
                    c2part.setNid2((Integer) entry.getValue());
                    break;
                case UUID1:
                    RefexNidAnalogBI<?> c1Uuid = (RefexNidAnalogBI<?>) refexAnalog;
                    c1Uuid.setNid1(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    break;
                case UUID3:
                    RefexNidNidNidAnalogBI<?> c3Uuid = (RefexNidNidNidAnalogBI<?>) refexAnalog;
                    c3Uuid.setNid3(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    break;
                case UUID2:
                    RefexNidNidAnalogBI<?> c2Uuid = (RefexNidNidAnalogBI<?>) refexAnalog;
                    c2Uuid.setNid2(Ts.get().getNidForUuids((UUID) entry.getValue()));
                    break;
                case INTEGER1:
                    RefexIntAnalogBI<?> intPart = (RefexIntAnalogBI<?>) refexAnalog;
                    intPart.setInt1((Integer) entry.getValue());
                    break;
                case LONG1:
                    RefexLongAnalogBI<?> longPart = (RefexLongAnalogBI<?>) refexAnalog;
                    longPart.setLong1((Long) entry.getValue());
                    break;
                case STATUS_NID:
                    // SAP property
                    break;
                case STRING1:
                    RefexStringAnalogBI<?> strPart = (RefexStringAnalogBI<?>) refexAnalog;
                    strPart.setString1((String) entry.getValue());
                    break;
                case ARRAY_BYTEARRAY:
                    RefexArrayOfBytearrayAnalogBI<?> arrayPart = (RefexArrayOfBytearrayAnalogBI<?>) refexAnalog;
                    arrayPart.setArrayOfByteArray((byte[][]) entry.getValue());
                    break;
                default:
                    throw new RuntimeException("Can't handle: " + entry.getKey());
            }
        }
    }

    /**
     * Validate.
     *
     * @param refexVersion the refex version
     * @return <code>true</code>, if successful
     */
    public boolean validate(RefexVersionBI<?> refexVersion) {
        if (memberType != null) {
            if (TK_REFEX_TYPE.classToType(refexVersion.getClass()) != memberType) {
                return false;
            }
        }
        for (Entry<RefexProperty, Object> entry : properties.entrySet()) {
            switch (entry.getKey()) {
                case RC_UUID:
                    try {
                        if (!entry.getValue().equals(Ts.get().getUuidPrimordialForNid(refexVersion.getReferencedComponentNid()))) {
                            return false;
                        }
                        break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                case COLLECTION_NID:
                    if (!entry.getValue().equals(refexVersion.getRefexNid())) {
                        return false;
                    }
                    break;
                case MEMBER_UUID:
                    try {
                        if (refexVersion.getNid() != Ts.get().getNidForUuids((UUID) entry.getValue())) {
                            return false;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case BOOLEAN1:
                    if (!RefexBooleanVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexBooleanVersionBI<?> booleanPart = (RefexBooleanVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(booleanPart.getBoolean1())) {
                        return false;
                    }
                    break;
                case CNID1:
                    if (!RefexNidVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexNidVersionBI<?> c1part = (RefexNidVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(c1part.getNid1())) {
                        return false;
                    }
                    break;
                case CNID3:
                    if (!RefexNidNidNidVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexNidNidNidVersionBI<?> c3part = (RefexNidNidNidVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(c3part.getNid3())) {
                        return false;
                    }
                    break;
                case CNID2:
                    if (!RefexNidNidVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexNidNidVersionBI<?> c2part = (RefexNidNidVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(c2part.getNid2())) {
                        return false;
                    }
                    break;
                case UUID1:
                    if (!RefexNidVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexNidVersionBI<?> c1p = (RefexNidVersionBI<?>) refexVersion;
                    try {
                        if (Ts.get().getNidForUuids((UUID) entry.getValue()) != c1p.getNid1()) {
                            return false;
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case UUID3:
                    if (!RefexNidNidNidVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexNidNidNidVersionBI<?> c3p = (RefexNidNidNidVersionBI<?>) refexVersion;
                    try {
                        if (Ts.get().getNidForUuids((UUID) entry.getValue()) != c3p.getNid1()) {
                            return false;
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case UUID2:
                    if (!RefexNidNidVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexNidNidVersionBI<?> c2p = (RefexNidNidVersionBI<?>) refexVersion;
                    try {
                        if (Ts.get().getNidForUuids((UUID) entry.getValue()) != c2p.getNid1()) {
                            return false;
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                case INTEGER1:
                    if (!RefexIntVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexIntVersionBI<?> intPart = (RefexIntVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(intPart.getInt1())) {
                        return false;
                    }
                    break;
                case LONG1:
                    if (!RefexLongVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexLongVersionBI<?> longPart = (RefexLongVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(longPart.getLong1())) {
                        return false;
                    }
                    break;
                case STATUS_NID:
                    if (!entry.getValue().equals(refexVersion.getStatusNid())) {
                        return false;
                    }
                    break;
                case STRING1:
                    if (!RefexStringVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexStringVersionBI<?> strPart = (RefexStringVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(strPart.getString1())) {
                        return false;
                    }
                    break;
                case ARRAY_BYTEARRAY:
                    if(!RefexArrayOfBytearrayVersionBI.class.isAssignableFrom(refexVersion.getClass())){
                        return false;
                    }
                    RefexArrayOfBytearrayVersionBI<?> arrayPart = (RefexArrayOfBytearrayVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(arrayPart.getArrayOfByteArray())) {
                        return false;
                    }
                    break;
                default:
                    throw new RuntimeException("Can't handle: " + entry.getKey());
            }
        }
        return true;
    }

    /**
     * Gets the int.
     *
     * @param key the key
     * @return the int
     */
    public int getInt(RefexProperty key) {
        return (Integer) properties.get(key);
    }

    /**
     * Gets the refex collection nid.
     *
     * @return the refex collection nid
     */
    public int getRefexCollectionNid() {
        return getInt(RefexProperty.COLLECTION_NID);
    }

    /**
     * Gets the referenced component uuid.
     *
     * @return the referenced component uuid
     */
    public UUID getReferencedComponentUuid() {
        return (UUID) properties.get(RefexProperty.RC_UUID);
    }

    /**
     * Gets the string.
     *
     * @param key the key
     * @return the string
     */
    public String getString(RefexProperty key) {
        assert key == RefexProperty.STRING1;
        return (String) properties.get(key);
    }

    /**
     * Gets the boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean getBoolean(RefexProperty key) {
        assert key == RefexProperty.BOOLEAN1;
        return (Boolean) properties.get(key);
    }

    /**
     * Gets the member uuid.
     *
     * @param key the key
     * @return the member uuid
     */
    public UUID getMemberUuid(RefexProperty key) {
        assert key == RefexProperty.MEMBER_UUID;
        return (UUID) properties.get(key);
    }

    /**
     * Gets the member uuid.
     *
     * @return the member uuid
     */
    public UUID getMemberUUID() {
        return (UUID) properties.get(RefexProperty.MEMBER_UUID);
    }

    /**
     * Gets the member type.
     *
     * @return the member type
     */
    public TK_REFEX_TYPE getMemberType() {
        return memberType;
    }

    /**
     * Sets the member type.
     *
     * @param memberType the new member type
     */
    public void setMemberType(TK_REFEX_TYPE memberType) {
        this.memberType = memberType;
    }

    /**
     * Sets the content uuid.
     *
     * @throws InvalidCAB the invalid cab
     * @throws IOException signals that an I/O exception has occurred.
     */
    public void setContentUuid() throws InvalidCAB, IOException {
        this.properties.put(RefexProperty.MEMBER_UUID,
                computeMemberContentUuid());
    }
}
