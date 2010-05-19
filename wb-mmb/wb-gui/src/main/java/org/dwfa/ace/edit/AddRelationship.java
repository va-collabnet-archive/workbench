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

public class AddRelationship extends AddComponent {

    public AddRelationship(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
        super(termContainer, config);
    }

    @Override
    protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e, I_ConfigAceFrame config)
            throws Exception {
    	I_GetConceptData cb = (I_GetConceptData) termContainer.getTermComponent();
        if (cb == null) {
            AceLog.getAppLog().alertAndLogException(
                new Exception("Cannot add a relationship while the component viewer is empty..."));
        } else {
            I_GetConceptData parent = config.getHierarchySelection();
            if (parent.getNid() == cb.getNid()) {
                AceLog.getAppLog().alertAndLogException(
                    new Exception("<html>Cannot create a self-referencing relationship<br>Hierarchy selection concept and " +
                    		"and viewer concept are the same:<br><font color='blue'>" + cb.getInitialText()));
            } else {
                Terms.get().newRelationship(UUID.randomUUID(), cb, 
                    config.getDefaultRelationshipType(),
                    parent,
                    config.getDefaultRelationshipCharacteristic(),
                    config.getDefaultRelationshipRefinability(), 
                    config.getDefaultStatus(),
                    0, config);
            Terms.get().addUncommitted(cb);
            termContainer.setTermComponent(cb);
            }
        }
    }

}
