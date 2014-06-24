/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.project.task;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.bpa.tasks.editor.AbstractComboEditor;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;

/**
 * The Class ProjectSelectorEditor.
 */
public class ProjectSelectorEditor extends AbstractComboEditor {

    /* (non-Javadoc)
     * @see org.dwfa.bpa.tasks.editor.AbstractComboEditor#setupEditor()
     */
    @Override
    public EditorComponent setupEditor() {
        // TODO Auto-generated method stub
        I_ConfigAceFrame config = ACE.getAceConfig().getAceFrames().iterator().next();
        List<String> projectNames = new ArrayList<String>();
        for (I_TerminologyProject project : TerminologyProjectDAO.getAllTranslationProjects(config)) {
            projectNames.add(project.getName());
        }
        return new EditorComponent(projectNames.toArray());
    }
}
