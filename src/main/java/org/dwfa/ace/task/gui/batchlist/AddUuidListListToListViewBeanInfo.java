package org.dwfa.ace.task.gui.batchlist;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class AddUuidListListToListViewBeanInfo extends SimpleBeanInfo {

   public PropertyDescriptor[] getPropertyDescriptors() {
      try {
          PropertyDescriptor uuidListListPropName =
              new PropertyDescriptor("uuidListListPropName", getBeanDescriptor().getBeanClass());
          uuidListListPropName.setBound(true);
          uuidListListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
          uuidListListPropName.setDisplayName("<html><font color='green'>Uuid List:");
          uuidListListPropName.setShortDescription("Uuid list.");
          
          PropertyDescriptor rv[] = { uuidListListPropName };
          return rv;
      } catch (IntrospectionException e) {
           throw new Error(e.toString());
      }
  }
  /**
   * @see java.beans.BeanInfo#getBeanDescriptor()
   */
  public BeanDescriptor getBeanDescriptor() {
      BeanDescriptor bd = new BeanDescriptor(AddUuidListListToListView.class);
      bd.setDisplayName("<html><font color='green'><center>Add UUID List list <br>to list view");
      return bd;
  }

}