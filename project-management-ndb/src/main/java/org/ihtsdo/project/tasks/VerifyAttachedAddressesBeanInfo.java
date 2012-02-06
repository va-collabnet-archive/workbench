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

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The Class VerifyAttachedAddressesBeanInfo.
 */
public class VerifyAttachedAddressesBeanInfo extends SimpleBeanInfo {

	/**
	 * Instantiates a new verify attached addresses bean info.
	 */
	public VerifyAttachedAddressesBeanInfo() {
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
			
			PropertyDescriptor translator = new PropertyDescriptor("translator", 
					getBeanDescriptor().getBeanClass());
			translator.setBound(true);
			translator.setPropertyEditorClass(CheckboxEditor.class);
			translator.setDisplayName("Translator role");
			translator.setShortDescription("Translator role");

			PropertyDescriptor reviewer1 = new PropertyDescriptor("reviewer1", 
					getBeanDescriptor().getBeanClass());
			reviewer1.setBound(true);
			reviewer1.setPropertyEditorClass(CheckboxEditor.class);
			reviewer1.setDisplayName("Reviewer one");
			reviewer1.setShortDescription("Reviewer one");

			PropertyDescriptor reviewer2 = new PropertyDescriptor("reviewer2", 
					getBeanDescriptor().getBeanClass());
			reviewer2.setBound(true);
			reviewer2.setPropertyEditorClass(CheckboxEditor.class);
			reviewer2.setDisplayName("Reviewer two");
			reviewer2.setShortDescription("Reviewer two");

			PropertyDescriptor sme = new PropertyDescriptor("sme", 
					getBeanDescriptor().getBeanClass());
			sme.setBound(true);
			sme.setPropertyEditorClass(CheckboxEditor.class);
			sme.setDisplayName("Subject matter expert");
			sme.setShortDescription("Subject matter expert");

			PropertyDescriptor editorialBoard = new PropertyDescriptor("editorialBoard", 
					getBeanDescriptor().getBeanClass());
			editorialBoard.setBound(true);
			editorialBoard.setPropertyEditorClass(CheckboxEditor.class);
			editorialBoard.setDisplayName("Editorial board");
			editorialBoard.setShortDescription("Editorial board");

			PropertyDescriptor rv[] =
			{profilePropName, translator, reviewer1, reviewer2, sme, editorialBoard};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}

	}        

	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(VerifyAttachedAddresses.class);
		bd.setDisplayName("<html><font color='green'><center>Verify attached<br>addresses");
		return bd;
	}

}
