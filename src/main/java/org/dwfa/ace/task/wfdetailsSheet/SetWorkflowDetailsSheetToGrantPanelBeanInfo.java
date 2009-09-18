package org.dwfa.ace.task.wfdetailsSheet;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetWorkflowDetailsSheetToGrantPanelBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
         try {  

             PropertyDescriptor commitProfilePropName =
                 new PropertyDescriptor("commitProfilePropName", getBeanDescriptor().getBeanClass());
             commitProfilePropName.setBound(true);
             commitProfilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
             commitProfilePropName.setDisplayName("<html><font color='green'>commit profile prop:");
             commitProfilePropName.setShortDescription("The property that contains the commit profile.");

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the working profile.");

            PropertyDescriptor rv[] =
                { commitProfilePropName, profilePropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWorkflowDetailsSheetToGrantPanel.class);
        bd.setDisplayName("<html><font color='green'><center>Set Workflow Details<br>Sheet to<br>Grant Panel");
        return bd;
    }
}
