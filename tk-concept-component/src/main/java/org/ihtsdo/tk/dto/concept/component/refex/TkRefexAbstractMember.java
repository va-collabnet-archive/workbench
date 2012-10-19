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
package org.ihtsdo.tk.dto.concept.component.refex;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRefexAbstractMember represents a refex member in the eConcept
 * format and contains methods generic for interacting with a refex member.
 * Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 * @param <V> the generic revision type
 */
public abstract class TkRefexAbstractMember<V extends TkRevision> extends TkComponent<V> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid of the referenced component.
     */
    public UUID componentUuid;
    /**
     * The uuid of the refex member.
     */
    public UUID refsetUuid;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Abstract Member.
     */
    public TkRefexAbstractMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Abstract Member based on the
     * <code>refexVersion</code>.
     *
     * @param refexVersion the refex version specifying how to construct this TK
     * Refex Abstract Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexAbstractMember(RefexVersionBI refexVersion) throws IOException {
        super(refexVersion);
        this.componentUuid = Ts.get().getComponent(refexVersion.getReferencedComponentNid()).getPrimUuid();
        this.refsetUuid = Ts.get().getComponent(refexVersion.getRefexNid()).getPrimUuid();
    }

    /**
     * Instantiates a new TK Refex Abstract Member based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in in the data input specifying how to construct this TK Refex
     * Abstract Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexAbstractMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Abstract Member based on
     * <code>another</code> TK Refex Abstract Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Abstract Member specifying how to construct
     * this TK Refex Abstract Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Abstract Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Abstract Member based on the conversion map
     */
    public TkRefexAbstractMember(TkRefexAbstractMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.componentUuid = conversionMap.get(another.componentUuid);
            this.refsetUuid = conversionMap.get(another.refsetUuid);
        } else {
            this.componentUuid = another.componentUuid;
            this.refsetUuid = another.refsetUuid;
        }
    }

    /**
     * Instantiates a new TK Refex Abstract Member based on a
     * <code>refexVersion</code> and allows for uuid conversion. Can exclude
     * components based on their nid.
     *
     * @param refexVersion the refex version specifying how to construct this TK
     * Refex Abstract Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Abstract Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Abstract Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Member based on the conversion map
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefexAbstractMember(RefexVersionBI refexVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll,
            ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.componentUuid =
                    conversionMap.get(Ts.get().getComponent(refexVersion.getReferencedComponentNid()).getPrimUuid());
            this.refsetUuid = conversionMap.get(Ts.get().getComponent(refexVersion.getRefexNid()).getPrimUuid());
        } else {
            this.componentUuid = Ts.get().getComponent(refexVersion.getReferencedComponentNid()).getPrimUuid();
            this.refsetUuid = Ts.get().getComponent(refexVersion.getRefexNid()).getPrimUuid();
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a <tt>ERefset</tt>
     * object, and contains the same values, field by field, as this
     * <tt>ERefset</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; <code>false</code>
     * otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkRefexAbstractMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexAbstractMember<?> another = (TkRefexAbstractMember<?>) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare refsetUuid
            if (!this.refsetUuid.equals(another.refsetUuid)) {
                return false;
            }

            // Compare componentUuid
            if (!this.componentUuid.equals(another.componentUuid)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefset</code>.
     *
     * @return a hash code value for this <tt>ERefset</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Description
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        refsetUuid = new UUID(in.readLong(), in.readLong());
        componentUuid = new UUID(in.readLong(), in.readLong());
    }

    /**
     * Returns a string representation of this TK Refex Abstract Member object.
     *
     * @return a string representation of this TK Refex Abstract Member object
     * including the refex collection concept and the referenced component.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(" refex:");
        buff.append(informAboutUuid(this.refsetUuid));
        buff.append(" component:");
        buff.append(informAboutUuid(this.componentUuid));
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    /**
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(refsetUuid.getMostSignificantBits());
        out.writeLong(refsetUuid.getLeastSignificantBits());
        out.writeLong(componentUuid.getMostSignificantBits());
        out.writeLong(componentUuid.getLeastSignificantBits());
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the uuid of the referenced component associated with this TK Refex
     * Abstract Member.
     *
     * @return the uuid of the referenced component
     */
    public UUID getComponentUuid() {
        return componentUuid;
    }

    /**
     * Gets the uuid of the refex collection associated with this TK Refex
     * Abstract Member.
     *
     * @return the uuid of the refex collection
     */
    public UUID getRefexUuid() {
        return refsetUuid;
    }

    /**
     * Gets the refex type of this TK Refex Abstract Member.
     *
     * @return the TK_REFEX_TYPE representing the refex type
     */
    public abstract TK_REFEX_TYPE getType();

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid of the referenced component associated with this TK Refex
     * Abstract Member.
     *
     * @param componentUuid the uuid of the referenced component
     */
    public void setComponentUuid(UUID componentUuid) {
        this.componentUuid = componentUuid;
    }

    /**
     * Sets the uuid of the refex collection associated with this TK Refex
     * Abstract Member.
     *
     * @param refsetUuid the uuid of the refex collection
     */
    public void setRefsetUuid(UUID refsetUuid) {
        this.refsetUuid = refsetUuid;
    }
}
