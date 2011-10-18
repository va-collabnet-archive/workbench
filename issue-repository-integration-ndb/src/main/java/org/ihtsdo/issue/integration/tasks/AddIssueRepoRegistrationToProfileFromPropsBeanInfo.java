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

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The Class AddIssueRepoRegistrationToProfileFromPropsBeanInfo.
 */
public class AddIssueRepoRegistrationToProfileFromPropsBeanInfo extends SimpleBeanInfo {

	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor profileProp = new PropertyDescriptor("profileProp", 
					getBeanDescriptor().getBeanClass());
			profileProp.setBound(true);
			profileProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
			profileProp.setDisplayName("profile");
			profileProp.setShortDescription("Property holding the profile to add repo registration to.");

			PropertyDescriptor issueRepoProp = new PropertyDescriptor("issueRepoConceptProp", 
					getBeanDescriptor().getBeanClass());
			issueRepoProp.setBound(true);
			issueRepoProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
			issueRepoProp.setDisplayName("issue repo");
			issueRepoProp.setShortDescription("Property holding the issue repository concept.");

			PropertyDescriptor usernameProp = new PropertyDescriptor("usernameProp", 
					getBeanDescriptor().getBeanClass());
			usernameProp.setBound(true);
			usernameProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
			usernameProp.setDisplayName("username");
			usernameProp.setShortDescription("Property holding the username.");

			PropertyDescriptor passwordProp = new PropertyDescriptor("passwordProp", 
					getBeanDescriptor().getBeanClass());
			passwordProp.setBound(true);
			passwordProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
			passwordProp.setDisplayName("password");
			passwordProp.setShortDescription("Property holding the password.");

			PropertyDescriptor rv[] =
			{ profileProp, issueRepoProp, usernameProp, passwordProp};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(AddIssueRepoRegistrationToProfileFromProps.class);
		bd.setDisplayName("<html><font color='green'><center>Add IssueRepoReg<br>to profile<br>from props");
		return bd;
	}

}
