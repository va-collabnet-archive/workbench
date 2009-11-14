package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GetRefsetVersionPanelDataTaskBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
         try {  

             PropertyDescriptor profilePropName =
                 new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
             profilePropName.setBound(true);
             profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             profilePropName.setDisplayName("<html><font color='green'>profile prop:");
             profilePropName.setShortDescription("The property that contains the working profile.");

             PropertyDescriptor positionSetPropName =
                 new PropertyDescriptor("positionSetPropName", getBeanDescriptor().getBeanClass());
             positionSetPropName.setBound(true);
             positionSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             positionSetPropName.setDisplayName("<html><font color='green'>position set prop:");
             positionSetPropName.setShortDescription("The property that will contain the Refset position set.");

            PropertyDescriptor rv[] =
                { profilePropName, positionSetPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetRefsetVersionPanelDataTask.class);
        bd.setDisplayName("<html><font color='green'><center>Get Refset Version<br>panel data from<br>WFD Sheet");
        return bd;
    }
}
