package org.dwfa.ace.task.gui;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info to GetSelectedAddresses class.
 * @author Susan Castillo	
 *
 */
public class GetSelectedAddressesBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public GetSelectedAddressesBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
       try {
          PropertyDescriptor selectedAddressesPropName =
              new PropertyDescriptor("selectedAddressesPropName", GetSelectedAddresses.class);
            selectedAddressesPropName.setBound(true);
            selectedAddressesPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
 
            selectedAddressesPropName.setDisplayName("<html><font color='green'>Get Selected Addresses:");
            selectedAddressesPropName.setShortDescription("Get Addresses");

           PropertyDescriptor rv[] = { selectedAddressesPropName };
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
        bd.setDisplayName("<html><font color='green'><center>Get Selected<br>Addresses");
        return bd;
    }

}
