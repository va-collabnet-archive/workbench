package org.dwfa.ace.task.address;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GetAddressesFromConceptStatusChangesBeanInfo extends SimpleBeanInfo {

   public PropertyDescriptor[] getPropertyDescriptors() {
      try {
         PropertyDescriptor activeConceptPropName = new PropertyDescriptor("activeConceptPropName", getBeanDescriptor()
               .getBeanClass());
         activeConceptPropName.setBound(true);
         activeConceptPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
         activeConceptPropName.setDisplayName("<html><font color='green'>concept prop:");
         activeConceptPropName.setShortDescription("Enter the property name to hold the concept to check for status changes.");

         PropertyDescriptor profilePropName = new PropertyDescriptor("profilePropName", getBeanDescriptor()
               .getBeanClass());
         profilePropName.setBound(true);
         profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
         profilePropName.setDisplayName("<html><font color='green'>profile prop:");
         profilePropName.setShortDescription("Enter the profile containing the view paths and allowed status values to check for status changes.");

         PropertyDescriptor addressListPropName = new PropertyDescriptor("addressListPropName", getBeanDescriptor()
               .getBeanClass());
         addressListPropName.setBound(true);
         addressListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
         addressListPropName.setDisplayName("<html><font color='green'>address list prop:");
         addressListPropName.setShortDescription("Enter the property name to hold the generated address list.");

         PropertyDescriptor rv[] = { activeConceptPropName, profilePropName, addressListPropName };
         return rv;
      } catch (IntrospectionException e) {
         throw new Error(e.toString());
      }
   }

   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
      BeanDescriptor bd = new BeanDescriptor(GetAddressesFromConceptStatusChanges.class);
      bd.setDisplayName("<html><font color='green'><center>get addresses<br>from status changes");
      return bd;
   }

}
