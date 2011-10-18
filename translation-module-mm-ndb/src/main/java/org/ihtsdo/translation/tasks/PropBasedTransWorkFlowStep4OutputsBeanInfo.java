/**
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
package org.ihtsdo.translation.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The Class PropBasedTransWorkFlowStep2OutputsBeanInfo.
 */
public class PropBasedTransWorkFlowStep4OutputsBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new translation work flow step bean info.
	 */
	public PropBasedTransWorkFlowStep4OutputsBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {
			PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
					getBeanDescriptor().getBeanClass());
			profilePropName.setBound(true);
			profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			profilePropName.setDisplayName("<html><font color='green'>Profile Prop Name:");
			profilePropName.setShortDescription("The property that will contain the current profile.");
			
			PropertyDescriptor stepRole = new PropertyDescriptor("stepRole", getBeanDescriptor().getBeanClass());
			stepRole.setBound(true);
			stepRole.setPropertyEditorClass(ConceptLabelEditor.class);
			stepRole.setDisplayName("<html><font color='green'>Role:");
			stepRole.setShortDescription("Role associated with this wf step");
			
			PropertyDescriptor exit1PropName = new PropertyDescriptor("exit1PropName", getBeanDescriptor().getBeanClass());
			exit1PropName.setBound(true);
			exit1PropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			exit1PropName.setDisplayName("<html><font color='green'>Exit1 Prop Name:");
			exit1PropName.setShortDescription("exit1PropName");
			
			PropertyDescriptor exit1Label = new PropertyDescriptor("exit1Label", getBeanDescriptor().getBeanClass());
			exit1Label.setBound(true);
			exit1Label.setPropertyEditorClass(JTextFieldEditorOneLine.class);
			exit1Label.setDisplayName("<html><font color='green'>Label for exit 1:");
			exit1Label.setShortDescription("Label for exit 1 button");
			
			PropertyDescriptor exit1NextStatus = new PropertyDescriptor("exit1NextStatus", getBeanDescriptor().getBeanClass());
			exit1NextStatus.setBound(true);
			exit1NextStatus.setPropertyEditorClass(ConceptLabelEditor.class);
			exit1NextStatus.setDisplayName("<html><font color='green'>Next status for exit 1:");
			exit1NextStatus.setShortDescription("Status assigned after this step on exit 1");
            
			PropertyDescriptor exit2PropName = new PropertyDescriptor("exit2PropName", getBeanDescriptor().getBeanClass());
			exit2PropName.setBound(true);
			exit2PropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			exit2PropName.setDisplayName("<html><font color='green'>Exit2 Prop Name:");
			exit2PropName.setShortDescription("exit2PropName");
			
			PropertyDescriptor exit2Label = new PropertyDescriptor("exit2Label", getBeanDescriptor().getBeanClass());
			exit2Label.setBound(true);
			exit2Label.setPropertyEditorClass(JTextFieldEditorOneLine.class);
			exit2Label.setDisplayName("<html><font color='green'>Label for exit 2:");
			exit2Label.setShortDescription("Label for exit 2 button");
			
			PropertyDescriptor exit2NextStatus = new PropertyDescriptor("exit2NextStatus", getBeanDescriptor().getBeanClass());
			exit2NextStatus.setBound(true);
			exit2NextStatus.setPropertyEditorClass(ConceptLabelEditor.class);
			exit2NextStatus.setDisplayName("<html><font color='green'>Next status for exit 2:");
			exit2NextStatus.setShortDescription("Status assigned after this step on exit 2");
			
			PropertyDescriptor exit3PropName = new PropertyDescriptor("exit3PropName", getBeanDescriptor().getBeanClass());
			exit3PropName.setBound(true);
			exit3PropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			exit3PropName.setDisplayName("<html><font color='green'>Exit3 Prop Name:");
			exit3PropName.setShortDescription("exit3PropName");
			
			PropertyDescriptor exit3Label = new PropertyDescriptor("exit3Label", getBeanDescriptor().getBeanClass());
			exit3Label.setBound(true);
			exit3Label.setPropertyEditorClass(JTextFieldEditorOneLine.class);
			exit3Label.setDisplayName("<html><font color='green'>Label for exit 3:");
			exit3Label.setShortDescription("Label for exit 3 button");
			
			PropertyDescriptor exit3NextStatus = new PropertyDescriptor("exit3NextStatus", getBeanDescriptor().getBeanClass());
			exit3NextStatus.setBound(true);
			exit3NextStatus.setPropertyEditorClass(ConceptLabelEditor.class);
			exit3NextStatus.setDisplayName("<html><font color='green'>Next status for exit 3:");
			exit3NextStatus.setShortDescription("Status assigned after this step on exit 3");
			
			PropertyDescriptor exit4PropName = new PropertyDescriptor("exit4PropName", getBeanDescriptor().getBeanClass());
			exit4PropName.setBound(true);
			exit4PropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			exit4PropName.setDisplayName("<html><font color='green'>Exit4 Prop Name:");
			exit4PropName.setShortDescription("exit4PropName");
			
			PropertyDescriptor exit4Label = new PropertyDescriptor("exit4Label", getBeanDescriptor().getBeanClass());
			exit4Label.setBound(true);
			exit4Label.setPropertyEditorClass(JTextFieldEditorOneLine.class);
			exit4Label.setDisplayName("<html><font color='green'>Label for exit 4:");
			exit4Label.setShortDescription("Label for exit 4 button");
			
			PropertyDescriptor exit4NextStatus = new PropertyDescriptor("exit4NextStatus", getBeanDescriptor().getBeanClass());
			exit4NextStatus.setBound(true);
			exit4NextStatus.setPropertyEditorClass(ConceptLabelEditor.class);
			exit4NextStatus.setDisplayName("<html><font color='green'>Next status for exit 4:");
			exit4NextStatus.setShortDescription("Status assigned after this step on exit 4");

			PropertyDescriptor todoStatus = new PropertyDescriptor("todoStatus", getBeanDescriptor().getBeanClass());
			todoStatus.setBound(true);
			todoStatus.setPropertyEditorClass(ConceptLabelEditor.class);
			todoStatus.setDisplayName("<html><font color='green'>To do status:");
			todoStatus.setShortDescription("To Do Status assigned for this step");
			
			PropertyDescriptor rv[] =
			{profilePropName, stepRole, exit1PropName, exit1Label, exit1NextStatus, 
					exit2PropName, exit2Label, exit2NextStatus, exit3PropName, exit3Label, exit3NextStatus,
					exit4PropName, exit4Label, exit4NextStatus, todoStatus};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(PropBasedTransWorkFlowStep4Outputs.class);
		bd.setDisplayName("<html><font color='green'><center>Translation WF<br>step by Props<br>4 outputs");
		return bd;
	}

}
