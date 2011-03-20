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
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class DescCAB extends CreateOrAmendBlueprint {

    public static final UUID descSpecNamespace =
            UUID.fromString("457e4a20-5284-11e0-b8af-0800200c9a66");
    private UUID conceptUuid;

    private UUID typeUuid;
    public String lang;
    public String text;
    public boolean initialCaseSignificant;

    public DescCAB(
            int conceptNid, int typeNid, String lang, String text,
            boolean initialCaseSignificant)
            throws IOException {
        this(Ts.get().getComponent(conceptNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                lang, text, initialCaseSignificant);
    }

    public DescCAB(
            UUID conceptUuid, UUID typeUuid, String lang, String text,
            boolean initialCaseSignificant)
            throws IOException {
        this(conceptUuid, typeUuid, lang, text, initialCaseSignificant, null);
    }

    public DescCAB(
            UUID conceptUuid, UUID typeUuid, String lang, String text,
            boolean initialCaseSignificant, UUID componentUuid) throws IOException {
        super(componentUuid);

        this.conceptUuid = conceptUuid;
        this.lang = lang;
        this.text = text;
        this.initialCaseSignificant = initialCaseSignificant;
        this.typeUuid = typeUuid;
        if (getComponentUuid() == null) {
            try {
                setComponentUuid(UuidT5Generator.get(descSpecNamespace,
                        getPrimoridalUuidStr(conceptUuid)
                        + lang
                        + text));
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

    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    public String getLang() {
        return lang;
    }

    public String getText() {
        return text;
    }
}
