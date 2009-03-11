package org.dwfa.bpa.worker.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditorOneLine;

public class NewGenericWorkerManagerBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    try {

      PropertyDescriptor jiniConfigFile = new PropertyDescriptor(
          "jiniConfigFile", getBeanDescriptor().getBeanClass());
      jiniConfigFile.setBound(true);
      jiniConfigFile.setPropertyEditorClass(JTextFieldEditorOneLine.class);

      PropertyDescriptor rv[] = { jiniConfigFile };
      return rv;
    } catch (IntrospectionException e) {
      throw new Error(e.toString());
    }
  }

  /**
   * @see java.beans.BeanInfo#getBeanDescriptor()
   */
  public BeanDescriptor getBeanDescriptor() {
    BeanDescriptor bd = new BeanDescriptor(NewGenericWorkerManager.class);
    bd.setDisplayName("<html><font color='green'><center>New Generic<br>Worker Manager");
    return bd;
  }
}
