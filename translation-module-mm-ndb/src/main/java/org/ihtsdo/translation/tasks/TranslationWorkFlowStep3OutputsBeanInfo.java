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

/**
 * The Class TranslationWorkFlowStep3OutputsBeanInfo.
 */
public class TranslationWorkFlowStep3OutputsBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new translation work flow step bean info.
	 */
	public TranslationWorkFlowStep3OutputsBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {
			PropertyDescriptor uiPanelName = new PropertyDescriptor("uiPanelName", getBeanDescriptor().getBeanClass());
			uiPanelName.setBound(true);
			uiPanelName.setPropertyEditorClass(JTextFieldEditorOneLine.class);
			uiPanelName.setDisplayName("<html><font color='green'>Panel name:");
			uiPanelName.setShortDescription("Name of the UI Panel displyed to the user");
			
			PropertyDescriptor stepRole = new PropertyDescriptor("stepRole", getBeanDescriptor().getBeanClass());
			stepRole.setBound(true);
			stepRole.setPropertyEditorClass(ConceptLabelEditor.class);
			stepRole.setDisplayName("<html><font color='green'>Role:");
			stepRole.setShortDescription("Role associated with this wf step");
			
			PropertyDescriptor exit1Label = new PropertyDescriptor("exit1LabelAndDestination", getBeanDescriptor().getBeanClass());
			exit1Label.setBound(true);
			exit1Label.setPropertyEditorClass(JTextFieldEditorOneLine.class);
			exit1Label.setDisplayName("<html><font color='green'>Label & Destination for exit 1:");
			exit1Label.setShortDescription("Label & Destination for exit 1 button");
			
			PropertyDescriptor exit1NextStatus = new PropertyDescriptor("exit1NextStatus", getBeanDescriptor().getBeanClass());
			exit1NextStatus.setBound(true);
			exit1NextStatus.setPropertyEditorClass(ConceptLabelEditor.class);
			exit1NextStatus.setDisplayName("<html><font color='green'>Next status for exit 1:");
			exit1NextStatus.setShortDescription("Status assigned after this step on exit 1");
            
			PropertyDescriptor exit2Label = new PropertyDescriptor("exit2LabelAndDestination", getBeanDescriptor().getBeanClass());
			exit2Label.setBound(true);
			exit2Label.setPropertyEditorClass(JTextFieldEditorOneLine.class);
			exit2Label.setDisplayName("<html><font color='green'>Label & Destination for exit 2:");
			exit2Label.setShortDescription("Label & Destination for exit 2 button");
			
			PropertyDescriptor exit2NextStatus = new PropertyDescriptor("exit2NextStatus", getBeanDescriptor().getBeanClass());
			exit2NextStatus.setBound(true);
			exit2NextStatus.setPropertyEditorClass(ConceptLabelEditor.class);
			exit2NextStatus.setDisplayName("<html><font color='green'>Next status for exit 2:");
			exit2NextStatus.setShortDescription("Status assigned after this step on exit 2");
			
			PropertyDescriptor exit3Label = new PropertyDescriptor("exit3LabelAndDestination", getBeanDescriptor().getBeanClass());
			exit3Label.setBound(true);
			exit3Label.setPropertyEditorClass(JTextFieldEditorOneLine.class);
			exit3Label.setDisplayName("<html><font color='green'>Label & Destination for exit 3:");
			exit3Label.setShortDescription("Label & Destination for exit 3 button");
			
			PropertyDescriptor exit3NextStatus = new PropertyDescriptor("exit3NextStatus", getBeanDescriptor().getBeanClass());
			exit3NextStatus.setBound(true);
			exit3NextStatus.setPropertyEditorClass(ConceptLabelEditor.class);
			exit3NextStatus.setDisplayName("<html><font color='green'>Next status for exit 3:");
			exit3NextStatus.setShortDescription("Status assigned after this step on exit 3");
			
			PropertyDescriptor rv[] =
			{uiPanelName, stepRole, exit1Label, exit1NextStatus, exit2Label, exit2NextStatus
					, exit3Label, exit3NextStatus};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(TranslationWorkFlowStep3Outputs.class);
		bd.setDisplayName("<html><font color='green'><center>Translation WF<br>step");
		return bd;
	}

}
