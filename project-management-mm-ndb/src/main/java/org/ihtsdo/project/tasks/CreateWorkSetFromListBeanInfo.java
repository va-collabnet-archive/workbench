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
package org.ihtsdo.project.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * The Class CreateWorkSetFromListBeanInfo.
 */
public class CreateWorkSetFromListBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new creates the work set from list bean info.
	 */
	public CreateWorkSetFromListBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
		
		try {
			PropertyDescriptor project = new PropertyDescriptor("project", CreateWorkSetFromList.class);
			project.setBound(true);
			project.setPropertyEditorClass(ProjectSelectorEditor.class);
			project.setDisplayName("project");
			project.setShortDescription("Select a project.");
			PropertyDescriptor worksetName = new PropertyDescriptor("worksetName", CreateWorkSetFromList.class);
			worksetName.setBound(true);
			worksetName.setPropertyEditorClass(JTextFieldEditor.class);
			worksetName.setDisplayName("worksetName");
			worksetName.setShortDescription("Name for the new WorkSet.");
			PropertyDescriptor worksetDescription = new PropertyDescriptor("worksetDescription", CreateWorkSetFromList.class);
			worksetDescription.setBound(true);
			worksetDescription.setPropertyEditorClass(JTextFieldEditor.class);
			worksetDescription.setDisplayName("worksetDescription");
			worksetDescription.setShortDescription("Description for the new Workset.");
			PropertyDescriptor rv[] = {project, worksetName, worksetDescription};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(CreateWorkSetFromList.class);
		bd.setDisplayName("<html><font color='green'><center>Create WorkSet from List");
		return bd;
	}

}
