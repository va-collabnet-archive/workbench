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

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class LoadSetLaunchProcessFromAttachmentBeanInfo extends SimpleBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LoadSetLaunchProcessFromAttachment.class);
        bd.setDisplayName("<html><center><font color='blue'>Load, Set, Launch<br>Process From Attachment");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor processPropName = new PropertyDescriptor("processPropName",
                getBeanDescriptor().getBeanClass());
            processPropName.setBound(true);
            processPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            processPropName.setDisplayName("process prop");
            processPropName.setShortDescription("A property containing a process which is loaded, set, then launched.");

            PropertyDescriptor dataCheckingSuppressed = new PropertyDescriptor("dataCheckingSuppressed",
                getBeanDescriptor().getBeanClass());
            dataCheckingSuppressed.setBound(true);
            dataCheckingSuppressed.setPropertyEditorClass(CheckboxEditor.class);
            dataCheckingSuppressed.setDisplayName("<html><font color='green'>Suppress data checks:");
            dataCheckingSuppressed.setShortDescription("Select if data checking are to be suppressed in supporting tasks");

            PropertyDescriptor rv[] = { processPropName, dataCheckingSuppressed };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
}
