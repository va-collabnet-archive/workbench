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

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * The Class CreateLangSpecRefset.
 */
public class ImportDescriptionsToLanguageRefsetBeanInfo extends SimpleBeanInfo {

	public ImportDescriptionsToLanguageRefsetBeanInfo() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		try {

			PropertyDescriptor release = new PropertyDescriptor("release", ImportDescriptionsToLanguageRefset.class);
			release.setBound(true);
			release.setPropertyEditorClass(JTextFieldEditor.class);
			release.setDisplayName("release date");
			release.setShortDescription("release date");
			
			PropertyDescriptor language = new PropertyDescriptor("language", ImportDescriptionsToLanguageRefset.class);
			language.setBound(true);
			language.setPropertyEditorClass(LanguageSelectorEditor.class);
			language.setDisplayName("language");
			language.setShortDescription("language");

			PropertyDescriptor rv[] =
			{release, language};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        
	
	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(ImportDescriptionsToLanguageRefset.class);
		bd.setDisplayName("<html><font color='green'><center>Import descriptions<br>to lang refset");
		return bd;
	}

}
