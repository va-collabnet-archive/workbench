package org.dwfa.bpa.tasks.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AppendStringToPropertyBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor valueTextPropertyName =
                new PropertyDescriptor("valueText", getBeanDescriptor().getBeanClass());
            valueTextPropertyName.setBound(true);
            valueTextPropertyName.setPropertyEditorClass(JTextFieldEditorOneLine.class);
            valueTextPropertyName.setDisplayName("<html><font color='blue'>Append value:");
            valueTextPropertyName.setShortDescription("Text to append. ");
            
            PropertyDescriptor stringPropName =
                new PropertyDescriptor("stringPropName", getBeanDescriptor().getBeanClass());
            stringPropName.setBound(true);
            stringPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            stringPropName.setDisplayName("<html><font color='blue'>property:");
            stringPropName.setShortDescription("Name of the property to append to. ");

            PropertyDescriptor rv[] = { stringPropName, valueTextPropertyName};
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(AppendStringToProperty.class);
           bd.setDisplayName("<html><center>Append String<br>To Property");
        return bd;
    }
}
