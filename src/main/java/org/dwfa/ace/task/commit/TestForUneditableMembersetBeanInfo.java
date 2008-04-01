package org.dwfa.ace.task.commit;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class TestForUneditableMembersetBeanInfo extends SimpleBeanInfo {
    

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  
            PropertyDescriptor showAlertOnFailure =
                new PropertyDescriptor("showAlertOnFailure", getBeanDescriptor().getBeanClass());
            showAlertOnFailure.setBound(true);
            showAlertOnFailure.setPropertyEditorClass(CheckboxEditor.class);
            showAlertOnFailure.setDisplayName("<html><font color='green'>Show alerts:");
            showAlertOnFailure.setShortDescription("Show alerts on failure...");

            PropertyDescriptor forCommit =
                new PropertyDescriptor("forCommit", getBeanDescriptor().getBeanClass());
            forCommit.setBound(true);
            forCommit.setPropertyEditorClass(CheckboxEditor.class);
            forCommit.setDisplayName("<html><font color='green'>Show alerts:");
            forCommit.setShortDescription("Show alerts on failure...");

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that contains the profile.");

            PropertyDescriptor componentPropName =
                new PropertyDescriptor("componentPropName", getBeanDescriptor().getBeanClass());
            componentPropName.setBound(true);
            componentPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            componentPropName.setDisplayName("<html><font color='green'>component prop:");
            componentPropName.setShortDescription("The property that contains the component to test.");

            PropertyDescriptor rv[] =
                { showAlertOnFailure, forCommit, profilePropName, componentPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
     }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TestForUneditableMemberset.class);
        bd.setDisplayName("<html><font color='green'><center>Test For<br>Uneditable Membersets");
        return bd;
    }
}//End class 
