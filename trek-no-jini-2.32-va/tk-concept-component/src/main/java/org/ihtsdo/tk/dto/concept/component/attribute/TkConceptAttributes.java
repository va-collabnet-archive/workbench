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
package org.ihtsdo.tk.dto.concept.component.attribute;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.ext.I_ConceptualizeExternally;
import org.ihtsdo.tk.dto.concept.component.TkComponent;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;

/**
 * The Class TkConceptAttributes represents a concept attribute in the eConcept
 * format and contains methods for interacting with a concept attribute. Further
 * discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 * @param <V> the generic revision type
 */
public class TkConceptAttributes extends TkComponent<TkConceptAttributesRevision>
        implements I_ConceptualizeExternally {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing its
     * own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The boolean value indicating if the concept associated with these concept
     * attributes is defined.
     */
    public boolean defined;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Concept Attributes.
     */
    public TkConceptAttributes() {
        super();
    }

    /**
     * Instantiates a new TK Concept Attributes based on the
     * <code>conceptAttributeChronicle</code>.
     *
     * @param conceptAttributeChronicle the concept attribute chronicle
     * specifying how to construct this TK Concept Attribute
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkConceptAttributes(ConceptAttributeChronicleBI conceptAttributeChronicle) throws IOException {
        this(conceptAttributeChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Concept Attributes based on the
     * <code>conceptAttributeVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param conceptAttributeVersion the concept attribute version specifying
     * how to construct this TK Concept Attribute
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkConceptAttributes(ConceptAttributeVersionBI conceptAttributeVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(conceptAttributeVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.defined = conceptAttributeVersion.isDefined();
        } else {
            Collection<? extends ConceptAttributeVersionBI> versions = conceptAttributeVersion.getVersions();
            Iterator<? extends ConceptAttributeVersionBI> itr = versions.iterator();
            ConceptAttributeVersionBI vers = itr.next();
            this.defined = vers.isDefined();

            if (versions.size() > 1) {
                revisions = new ArrayList<TkConceptAttributesRevision>(versions.size() - 1);

                while (itr.hasNext()) {
                    vers = itr.next();
                    revisions.add(new TkConceptAttributesRevision(vers));
                }
            }
        }

    }

    /**
     * Instantiates a new TK Concept Attributes based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Concept
     * Attributes
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkConceptAttributes(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Concept Attributes based on
     * <code>another</code> TK Concept Attributes and allows for uuid
     * conversion.
     *
     * @param another the TK Concept Attributes specifying how to construct this
     * TK Concept Attributes
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Concept Attributes
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Concept Attributes based on the conversion map
     */
    public TkConceptAttributes(TkConceptAttributes another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.defined = another.defined;
    }

    /**
     * Instantiates a new TK Concept Attributes based on a
     * <code>conceptAttributeVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param conceptAttributeVersion the concept attribute version specifying
     * how to construct this TK Concept Attributes
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Concept Attributes
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Concept Attributes
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Concept Attributes based on the conversion map
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a component is
     * found for the specified view coordinate
     */
    public TkConceptAttributes(ConceptAttributeVersionBI conceptAttributeVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(conceptAttributeVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.defined = conceptAttributeVersion.isDefined();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EConceptAttributes</tt> object, and contains the same values, field
     * by field, as this <tt>EConceptAttributes</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code>, if successful <code>true</code> if the objects
     * are the same; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            TkConceptAttributes another = (TkConceptAttributes) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare defined
            if (this.defined != another.defined) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>EConceptAttributes</code>.
     *
     * @return a hash code value for this <tt>EConceptAttributes</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Concept Attributes
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Concept Attributes
     * based on the conversion map
     * @return the converted TK Concept Attributes
     */
    @Override
    public TkConceptAttributes makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkConceptAttributes(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Concept
     * Attributes
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        defined = in.readBoolean();

        int versionCount = in.readInt();

        assert versionCount < 1024 : "Version count is: " + versionCount;

        if (versionCount > 0) {
            revisions = new ArrayList<TkConceptAttributesRevision>(versionCount);

            for (int i = 0; i < versionCount; i++) {
                revisions.add(new TkConceptAttributesRevision(in, dataVersion));
            }
        }
    }

    /**
     * Returns a String representation of this TK Concept Attributes object.
     *
     * @return a String representation of this TK Concept Attributes object
     * including if the concept is defined or not
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" defined: ");
        buff.append(this.defined);
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
        out.writeBoolean(defined);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            assert revisions.size() < 1024 : "Version count is: " + revisions.size() + "\n\n" + this.toString();
            out.writeInt(revisions.size());

            for (TkConceptAttributesRevision cav : revisions) {
                cav.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     *
     * @return a list of revisions on this TK Concept Attribute
     */
    @Override
    public List<TkConceptAttributesRevision> getRevisionList() {
        return revisions;
    }

    /**
     *
     * @return <code>true</code> if associated concept is defined
     */
    @Override
    public boolean isDefined() {
        return defined;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Indicates the associated concept is defined.
     *
     * @param defined set to <code>true</code> to indicate the associated
     * concept is defined
     */
    public void setDefined(boolean defined) {
        this.defined = defined;
    }
}
