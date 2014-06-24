/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.task;

import org.dwfa.bpa.tasks.editor.AbstractComboEditor;
import org.ihtsdo.project.workflow.model.WfInstance;

/**
 * The Class ActionReportEditor.
 */
public class ActionReportEditor extends AbstractComboEditor {

    /* (non-Javadoc)
     * @see org.dwfa.bpa.tasks.editor.AbstractComboEditor#setupEditor()
     */
    @Override
    public EditorComponent setupEditor() {
        return new EditorComponent(WfInstance.ActionReport.values());

    }
}
