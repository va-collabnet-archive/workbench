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

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_Path;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinConPart;

public class AddConceptPart extends AddComponent {

    public AddConceptPart(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
        super(termContainer, config);
    }

    @Override
    protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e, I_ConfigAceFrame config)
            throws Exception {
        ConceptBean cb = (ConceptBean) termContainer.getTermComponent();
        for (I_Path p : termContainer.getConfig().getEditingPathSet()) {
            ThinConPart part = new ThinConPart();
            part.setVersion(Integer.MAX_VALUE);
            part.setPathId(p.getConceptId());
            part.setDefined(false);
            part.setConceptStatus(config.getDefaultStatus().getConceptId());
            cb.getConceptAttributes().getVersions().add(part);
        }
        ACE.addUncommitted(cb);
        termContainer.setTermComponent(cb);
    }
}
