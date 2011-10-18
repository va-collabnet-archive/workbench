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
 * The Class CreateNewContextualizedDescriptionBeanInfo.
 */
public class CreateTargetLangMembershipRefsetBeanInfo extends SimpleBeanInfo {

	public CreateTargetLangMembershipRefsetBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor concept = new PropertyDescriptor("concept", CreateTargetLangMembershipRefset.class);
			concept.setBound(true);
			concept.setPropertyEditorClass(JTextFieldEditor.class);
			concept.setDisplayName("Target Language refset name");
			concept.setShortDescription("Target Language refset name");

			PropertyDescriptor languageCode = new PropertyDescriptor("languageCode", CreateTargetLangMembershipRefset.class);
			languageCode.setBound(true);
			languageCode.setPropertyEditorClass(ConceptLabelPropEditor.class);
			languageCode.setDisplayName("language Code");
			languageCode.setShortDescription("Language Code.");

			PropertyDescriptor languageRefset = new PropertyDescriptor("languageRefset", CreateTargetLangMembershipRefset.class);
			languageRefset.setBound(true);
			languageRefset.setPropertyEditorClass(ConceptLabelPropEditor.class);
			languageRefset.setDisplayName("Parent language Refset");
			languageRefset.setShortDescription("Parent Language Refset.");
			

			PropertyDescriptor rv[] =
			{concept, languageCode, languageRefset};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(CreateTargetLangMembershipRefset.class);
		bd.setDisplayName("<html><font color='green'><center>Create new<br>Target Language Refset");
		return bd;
	}

}
