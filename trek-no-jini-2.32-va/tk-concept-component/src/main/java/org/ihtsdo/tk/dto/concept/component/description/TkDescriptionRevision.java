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
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.ext.I_DescribeExternally;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkDescriptionRevision represents a version of a description in the
 * eConcept format and contains methods for interacting with a version of a
 * description. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkDescriptionRevision extends TkRevision implements I_DescribeExternally {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
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
     * Instantiates a new TK Description Revision.
     */
    public TkDescriptionRevision() {
        super();
    }

    /**
     * Instantiates a new TK Description Revision based on the
     * <code>descriptionVersion</code>.
     *
     * @param descriptionVersion the description version specifying how to
     * construct this TK Description
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkDescriptionRevision(DescriptionVersionBI descriptionVersion) throws IOException {
        super(descriptionVersion);
        this.initialCaseSignificant = descriptionVersion.isInitialCaseSignificant();
        this.lang = descriptionVersion.getLang();
        this.text = descriptionVersion.getText();
        this.typeUuid = Ts.get().getUuidPrimordialForNid(descriptionVersion.getTypeNid());
    }

    /**
     * Instantiates a new TK Description Revision based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in in the data input specifying how to construct this TK
     * Description Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkDescriptionRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Description Revision based on
     * <code>another</code> TK Description Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Description Revision specifying how to construct
     * this TK Description Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Concept Attributes
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Description Revision based on the conversion map
     */
    public TkDescriptionRevision(TkDescriptionRevision another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.initialCaseSignificant = another.initialCaseSignificant;
        this.lang = another.lang;
        this.text = another.text;

        if (mapAll) {
            this.typeUuid = conversionMap.get(another.typeUuid);
        } else {
            this.typeUuid = another.typeUuid;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EDescriptionVersion</tt> object, and contains the same values, field
     * by field, as this <tt>EDescriptionVersion</tt>.
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

        if (TkDescriptionRevision.class.isAssignableFrom(obj.getClass())) {
            TkDescriptionRevision another = (TkDescriptionRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
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
     * TK Description Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Description Revision based on the conversion map
     * @return the converted TK Description Revision
     */
    @Override
    public TkDescriptionRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkDescriptionRevision(this, conversionMap, offset, mapAll);
    }

    /**
     * 
     * @param in the data input specifying how to construct this TK Description Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        initialCaseSignificant = in.readBoolean();
        lang = in.readUTF();

        if (dataVersion < 7) {
            text = in.readUTF();
        } else {
            int textlength = in.readInt();

            if (textlength > 32000) {
                int textBytesLength = in.readInt();
                byte[] textBytes = new byte[textBytesLength];

                in.readFully(textBytes);
                text = new String(textBytes, "UTF-8");
            } else {
                text = in.readUTF();
            }
        }

        typeUuid = new UUID(in.readLong(), in.readLong());
    }

    /**
     * Returns a string representation of this TK Description Revision object.
     *
     * @return a string representation of this TK Description Revision object
     * including
     * the enclosing concept, initial case sensitivity, language, and type
     *
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" ics:");
        buff.append(this.initialCaseSignificant);
        buff.append(" lang:");
        buff.append("'").append(this.lang).append("'");
        buff.append(" text:");
        buff.append("'").append(this.text).append("'");
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
        out.writeBoolean(initialCaseSignificant);
        out.writeUTF(lang);
        out.writeInt(text.length());

        if (text.length() > 32000) {
            byte[] textBytes = text.getBytes("UTF-8");

            out.writeInt(textBytes.length);
            out.write(textBytes);
        } else {
            out.writeUTF(text);
        }

        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * 
     * @return the two character abbreviation of the language of the description text
     */
    @Override
    public String getLang() {
        return lang;
    }

    /**
     *
     * @return a String representing the text associated with this TK
     * Description Revision
     */
    @Override
    public String getText() {
        return text;
    }

    /**
     *
     * @return the uuid of the type of this TK Description Revision
     */
    @Override
    public UUID getTypeUuid() {
        return typeUuid;
    }

    /**
     *
     * @return <code>true</code>, if the text of this TK Description Revision is case
     * significant
     */
    @Override
    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Indicates that the text associated with this TK Description Revision is initial
     * case significant.
     *
     * @param initialCaseSignificant set to <code>true</code> to indicate that
     * the description text is initial case significant
     */
    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificant = initialCaseSignificant;
    }

    /**
     * Sets the language associated with the text of this TK Description Revision.
     *
     * @param lang the two character String abbreviation of the language of the description text
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Sets the text associated with this TK Description Revision.
     *
     * @param text the String representing the description text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Sets the uuid of the type associated with this TK Description Revision.
     *
     * @param typeUuid the uuid representing the description type
     */
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }
}
