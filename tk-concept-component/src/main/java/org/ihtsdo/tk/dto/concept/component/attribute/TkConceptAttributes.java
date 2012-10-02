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

// TODO: Auto-generated Javadoc
/**
 * The Class TkConceptAttributes.
 */
public class TkConceptAttributes extends TkComponent<TkConceptAttributesRevision>
        implements I_ConceptualizeExternally {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The defined. */
    public boolean defined;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk concept attributes.
     */
    public TkConceptAttributes() {
        super();
    }

    /**
     * Instantiates a new tk concept attributes.
     *
     * @param conceptAttributeChronicle the concept attribute chronicle
     * @throws IOException signals that an I/O exception has occurred.
     */
    public TkConceptAttributes(ConceptAttributeChronicleBI conceptAttributeChronicle) throws IOException {
        this(conceptAttributeChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk concept attributes.
     *
     * @param conceptAttributeVersion the concept attribute version
     * @param revisionHandling the revision handling
     * @throws IOException signals that an I/O exception has occurred.
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
     * Instantiates a new tk concept attributes.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public TkConceptAttributes(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk concept attributes.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     */
    public TkConceptAttributes(TkConceptAttributes another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.defined = another.defined;
    }

    /**
     * Instantiates a new tk concept attributes.
     *
     * @param conceptAttributeVersion the concept attribute version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    public TkConceptAttributes(ConceptAttributeVersionBI conceptAttributeVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(conceptAttributeVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.defined = conceptAttributeVersion.isDefined();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>EConceptAttributes</tt> object, and contains the same values, field by
     * field, as this <tt>EConceptAttributes</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code>, if successful
     * <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkConceptAttributes makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkConceptAttributes(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#readExternal(java.io.DataInput, int)
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
     * Returns a string representation of the object.
     *
     * @return the string
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#writeExternal(java.io.DataOutput)
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
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    @Override
    public List<TkConceptAttributesRevision> getRevisionList() {
        return revisions;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ext.I_ConceptualizeExternally#isDefined()
     */
    @Override
    public boolean isDefined() {
        return defined;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the defined.
     *
     * @param defined the new defined
     */
    public void setDefined(boolean defined) {
        this.defined = defined;
    }
}
