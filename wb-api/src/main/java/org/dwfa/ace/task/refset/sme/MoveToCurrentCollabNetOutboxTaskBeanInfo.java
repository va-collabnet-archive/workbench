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
package org.dwfa.ace.task.refset.sme;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

/**
 * This task finds a matching CollabNet outbox queue based on the current
 * workbench user.
 * The outbox name must be named using the convention (outbox name set in the
 * queue config file):
 * workbenchUserName-collabnet.outbox
 * 
 */
public class MoveToCurrentCollabNetOutboxTaskBeanInfo extends SimpleBeanInfo {

    public MoveToCurrentCollabNetOutboxTaskBeanInfo() {
        super();
    }

    /**
     * Returns a list of property descriptors for this task.
     * 
     * @return Returns a PropertyDescriptor array containing the properties of
     *         this task
     * @exception Error Thrown when an exception happens during Introspection
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor message;
            message = new PropertyDescriptor("message", MoveToCurrentCollabNetOutboxTask.class);
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("message");
            message.setShortDescription("A message to present to the user in a dialog after moving to queue.");
            PropertyDescriptor rv[] = { message };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * Return the descriptor for this JavaBean which contains a reference to the
     * JavaBean that implements this task as well as the display name of the
     * task along
     * with formating information.
     * 
     * @see java.beans.BeanInfo#getBeanDescriptor()
     * @return Returns the BeanDescriptor for this task
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(MoveToCurrentCollabNetOutboxTask.class);
        bd.setDisplayName("<html><font color='green'><center>Move BP to current user's <br>Collabnet outbox");
        return bd;
    }

}
