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
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;

public class AddDescription extends AddComponent {

    public AddDescription(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
        super(termContainer, config);
    }

    @Override
    protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e, I_ConfigAceFrame config)
            throws Exception {
        ConceptBean cb = (ConceptBean) termContainer.getTermComponent();
        if (cb == null) {
            AceLog.getAppLog().alertAndLogException(
                new Exception("Cannot add a description while the component viewer is empty..."));
        } else {
            UUID newDescUid = UUID.randomUUID();
            int idSource = AceConfig.getVodb().uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
            int descId = AceConfig.getVodb().uuidToNativeWithGeneration(newDescUid, idSource,
                config.getEditingPathSet(), Integer.MAX_VALUE);
            ThinDescVersioned desc = new ThinDescVersioned(descId, cb.getConceptId(), 1);
            ThinDescPart descPart = new ThinDescPart();
            desc.addVersion(descPart);
            boolean capStatus = false;
            String lang = "en-AU";
            int status = config.getDefaultStatus().getConceptId();
            int typeId = AceConfig.getVodb().uuidToNative(
                ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
            String text = "New Description";
            for (I_Path p : termContainer.getConfig().getEditingPathSet()) {
                descPart.setVersion(Integer.MAX_VALUE);
                descPart.setPathId(p.getConceptId());
                descPart.setInitialCaseSignificant(capStatus);
                descPart.setLang(lang);
                descPart.setStatusId(status);
                descPart.setText(text);
                descPart.setTypeId(typeId);
            }
            cb.getUncommittedDescriptions().add(desc);
            cb.getUncommittedIds().add(descId);
            ACE.addUncommitted(cb);
            termContainer.setTermComponent(cb);
        }
    }

}
