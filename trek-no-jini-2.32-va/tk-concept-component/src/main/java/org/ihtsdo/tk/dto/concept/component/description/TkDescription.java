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

/**
 * The Class TkDescription represents a concept attribute in the eConcept format
 * and contains methods for interacting with a description. Further discussion
 * of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 * @param <V> the generic revision type
 */
public class TkDescription extends TkComponent<TkDescriptionRevision> implements I_DescribeExternally {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The the uuid of the enclosing concept.
     */
    public UUID conceptUuid;
    /**
     * The boolean value indicating if the description text is initial case
     * significant.
     */
    public boolean initialCaseSignificant;
    /**
     * The a two character abbreviation of language of the description text.
     */
    public String lang;
    /**
     * The text associated with a description.
     */
    public String text;
    /**
     * The uuid representing the type of a description.
     */
    public UUID typeUuid;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Description.
     */
    public TkDescription() {
        super();
    }

    /**
     * Instantiates a new TK Description based on the
     * <code>descriptionChronicle</code>.
     *
     * @param descriptionChronicle the description chronicle specifying how to
     * construct this TK Description
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkDescription(DescriptionChronicleBI descriptionChronicle) throws IOException {
        this(descriptionChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Description based on the
     * <code>descriptionVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param descriptionVersion the description version specifying how to
     * construct this TK Description
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
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
     * Instantiates a new TK Description based on the specified data input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK
     * Description
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkDescription(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Description based on
     * <code>another</code> TK Description and allows for uuid conversion.
     *
     * @param another the TK Description specifying how to construct this TK
     * Description
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Description
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Description based on the conversion map
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
     * Instantiates a new TK Description based on a
     * <code>descriptionVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param descriptionVersion the description version specifying how to
     * construct this TK Description
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Description
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Description
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Description based on the conversion map
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a component is
     * found for the specified view coordinate
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
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EDescription</tt> object, and contains the same values, field by
     * field, as this <tt>EDescription</tt>.
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

    /**
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Description
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Description based on the conversion map
     * @return the converted TK Description
     */
    @Override
    public TkDescription makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkDescription(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Description
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
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
     * Returns a string representation of this TK Description object.
     *
     * @return a string representation of this TK Description object including
     * the enclosing concept, initial case sensitivity, language, and type
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

    /**
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
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
     * Gets the uuid of the enclosing concept.
     *
     * @return the uuid of the enclosing concept
     */
    public UUID getConceptUuid() {
        return conceptUuid;
    }

    /**
     *
     * @return the two character abbreviation of the language of the description
     * text
     */
    @Override
    public String getLang() {
        return lang;
    }

    /**
     *
     * @return a list of revisions on this TK Description
     */
    @Override
    public List<TkDescriptionRevision> getRevisionList() {
        return revisions;
    }

    /**
     *
     * @return a String representing the text associated with this TK
     * Description
     */
    @Override
    public String getText() {
        return text;
    }

    /**
     *
     * @return the uuid of the type of this TK Description
     */
    @Override
    public UUID getTypeUuid() {
        return typeUuid;
    }

    /**
     *
     * @return <code>true</code>, if the text of this TK Description is case
     * significant
     */
    @Override
    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets uuid associated with the enclosing concept of this TK Description.
     *
     * @param conceptUuid the uuid associated with the enclosing concept
     */
    public void setConceptUuid(UUID conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    /**
     * Indicates that the text associated with this TK Description is initial
     * case significant.
     *
     * @param initialCaseSignificant set to <code>true</code> to indicate that
     * the description text is initial case significant
     */
    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificant = initialCaseSignificant;
    }

    /**
     * Sets the language associated with the text of this TK Description.
     *
     * @param lang the two character String abbreviation of the language of the description text
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Sets the text associated with this TK Description.
     *
     * @param text the String representing the description text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Sets the uuid for the type associated with this TK Description.
     *
     * @param typeUuid the uuid representing the description type
     */
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }
}
