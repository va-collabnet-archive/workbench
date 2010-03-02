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

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;

public class AddDescription extends AddComponent {
	
	private static I_GetConceptData synonymDescType;

    public AddDescription(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
        super(termContainer, config);
    }

    @Override
    protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e, I_ConfigAceFrame config)
            throws Exception {
    	I_GetConceptData cb = (I_GetConceptData) termContainer.getTermComponent();
        if (cb == null) {
            AceLog.getAppLog().alertAndLogException(
                new Exception("Cannot add a description while the component viewer is empty..."));
        } else {
            if (synonymDescType == null) {
                int typeId = Terms.get().uuidToNative(
                        ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
                synonymDescType = Terms.get().getConcept(typeId);
            }
            
            Terms.get().newDescription(UUID.randomUUID(), 
            		cb, "en", "New Description", synonymDescType, config);
            Terms.get().addUncommitted(cb);
            termContainer.setTermComponent(cb);
        }
    }

}
