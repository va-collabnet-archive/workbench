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
/*
 * Created on Apr 7, 2006
 */
package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class LoadSetLaunchProcessFromURLBeanInfo extends SimpleBeanInfo {

    public LoadSetLaunchProcessFromURLBeanInfo() {
        super();
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LoadSetLaunchProcessFromURL.class);
        bd.setDisplayName("<html><center><font color='blue'>Load, Set, Launch<br>Process From URL");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor processURLString = new PropertyDescriptor("processURLString",
                getBeanDescriptor().getBeanClass());
            processURLString.setBound(true);
            processURLString.setPropertyEditorClass(JTextFieldEditor.class);
            processURLString.setDisplayName("process URL");
            processURLString.setShortDescription("A URL from which a process is loaded.");

            PropertyDescriptor rv[] = { processURLString };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
}
