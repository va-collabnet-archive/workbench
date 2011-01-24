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

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The Class CreateLangSpecRefset.
 */
public class GenerateWorklistFromAttachmentsBeanInfo extends SimpleBeanInfo {

	public GenerateWorklistFromAttachmentsBeanInfo() {
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
			profilePropName.setDisplayName("<html><font color='green'>profile prop:");
			profilePropName.setShortDescription("The property that will contain the current profile.");

			PropertyDescriptor processPropName = new PropertyDescriptor("processPropName",
					getBeanDescriptor().getBeanClass());
			processPropName.setBound(true);
			processPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			processPropName.setDisplayName("<html><font color='green'>processPropName:");
			processPropName.setShortDescription("processPropName.");

			PropertyDescriptor worklistNamePropName = new PropertyDescriptor("worklistNamePropName",
					getBeanDescriptor().getBeanClass());
			worklistNamePropName.setBound(true);
			worklistNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			worklistNamePropName.setDisplayName("<html><font color='green'>worklistNamePropName:");
			worklistNamePropName.setShortDescription("worklistNamePropName.");
			
			PropertyDescriptor translatorInboxPropName = new PropertyDescriptor("translatorInboxPropName",
					getBeanDescriptor().getBeanClass());
			translatorInboxPropName.setBound(true);
			translatorInboxPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			translatorInboxPropName.setDisplayName("<html><font color='green'>translatorInboxPropName:");
			translatorInboxPropName.setShortDescription("translatorInboxPropName.");
			
			PropertyDescriptor partitionPropName = new PropertyDescriptor("partitionPropName",
					getBeanDescriptor().getBeanClass());
			partitionPropName.setBound(true);
			partitionPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			partitionPropName.setDisplayName("<html><font color='green'>partitionPropName:");
			partitionPropName.setShortDescription("partitionPropName.");

			PropertyDescriptor rv[] =
			{profilePropName, processPropName, worklistNamePropName, translatorInboxPropName, partitionPropName};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        

	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(GenerateWorklistFromAttachments.class);
		bd.setDisplayName("<html><font color='green'><center>Generate Worklist<br>from Attachments");
		return bd;
	}

}
