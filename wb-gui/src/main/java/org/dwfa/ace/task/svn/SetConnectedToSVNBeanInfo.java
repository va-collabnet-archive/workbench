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
package org.dwfa.ace.task.svn;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;

/**
 * Bean info to SetConnectedToSVN class.
 * 
 * @author Christine Hill
 * 
 */
public class SetConnectedToSVNBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public SetConnectedToSVNBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor connectToSvn = new PropertyDescriptor("connectToSvn", SetConnectedToSVN.class);
            connectToSvn.setBound(true);
            connectToSvn.setPropertyEditorClass(CheckboxEditor.class);
            connectToSvn.setDisplayName("<html><font color='green'>Connect to SVN?");
            connectToSvn.setShortDescription("Connect to SVN?");

            PropertyDescriptor rv[] = { connectToSvn };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetConnectedToSVN.class);
        bd.setDisplayName("<html><font color='green'><center>Connect to SVN?");
        return bd;
    }

}
