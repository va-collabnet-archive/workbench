/*
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
/*
 * Created on Mar 24, 2005
 */
package org.ihtsdo.translation.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.prop.editor.ConceptLabelPropEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The Class RoleBasedWFDestinationSelectorBeanInfo.
 *
 * @author kec
 */
public class RoleBasedWFDestinationSelectorBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new role based wf destination selector bean info.
	 */
	public RoleBasedWFDestinationSelectorBeanInfo() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor exit1PropName = new PropertyDescriptor("exit1PropName", getBeanDescriptor().getBeanClass());
			exit1PropName.setBound(true);
			exit1PropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			exit1PropName.setDisplayName("<html><font color='green'>Exit1 Prop Name:");
			exit1PropName.setShortDescription("exit1PropName");
			
			PropertyDescriptor stepRole = new PropertyDescriptor("stepRole", getBeanDescriptor().getBeanClass());
			stepRole.setBound(true);
			stepRole.setPropertyEditorClass(ConceptLabelPropEditor.class);
			stepRole.setDisplayName("<html><font color='green'>Role:");
			stepRole.setShortDescription("Role associated with this wf step");

			PropertyDescriptor rv[] = {exit1PropName, stepRole};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}

	/**
	 * Gets the bean descriptor.
	 *
	 * @return the bean descriptor
	 * @see java.beans.BeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(RoleBasedWFDestinationSelector.class);
		bd.setDisplayName("<html><font color='green'>Role based<br>destination selector");
		return bd;
	}

}
