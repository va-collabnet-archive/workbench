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

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * The Class CreateNewProjectBeanInfo.
 */
public class CreateNewProjectBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new creates the new project bean info.
	 */
	public CreateNewProjectBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {
			PropertyDescriptor projectName = new PropertyDescriptor("projectName", CreateNewProject.class);
			projectName.setBound(true);
			projectName.setPropertyEditorClass(JTextFieldEditor.class);
			projectName.setDisplayName("projectName");
			projectName.setShortDescription("Name for the new project.");
			

			PropertyDescriptor sequence = new PropertyDescriptor("sequence", CreateNewProject.class);
			sequence.setBound(true);
			sequence.setPropertyEditorClass(JTextFieldEditor.class);
			sequence.setDisplayName("Sequence");
			sequence.setShortDescription("Sequence for project.");

			PropertyDescriptor sourceLanguageRefset = new PropertyDescriptor("sourceLanguageRefset", CreateNewProject.class);
			sourceLanguageRefset.setBound(true);
			sourceLanguageRefset.setPropertyEditorClass(ConceptLabelPropEditor.class);
			sourceLanguageRefset.setDisplayName("Source language Refset");
			sourceLanguageRefset.setShortDescription("Source Language Refset.");

			PropertyDescriptor targetLanguageRefset = new PropertyDescriptor("targetLanguageRefset", CreateNewProject.class);
			targetLanguageRefset.setBound(true);
			targetLanguageRefset.setPropertyEditorClass(ConceptLabelPropEditor.class);
			targetLanguageRefset.setDisplayName("Target language Refset");
			targetLanguageRefset.setShortDescription("Target Language Refset.");
			
			PropertyDescriptor rv[] =
			{projectName,sequence,sourceLanguageRefset,targetLanguageRefset};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(CreateNewProject.class);
		bd.setDisplayName("<html><font color='green'><center>Create new Project");
		return bd;
	}

}
