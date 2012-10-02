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
package org.ihtsdo.tk.dto.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.ext.I_DescribeExternally;
import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.TkComponent;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;

// TODO: Auto-generated Javadoc
/**
 * The Class TkDescription.
 */
public class TkDescription extends TkComponent<TkDescriptionRevision> implements I_DescribeExternally {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The concept uuid. */
    public UUID conceptUuid;
    
    /** The initial case significant. */
    public boolean initialCaseSignificant;
    
    /** The lang. */
    public String lang;
    
    /** The text. */
    public String text;
    
    /** The type uuid. */
    public UUID typeUuid;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk description.
     */
    public TkDescription() {
        super();
    }

    /**
     * Instantiates a new tk description.
     *
     * @param descriptionChronicle the description chronicle
     * @throws IOException signals that an I/O exception has occurred.
     */
    public TkDescription(DescriptionChronicleBI descriptionChronicle) throws IOException {
        this(descriptionChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk description.
     *
     * @param descriptionVersion the description version
     * @param revisionHandling the revision handling
     * @throws IOException signals that an I/O exception has occurred.
     */
    public TkDescription(DescriptionVersionBI descriptionVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(descriptionVersion);
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            conceptUuid = ts.getUuidPrimordialForNid(descriptionVersion.getConceptNid());
            initialCaseSignificant = descriptionVersion.isInitialCaseSignificant();
            lang = descriptionVersion.getLang();
            text = descriptionVersion.getText();
            typeUuid = ts.getUuidPrimordialForNid(descriptionVersion.getTypeNid());
            pathUuid = ts.getUuidPrimordialForNid(descriptionVersion.getPathNid());
            statusUuid = ts.getUuidPrimordialForNid(descriptionVersion.getStatusNid());
            time = descriptionVersion.getTime();
        } else {
            Collection<? extends DescriptionVersionBI> versions = descriptionVersion.getVersions();
            Iterator<? extends DescriptionVersionBI> itr = versions.iterator();
            int partCount = versions.size();
            DescriptionVersionBI version = itr.next();

            conceptUuid = ts.getUuidPrimordialForNid(descriptionVersion.getConceptNid());
            initialCaseSignificant = version.isInitialCaseSignificant();
            lang = version.getLang();
            text = version.getText();
            typeUuid = ts.getUuidPrimordialForNid(version.getTypeNid());
            pathUuid = ts.getUuidPrimordialForNid(version.getPathNid());
            statusUuid = ts.getUuidPrimordialForNid(version.getStatusNid());
            time = version.getTime();

            if (partCount > 1) {
                revisions = new ArrayList<TkDescriptionRevision>(partCount - 1);

                while (itr.hasNext()) {
                    DescriptionVersionBI dv = itr.next();
                    revisions.add(new TkDescriptionRevision(dv));
                }
            }
        }
    }

    /**
     * Instantiates a new tk description.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public TkDescription(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk description.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     */
    public TkDescription(TkDescription another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.conceptUuid = conversionMap.get(another.conceptUuid);
            this.typeUuid = conversionMap.get(another.typeUuid);
        } else {
            this.conceptUuid = another.conceptUuid;
            this.typeUuid = another.typeUuid;
        }

        this.initialCaseSignificant = another.initialCaseSignificant;
        this.lang = another.lang;
        this.text = another.text;
    }

    /**
     * Instantiates a new tk description.
     *
     * @param descriptionVersion the description version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    public TkDescription(DescriptionVersionBI descriptionVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(descriptionVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.conceptUuid = conversionMap.get(Ts.get().getComponent(descriptionVersion.getConceptNid()).getPrimUuid());
            this.typeUuid = conversionMap.get(Ts.get().getComponent(descriptionVersion.getTypeNid()).getPrimUuid());
        } else {
            this.conceptUuid = Ts.get().getComponent(descriptionVersion.getConceptNid()).getPrimUuid();
            this.typeUuid = Ts.get().getComponent(descriptionVersion.getTypeNid()).getPrimUuid();
        }

        this.initialCaseSignificant = descriptionVersion.isInitialCaseSignificant();
        this.lang = descriptionVersion.getLang();
        this.text = descriptionVersion.getText();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>EDescription</tt> object, and contains the same values, field by field,
     * as this <tt>EDescription</tt>.
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

        if (TkDescription.class.isAssignableFrom(obj.getClass())) {
            TkDescription another = (TkDescription) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare conceptUuid
            if (!this.conceptUuid.equals(another.conceptUuid)) {
                return false;
            }

            // Compare initialCaseSignificant
            if (this.initialCaseSignificant != another.initialCaseSignificant) {
                return false;
            }

            // Compare lang
            if (!this.lang.equals(another.lang)) {
                return false;
            }

            // Compare text
            if (!this.text.equals(another.text)) {
                return false;
            }

            // Compare typeUuid
            if (!this.typeUuid.equals(another.typeUuid)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkDescription makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkDescription(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#readExternal(java.io.DataInput, int)
     */
    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        conceptUuid = new UUID(in.readLong(), in.readLong());
        initialCaseSignificant = in.readBoolean();
        lang = in.readUTF();
        text = UtfHelper.readUtfV6(in, dataVersion);
        typeUuid = new UUID(in.readLong(), in.readLong());

        int versionLength = in.readInt();

        if (versionLength > 0) {
            revisions = new ArrayList<TkDescriptionRevision>(versionLength);

            for (int i = 0; i < versionLength; i++) {
                revisions.add(new TkDescriptionRevision(in, dataVersion));
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
        buff.append("'").append(this.text).append("'");
        buff.append(" concept:");
        buff.append(informAboutUuid(this.conceptUuid));
        buff.append(" ics:");
        buff.append(this.initialCaseSignificant);
        buff.append(" lang:");
        buff.append("'").append(this.lang).append("'");
        buff.append(" type:");
        buff.append(informAboutUuid(this.typeUuid));
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
        out.writeLong(conceptUuid.getMostSignificantBits());
        out.writeLong(conceptUuid.getLeastSignificantBits());
        out.writeBoolean(initialCaseSignificant);
        out.writeUTF(lang);
        UtfHelper.writeUtf(out, text);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkDescriptionRevision edv : revisions) {
                edv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the concept uuid.
     *
     * @return the concept uuid
     */
    public UUID getConceptUuid() {
        return conceptUuid;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ext.I_DescribeExternally#getLang()
     */
    @Override
    public String getLang() {
        return lang;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    @Override
    public List<TkDescriptionRevision> getRevisionList() {
        return revisions;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ext.I_DescribeExternally#getText()
     */
    @Override
    public String getText() {
        return text;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ext.I_DescribeExternally#getTypeUuid()
     */
    @Override
    public UUID getTypeUuid() {
        return typeUuid;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.ext.I_DescribeExternally#isInitialCaseSignificant()
     */
    @Override
    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the concept uuid.
     *
     * @param conceptUuid the new concept uuid
     */
    public void setConceptUuid(UUID conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    /**
     * Sets the initial case significant.
     *
     * @param initialCaseSignificant the new initial case significant
     */
    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificant = initialCaseSignificant;
    }

    /**
     * Sets the lang.
     *
     * @param lang the new lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Sets the text.
     *
     * @param text the new text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Sets the type uuid.
     *
     * @param typeUuid the new type uuid
     */
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }
}
