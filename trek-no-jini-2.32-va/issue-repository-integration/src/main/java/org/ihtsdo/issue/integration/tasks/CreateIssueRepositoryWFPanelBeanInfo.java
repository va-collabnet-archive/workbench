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

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * The Class CreateIssueRepositoryWFPanelBeanInfo.
 */
public class CreateIssueRepositoryWFPanelBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new creates the issue repository wf panel bean info.
	 */
	public CreateIssueRepositoryWFPanelBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {
			PropertyDescriptor infoRepositoryId = new PropertyDescriptor("repositoryId", CreateIssueRepositoryWFPanel.class);
			infoRepositoryId.setBound(true);
			infoRepositoryId.setPropertyEditorClass(JTextFieldEditor.class);
			infoRepositoryId.setDisplayName("repositoryId");
			infoRepositoryId.setShortDescription("ID of the repository.");
			
			PropertyDescriptor infoRepositoryUrl = new PropertyDescriptor("repositoryUrl", CreateIssueRepositoryWFPanel.class);
			infoRepositoryUrl.setBound(true);
			infoRepositoryUrl.setPropertyEditorClass(JTextFieldEditor.class);
			infoRepositoryUrl.setDisplayName("repositoryUrl");
			infoRepositoryUrl.setShortDescription("URL of the repository.");

			PropertyDescriptor infoRepositoryName = new PropertyDescriptor("repositoryName", CreateIssueRepositoryWFPanel.class);
			infoRepositoryName.setBound(true);
			infoRepositoryName.setPropertyEditorClass(JTextFieldEditor.class);
			infoRepositoryName.setDisplayName("respositoryName");
			infoRepositoryName.setShortDescription("Name of the repository.");
			
			PropertyDescriptor infoRepositoryType = new PropertyDescriptor("repositoryType", CreateIssueRepositoryWFPanel.class);
			infoRepositoryType.setBound(true);
			infoRepositoryType.setPropertyEditorClass(RepositoryTypeSelectorEditor.class);
			infoRepositoryType.setDisplayName("repositoryType");
			infoRepositoryType.setShortDescription("Select a repositoryType.");
			
			PropertyDescriptor rv[] =  {/*infoRepositoryId, infoRepositoryUrl, infoRepositoryName, infoRepositoryType*/};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(CreateIssueRepositoryWFPanel.class);
		bd.setDisplayName("<html><font color='green'><center>Create new<br>issue repository<br>WF Panel");
		return bd;
	}

}
