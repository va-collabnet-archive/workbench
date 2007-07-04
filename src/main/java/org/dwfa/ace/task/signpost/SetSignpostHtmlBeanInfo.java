package org.dwfa.ace.task.signpost;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetSignpostHtmlBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor htmlPropName =
                new PropertyDescriptor("htmlPropName", getBeanDescriptor().getBeanClass());
            htmlPropName.setBound(true);
            htmlPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            htmlPropName.setDisplayName("<html><font color='green'>Set signpost html");
            htmlPropName.setShortDescription("Set the signpost html to value contained in this property.");

            PropertyDescriptor rv[] = { htmlPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetSignpostHtml.class);
        bd.setDisplayName("<html><font color='green'><center>Set Signpost html");
        return bd;
    }

}
