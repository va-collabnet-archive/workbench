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
package org.dwfa.bpa.tasks.web;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;

public class FetchFromWebBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public FetchFromWebBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor webURLString = new PropertyDescriptor("webURLString", FetchFromWeb.class);
            webURLString.setBound(true);
            webURLString.setPropertyEditorClass(JTextFieldEditor.class);
            webURLString.setDisplayName("webURLString");
            webURLString.setShortDescription("A webURL to fetch strings from to present to the user.");

            PropertyDescriptor rv[] = { webURLString };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(FetchFromWeb.class);
        bd.setDisplayName("<html><font color='green'><center>Fetch string from web");
        return bd;
    }

}
