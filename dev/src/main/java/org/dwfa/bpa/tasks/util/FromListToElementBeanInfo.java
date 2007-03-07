/*
 * Created on Mar 23, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
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
            elementId.setShortDescription("The element that containes the value to add to the list.");

            PropertyDescriptor listId =
                new PropertyDescriptor("listId", FromListToElement.class);
            listId.setBound(true);
            listId.setPropertyEditorClass(DataIdEditorOneLine.class);
            listId.setDisplayName("List");
            listId.setShortDescription("The list that the value is added to.");

            PropertyDescriptor rv[] =
                {elementId, listId};
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