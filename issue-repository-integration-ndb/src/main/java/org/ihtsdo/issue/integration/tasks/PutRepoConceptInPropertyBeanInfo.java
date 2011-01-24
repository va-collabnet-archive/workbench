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
package org.ihtsdo.issue.integration.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The Class BeanInfo.
 */
public class PutRepoConceptInPropertyBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new adds the issue repo registration to profile bean info.
	 */
	public PutRepoConceptInPropertyBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor issueRepoConcept = new PropertyDescriptor("issueRepoConcept", getBeanDescriptor().getBeanClass());
			issueRepoConcept.setBound(true);
			issueRepoConcept.setPropertyEditorClass(ConceptLabelPropEditor.class);
			issueRepoConcept.setDisplayName("issueRepoProp");
			issueRepoConcept.setShortDescription("Issue Repository Concept.");
			
			PropertyDescriptor repositoryConceptPropName =
				new PropertyDescriptor("repositoryConceptPropName", getBeanDescriptor().getBeanClass());
			repositoryConceptPropName.setBound(true);
			repositoryConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			repositoryConceptPropName.setDisplayName("<html><font color='green'>repositoryConceptPropName:");
			repositoryConceptPropName.setShortDescription("repositoryConceptPropName");
			
			PropertyDescriptor rv[] =
			{issueRepoConcept, repositoryConceptPropName};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(PutRepoConceptInProperty.class);
		bd.setDisplayName("<html><font color='green'><center>Put repository<br>in property");
		return bd;
	}

}
