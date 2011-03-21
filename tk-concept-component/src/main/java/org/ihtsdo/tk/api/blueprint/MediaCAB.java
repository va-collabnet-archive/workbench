/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class MediaCAB extends CreateOrAmendBlueprint {

    public static final UUID mediaSpecNamespace =
            UUID.fromString("743f0510-5285-11e0-b8af-0800200c9a66");
    private UUID conceptUuid;
    private UUID typeUuid;
    public String format;
    public String textDescription;
    public byte[] dataBytes;

    public MediaCAB(
            int conceptNid, int typeNid, String format, String textDescription,
            byte[] dataBytes)
            throws IOException {
        this(Ts.get().getComponent(conceptNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                format, textDescription, dataBytes);
    }

    public MediaCAB(
            UUID conceptUuid, UUID typeUuid, String format, String textDescription,
            byte[] dataBytes)
            throws IOException {
        this(conceptUuid, typeUuid, format, textDescription, dataBytes, null);
    }

    public MediaCAB(
            UUID conceptUuid, UUID typeUuid, String format, String textDescription,
            byte[] dataBytes, UUID componentUuid) throws IOException {
        super(componentUuid);

        this.conceptUuid = conceptUuid;
        this.typeUuid = typeUuid;
        this.format = format;
        this.textDescription = textDescription;
        this.dataBytes = dataBytes;
        if (getComponentUuid() == null) {
            try {
                setComponentUuid(
                        UuidT5Generator.get(mediaSpecNamespace,
                        getPrimoridalUuidStr(conceptUuid)
                        + dataBytes));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidCAB ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public UUID getTypeUuid() {
        return typeUuid;
    }

    public int getTypeNid() throws IOException {
        return Ts.get().getNidForUuids(typeUuid);
    }

    public int getConceptNid() throws IOException {
        return Ts.get().getNidForUuids(conceptUuid);
    }

    public UUID getConceptUuid() {
        return conceptUuid;
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public String getFormat() {
        return format;
    }

    public String getTextDescription() {
        return textDescription;
    }
    
       
    public boolean validate(MediaVersionBI version) throws IOException {
        if (version.getStatusNid() != getStatusNid()) {
            return false;
        }
        if (version.getNid() != getComponentNid()) {
            return false;
        }
        if (version.getConceptNid() != getConceptNid()) {
            return false;
        }
        if (version.getTypeNid() != getTypeNid()) {
            return false;
        }
        if (!version.getFormat().equals(getFormat())) {
            return false;
        }
        if (!version.getTextDescription().equals(getTextDescription())) {
            return false;
        }
        if (!Arrays.equals(version.getMedia(), getDataBytes())) {
            return false;
        }
        return true;
    }

}
