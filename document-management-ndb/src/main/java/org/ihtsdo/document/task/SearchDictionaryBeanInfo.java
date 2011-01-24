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
package org.ihtsdo.document.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * The Class SearchDictionaryBeanInfo.
 */
public class SearchDictionaryBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new search dictionary bean info.
	 */
	public SearchDictionaryBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {
		PropertyDescriptor queryInput = null;

		try {
			queryInput = new PropertyDescriptor("query", SearchDictionary.class);
			queryInput.setBound(true);
			queryInput.setPropertyEditorClass(JTextFieldEditor.class);
			queryInput.setDisplayName("query");
			queryInput.setShortDescription("Query to search.");
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PropertyDescriptor rv[] =
		{queryInput};
		return rv;

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(SearchDictionary.class);
		bd.setDisplayName("<html><font color='green'><center>Search Dictionary");
		return bd;
	}

}
