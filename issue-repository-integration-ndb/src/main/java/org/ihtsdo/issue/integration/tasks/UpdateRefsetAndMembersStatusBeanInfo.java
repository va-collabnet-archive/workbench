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

/**
 * The Class UpdateRefsetAndMembersStatusBeanInfo.
 */
public class UpdateRefsetAndMembersStatusBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new update refset and members status bean info.
	 */
	public UpdateRefsetAndMembersStatusBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {
			PropertyDescriptor newStatus = new PropertyDescriptor("newStatus", UpdateRefsetAndMembersStatus.class);
			newStatus.setBound(true);
			newStatus.setPropertyEditorClass(ConceptLabelPropEditor.class);
			newStatus.setDisplayName("newStatus");
			newStatus.setShortDescription("New Status.");

			PropertyDescriptor rv[] = {newStatus};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(UpdateRefsetAndMembersStatus.class);
		bd.setDisplayName("<html><font color='green'><center>Update Refset<br>and Members<br>status");
		return bd;
	}

}
