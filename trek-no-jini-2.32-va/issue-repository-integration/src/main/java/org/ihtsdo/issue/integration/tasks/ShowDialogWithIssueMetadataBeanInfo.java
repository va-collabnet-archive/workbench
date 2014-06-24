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

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * The Class ShowDialogWithIssueMetadataBeanInfo.
 */
public class ShowDialogWithIssueMetadataBeanInfo extends SimpleBeanInfo {
    
    /**
     * Instantiates a new show dialog with issue metadata bean info.
     */
    public ShowDialogWithIssueMetadataBeanInfo() {
        super();
    }
    
    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor message =
                new PropertyDescriptor("message", ShowDialogWithIssueMetadata.class);
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("message");
            message.setShortDescription("A message to present to the user in a dialog.");


            PropertyDescriptor rv[] =
                {message};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    
    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowDialogWithIssueMetadata.class);
        bd.setDisplayName("<html><font color='green'><center>Show Attached<br> issue metadata<br>on Dialog");
        return bd;
    }

}
