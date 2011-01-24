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
 * The Class CreateIssueBeanInfo.
 */
public class CreateIssueBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new creates the issue bean info.
	 */
	public CreateIssueBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor issueRepoProp = new PropertyDescriptor("issueRepoProp", CreateIssue.class);
			issueRepoProp.setBound(true);
			issueRepoProp.setPropertyEditorClass(ConceptLabelPropEditor.class);
			issueRepoProp.setDisplayName("issueRepoProp");
			issueRepoProp.setShortDescription("Issue Repository Concept.");
			
			PropertyDescriptor title = new PropertyDescriptor("title", CreateIssue.class);
			title.setBound(true);
			title.setPropertyEditorClass(JTextFieldEditor.class);
			title.setDisplayName("title");
			title.setShortDescription("Title of issue.");

			PropertyDescriptor description = new PropertyDescriptor("description", CreateIssue.class);
			description.setBound(true);
			description.setPropertyEditorClass(JTextFieldEditor.class);
			description.setDisplayName("description");
			description.setShortDescription("Description of issue.");

			PropertyDescriptor componentId = new PropertyDescriptor("componentId", CreateIssue.class);
			componentId.setBound(true);
			componentId.setPropertyEditorClass(JTextFieldEditor.class);
			componentId.setDisplayName("componentId");
			componentId.setShortDescription("Component Id of issue.");

			PropertyDescriptor priority = new PropertyDescriptor("priority", CreateIssue.class);
			priority.setBound(true);
			priority.setPropertyEditorClass(JTextFieldEditor.class);
			priority.setDisplayName("priority");
			priority.setShortDescription("Priority of issue.");

			PropertyDescriptor user = new PropertyDescriptor("user", CreateIssue.class);
			user.setBound(true);
			user.setPropertyEditorClass(JTextFieldEditor.class);
			user.setDisplayName("user");
			user.setShortDescription("User who create the issue.");

			PropertyDescriptor rv[] =
			{issueRepoProp, title, description,componentId,priority,user};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(CreateIssue.class);
		bd.setDisplayName("<html><font color='green'><center>Post New<br>Issue ");
		return bd;
	}

}
