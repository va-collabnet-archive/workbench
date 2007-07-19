package org.dwfa.ace.task.assignment;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to LaunchAssignmentProcess class.
 * @author Susan Castillo	
 *
 */          
public class LaunchAssignmentProcessBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
       try {
          PropertyDescriptor selectedAddressesPropName =
              new PropertyDescriptor("selectedAddressesPropName", getBeanDescriptor().getBeanClass());
            selectedAddressesPropName.setBound(true);
            selectedAddressesPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            selectedAddressesPropName.setDisplayName("<html><font color='green'>Assignment Addresses:");
            selectedAddressesPropName.setShortDescription("Addresses");
            
            PropertyDescriptor conceptUuidPropName =
                new PropertyDescriptor("conceptUuidPropName", getBeanDescriptor().getBeanClass());
            conceptUuidPropName.setBound(true);
            conceptUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptUuidPropName.setDisplayName("<html><font color='green'>Uuid List:");
            conceptUuidPropName.setShortDescription("Uuid List");

            PropertyDescriptor processFileNamePropName =
                new PropertyDescriptor("processFileNamePropName", getBeanDescriptor().getBeanClass());
            processFileNamePropName.setBound(true);
            processFileNamePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            processFileNamePropName.setDisplayName("<html><font color='green'>Process File Name:");
            processFileNamePropName.setShortDescription("The file name of the process to loadset, launch");
              
            PropertyDescriptor rv[] = { selectedAddressesPropName, conceptUuidPropName, 
            			processFileNamePropName };
            return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
   }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LaunchAssignmentProcess.class);
        bd.setDisplayName("<html><font color='green'><center>Launch Assignment<br>Process");
        return bd;
    }

}
