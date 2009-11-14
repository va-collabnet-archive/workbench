package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetWDFSheetToRefsetVersionPanelTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public SetWDFSheetToRefsetVersionPanelTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {  

            PropertyDescriptor profilePropName =
                new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
            profilePropName.setBound(true);
            profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            profilePropName.setDisplayName("<html><font color='green'>profile prop:");
            profilePropName.setShortDescription("The property that will contain the current profile.");
            
            PropertyDescriptor positionSetPropName =
                new PropertyDescriptor("positionSetPropName", getBeanDescriptor().getBeanClass());
            positionSetPropName.setBound(true);
            positionSetPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            positionSetPropName.setDisplayName("<html><font color='green'>position set prop:");
            positionSetPropName.setShortDescription("The property that will contain the Refset position set.");

            PropertyDescriptor refsetUuidPropName;
            refsetUuidPropName = new PropertyDescriptor("refsetUuidPropName", getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='green'>member refset UUID prop:");
            refsetUuidPropName.setShortDescription("The property to put the member refset UUID into.");

           PropertyDescriptor rv[] =
               { profilePropName, positionSetPropName, refsetUuidPropName };

            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetWDFSheetToRefsetVersionPanelTask.class);
        bd.setDisplayName("<html><font color='green'><center>Set WFD Sheet to<br>Refset Version panel");
        return bd;
    }

}


