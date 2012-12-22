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

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * The Class OpenTranslationForSelectedConceptBeanInfo.
 */
public class SetAutoOpenNextItemBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new open translation for selected concept bean info.
	 */
	public SetAutoOpenNextItemBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		PropertyDescriptor autoOpenItem;
		try {
			autoOpenItem = new PropertyDescriptor("autoOpenItem", 
					getBeanDescriptor().getBeanClass());
			autoOpenItem.setBound(true);
			autoOpenItem.setPropertyEditorClass(CheckboxEditor.class);
			autoOpenItem.setDisplayName("Auto Open Next Item");
			autoOpenItem.setShortDescription("Auto Open Next Item");
		PropertyDescriptor rv[] ={autoOpenItem};
		return rv;

		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(SetAutoOpenNextItem.class);
		bd.setDisplayName("<html><font color='green'><center>Setup Auto<br>Open Next Item");
		return bd;
	}

}
