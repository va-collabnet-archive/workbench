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
package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class SetSignpostToggleIconBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor iconResource = new PropertyDescriptor("iconResource", getBeanDescriptor().getBeanClass());
            iconResource.setBound(true);
            iconResource.setPropertyEditorClass(JTextFieldEditor.class);
            iconResource.setDisplayName("<html><font color='green'>Signpost toggle icon");
            iconResource.setShortDescription("Set the signpost toggle icon from classpath resource.<br><br>"
                + "help.png, help2.png, question_and_answer.png,<br>"
                + "about.png, lightbulb.png, lightbulb_on.png,<br>"
                + "message.png, message_add.png, message_delete.png,<br>"
                + "gear.png, gear_view.png, gears.png, gears_view.png");

            PropertyDescriptor rv[] = { iconResource };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSignpostToggleIcon.class);
        bd.setDisplayName("<html><font color='green'><center>Set Signpost <br>" + "toggle icon");
        return bd;
    }

}
