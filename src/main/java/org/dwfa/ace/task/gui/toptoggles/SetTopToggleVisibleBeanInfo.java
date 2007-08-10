package org.dwfa.ace.task.gui.toptoggles;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetTopToggleVisibleBeanInfo extends SimpleBeanInfo {
   

   public PropertyDescriptor[] getPropertyDescriptors() {
       try {  
           PropertyDescriptor visible =
               new PropertyDescriptor("visible", getBeanDescriptor().getBeanClass());
           visible.setBound(true);
           visible.setPropertyEditorClass(CheckboxEditor.class);
           visible.setDisplayName("<html><font color='green'>visible:");
           visible.setShortDescription("Set the toggle visible or hidden...");

           PropertyDescriptor type =
               new PropertyDescriptor("type", getBeanDescriptor().getBeanClass());
           type.setBound(true);
           type.setPropertyEditorClass(TopToggleTypeEditor.class);
           type.setDisplayName("<html><font color='green'>type:");
           type.setShortDescription("The toggle to set..");

           PropertyDescriptor profilePropName =
               new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
           profilePropName.setBound(true);
           profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           profilePropName.setDisplayName("<html><font color='green'>profile prop:");
           profilePropName.setShortDescription("The property that contains the profile.");

           PropertyDescriptor rv[] =
               { visible, type, profilePropName };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(SetTopToggleVisible.class);
       bd.setDisplayName("<html><font color='green'><center>Set Top Toggle");
       return bd;
   }
}
