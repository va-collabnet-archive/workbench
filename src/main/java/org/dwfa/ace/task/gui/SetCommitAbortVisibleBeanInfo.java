package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetCommitAbortVisibleBeanInfo extends SimpleBeanInfo {
   

   public PropertyDescriptor[] getPropertyDescriptors() {
       try {  
           PropertyDescriptor visible =
               new PropertyDescriptor("visible", getBeanDescriptor().getBeanClass());
           visible.setBound(true);
           visible.setPropertyEditorClass(CheckboxEditor.class);
           visible.setDisplayName("<html><font color='green'>visible:");
           visible.setShortDescription("Set the commit/abort buttons visible or hidden...");

           PropertyDescriptor profilePropName =
               new PropertyDescriptor("profilePropName", getBeanDescriptor().getBeanClass());
           profilePropName.setBound(true);
           profilePropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           profilePropName.setDisplayName("<html><font color='green'>profile prop:");
           profilePropName.setShortDescription("The property that contains the profile.");

           PropertyDescriptor rv[] =
               { visible, profilePropName };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
    }        
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(SetCommitAbortVisible.class);
       bd.setDisplayName("<html><font color='green'><center>Set Commit/Abort<br>Visible");
       return bd;
   }
}
