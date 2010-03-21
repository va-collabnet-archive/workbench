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

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class CheckoutSelectedProfileBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor svnUrl = new PropertyDescriptor("svnUrl", getBeanDescriptor().getBeanClass());
            svnUrl.setBound(true);
            svnUrl.setPropertyEditorClass(JTextFieldEditor.class);
            svnUrl.setDisplayName("<html><font color='green'>svn url:");
            svnUrl.setShortDescription("The URL of the repository to checkout.");

            PropertyDescriptor checkoutLocation = new PropertyDescriptor("checkoutLocation",
                getBeanDescriptor().getBeanClass());
            checkoutLocation.setBound(true);
            checkoutLocation.setPropertyEditorClass(JTextFieldEditor.class);
            checkoutLocation.setDisplayName("<html><font color='green'>checkout location:");
            checkoutLocation.setShortDescription("The local directory to hold the working copy.");

            PropertyDescriptor rv[] = { svnUrl, checkoutLocation };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(CheckoutSelectedProfile.class);
        bd.setDisplayName("<html><font color='green'><center>checkout selected<br>profile");
        return bd;
    }
}
