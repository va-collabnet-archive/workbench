package org.dwfa.ace.task.refset.rfc;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class GetRequestForChangePanelDataTaskBeanInfo extends SimpleBeanInfo {

    /**
    *
    */
    public GetRequestForChangePanelDataTaskBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        try {

            PropertyDescriptor refsetSpecUuidPropName;
            refsetSpecUuidPropName =
                    new PropertyDescriptor("refsetSpecUuidPropName", getBeanDescriptor().getBeanClass());
            refsetSpecUuidPropName.setBound(true);
            refsetSpecUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetSpecUuidPropName.setDisplayName("<html><font color='green'>specification refset UUID prop:");
            refsetSpecUuidPropName.setShortDescription("The property to put the specification refset UUID into.");

            PropertyDescriptor nextUserTermEntryPropName;
            nextUserTermEntryPropName =
                    new PropertyDescriptor("nextUserTermEntryPropName", getBeanDescriptor().getBeanClass());
            nextUserTermEntryPropName.setBound(true);
            nextUserTermEntryPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            nextUserTermEntryPropName.setDisplayName("<html><font color='green'>next person:");
            nextUserTermEntryPropName.setShortDescription("The next person the BP will go to.");

            PropertyDescriptor commentsPropName;
            commentsPropName = new PropertyDescriptor("commentsPropName", getBeanDescriptor().getBeanClass());
            commentsPropName.setBound(true);
            commentsPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            commentsPropName.setDisplayName("<html><font color='green'>comments prop name:");
            commentsPropName.setShortDescription("The property to put the comments into.");

            PropertyDescriptor refsetUuidPropName;
            refsetUuidPropName = new PropertyDescriptor("refsetUuidPropName", getBeanDescriptor().getBeanClass());
            refsetUuidPropName.setBound(true);
            refsetUuidPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            refsetUuidPropName.setDisplayName("<html><font color='green'>member refset UUID prop:");
            refsetUuidPropName.setShortDescription("The property to put the member refset UUID into.");

            PropertyDescriptor originalRequestPropName;
            originalRequestPropName =
                    new PropertyDescriptor("originalRequestPropName", getBeanDescriptor().getBeanClass());
            originalRequestPropName.setBound(true);
            originalRequestPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            originalRequestPropName.setDisplayName("<html><font color='green'>original request prop:");
            originalRequestPropName.setShortDescription("The property to put the original request into.");

            PropertyDescriptor rv[] =
                    { nextUserTermEntryPropName, commentsPropName, 
            		  refsetUuidPropName, originalRequestPropName, 
            		  refsetSpecUuidPropName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetRequestForChangePanelDataTask.class);
        bd.setDisplayName("<html><font color='green'><center>Get request for change<br>panel data");
        return bd;
    }

}
