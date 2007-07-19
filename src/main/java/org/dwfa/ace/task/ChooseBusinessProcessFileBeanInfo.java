package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
* Bean info to ChooseBusinessProcessFile class.
* @author Susan Castillo
*
*/
public class ChooseBusinessProcessFileBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public ChooseBusinessProcessFileBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor processFileNamePropName =
                new PropertyDescriptor("processFileNamePropName", getBeanDescriptor().getBeanClass());
            processFileNamePropName.setBound(true);
            processFileNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            processFileNamePropName.setDisplayName("<html><font color='green'>Business Process<br>File:");
            processFileNamePropName.setShortDescription("Business Process");

            PropertyDescriptor rv[] = { processFileNamePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ChooseBusinessProcessFile.class);
        bd.setDisplayName("<html><font color='green'><center>Choose Business Process");
        return bd;
    }

}

