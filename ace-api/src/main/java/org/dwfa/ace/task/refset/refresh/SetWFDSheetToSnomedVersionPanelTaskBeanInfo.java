/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * The SetWFDSheetToSnomedVersionPanelTaskBeanInfo class describes the visible elements of the 
 * Workflow task SetWFDSheetToSnomedVersionPanelTask so that it can be displayed in the 
 * Process Builder. 
 * 
 * @author  Perry Reid
 * @version 1.0, November 2009 
 */
public class SetWFDSheetToSnomedVersionPanelTaskBeanInfo extends SimpleBeanInfo {

    public SetWFDSheetToSnomedVersionPanelTaskBeanInfo() {
        super();
    }

    /**
     * Returns a list of property descriptors for this task.   
     * @return  	Returns a PropertyDescriptor array containing the properties of this task  
     * @exception  	Error Thrown when an exception happens during Introspection
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            // The color "green" = denotes an [IN] property 
            // The color "blue"  = denotes an [OUT] property 
            PropertyDescriptor profilePropName =
                    new PropertyDescriptor("profilePropName",
                        getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName
                .setDisplayName("<html><font color='green'>profile prop:");
            profilePropName
                .setShortDescription("[IN] The property that will contain the current profile.");

            PropertyDescriptor snomedVersionPropName =
                    new PropertyDescriptor("snomedVersionPropName",
                        getBeanDescriptor().getBeanClass());
            snomedVersionPropName.setBound(true);
            snomedVersionPropName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            snomedVersionPropName
                .setDisplayName("<html><font color='blue'>SNOMED position set prop:");
            snomedVersionPropName
                .setShortDescription("[OUT] The property that will contain the SNOMED version.");

            PropertyDescriptor rv[] =
                    { profilePropName, snomedVersionPropName };

            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /** 
     * Return the descriptor for this JavaBean which contains a reference to the JavaBean 
     * that implements this task as well as the display name of the task along with 
     * formating information.
     * @see java.beans.BeanInfo#getBeanDescriptor()
     * @return	Returns the BeanDescriptor for this task      
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd =
                new BeanDescriptor(SetWFDSheetToSnomedVersionPanelTask.class);
        bd
            .setDisplayName("<html><font color='green'><center>Set WFD Sheet to<br>SNOMED Version panel");
        return bd;
    }

}
