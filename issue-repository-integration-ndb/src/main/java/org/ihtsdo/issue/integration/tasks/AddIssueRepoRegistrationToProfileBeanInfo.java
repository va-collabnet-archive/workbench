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
 * The Class AddIssueRepoRegistrationToProfileBeanInfo.
 */
public class AddIssueRepoRegistrationToProfileBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new adds the issue repo registration to profile bean info.
	 */
	public AddIssueRepoRegistrationToProfileBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor issueRepoProp = new PropertyDescriptor("issueRepoProp", AddIssueRepoRegistrationToProfile.class);
			issueRepoProp.setBound(true);
			issueRepoProp.setPropertyEditorClass(ConceptLabelPropEditor.class);
			issueRepoProp.setDisplayName("issueRepoProp");
			issueRepoProp.setShortDescription("Issue Repository Concept.");
			
			PropertyDescriptor usernameProp = new PropertyDescriptor("username", AddIssueRepoRegistrationToProfile.class);
			usernameProp.setBound(true);
			usernameProp.setPropertyEditorClass(JTextFieldEditor.class);
			usernameProp.setDisplayName("username");
			usernameProp.setShortDescription("Username.");

			PropertyDescriptor passwordProp = new PropertyDescriptor("password", AddIssueRepoRegistrationToProfile.class);
			passwordProp.setBound(true);
			passwordProp.setPropertyEditorClass(JTextFieldEditor.class);
			passwordProp.setDisplayName("password");
			passwordProp.setShortDescription("Password.");

			PropertyDescriptor rv[] =
			{issueRepoProp, usernameProp, passwordProp};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(AddIssueRepoRegistrationToProfile.class);
		bd.setDisplayName("<html><font color='green'><center>Add IssueRepoReg<br>to profile ");
		return bd;
	}

}
