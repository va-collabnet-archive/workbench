package org.dwfa.ace.task.wfdetailsSheet;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GetPositionSetFromWorkflowDetailsPanelBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
         try {  

             PropertyDescriptor positionSetPropName =
                 new PropertyDescriptor("positionSetPropName", getBeanDescriptor().getBeanClass());
             positionSetPropName.setBound(true);
             positionSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             positionSetPropName.setDisplayName("<html><font color='green'>position set prop:");
             positionSetPropName.setShortDescription("The property that will contain the position set.");

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the working profile.");

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
        BeanDescriptor bd = new BeanDescriptor(GetPositionSetFromWorkflowDetailsPanel.class);
        bd.setDisplayName("<html><font color='green'><center>Get Position Set<br>From Details Sheet");
        return bd;
    }
}
