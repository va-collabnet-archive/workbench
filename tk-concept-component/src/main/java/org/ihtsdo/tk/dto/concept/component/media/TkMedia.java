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

// TODO: Auto-generated Javadoc
/**
 * The Class TkMedia.
 */
public class TkMedia extends TkComponent<TkMediaRevision> {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The concept uuid. */
    public UUID conceptUuid;
    
    /** The data bytes. */
    public byte[] dataBytes;
    
    /** The format. */
    public String format;
    
    /** The text description. */
    public String textDescription;
    
    /** The type uuid. */
    public UUID typeUuid;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk media.
     */
    public TkMedia() {
        super();
    }

    /**
     * Instantiates a new tk media.
     *
     * @param mediaChronicle the media chronicle
     * @throws IOException signals that an I/O exception has occurred.
     */
    public TkMedia(MediaChronicleBI mediaChronicle) throws IOException {
        this(mediaChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk media.
     *
     * @param mediaVersion the media version
     * @param revisionHandling the revision handling
     * @throws IOException signals that an I/O exception has occurred.
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
     * Instantiates a new tk media.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public TkMedia(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk media.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
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
     * Instantiates a new tk media.
     *
     * @param mediaVersion the media version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
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
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>EImage</tt> object, and contains the same values, field by field, as
     * this <tt>EImage</tt>.
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkMedia makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkMedia(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#readExternal(java.io.DataInput, int)
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
     * Returns a string representation of the object.
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#writeExternal(java.io.DataOutput)
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
     * Gets the concept uuid.
     *
     * @return the concept uuid
     */
    public UUID getConceptUuid() {
        return conceptUuid;
    }

    /**
     * Gets the data bytes.
     *
     * @return the data bytes
     */
    public byte[] getDataBytes() {
        return dataBytes;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    @Override
    public List<TkMediaRevision> getRevisionList() {
        return revisions;
    }

    /**
     * Gets the text description.
     *
     * @return the text description
     */
    public String getTextDescription() {
        return textDescription;
    }

    /**
     * Gets the type uuid.
     *
     * @return the type uuid
     */
    public UUID getTypeUuid() {
        return typeUuid;
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
     * Sets the data bytes.
     *
     * @param data the new data bytes
     */
    public void setDataBytes(byte[] data) {
        this.dataBytes = data;
    }

    /**
     * Sets the format.
     *
     * @param format the new format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Sets the text description.
     *
     * @param textDescription the new text description
     */
    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
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
