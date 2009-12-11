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
 * The RefreshRefsetSpecCompareTaskBeanInfo class describes the visible elements of the 
 * Workflow task RefreshRefsetSpecCompareTask so that it can be displayed in the 
 * Process Builder.  The RefreshRefsetSpecCompareTask uses the information 
 * collected in the RefreshRefsetSpecWizardTask task to create a list of differences 
 * between the selected Refset and the selected version of SNOMED.  
 * 
 * @author  Perry Reid
 * @version 1, November 2009 
 */
public class RefreshRefsetSpecCompareTaskBeanInfo extends SimpleBeanInfo {

    /**
    *  Constructor
    */
    public RefreshRefsetSpecCompareTaskBeanInfo() {
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
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("[IN] The property that contains the current profile.");
            
            PropertyDescriptor refsetUuidPropName;
            refsetUuidPropName = new PropertyDescriptor("refsetUuidPropName", getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='green'>member refset UUID prop:");
            refsetUuidPropName.setShortDescription("[IN] The property that contains the Refset UUID.");
            
            PropertyDescriptor refsetSpecVersionPropName =
                new PropertyDescriptor("refsetSpecVersionPropName", getBeanDescriptor().getBeanClass());
            refsetSpecVersionPropName.setBound(true);
            refsetSpecVersionPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetSpecVersionPropName.setDisplayName("<html><font color='green'>refset version prop:");
            refsetSpecVersionPropName.setShortDescription("[IN] The property that contains the Refset version.");

            PropertyDescriptor snomedVersionPropName =
                new PropertyDescriptor("snomedVersionPropName", getBeanDescriptor().getBeanClass());
            snomedVersionPropName.setBound(true);
            snomedVersionPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            snomedVersionPropName.setDisplayName("<html><font color='green'>SNOMED version prop:");
            snomedVersionPropName.setShortDescription("[IN] The property that contains the SNOMED version.");

            PropertyDescriptor changesListPropName =
                new PropertyDescriptor("changesListPropName", getBeanDescriptor().getBeanClass());
            changesListPropName.setBound(true);
            changesListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            changesListPropName.setDisplayName("<html><font color='blue'>List of changes:");
            changesListPropName.setShortDescription("[OUT] The property that will contain the list of observed changes.");

            PropertyDescriptor reviewCountPropName =
                new PropertyDescriptor("reviewCountPropName", getBeanDescriptor().getBeanClass());
            reviewCountPropName.setBound(true);
            reviewCountPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reviewCountPropName.setDisplayName("<html><font color='blue'>review count prop:");
            reviewCountPropName.setShortDescription("[OUT] The property that will contain the number of items to be reviewed.");

            PropertyDescriptor reviewIndexPropName =
                new PropertyDescriptor("reviewIndexPropName", getBeanDescriptor().getBeanClass());
            reviewIndexPropName.setBound(true);
            reviewIndexPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            reviewIndexPropName.setDisplayName("<html><font color='blue'>review index prop:");
            reviewIndexPropName.setShortDescription("[OUT] The property that will contain the index of the item currently being reviewed.");

            PropertyDescriptor rv[] =
               { profilePropName, refsetSpecVersionPropName, refsetUuidPropName, snomedVersionPropName, 
            		changesListPropName, reviewCountPropName, reviewIndexPropName };

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
        BeanDescriptor bd = new BeanDescriptor(RefreshRefsetSpecCompareTask.class);
        bd.setDisplayName("<html><font color='green'><center>"
        		+ "Compare Refset to SNOMED<br>and List Differences");
        return bd;
    }

}