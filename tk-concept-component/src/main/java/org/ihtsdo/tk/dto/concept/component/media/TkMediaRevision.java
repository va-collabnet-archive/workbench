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
package org.ihtsdo.tk.dto.concept.component.media;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkMediaRevision represents a version of a description in the
 * eConcept format and contains methods for interacting with a version of a
 * description. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkMediaRevision extends TkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The text associated with a description of the media.
     */
    public String textDescription;
    /**
     * The uuid representing the type of media.
     */
    public UUID typeUuid;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Media Revision.
     */
    public TkMediaRevision() {
        super();
    }

    /**
     * Instantiates a new TK Media Revision based on the
     * <code>mediaVersion</code>.
     *
     * @param mediaVersion the media version specifying how to construct
     * this TK Media Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkMediaRevision(MediaVersionBI mediaVersion) throws IOException {
        super(mediaVersion);
        this.textDescription = mediaVersion.getTextDescription();
        this.typeUuid = Ts.get().getUuidPrimordialForNid(mediaVersion.getTypeNid());
    }

    /**
     * Instantiates a new TK Media Revision based on the specified data input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Media Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkMediaRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Media Revision based on
     * <code>another</code> TK Media and allows for uuid conversion..
     *
     * @param another the TK Media Revision specifying how to construct this TK Media
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Media Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Media Revision based on the conversion map
     */
    public TkMediaRevision(TkMediaRevision another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.textDescription = another.textDescription;

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
     * <tt>EImageVersion</tt> object, and contains the same values, field by
     * field, as this <tt>EImageVersion</tt>.
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

        if (TkMediaRevision.class.isAssignableFrom(obj.getClass())) {
            TkMediaRevision another = (TkMediaRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare textDescription
            if (!this.textDescription.equals(another.textDescription)) {
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
     * TK Media Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Media Revision based on the conversion map
     * @return the converted TK Media Revision
     */
    @Override
    public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkMediaRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Media Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        textDescription = in.readUTF();
        typeUuid = new UUID(in.readLong(), in.readLong());
    }

    /**
     * Returns a string representation of this TK Media object.
     *
     * @return a string representation of this TK Media object including 
     * the description and type of media.
     *
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" desc:");
        buff.append("'").append(this.textDescription).append("'");
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
        out.writeUTF(textDescription);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the text describing this media.
     *
     * @return a String representing a description of this media
     */
    public String getTextDescription() {
        return textDescription;
    }

    /**
     * Gets the uuid representing the type of media.
     *
     * @return the type uuid
     */
    public UUID getTypeUuid() {
        return typeUuid;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the text description of this media.
     *
     * @param textDescription the String representing the text description of
     * this TK Media
     */
    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
    }

    /**
     * Sets the uuid for the type associated with this TK Media.
     *
     * @param typeUuid the uuid representing the media type
     */
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }
}
