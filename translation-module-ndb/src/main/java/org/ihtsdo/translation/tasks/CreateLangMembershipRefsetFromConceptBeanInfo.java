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

/**
 * The Class CreateNewContextualizedDescriptionBeanInfo.
 */
public class CreateLangMembershipRefsetFromConceptBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new creates the lang membership refset from concept bean info.
	 */
	public CreateLangMembershipRefsetFromConceptBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor languageCode = new PropertyDescriptor("languageCode", CreateLangMembershipRefsetFromConcept.class);
			languageCode.setBound(true);
			languageCode.setPropertyEditorClass(ConceptLabelPropEditor.class);
			languageCode.setDisplayName("language Code");
			languageCode.setShortDescription("Language Code.");

			PropertyDescriptor languageRefset = new PropertyDescriptor("languageRefset", CreateLangMembershipRefsetFromConcept.class);
			languageRefset.setBound(true);
			languageRefset.setPropertyEditorClass(ConceptLabelPropEditor.class);
			languageRefset.setDisplayName("Language Refset");
			languageRefset.setShortDescription("Language Refset.");
			

			PropertyDescriptor rv[] =
			{languageCode, languageRefset};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(CreateLangMembershipRefsetFromConcept.class);
		bd.setDisplayName("<html><font color='green'><center>Create new<br>Language Refset<br>from concept");
		return bd;
	}

}
