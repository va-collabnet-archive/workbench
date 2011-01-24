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
 * The Class SearchIssuesBeanInfo.
 */
public class SearchIssuesBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new search issues bean info.
	 */
	public SearchIssuesBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {
			
			PropertyDescriptor issueRepoProp = new PropertyDescriptor("issueRepoProp", SearchIssues.class);
			issueRepoProp.setBound(true);
			issueRepoProp.setPropertyEditorClass(ConceptLabelPropEditor.class);
			issueRepoProp.setDisplayName("issueRepoProp");
			issueRepoProp.setShortDescription("Issue Repository Concept.");
			
			PropertyDescriptor searchStatus = new PropertyDescriptor("statusString", SearchIssues.class);
			searchStatus.setBound(true);
			searchStatus.setPropertyEditorClass(JTextFieldEditor.class);
			searchStatus.setDisplayName("Status");
			searchStatus.setShortDescription("Status to search.");
			
			PropertyDescriptor searchPriority = new PropertyDescriptor("priorityString", SearchIssues.class);
			searchPriority.setBound(true);
			searchPriority.setPropertyEditorClass(JTextFieldEditor.class);
			searchPriority.setDisplayName("Priority");
			searchPriority.setShortDescription("Priority to search.");
			
			PropertyDescriptor searchUser = new PropertyDescriptor("userString", SearchIssues.class);
			searchUser.setBound(true);
			searchUser.setPropertyEditorClass(JTextFieldEditor.class);
			searchUser.setDisplayName("User");
			searchUser.setShortDescription("User to search.");
			
			PropertyDescriptor mapKey = new PropertyDescriptor("propertyMapKey", SearchIssues.class);
			mapKey.setBound(true);
			mapKey.setPropertyEditorClass(JTextFieldEditor.class);
			mapKey.setDisplayName("Properties Map Key");
			mapKey.setShortDescription("Key to change.");
			
			PropertyDescriptor mapStringValue = new PropertyDescriptor("propertyMapStringValue", SearchIssues.class);
			mapStringValue.setBound(true);
			mapStringValue.setPropertyEditorClass(JTextFieldEditor.class);
			mapStringValue.setDisplayName("Properties Map String");
			mapStringValue.setShortDescription("String to search.");
			
			PropertyDescriptor mapConceptValue = new PropertyDescriptor("propertyMapConceptValue", SearchIssues.class);
			mapConceptValue.setBound(true);
			mapConceptValue.setPropertyEditorClass(ConceptLabelPropEditor.class);
			mapConceptValue.setDisplayName("Properties Map Concept");
			mapConceptValue.setShortDescription("Concept to search.");
			
			PropertyDescriptor rv[] =  {issueRepoProp, searchStatus, searchPriority, searchUser, mapKey, mapStringValue, mapConceptValue};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(SearchIssues.class);
		bd.setDisplayName("<html><font color='green'><center>Search issues");
		return bd;
	}

}
