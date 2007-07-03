package org.dwfa.ace.task.assignment;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.ace.task.gui.GetSelectedAddresses;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to LaunchAssignmentProcess class.
 * @author Susan Castillo	
 *
 */
public class LaunchAssignmentProcessBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public LaunchAssignmentProcessBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
       try {
          PropertyDescriptor selectedAddressesPropName =
              new PropertyDescriptor("selectedAddressesPropName", LaunchAssignmentProcess.class);
            selectedAddressesPropName.setBound(true);
            selectedAddressesPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            selectedAddressesPropName.setDisplayName("<html><font color='green'>Assignment Addresses:");
            selectedAddressesPropName.setShortDescription("Addresses");
            
            PropertyDescriptor conceptUuidPropName =
                new PropertyDescriptor("conceptUuidPropName", LaunchAssignmentProcess.class);
            conceptUuidPropName.setBound(true);
            conceptUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptUuidPropName.setDisplayName("<html><font color='green'>Uuid List:");
            conceptUuidPropName.setShortDescription("Uuid List");

            PropertyDescriptor processUrlStr =
                new PropertyDescriptor("processUrlStr", LaunchAssignmentProcess.class);
            processUrlStr.setBound(true);
            processUrlStr.setPropertyEditorClass(JTextFieldEditor.class);
            processUrlStr.setDisplayName("Process URL:");
            processUrlStr.setShortDescription("A URL from which a process is downloaded, then executed.");
           
            
            PropertyDescriptor assigneeAddrPropName =
                new PropertyDescriptor("assigneeAddrPropName", LaunchAssignmentProcess.class);
            assigneeAddrPropName.setBound(true);
            assigneeAddrPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            assigneeAddrPropName.setDisplayName("<html><font color='green'>Assignee Address:");
            assigneeAddrPropName.setShortDescription("Assignment Address");
            
              PropertyDescriptor alternateAddrPropName =
                  new PropertyDescriptor("alternateAddrPropName", LaunchAssignmentProcess.class);
              alternateAddrPropName.setBound(true);
              alternateAddrPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
              alternateAddrPropName.setDisplayName("<html><font color='green'>Alt Address:");
              alternateAddrPropName.setShortDescription("Alt Address");
              
            PropertyDescriptor rv[] = { selectedAddressesPropName, conceptUuidPropName, 
            			processUrlStr, assigneeAddrPropName, alternateAddrPropName };
            return rv;
            
            
            
      } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
   }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetSelectedAddresses.class);
        bd.setDisplayName("<html><font color='green'><center>Launch Assignment <br>Process");
        return bd;
    }

}
