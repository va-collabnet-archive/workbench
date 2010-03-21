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
package org.dwfa.ace.edit;

import java.awt.event.ActionEvent;
import java.util.UUID;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

public class AddRelationship extends AddComponent {

    public AddRelationship(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
        super(termContainer, config);
    }

    @Override
    protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e, I_ConfigAceFrame config)
            throws Exception {
        ConceptBean cb = (ConceptBean) termContainer.getTermComponent();
        UUID newRelUid = UUID.randomUUID();
        int idSource = AceConfig.getVodb().uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
        int relId = AceConfig.getVodb().uuidToNativeWithGeneration(newRelUid, idSource, config.getEditingPathSet(),
            Integer.MAX_VALUE);
        int parentId = Integer.MAX_VALUE;

        if (config.getHierarchySelection() != null) {
            parentId = config.getHierarchySelection().getConceptId();
        } else {
            parentId = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.localize().getNid();
        }
        if (cb != null) {
            ThinRelVersioned rel = new ThinRelVersioned(relId, cb.getConceptId(), parentId, 1);
            ThinRelPart relPart = new ThinRelPart();
            rel.addVersion(relPart);
            int status = config.getDefaultStatus().getConceptId();
            for (I_Path p : termContainer.getConfig().getEditingPathSet()) {
                relPart.setVersion(Integer.MAX_VALUE);
                relPart.setPathId(p.getConceptId());
                relPart.setStatusId(status);
                relPart.setTypeId(config.getDefaultRelationshipType().getConceptId());
                relPart.setCharacteristicId(config.getDefaultRelationshipCharacteristic().getConceptId());
                relPart.setRefinabilityId(config.getDefaultRelationshipRefinability().getConceptId());
                relPart.setGroup(0);
            }
            cb.getUncommittedSourceRels().add(rel);
            cb.getUncommittedIds().add(relId);
            ACE.addUncommitted(cb);
            termContainer.setTermComponent(cb);
        } else {
            AceLog.getAppLog().alertAndLogException(
                new Exception("Cannot add a relationship while the component viewer is empty..."));
        }
    }

}
