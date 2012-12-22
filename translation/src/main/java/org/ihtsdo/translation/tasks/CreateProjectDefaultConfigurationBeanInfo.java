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

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The Class CreateProjectDefaultConfigurationBeanInfo.
 */
public class CreateProjectDefaultConfigurationBeanInfo extends SimpleBeanInfo {

    /**
     * Instantiates a new list projects to signpost bean info.
     */
    public CreateProjectDefaultConfigurationBeanInfo() {
        super();
    }
    
    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
    	PropertyDescriptor projectPropName;
		try {
			projectPropName = new PropertyDescriptor("projectPropName",getBeanDescriptor().getBeanClass());
			projectPropName.setBound(true);
			projectPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
			projectPropName.setDisplayName("<html><font color='green'>project prop:");
			projectPropName.setShortDescription("The property that will contain the current project.");
			PropertyDescriptor rv[] = { projectPropName };
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
     }        
    
    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CreateProjectDefaultConfiguration.class);
        bd.setDisplayName("<html><font color='green'><center>Create Project<br>Default Configuration.");
        return bd;
    }

}
