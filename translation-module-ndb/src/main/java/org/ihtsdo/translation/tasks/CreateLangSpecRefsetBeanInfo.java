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
 * The Class CreateLangSpecRefset.
 */
public class CreateLangSpecRefsetBeanInfo extends SimpleBeanInfo {

	public CreateLangSpecRefsetBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor concept = new PropertyDescriptor("LangSpecName", CreateLangSpecRefset.class);
			concept.setBound(true);
			concept.setPropertyEditorClass(JTextFieldEditor.class);
			concept.setDisplayName("Language spec name");
			concept.setShortDescription("Language spec name");
			
			PropertyDescriptor languageRefset = new PropertyDescriptor("parentConcept", CreateLangSpecRefset.class);
			languageRefset.setBound(true);
			languageRefset.setPropertyEditorClass(ConceptLabelPropEditor.class);
			languageRefset.setDisplayName("Parent refset concept");
			languageRefset.setShortDescription("Parent refset concept.");
			
			PropertyDescriptor originLangMemberRefset = new PropertyDescriptor("originLangMemberRefset", CreateLangSpecRefset.class);
			originLangMemberRefset.setBound(true);
			originLangMemberRefset.setPropertyEditorClass(ConceptLabelPropEditor.class);
			originLangMemberRefset.setDisplayName("Origin language Refset");
			originLangMemberRefset.setShortDescription("Origin Language Refset.");

			PropertyDescriptor langMemberRefset = new PropertyDescriptor("langMemberRefset", CreateLangSpecRefset.class);
			langMemberRefset.setBound(true);
			langMemberRefset.setPropertyEditorClass(ConceptLabelPropEditor.class);
			langMemberRefset.setDisplayName("Target language Refset");
			langMemberRefset.setShortDescription("Target Language Refset.");

			PropertyDescriptor rv[] =
			{concept, languageRefset,originLangMemberRefset,langMemberRefset};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(CreateLangSpecRefset.class);
		bd.setDisplayName("<html><font color='green'><center>Create new<br>Language Spec Refset");
		return bd;
	}

}
