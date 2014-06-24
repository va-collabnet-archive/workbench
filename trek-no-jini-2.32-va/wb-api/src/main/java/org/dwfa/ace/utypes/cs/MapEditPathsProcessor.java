/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.utypes.cs;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.utypes.I_VersionComponent;
import org.dwfa.ace.utypes.UniversalAceConceptAttributesPart;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.ace.utypes.UniversalAceImagePart;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;

public class MapEditPathsProcessor extends AbstractUncommittedProcessor {

    Map<UUID, I_GetConceptData> uuidConceptMap = new TreeMap<UUID, I_GetConceptData>();

    public MapEditPathsProcessor(Map<I_GetConceptData, I_GetConceptData> conceptMap) throws IOException {
        super();
        for (Entry<I_GetConceptData, I_GetConceptData> e : conceptMap.entrySet()) {
            for (UUID id : e.getKey().getUids()) {
                uuidConceptMap.put(id, e.getValue());
            }
        }
    }

    @Override
    protected void processNewUniversalAcePath(UniversalAcePath path) {
        // nothing to do...

    }

    @Override
    protected void processUncommittedUniversalAceConceptAttributesPart(UniversalAceConceptAttributesPart part)
            throws IOException {
        processPart(part);
    }

    @Override
    protected void processUncommittedUniversalAceDescriptionPart(UniversalAceDescriptionPart part) throws IOException {
        processPart(part);
    }

    @Override
    protected void processUncommittedUniversalAceExtByRefPart(UniversalAceExtByRefPart part) throws IOException {
        processPart(part);
    }

    @Override
    protected void processUncommittedUniversalAceIdentificationPart(UniversalAceIdentificationPart part)
            throws IOException {
        processPart(part);
    }

    @Override
    protected void processUncommittedUniversalAceImagePart(UniversalAceImagePart part) throws IOException {
        processPart(part);
    }

    @Override
    protected void processUncommittedUniversalAceRelationshipPart(UniversalAceRelationshipPart part) throws IOException {
        processPart(part);
    }

    private void processPart(I_VersionComponent part) throws IOException {
        I_GetConceptData mappedConcept = null;
        for (UUID id : part.getPathId()) {
            mappedConcept = uuidConceptMap.get(id);
            if (mappedConcept != null) {
                break;
            }
        }
        if (mappedConcept != null) {
            part.setPathId(mappedConcept.getUids());
        } else {
            throw new IOException("no mapping for: " + part.getPathId());
        }
    }
}
