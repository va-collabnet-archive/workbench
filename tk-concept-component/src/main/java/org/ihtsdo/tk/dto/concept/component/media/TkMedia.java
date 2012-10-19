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
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkComponent;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;

/**
 * The Class TkMedia represents a type of media in the eConcept format and
 * contains methods for interacting with a type of media. Further discussion of
 * the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 * @param <V> the generic revision type
 */
public class TkMedia extends TkComponent<TkMediaRevision> {

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
     * The data bytes representing the media.
     */
    public byte[] dataBytes;
    /**
     * The String representing the format of this media.
     */
    public String format;
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
     * Instantiates a new TK Media.
     */
    public TkMedia() {
        super();
    }

    /**
     * Instantiates a new TK Media based on the
     * <code>mediaChronicle</code>.
     *
     * @param mediaChronicle the media chronicle specifying how to construct
     * this TK Media
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkMedia(MediaChronicleBI mediaChronicle) throws IOException {
        this(mediaChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Media based on the
     * <code>descriptionVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param mediaVersion the media version specifying how to construct this TK
     * Media
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkMedia(MediaVersionBI mediaVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(mediaVersion);
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.conceptUuid = ts.getUuidPrimordialForNid(mediaVersion.getConceptNid());
            this.typeUuid = ts.getUuidPrimordialForNid(mediaVersion.getTypeNid());
            this.dataBytes = mediaVersion.getMedia();
            this.format = mediaVersion.getFormat();
            this.textDescription = mediaVersion.getTextDescription();
        } else {
            Collection<? extends MediaVersionBI> media = mediaVersion.getVersions();
            int partCount = media.size();
            Iterator<? extends MediaVersionBI> itr = media.iterator();
            MediaVersionBI mv = itr.next();

            this.conceptUuid = ts.getUuidPrimordialForNid(mv.getConceptNid());
            this.typeUuid = ts.getUuidPrimordialForNid(mv.getTypeNid());
            this.dataBytes = mv.getMedia();
            this.format = mv.getFormat();
            this.textDescription = mv.getTextDescription();

            if (partCount > 1) {
                revisions = new ArrayList<TkMediaRevision>(partCount - 1);

                while (itr.hasNext()) {
                    mv = itr.next();
                    revisions.add(new TkMediaRevision(mv));
                }
            }
        }
    }

    /**
     * Instantiates a new TK Media based on the specified data input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Media
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkMedia(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Media based on
     * <code>another</code> TK Media and allows for uuid conversion.
     *
     * @param another the TK Media specifying how to construct this TK Media
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Media
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Media based on the conversion map
     */
    public TkMedia(TkMedia another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.conceptUuid = conversionMap.get(another.conceptUuid);
            this.dataBytes = another.dataBytes;
            this.format = another.format;
            this.textDescription = another.textDescription;
            this.typeUuid = conversionMap.get(another.typeUuid);
        } else {
            this.conceptUuid = another.conceptUuid;
            this.dataBytes = another.dataBytes;
            this.format = another.format;
            this.textDescription = another.textDescription;
            this.typeUuid = another.typeUuid;
        }
    }

    /**
     * Instantiates a new TK Media based on a
     * <code>mediaVersion</code> and allows for uuid conversion. Can exclude
     * components based on their nid.
     *
     * @param mediaVersion the media version specifying how to construct this TK
     * Description
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Media
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Media
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Media based on the conversion map
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a component is
     * found for the specified view coordinate
     */
    public TkMedia(MediaVersionBI mediaVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(mediaVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.conceptUuid = conversionMap.get(Ts.get().getComponent(mediaVersion.getConceptNid()).getPrimUuid());
            this.typeUuid = conversionMap.get(Ts.get().getComponent(mediaVersion.getTypeNid()).getPrimUuid());
        } else {
            this.conceptUuid = Ts.get().getComponent(mediaVersion.getConceptNid()).getPrimUuid();
            this.typeUuid = Ts.get().getComponent(mediaVersion.getTypeNid()).getPrimUuid();
        }

        this.dataBytes = mediaVersion.getMedia();
        this.format = mediaVersion.getFormat();
        this.textDescription = mediaVersion.getTextDescription();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a <tt>EImage</tt>
     * object, and contains the same values, field by field, as this
     * <tt>EImage</tt>.
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

        if (TkMedia.class.isAssignableFrom(obj.getClass())) {
            TkMedia another = (TkMedia) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare conceptUuid
            if (!this.conceptUuid.equals(another.conceptUuid)) {
                return false;
            }

            // Compare format
            if (!this.format.equals(another.format)) {
                return false;
            }

            // Compare image (had to loop through the array)
            for (int i = 0; i < this.dataBytes.length; i++) {
                if (this.dataBytes[i] != another.dataBytes[i]) {
                    return false;
                }
            }

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
     * Returns a hash code for this
     * <code>EImage</code>.
     *
     * @return a hash code value for this <tt>EImage</tt>.
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
     * TK Media
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Media based on the conversion map
     * @return the converted TK Media
     */
    @Override
    public TkMedia makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkMedia(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Media
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        conceptUuid = new UUID(in.readLong(), in.readLong());
        format = in.readUTF();

        int imageSize = in.readInt();

        dataBytes = new byte[imageSize];
        in.readFully(dataBytes);
        textDescription = in.readUTF();
        typeUuid = new UUID(in.readLong(), in.readLong());

        int versionLength = in.readInt();

        if (versionLength > 0) {
            revisions = new ArrayList<TkMediaRevision>(versionLength);

            for (int i = 0; i < versionLength; i++) {
                revisions.add(new TkMediaRevision(in, dataVersion));
            }
        }
    }

    /**
     * Returns a string representation of this TK Media object.
     *
     * @return a string representation of this TK Media object including the
     * enclosing concept, format, image, description, and media type.
     *
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" concept:");
        buff.append(informAboutUuid(this.conceptUuid));
        buff.append(" format:");
        buff.append("'").append(this.format).append("'");
        buff.append(" image:");
        buff.append(new String(this.dataBytes));
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
        out.writeLong(conceptUuid.getMostSignificantBits());
        out.writeLong(conceptUuid.getLeastSignificantBits());
        out.writeUTF(format);
        out.writeInt(dataBytes.length);
        out.write(dataBytes);
        out.writeUTF(textDescription);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkMediaRevision eiv : revisions) {
                eiv.writeExternal(out);
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
     * Gets the data bytes containing the media.
     *
     * @return the media data bytes
     */
    public byte[] getDataBytes() {
        return dataBytes;
    }

    /**
     * Gets the format of the media.
     *
     * @return a String representation of the format of the media
     */
    public String getFormat() {
        return format;
    }

    /**
     *
     * @return a list of revisions on this TK Media
     */
    @Override
    public List<TkMediaRevision> getRevisionList() {
        return revisions;
    }

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
     * Sets uuid associated with the enclosing concept of this TK Description.
     *
     * @param conceptUuid the uuid associated with the enclosing concept
     */
    public void setConceptUuid(UUID conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    /**
     * Sets the data bytes containing the media.
     *
     * @param data the data bytes representing the media
     */
    public void setDataBytes(byte[] data) {
        this.dataBytes = data;
    }

    /**
     * Sets the String representing the format of this TK Media.
     *
     * @param format the String representing the format of this TK Media
     */
    public void setFormat(String format) {
        this.format = format;
    }

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
