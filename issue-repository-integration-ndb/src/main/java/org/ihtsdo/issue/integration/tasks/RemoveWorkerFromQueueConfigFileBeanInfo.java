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
 * The Class RemoveWorkerFromQueueConfigFileBeanInfo.
 */
public class RemoveWorkerFromQueueConfigFileBeanInfo extends SimpleBeanInfo {
	
	/**
	 * Instantiates a new removes the worker from queue config file bean info.
	 */
	public RemoveWorkerFromQueueConfigFileBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
		try {
			PropertyDescriptor issueRepoProp = new PropertyDescriptor("issueRepoProp", RemoveWorkerFromQueueConfigFile.class);
			issueRepoProp.setBound(true);
			issueRepoProp.setPropertyEditorClass(ConceptLabelPropEditor.class);
			issueRepoProp.setDisplayName("issueRepoProp");
			issueRepoProp.setShortDescription("Issue Repository Concept.");

			PropertyDescriptor selectedWorker = new PropertyDescriptor("selectedWorker", RemoveWorkerFromQueueConfigFile.class);
			selectedWorker.setBound(true);
			selectedWorker.setPropertyEditorClass(QueueWorkerSelectorEditor.class);
			selectedWorker.setDisplayName("Select Worker");
			selectedWorker.setShortDescription("Select Worker to include.");
			
			PropertyDescriptor message = new PropertyDescriptor("message", RemoveWorkerFromQueueConfigFile.class);
			message.setBound(true);
			message.setPropertyEditorClass(JTextFieldEditor.class);
			message.setDisplayName("<html><font color='green'>Prompt to user:");
			message.setShortDescription("Message when prompting user for file");

			PropertyDescriptor rv[] = {issueRepoProp, selectedWorker};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(RemoveWorkerFromQueueConfigFile.class);
		bd.setDisplayName("<html><font color='green'><center>Remove worker<br>from Queue<br>config file");
		return bd;
	}

}
