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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.tasks.editor.AbstractComboEditor;
import org.ihtsdo.workflow.refset.semArea.SemanticAreaSearchRefset;

public class SemanticHierarchyEditor extends AbstractComboEditor {

    @Override
    public EditorComponent setupEditor() {
    	SortedSet<String> hierarchies = new TreeSet<String>();
		I_ExtendByRefPartStr props = null;

    	try {
    		SemanticAreaSearchRefset refset = new SemanticAreaSearchRefset();
			try {
				for (I_ExtendByRef extension : Terms.get().getRefsetExtensionMembers(refset.getRefsetId())) 
				{
					props = (I_ExtendByRefPartStr)extension;
				
					hierarchies.add(props.getStringValue());
		        }
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, props.getStringValue(), e);
			}

			EditorComponent ec = new EditorComponent(hierarchies.toArray());
	
	    	Dimension d = ec.getPreferredSize();
	    	d.width = 275;
	    	ec.setPreferredSize(d);
	    	Rectangle r = new Rectangle(d);
	    	
	    	ec.setBounds(r);
	
	    	return ec;
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, props.getStringValue(), e);
		}
		
		return null;
    }

}