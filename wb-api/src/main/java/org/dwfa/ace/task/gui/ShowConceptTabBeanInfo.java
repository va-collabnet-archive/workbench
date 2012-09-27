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

import org.dwfa.bpa.tasks.editor.IncrementEditor;

/**
 * Bean info to ShowConceptTab class.
 * 
 * @author Christine Hill
 */
public class ShowConceptTabBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ShowConceptTabBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {
            PropertyDescriptor index = new PropertyDescriptor("index", ShowConceptTab.class);
            index.setBound(true);
            index.setPropertyEditorClass(IncrementEditor.class);
            index.setDisplayName("<html><font color='green'>Concept tab:");
            index.setShortDescription("Index of tab to switch to. ");
            PropertyDescriptor rv[] = { index };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ShowConceptTab.class);
        bd.setDisplayName("<html><font color='green'><center>Switch to <br>" + "Concept Tab");
        return bd;
    }

}
