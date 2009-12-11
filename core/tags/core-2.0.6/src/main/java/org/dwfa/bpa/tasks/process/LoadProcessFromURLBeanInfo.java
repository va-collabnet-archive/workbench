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
 * Created on Mar 8, 2006
 */
package org.dwfa.bpa.tasks.process;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.ProcessDataIdEditor;

public class LoadProcessFromURLBeanInfo extends SimpleBeanInfo {

    public LoadProcessFromURLBeanInfo() {
        super();
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LoadProcessFromURL.class);
        bd
            .setDisplayName("<html><center><font color='green'>Load Process<br>From URL");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor processURL =
                    new PropertyDescriptor("processURLString",
                        LoadProcessFromURL.class);
            processURL.setBound(true);
            processURL.setPropertyEditorClass(JTextFieldEditor.class);
            processURL.setDisplayName("process URL");
            processURL
                .setShortDescription("A URL from which a process is loaded.");

            PropertyDescriptor process =
                    new PropertyDescriptor("processDataId",
                        LoadProcessFromURL.class);
            process.setBound(true);
            process.setPropertyEditorClass(ProcessDataIdEditor.class);
            process.setDisplayName("<html><font color='green'>Process");
            process
                .setShortDescription("A data id for the process container to load. Only data containers that contain I_EncodeBusinessProcess objects can be dropped. ");

            PropertyDescriptor rv[] = { processURL, process };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
}
