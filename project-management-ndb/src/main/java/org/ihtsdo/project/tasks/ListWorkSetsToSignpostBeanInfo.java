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

/**
 * The Class ListWorkSetsToSignpostBeanInfo.
 */
public class ListWorkSetsToSignpostBeanInfo extends SimpleBeanInfo {

    /**
     * Instantiates a new list work sets to signpost bean info.
     */
    public ListWorkSetsToSignpostBeanInfo() {
        super();
    }
    
    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
    	try {
			PropertyDescriptor projectName = new PropertyDescriptor("projectName", ListWorkSetsToSignpost.class);
			projectName.setBound(true);
			projectName.setPropertyEditorClass(ProjectSelectorEditor.class);
			projectName.setDisplayName("projectName");
			projectName.setShortDescription("Select a project.");
			PropertyDescriptor rv[] = {projectName};
			return rv;
		} catch (IntrospectionException e) {
			throw new Error(e.toString());
		}
     }        
    
    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ListWorkSetsToSignpost.class);
        bd.setDisplayName("<html><font color='green'><center>List WorkSets to<br>Signpost");
        return bd;
    }

}
