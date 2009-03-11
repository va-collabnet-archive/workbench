package org.dwfa.bpa.tasks.dialog;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ShowInfoDialogFromPropertyBeanInfo extends SimpleBeanInfo {


    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
           
            PropertyDescriptor messagePropertyName =
                new PropertyDescriptor("messagePropertyName", getBeanDescriptor().getBeanClass());
            messagePropertyName.setBound(true);
            messagePropertyName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            messagePropertyName.setDisplayName("<html><font color='blue'>Msg property:");
            messagePropertyName.setShortDescription("Name of the property containing the dialog message text. ");

            PropertyDescriptor rv[] = { messagePropertyName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(ShowInfoDialogFromProperty.class);
           bd.setDisplayName("<html><center>Show Dialog<br>From Property");
        return bd;
    }
}
