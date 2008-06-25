package org.dwfa.ace.task.gui.batchlist;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;


public class DeleteMutipleSelectedFromListViewBeanInfo extends SimpleBeanInfo {

	public PropertyDescriptor[] getPropertyDescriptors() {
	      
	          
	          PropertyDescriptor rv[] = { };
	          return rv;
	      
	  }
  /**
   * @see java.beans.BeanInfo#getBeanDescriptor()
   */
  public BeanDescriptor getBeanDescriptor() {
      BeanDescriptor bd = new BeanDescriptor(DeleteMutipleSelectedFromListView.class);
      bd.setDisplayName("<html><font color='green'><center>Delete multiple concept <br>from list view");
      return bd;
  }

}