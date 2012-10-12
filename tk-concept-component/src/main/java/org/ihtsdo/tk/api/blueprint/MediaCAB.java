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
package org.ihtsdo.tk.api.blueprint;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.uuid.UuidT5Generator;

// TODO: Auto-generated Javadoc
/**
 * The Class MediaCAB.
 *
 * @author kec
 */
public class MediaCAB extends CreateOrAmendBlueprint {

    /** The Constant mediaSpecNamespace. */
    public static final UUID mediaSpecNamespace =
            UUID.fromString("743f0510-5285-11e0-b8af-0800200c9a66");
    
    /** The concept uuid. */
    private UUID conceptUuid;
    
    /** The type uuid. */
    private UUID typeUuid;
    
    /** The format. */
    public String format;
    
    /** The text description. */
    public String textDescription;
    
    /** The data bytes. */
    public byte[] dataBytes;

    /**
     * Instantiates a new media cab.
     *
     * @param conceptNid the concept nid
     * @param typeNid the type nid
     * @param format the format
     * @param textDescription the text description
     * @param dataBytes the data bytes
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public MediaCAB(
            int conceptNid, int typeNid, String format, String textDescription,
            byte[] dataBytes)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                format, textDescription, dataBytes);
    }

    /**
     * Instantiates a new media cab.
     *
     * @param conceptUuid the concept uuid
     * @param typeUuid the type uuid
     * @param format the format
     * @param textDescription the text description
     * @param dataBytes the data bytes
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public MediaCAB(
            UUID conceptUuid, UUID typeUuid, String format, String textDescription,
            byte[] dataBytes)
            throws IOException, InvalidCAB, ContradictionException {
        this(conceptUuid, typeUuid, format, textDescription, dataBytes,
                null, null, null);
    }

    /**
     * Instantiates a new media cab.
     *
     * @param conceptNid the concept nid
     * @param typeNid the type nid
     * @param format the format
     * @param textDescription the text description
     * @param dataBytes the data bytes
     * @param mediaVersion the media version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public MediaCAB(
            int conceptNid, int typeNid, String format, String textDescription,
            byte[] dataBytes, MediaVersionBI mediaVersion, ViewCoordinate viewCoordinate)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                format, textDescription, dataBytes, mediaVersion, viewCoordinate);
    }

    /**
     * Instantiates a new media cab.
     *
     * @param conceptUuid the concept uuid
     * @param typeUuid the type uuid
     * @param format the format
     * @param textDescription the text description
     * @param dataBytes the data bytes
     * @param mediaVersion the media version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public MediaCAB(
            UUID conceptUuid, UUID typeUuid, String format, String textDescription,
            byte[] dataBytes, MediaVersionBI mediaVersion, ViewCoordinate viewCoordinate)
            throws IOException, InvalidCAB, ContradictionException {
        this(conceptUuid, typeUuid, format, textDescription, dataBytes,
                null, mediaVersion, viewCoordinate);
    }

    /**
     * Instantiates a new media cab.
     *
     * @param conceptUuid the concept uuid
     * @param typeUuid the type uuid
     * @param format the format
     * @param textDescription the text description
     * @param dataBytes the data bytes
     * @param componentUuid the component uuid
     * @param mediaVersion the media version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are invalid
     * @throws ContradictionException the contradiction exception
     */
    public MediaCAB(
            UUID conceptUuid, UUID typeUuid, String format, String textDescription,
            byte[] dataBytes, UUID componentUuid, MediaVersionBI mediaVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        super(componentUuid, mediaVersion, viewCoordinate);

        this.conceptUuid = conceptUuid;
        this.typeUuid = typeUuid;
        this.format = format;
        this.textDescription = textDescription;
        this.dataBytes = dataBytes;
        if (getComponentUuid() == null) {
            try {
                recomputeUuid();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidCAB ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#recomputeUuid()
     */
    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, IOException, InvalidCAB, ContradictionException {
        setComponentUuid(
                UuidT5Generator.get(mediaSpecNamespace,
                getPrimoridalUuidString(conceptUuid)
                + dataBytes));
        for(RefexCAB annotBp: getAnnotationBlueprints()){
            annotBp.setReferencedComponentUuid(getComponentUuid());
            annotBp.recomputeUuid();
        }
    }

    /**
     * Gets the type uuid.
     *
     * @return the type uuid
     */
    public UUID getTypeUuid() {
        return typeUuid;
    }

    /**
     * Gets the type nid.
     *
     * @return the type nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getTypeNid() throws IOException {
        return Ts.get().getNidForUuids(typeUuid);
    }

    /**
     * Gets the concept nid.
     *
     * @return the concept nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getConceptNid() throws IOException {
        return Ts.get().getNidForUuids(conceptUuid);
    }

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

    /**
     * Gets the text description.
     *
     * @return the text description
     */
    public String getTextDescription() {
        return textDescription;
    }
    
    /**
     * Sets the concept uuid.
     *
     * @param conceptNewUuid the new concept uuid
     */
    protected void setConceptUuid(UUID conceptNewUuid){
        this.conceptUuid = conceptNewUuid;
    }

    /**
     * Validate.
     *
     * @param mediaVersion the media version
     * @return <code>true</code>, if successful
     * @throws IOException signals that an I/O exception has occurred
     */
    public boolean validate(MediaVersionBI mediaVersion) throws IOException {
        if (mediaVersion.getStatusNid() != getStatusNid()) {
            return false;
        }
        if (mediaVersion.getNid() != getComponentNid()) {
            return false;
        }
        if (mediaVersion.getConceptNid() != getConceptNid()) {
            return false;
        }
        if (mediaVersion.getTypeNid() != getTypeNid()) {
            return false;
        }
        if (!mediaVersion.getFormat().equals(getFormat())) {
            return false;
        }
        if (!mediaVersion.getTextDescription().equals(getTextDescription())) {
            return false;
        }
        if (!Arrays.equals(mediaVersion.getMedia(), getDataBytes())) {
            return false;
        }
        return true;
    }
}
