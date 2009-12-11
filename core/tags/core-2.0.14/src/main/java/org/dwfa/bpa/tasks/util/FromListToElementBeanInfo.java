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
 * Created on Mar 23, 2005
 */
package org.dwfa.bpa.tasks.util;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.DataIdEditorOneLine;

/**
 * @author kec
 *
 */
public class FromListToElementBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public FromListToElementBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor elementId =
                    new PropertyDescriptor("elementId", FromListToElement.class);
            elementId.setBound(true);
            elementId.setPropertyEditorClass(DataIdEditorOneLine.class);
            elementId.setDisplayName("Element");
            elementId
                .setShortDescription("The element that containes the value to add to the list.");

            PropertyDescriptor listId =
                    new PropertyDescriptor("listId", FromListToElement.class);
            listId.setBound(true);
            listId.setPropertyEditorClass(DataIdEditorOneLine.class);
            listId.setDisplayName("List");
            listId.setShortDescription("The list that the value is added to.");

            PropertyDescriptor rv[] = { elementId, listId };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(FromListToElement.class);
        bd.setDisplayName("<html><center>List to Element");
        return bd;
    }

}
