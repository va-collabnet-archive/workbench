package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to ChooseUuidFile class.
 * @author Susan Castillo
 *
 */
public class ChooseUuidFileBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ChooseUuidFileBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor uuidFileNamePropName =
                new PropertyDescriptor("uuidFileNamePropName", getBeanDescriptor().getBeanClass());
            uuidFileNamePropName.setBound(true);
            uuidFileNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            uuidFileNamePropName.setDisplayName("<html><font color='green'>UUID File:");
            uuidFileNamePropName.setShortDescription("UUID File");

            PropertyDescriptor rv[] = { uuidFileNamePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChooseUuidFile.class);
        bd.setDisplayName("<html><font color='green'><center>Choose UUID File");
        return bd;
    }

}


