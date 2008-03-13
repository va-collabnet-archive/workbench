package org.dwfa.ace.task.data.checks;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class VerifyRefsetMemberOverlapsBeanInfo extends SimpleBeanInfo{
	/**
	 * Bean info for VerifyRefsetOverlapsBeanInfo class.
	 *
	 */
    public VerifyRefsetMemberOverlapsBeanInfo() {
        super();
     }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
//        try {
            /*PropertyDescriptor conceptUuidStrPropName =
                new PropertyDescriptor("conceptUuidStrPropName", AddConceptAndPotDupToList.class);
            conceptUuidStrPropName.setBound(true);
            conceptUuidStrPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            conceptUuidStrPropName.setDisplayName("<html><font color='green'>Uuid:");
            conceptUuidStrPropName.setShortDescription("Uuid");
*/
            PropertyDescriptor rv[] = { /*conceptUuidStrPropName */};
            return rv;
//        } catch (IntrospectionException e) {
//             throw new Error(e.toString());
//        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(VerifyRefsetMemberOverlapsBeanInfo.class);
        bd.setDisplayName("<html><font color='green'><center>Verify refset<br>member overlaps");
        return bd;
    }
}//End class VerifyRefsetMemberOverlapsBeanInfo