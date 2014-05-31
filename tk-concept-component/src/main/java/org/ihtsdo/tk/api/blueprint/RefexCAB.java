/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
import org.ihtsdo.tk.api.refex.type_float.RefexFloatAnalogBI;
import org.ihtsdo.tk.api.refex.type_float.RefexFloatVersionBI;
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

/**
 * The Class RefexCAB contains methods for creating a media blueprint. This
 * blueprint can be constructed into a type of
 * <code>MediaChronicleBI</code>. This is the preferred method for updating or
 * creating new descriptions.
 *
 * @see TerminologyBuilderBI
 * @see RefexChronicleBI
 *
 */
public class RefexCAB extends CreateOrAmendBlueprint {

    public static final UUID refexSpecNamespace =
            UUID.fromString("c44bc030-1166-11e0-ac64-0800200c9a66");
    private TK_REFEX_TYPE memberType;

    /**
     * Computes the uuid of the refex member based on the member type, refex
     * collection, and referenced component. Should be used when there is a 1-1
     * relationship between the refex collection and the referenced component.
     * Otherwise use
     * <code>computeMemberContentUuid()</code>.
     *
     * @return the uuid of the refex member
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
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
     * Computes the uuid of the refex member and sets the member uuid property.
     * Uses
     * <code>computeMemberContentUuid()</code> to compute the uuid.
     *
     * @return the uuid of the refex member
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws IOException signals that an I/O exception has occurred
     */
    public UUID setMemberContentUuid() throws InvalidCAB, IOException {
        UUID memberContentUuid = computeMemberContentUuid();
        properties.put(RefexProperty.MEMBER_UUID, memberContentUuid);
        return memberContentUuid;
    }

    /**
     * Computes the uuid of a the refex member based on the refex properties.
     * Use when the 1-1 relationship between a refex and a referenced component
     * does not apply.
     *
     * @return A <code>UUID</code> based on a Type 5 generator that uses the
     * content fields of the refex.
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws IOException signals that an I/O exception has occurred
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

    /**
     * Recomputes the refex member uuid. Component
     *
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    @Override
    public void recomputeUuid() throws InvalidCAB, IOException, ContradictionException {
        setComponentUuid(computeMemberComponentUuid());
        for (RefexCAB annotBp : getAnnotationBlueprints()) {
            annotBp.setReferencedComponentUuid(getComponentUuid());
            annotBp.recomputeUuid();
        }
    }

    /**
     * Gets a string representing the primordial uuid of the specified nid-based
     * <code>refexProperty</code>.
     *
     * @param refexProperty the refexProperty representing the nid-bsed property
     * @return a String representing the primordial uuid of the refex property
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     */
    private String getPrimordialUuidStringForNidProp(RefexProperty refexProperty)
            throws IOException, InvalidCAB {
        Object idObj = properties.get(refexProperty);
        if (idObj == null) {
            throw new InvalidCAB(
                    "No data for: " + refexProperty);
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
        throw new InvalidCAB("Can't find nid for: " + refexProperty
                + " props: " + this.properties);
    }
    protected EnumMap<RefexProperty, Object> properties =
            new EnumMap<RefexProperty, Object>(RefexProperty.class);

    /**
     * Instantiates a new refex blueprint using nid values. This constructor
     * creates a refex member uuid that is computed from a type 5 UUID generator
     * that uses a hash of the
     * <code>memberType</code>,
     * <code>referencedComponentNid</code>, and
     * <code>collectionNid</code>. This member ID is suitable for all refex
     * collections where there should be no more than one refex member per
     * referenced component.
     *
     * @param memberType the refex member type
     * @param referencedComponentNid the nid of the referenced component
     * @param collectionNid the nid of the refex collection concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public RefexCAB(TK_REFEX_TYPE memberType,
            int referencedComponentNid, int collectionNid) throws IOException, InvalidCAB, ContradictionException {
        this(memberType, Ts.get().getUuidPrimordialForNid(referencedComponentNid), collectionNid, null, null, null);

        this.properties.put(RefexProperty.MEMBER_UUID,
                computeMemberComponentUuid());
    }

    /**
     * Instantiates a new refex blueprint using nid values and a given
     * <code>refexVersion</code>. This constructor creates a refex member uuid
     * that is computed from a type 5 UUID generator that uses a hash of the
     * <code>memberType</code>,
     * <code>referencedComponentNid</code>, and
     * <code>collectionNid</code>. This member ID is suitable for all refex
     * collections where there should be no more than one refex member per
     * referenced component.
     *
     * @param memberType the refex member type
     * @param referencedComponentNid the nid of the referenced component
     * @param collectionNid the nid of the refex collection concept
     * @param refexVersion the refex version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
     */
    public RefexCAB(TK_REFEX_TYPE memberType,
            UUID referencedComponentUuid, int collectionNid, RefexVersionBI refexVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        this(memberType, referencedComponentUuid, collectionNid, null, refexVersion, viewCoordinate);

        this.properties.put(RefexProperty.MEMBER_UUID,
                computeMemberComponentUuid());
    }

    /**
     * Instantiates a new refex blueprint using nid values and a given
     * <code>refexVersion</code>. Uses the given
     * <code>memberUuid</code> as the refex member uuid.
     *
     * @param memberType the refex member type
     * @param referencedComponentNid the nid of the referenced component
     * @param collectionNid the nid of the refex collection concept
     * @param memberUuid the uuid of the refex member
     * @param refexVersion the refex version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * given position or view coordinate
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

    /**
     *
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

    /**
     *
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
     * Sets the uuid for the referenced component associated with this refex
     * blueprint.
     *
     * @param referencedComponentUuid the uuid of the referenced component
     */
    public void setReferencedComponentUuid(UUID referencedComponentUuid) {
        this.properties.put(RefexProperty.RC_UUID, referencedComponentUuid);
    }

    /**
     * Gets the nid of the status associated with this refex blueprint.
     *
     * @return the uuid of the status associated with this refex blueprint
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

    /**
     * Sets the uuid of the status associated with this refex blueprint.
     *
     * @param statusUuid the uuid of the status associated with this refex
     * blueprint
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
     * The Enum RefexProperty lists the available properties that can be
     * associated with a refex member.
     */
    public enum RefexProperty {

        /**
         * The member uuid.
         */
        MEMBER_UUID,
        /**
         * The refex collection nid.
         */
        COLLECTION_NID,
        /**
         * The referenced component uuid.
         */
        RC_UUID,
        /**
         * The refex member status nid.
         */
        STATUS_NID,
        /**
         * The nid of the first concept associated with this refex member.
         */
        CNID1,
        /**
         * The nid of the second concept associated with this refex member.
         */
        CNID2,
        /**
         * The nid of the third concept associated with this refex member.
         */
        CNID3,
        /**
         * The uuid of the first concept associated with this refex member.
         */
        UUID1,
        /**
         * The uuid of the second concept associated with this refex member.
         */
        UUID2,
        /**
         * The uuid of the third concept associated with this refex member.
         */
        UUID3,
        /**
         * The boolean value associated with this refex member.
         */
        BOOLEAN1,
        /**
         * The integer value associated with this refex member.
         */
        INTEGER1,
        /**
         * The first string value associated with this refex member.
         */
        STRING1,
        /**
         * The second string value associated with this refex member.
         */
        STRING2,
        /**
         * The long value associated with this refex member.
         */
        LONG1,
        /**
         * The float value associated with this refex member.
         */
        FLOAT1,
        /**
         * The array bytearray value associated with this refex member.
         */
        ARRAY_BYTEARRAY,
    }

    /**
     * Sets the refex member uuid associated with this refex blueprint.
     *
     * @param memberUuid the refex member uuid
     */
    public void setMemberUuid(UUID memberUuid) {
        setComponentUuid(memberUuid);
        properties.put(RefexProperty.MEMBER_UUID, memberUuid);
    }

    /**
     * Checks if the refex properties contain the specified property.
     *
     * @param key the refex property in question
     * @return <code>true</code>, if the refex properties contain the specified
     * property
     */
    public boolean containsKey(RefexProperty key) {
        return properties.containsKey(key);
    }

    /**
     * Gets a set of refex properties mapped to their corresponding values.
     *
     * @return a set of refex properties mapped to their corresponding values
     */
    public Set<Entry<RefexProperty, Object>> entrySet() {
        return properties.entrySet();
    }

    /**
     * Gets the refex properties.
     *
     * @return a set of refex properties
     */
    public Set<RefexProperty> keySet() {
        return properties.keySet();
    }

    /**
     * Maps the given Number
     * <code>value</code> to the specified refex property
     * <code>key</code>.
     *
     * @param key the refex property
     * @param value the value to associate with the refex property
     * @return the previous value associated with the specified 
     * key, <code>null</code> if no value was previously associated
     */
    public Object put(RefexProperty key, Number value) {
        return properties.put(key, value);
    }

    /**
     * Puts the given
     * <code>stringValue</code> in the
     * <code>RefexProperty.STRING1</code>. Will throw an assertion error if a
     * different property is used for the key.
     *
     * @param key RefexProperty.STRING1
     * @param stringValue the string to associate with this refex blueprint
     * @return the previous value associated with the specified 
     * key, <code>null</code> if no value was previously associated
     */
    public Object put(RefexProperty key, String stringValue) {
        assert key == RefexProperty.STRING1;
        return properties.put(key, stringValue);
    }

    /**
     * Puts the given
     * <code>booleanValue</code> in the
     * <code>RefexProperty.BOOLEAN1</code>. Will throw an assertion error if a
     * different property is used for the key.
     *
     * @param key RefexProperty.BOOLEAN1
     * @param booleanValue the boolean to associate with this refex blueprint
     * @return the previous value associated with the specified
     * key, <code>null</code> if no value was previously associated
     */
    public Object put(RefexProperty key, Boolean booleanValue) {
        assert key == RefexProperty.BOOLEAN1;
        return properties.put(key, booleanValue);
    }

    /**
     * Puts the given
     * <code>uuidValue</code> in a uuid based refex property. Will throw an
     * assertion error if the property used for the key is not one of the
     * following:
     * <code>RefexProperty.MEMBER_UUID</code>,
     * <code>RefexProperty.UUID1</code>,
     * <code>RefexProperty.UUID2</code>, or
     * <code>RefexProperty.UUID3</code>.
     *
     * @param key a uuid based refex property
     * @param uuidValue the uuid to associate with this refex blueprint
     * @return the previous value associated with the specified
     * key, <code>null</code> if no value was previously associated
     */
    public Object put(RefexProperty key, UUID uuidValue) {
        assert key == RefexProperty.MEMBER_UUID
                || key == RefexProperty.UUID1
                || key == RefexProperty.UUID2
                || key == RefexProperty.UUID3;
        return properties.put(key,
                uuidValue);
    }

    /**
     * Puts the given
     * <code>arrayOfByteArray</code> in the
     * <code>RefexProperty.ARRAY_BYTEARRAY</code>. Will throw an assertion error
     * if a different property is used for the key.
     *
     * @param key RefexProperty.ARRAY_BYTEARRAY
     * @param arrayOfByteArray the array of byte array to associate with this
     * refex blueprint
     * @return the previous value associated with the specified
     * key, <code>null</code> if no value was previously associated
     */
    public Object put(RefexProperty key, byte[][] arrayOfByteArray) {
        assert key == RefexProperty.ARRAY_BYTEARRAY;
        return properties.put(RefexProperty.ARRAY_BYTEARRAY,
                arrayOfByteArray);
    }

    /**
     * Generates a string representation of this refex blueprint. Includes the
     * refex member type and the properties.
     *
     * @return a string representation of this refex blueprint
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " "
                + memberType + " " + properties;
    }

    /**
     * Maps the given number
     * <code>value</code> to the specified refex property
     * <code>key</code>. Returns this refex member with the new property.
     *
     * @param key a refex property
     * @param value a number value to associate with this refex member
     * @return this refex member blueprint with the new property
     */
    public RefexCAB with(RefexProperty key, Number value) {
        put(key, value);
        return this;
    }

    /**
     * Puts the given
     * <code>stringValue</code> in the
     * <code>RefexProperty.STRING1</code>. Will throw an assertion error if a
     * different property is used for the key. Returns this refex member
     * blueprint with the new string property.
     *
     * @param key RefexProperty.STRING1
     * @param stringValue the string to associate with this refex blueprint
     * @return this refex member blueprint with the new string property
     */
    public RefexCAB with(RefexProperty key, String stringValue) {
        assert key == RefexProperty.STRING1;
        properties.put(key, stringValue);
        return this;
    }

    /**
     * Puts the given
     * <code>booleanValue</code> in the
     * <code>RefexProperty.BOOLEAN1</code>. Will throw an assertion error if a
     * different property is used for the key. Returns this refex member
     * blueprint with the new boolean property.
     *
     * @param key RefexProperty.BOOLEAN1
     * @param booleanValue the boolean to associate with this refex blueprint
     * @return this refex member blueprint with the new boolean property
     */
    public RefexCAB with(RefexProperty key, Boolean booleanValue) {
        assert key == RefexProperty.BOOLEAN1;
        properties.put(key, booleanValue);
        return this;
    }

    /**
     * Puts the given
     * <code>arrayOfByteArray</code> in the
     * <code>RefexProperty.ARRAY_BYTEARRAY</code>. Will throw an assertion error
     * if a different property is used for the key. Returns this refex member
     * blueprint with the new array of byte array property.
     *
     * @param key RefexProperty.ARRAY_BYTEARRAY
     * @param arrayOfByteArray the array of byte array to associate with this
     * refex blueprint
     * @return this refex member blueprint with the new array byte array
     * property
     */
    public RefexCAB with(RefexProperty key, byte[][] arrayOfByteArray) {
        assert key == RefexProperty.ARRAY_BYTEARRAY;
        properties.put(key, arrayOfByteArray);
        return this;
    }

    /**
     * Checks if the refex properties contain the specified property.
     *
     * @param key the refex property in question
     * @return <code>true</code>, if the refex properties contain the specified
     * property
     */
    public boolean hasProperty(RefexProperty key) {
        return properties.containsKey(key);
    }

    /**
     * Writes this refex member blueprint to the given
     * <code>refexAnalog</code>.
     *
     * @param refexAnalog the refex analog to write this refex blueprint to
     * @throws PropertyVetoException if the new value is not valid
     * @throws IOException signals that an I/O exception has occurred
     */
    public void writeTo(RefexAnalogBI<?> refexAnalog) throws PropertyVetoException, IOException {
        setProperties(refexAnalog);
    }

    /**
     * Sets the properties in the given
     * <code>refexAnalog</code> based on the properties of this refex blueprint.
     *
     * @param refexAnalog the refex analog to write this refex blueprint to
     * @throws PropertyVetoException if the new value is not valid
     * @throws IOException signals that an I/O exception has occurred
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
                case FLOAT1:
                    RefexFloatAnalogBI<?> floatPart = (RefexFloatAnalogBI<?>) refexAnalog;
                    floatPart.setFloat1((Float.parseFloat(entry.getValue().toString())));
                    break;

                default:
                    throw new RuntimeException("Can't handle: " + entry.getKey());
            }
        }
    }

    /**
     * Sets the properties in the given
     * <code>refexAnalog</code> based on the properties of this refex blueprint.
     * Does not set the status property.
     *
     * @param refexAnalog the refex analog to write this refex blueprint to
     * @throws PropertyVetoException if the new value is not valid
     * @throws IOException signals that an I/O exception has occurred
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
                case FLOAT1:
                    RefexFloatAnalogBI<?> floatPart = (RefexFloatAnalogBI<?>) refexAnalog;
                    floatPart.setFloat1((Float.parseFloat(entry.getValue().toString())));
                    break;
                default:
                    throw new RuntimeException("Can't handle: " + entry.getKey());
            }
        }
    }

    /**
     * Validates this refex blueprint's properties against the given
     * <code>refexVersion</code>.
     *
     * @param refexVersion the refex version
     * @return <code>true</code>, if this refex blueprint's properties are equal to
     * the specified refex version
     * @see RefexCAB.RefexProperty
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
                    if (!RefexArrayOfBytearrayVersionBI.class.isAssignableFrom(refexVersion.getClass())) {
                        return false;
                    }
                    RefexArrayOfBytearrayVersionBI<?> arrayPart = (RefexArrayOfBytearrayVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(arrayPart.getArrayOfByteArray())) {
                        return false;
                    }
                    break;
                case FLOAT1:
                    if(!RefexFloatVersionBI.class.isAssignableFrom(refexVersion.getClass())){
                        return false;
                    }
                    RefexFloatVersionBI<?> floatPart = (RefexFloatVersionBI<?>) refexVersion;
                    if (!entry.getValue().equals(floatPart.getFloat1())) {
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
     * Gets an integer representation of the value associated with the given
     * refex property <code>key</code>.
     *
     * @param key the refex property key
     * @return an integer representation of the value associated with the specified
     * refex property
     */
    public int getInt(RefexProperty key) {
        return (Integer) properties.get(key);
    }

    /**
     * Gets the nid of the refex collection concept associated with this refex
     * blueprint.
     *
     * @return the refex collection nid
     */
    public int getRefexCollectionNid() {
        return getInt(RefexProperty.COLLECTION_NID);
    }

    /**
     * Gets the uuid of the referenced component associated with this refex
     * blueprint.
     *
     * @return the referenced component uuid
     */
    public UUID getReferencedComponentUuid() {
        return (UUID) properties.get(RefexProperty.RC_UUID);
    }

    /**
     * Gets the string associated with this refex blueprint. Will throw an assertion
     * error if the given key is not <code>RefexProperty.STRING1<code>.
     *
     * @param key RefexProperty.STRING1
     * @return the string associated with this refex blueprint
     */
    public String getString(RefexProperty key) {
        assert key == RefexProperty.STRING1;
        return (String) properties.get(key);
    }

    /**
     * Gets the boolean associated with this refex blueprint. Will throw an assertion
     * error if the given key is not <code>RefexProperty.BOOLEAN1<code>.
     *
     * @param key RefexProperty.BOOLEAN1
     * @return the boolean associated with this refex blueprint
     */
    public boolean getBoolean(RefexProperty key) {
        assert key == RefexProperty.BOOLEAN1;
        return (Boolean) properties.get(key);
    }

    /**
     * Gets the refex member uuid of this refex blueprint. Will throw an assertion
     * error if the given key is not <code>RefexProperty.MEMBER_UUID<code>.
     *
     * @param key RefexProperty.MEMBER_UUID
     * @return the refex member uuid
     */
    public UUID getMemberUuid(RefexProperty key) {
        assert key == RefexProperty.MEMBER_UUID;
        return (UUID) properties.get(key);
    }

    /**
     * Gets the refex member uuid of this refex blueprint.
     *
     * @return the refex member uuid
     */
    public UUID getMemberUUID() {
        return (UUID) properties.get(RefexProperty.MEMBER_UUID);
    }

    /**
     * Gets the refex member type of the refex associated with this refex blueprint.
     *
     * @return the refex member type
     */
    public TK_REFEX_TYPE getMemberType() {
        return memberType;
    }

    /**
     * Sets the refex member type of the refex associated with this refex blueprint.
     *
     * @param memberType the refex member type
     */
    public void setMemberType(TK_REFEX_TYPE memberType) {
        this.memberType = memberType;
    }

    /**
     *Computes the uuid of the refex member and sets the member uuid property.
     * Uses
     * <code>computeMemberContentUuid()</code> to compute the uuid.
     *
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws IOException signals that an I/O exception has occurred
     */
    public void setContentUuid() throws InvalidCAB, IOException {
        this.properties.put(RefexProperty.MEMBER_UUID,
                computeMemberContentUuid());
    }
}
