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

import org.dwfa.bpa.tasks.editor.ConceptLabelEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The Class CreateLangSpecRefset.
 */
public class AskReasonForWorkListMemberRejectionBeanInfo extends SimpleBeanInfo {

	public AskReasonForWorkListMemberRejectionBeanInfo() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName",
					getBeanDescriptor().getBeanClass());
			profilePropName.setBound(true);
			profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			profilePropName.setDisplayName("<html><font color='green'>profile prop:");
			profilePropName.setShortDescription("The property that will contain the current profile.");

			PropertyDescriptor worklistMemberPropName = new PropertyDescriptor("worklistMemberPropName",
					getBeanDescriptor().getBeanClass());
			worklistMemberPropName.setBound(true);
			worklistMemberPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			worklistMemberPropName.setDisplayName("<html><font color='green'>worklistMemberPropName:");
			worklistMemberPropName.setShortDescription("worklistMemberPropName.");
			
			PropertyDescriptor stepRole = new PropertyDescriptor("stepRole", getBeanDescriptor().getBeanClass());
			stepRole.setBound(true);
			stepRole.setPropertyEditorClass(ConceptLabelEditor.class);
			stepRole.setDisplayName("<html><font color='green'>Role:");
			stepRole.setShortDescription("Role associated with this wf step");

			PropertyDescriptor rv[] = {profilePropName, worklistMemberPropName, stepRole};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        

	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(AskReasonForWorkListMemberRejection.class);
		bd.setDisplayName("<html><font color='green'><center>Ask reason for<br>rejection of<br>worklist item");
		return bd;
	}

}
