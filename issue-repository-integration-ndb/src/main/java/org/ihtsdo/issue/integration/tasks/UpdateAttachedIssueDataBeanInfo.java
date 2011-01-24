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

/**
 * The Class UpdateAttachedIssueDataBeanInfo.
 */
public class UpdateAttachedIssueDataBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new update attached issue data bean info.
	 */
	public UpdateAttachedIssueDataBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor selectedProp = new PropertyDescriptor("selectedProp", UpdateAttachedIssueData.class);
			selectedProp.setBound(true);
			selectedProp.setPropertyEditorClass(IssuePropSelectorEditor.class);
			selectedProp.setDisplayName("Select Issue Property");
			selectedProp.setShortDescription("Select Property to include.");
			
			PropertyDescriptor propertyValue = new PropertyDescriptor("propertyValue", UpdateAttachedIssueData.class);
			propertyValue.setBound(true);
			propertyValue.setPropertyEditorClass(JTextFieldEditor.class);
			propertyValue.setDisplayName("Property value");
			propertyValue.setShortDescription("Value for issue property.");
			
			PropertyDescriptor mapKey = new PropertyDescriptor("propertyMapKey", UpdateAttachedIssueData.class);
			mapKey.setBound(true);
			mapKey.setPropertyEditorClass(JTextFieldEditor.class);
			mapKey.setDisplayName("Properties Map Key");
			mapKey.setShortDescription("Key to change.");
			
			PropertyDescriptor mapStringValue = new PropertyDescriptor("propertyMapStringValue", UpdateAttachedIssueData.class);
			mapStringValue.setBound(true);
			mapStringValue.setPropertyEditorClass(JTextFieldEditor.class);
			mapStringValue.setDisplayName("Properties Map String Value");
			mapStringValue.setShortDescription("String to change.");
			
			PropertyDescriptor mapConceptValue = new PropertyDescriptor("propertyMapConceptValue", UpdateAttachedIssueData.class);
			mapConceptValue.setBound(true);
			mapConceptValue.setPropertyEditorClass(ConceptLabelPropEditor.class);
			mapConceptValue.setDisplayName("Properties Map Conept Value");
			mapConceptValue.setShortDescription("Concept to change.");
			
			PropertyDescriptor rv[] =  {selectedProp, propertyValue, mapKey, mapStringValue, mapConceptValue};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(UpdateAttachedIssueData.class);
		bd.setDisplayName("<html><font color='green'><center>Update attached<br>issue data");
		return bd;
	}

}
