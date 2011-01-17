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
package org.ihtsdo.ace.task.gui.component;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.LinkedList;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.tasks.editor.AbstractComboEditor;


public class WorkflowActionEditor extends AbstractComboEditor {

    @Override
    public EditorComponent setupEditor() {
    	//Sort the list by name
    	LinkedList<I_GetConceptData> editors = new LinkedList<I_GetConceptData>();
    	
    	try {
			for (I_GetConceptData action : Terms.get().getActiveAceFrameConfig().getWorkflowActions()) 
				editors.add(action);
		} catch (Exception e) {
			e.printStackTrace();
		}

    	EditorComponent ec = new EditorComponent(editors.toArray());

    	Dimension d = ec.getPreferredSize();
    	d.width = 275;
    	ec.setPreferredSize(d);
    	Rectangle r = new Rectangle(d);
    	
    	ec.setBounds(r);
    	
    	return ec;
    }

}
