package org.dwfa.ace.task.search;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class IterateConceptsBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
       try {
           PropertyDescriptor uuidListListPropName =
               new PropertyDescriptor("uuidListListPropName", getBeanDescriptor().getBeanClass());
           uuidListListPropName.setBound(true);
           uuidListListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           uuidListListPropName.setDisplayName("<html><font color='green'>Uuid List:");
           uuidListListPropName.setShortDescription("Uuid list.");
           
           PropertyDescriptor componentPropName =
               new PropertyDescriptor("componentPropName", getBeanDescriptor().getBeanClass());
           componentPropName.setBound(true);
           componentPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
           componentPropName.setDisplayName("<html><font color='green'>component prop:");
           componentPropName.setShortDescription("The property that contains the component to test.");


           PropertyDescriptor rv[] = { componentPropName, uuidListListPropName };
           return rv;
       } catch (IntrospectionException e) {
            throw new Error(e.toString());
       }
   }
   /**
    * @see java.beans.BeanInfo#getBeanDescriptor()
    */
   public BeanDescriptor getBeanDescriptor() {
       BeanDescriptor bd = new BeanDescriptor(IterateConcepts.class);
       bd.setDisplayName("<html><font color='green'><center>Iterate Concepts");
       return bd;
   }

 }