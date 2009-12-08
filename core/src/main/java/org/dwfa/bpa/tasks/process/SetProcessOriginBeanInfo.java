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
package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class SetProcessOriginBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SetProcessOriginBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor newOrigin = new PropertyDescriptor("newOrigin", SetProcessOrigin.class);
            newOrigin.setBound(true);
            newOrigin.setPropertyEditorClass(JTextFieldEditor.class);
            newOrigin.setDisplayName("set process origin");
            newOrigin.setShortDescription("Sets the origin of the process to the provided value.");

            PropertyDescriptor rv[] = { newOrigin };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetProcessOrigin.class);
        bd.setDisplayName("<html><font color='green'><center>Set Process Origin");
        return bd;
    }

}
