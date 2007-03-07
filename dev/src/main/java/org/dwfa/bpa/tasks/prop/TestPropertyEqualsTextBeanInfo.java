/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class TestPropertyEqualsTextBeanInfo extends SimpleBeanInfo {

    public TestPropertyEqualsTextBeanInfo() {
        super();
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor valueTextPropertyName =
                new PropertyDescriptor("valueText", TestPropertyEqualsText.class);
            valueTextPropertyName.setBound(true);
            valueTextPropertyName.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            valueTextPropertyName.setDisplayName("<html><font color='blue'>Test value:");
            valueTextPropertyName.setShortDescription("Text representation of desired value. ");
            
            PropertyDescriptor localPropertyName =
                new PropertyDescriptor("localPropName", TestPropertyEqualsText.class);
            localPropertyName.setBound(true);
            localPropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName.setDisplayName("<html><font color='blue'>Local property:");
            localPropertyName.setShortDescription("Name of the local property to test. ");

            PropertyDescriptor rv[] = { localPropertyName, valueTextPropertyName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(TestPropertyEqualsText.class);
           bd.setDisplayName("<html><center>Test Property<br>Equals Text");
        return bd;
    }
}
