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
package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

public class SetQueueButtonVisibilityBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            PropertyDescriptor enableNewInboxButton =
                    new PropertyDescriptor("enableNewInboxButton", getBeanDescriptor().getBeanClass());
            enableNewInboxButton.setBound(true);
            enableNewInboxButton.setPropertyEditorClass(CheckboxEditor.class);
            enableNewInboxButton.setDisplayName("<html><font color='green'>enable add-new-inbox button:");
            enableNewInboxButton.setShortDescription("State to set toggle to. ");

            PropertyDescriptor enableExistingInboxButton =
                    new PropertyDescriptor("enableExistingInboxButton", getBeanDescriptor().getBeanClass());
            enableExistingInboxButton.setBound(true);
            enableExistingInboxButton.setPropertyEditorClass(CheckboxEditor.class);
            enableExistingInboxButton.setDisplayName("<html><font color='green'>enable add-existing-inbox button:");
            enableExistingInboxButton.setShortDescription("State to set toggle to. ");

            PropertyDescriptor enableMoveListenerButton =
                    new PropertyDescriptor("enableMoveListenerButton", getBeanDescriptor().getBeanClass());
            enableMoveListenerButton.setBound(true);
            enableMoveListenerButton.setPropertyEditorClass(CheckboxEditor.class);
            enableMoveListenerButton.setDisplayName("<html><font color='green'>enable take-selected-and-save button:");
            enableMoveListenerButton.setShortDescription("State to set toggle to. ");

            PropertyDescriptor enableAllQueuesButton =
                    new PropertyDescriptor("enableAllQueuesButton", getBeanDescriptor().getBeanClass());
            enableAllQueuesButton.setBound(true);
            enableAllQueuesButton.setPropertyEditorClass(CheckboxEditor.class);
            enableAllQueuesButton.setDisplayName("<html><font color='green'>enable show-all-queues button:");
            enableAllQueuesButton.setShortDescription("State to set toggle to. ");

            PropertyDescriptor rv[] =
                    { enableNewInboxButton, enableExistingInboxButton, enableMoveListenerButton, enableAllQueuesButton };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetQueueButtonVisibility.class);
        bd.setDisplayName("<html><font color='green'><center>Set visibility of<br>queue buttons");
        return bd;
    }

}
