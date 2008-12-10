package org.dwfa.ace.task.prop.set;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class OneItemInSetBeanInfo extends SimpleBeanInfo {


    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor inputPropName =
                new PropertyDescriptor("inputPropName", getBeanDescriptor().getBeanClass());
            inputPropName.setBound(true);
            inputPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            inputPropName.setDisplayName("<html><font color='green'>input set property");
            inputPropName.setShortDescription("");

            PropertyDescriptor rv[] =
                { inputPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(OneItemInSet.class);
        bd.setDisplayName("<html><font color='green'><center>Check if 1<br>item in set");
        return bd;
    }
}
