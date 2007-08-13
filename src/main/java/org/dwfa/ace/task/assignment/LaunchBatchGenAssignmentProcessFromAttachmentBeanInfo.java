package org.dwfa.ace.task.assignment;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class LaunchBatchGenAssignmentProcessFromAttachmentBeanInfo extends SimpleBeanInfo {

   public PropertyDescriptor[] getPropertyDescriptors() {
      try {
         PropertyDescriptor batchGenAssigneePropName =
             new PropertyDescriptor("batchGenAssigneePropName", getBeanDescriptor().getBeanClass());
           batchGenAssigneePropName.setBound(true);
           batchGenAssigneePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           batchGenAssigneePropName.setDisplayName("<html><font color='green'>WF Mngr Inbox:");
           batchGenAssigneePropName.setShortDescription("Workflow Mngr");
           
           PropertyDescriptor uuidListListPropName =
               new PropertyDescriptor("uuidListListPropName", getBeanDescriptor().getBeanClass());
           uuidListListPropName.setBound(true);
           uuidListListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           uuidListListPropName.setDisplayName("<html><font color='green'>uuid List List:");
           uuidListListPropName.setShortDescription("uuid List");

           PropertyDescriptor proccessToLaunchPropName =
               new PropertyDescriptor("proccessToLaunchPropName", getBeanDescriptor().getBeanClass());
           proccessToLaunchPropName.setBound(true);
           proccessToLaunchPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           proccessToLaunchPropName.setDisplayName("<html><font color='green'>Process to launch:");
           proccessToLaunchPropName.setShortDescription("Process to launch that will generate the assignments. Process is a marshalled object stored as a process attachment or property");
             
           PropertyDescriptor processToAssignPropName =
               new PropertyDescriptor("processToAssignPropName", getBeanDescriptor().getBeanClass());
           processToAssignPropName.setBound(true);
           processToAssignPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           processToAssignPropName.setDisplayName("<html><font color='green'>Assignment Process:");
           processToAssignPropName.setShortDescription("Assign Name");
           
           PropertyDescriptor rv[] = { batchGenAssigneePropName, uuidListListPropName, 
                  proccessToLaunchPropName, processToAssignPropName };
           return rv;
      } catch (IntrospectionException e) {
           throw new Error(e.toString());
       }
  }
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(LaunchBatchGenAssignmentProcessFromAttachment.class);
       bd.setDisplayName("<html><font color='green'><center>Launch Generate <br>Assignment Process<br>From Attachment");
       return bd;
   }

}

