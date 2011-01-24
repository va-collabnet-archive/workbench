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
package org.ihtsdo.project.tasks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * The Class SearchAndDeleteQueueItemsBeanInfo.
 */
public class SearchAndDeleteQueueItemsBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new creates the new project bean info.
	 */
	public SearchAndDeleteQueueItemsBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {
			PropertyDescriptor queueName = new PropertyDescriptor("queueName", SearchAndDeleteQueueItems.class);
			queueName.setBound(true);
			queueName.setPropertyEditorClass(JTextFieldEditor.class);
			queueName.setDisplayName("Queue Name");
			queueName.setShortDescription("Name for the queue to consolidate.");
			PropertyDescriptor rv[] =
			{queueName};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(SearchAndDeleteQueueItems.class);
		bd.setDisplayName("<html><font color='green'><center>Consolidate Queue");
		return bd;
	}

}
