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
package org.dwfa.bpa.worker.task;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.IncrementEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddFormCompleterPluginBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor workerPropName = new PropertyDescriptor("workerPropName",
                getBeanDescriptor().getBeanClass());
            workerPropName.setBound(true);
            workerPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            workerPropName.setDisplayName("<html><font color='green'>worker prop:");
            workerPropName.setShortDescription("The property that holds the worker to add the plugin to.");

            PropertyDescriptor completeDelay = new PropertyDescriptor("completeDelay",
                getBeanDescriptor().getBeanClass());
            completeDelay.setBound(true);
            completeDelay.setPropertyEditorClass(IncrementEditor.class);
            completeDelay.setDisplayName("complete delay (s)");
            completeDelay.setShortDescription("Shows or hides this component depending on the selected value.");

            PropertyDescriptor rv[] = { workerPropName, completeDelay };
            return rv;
        } catch (Exception e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(AddFormCompleterPlugin.class);
        bd.setDisplayName("<html><font color='green'><center>Add Plugin:<br>I_CompleteForm");
        return bd;
    }

}
