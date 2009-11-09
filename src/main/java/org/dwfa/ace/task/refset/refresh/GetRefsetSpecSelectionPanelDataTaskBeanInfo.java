package org.dwfa.ace.task.refset.refresh;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GetRefsetSpecSelectionPanelDataTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public GetRefsetSpecSelectionPanelDataTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

//            PropertyDescriptor commentsPropName;
//            commentsPropName = new PropertyDescriptor("commentsPropName", getBeanDescriptor().getBeanClass());
//            commentsPropName.setBound(true);
//            commentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
//            commentsPropName.setDisplayName("<html><font color='green'>comments prop name:");
//            commentsPropName.setShortDescription("The property to put the comments into.");

            PropertyDescriptor refsetUuidPropName;
            refsetUuidPropName = new PropertyDescriptor("refsetUuidPropName", getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='green'>member refset UUID prop:");
            refsetUuidPropName.setShortDescription("The property to put the member refset UUID into.");


            PropertyDescriptor rv[] =
                    { refsetUuidPropName };
//            { commentsPropName, refsetUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetRefsetSpecSelectionPanelDataTask.class);
        bd.setDisplayName("<html><font color='green'><center>Get Refset Spec<br>Selection panel data<br>from WFD Sheet");
        return bd;
    }

}
