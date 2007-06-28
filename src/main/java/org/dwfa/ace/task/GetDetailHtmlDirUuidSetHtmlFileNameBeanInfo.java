package org.dwfa.ace.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

/**
 * Bean info for GetDetailHtmlDirUuidSetHtmlFileName class.
 * @author Susan Castillo
 *
 */
public class GetDetailHtmlDirUuidSetHtmlFileNameBeanInfo extends SimpleBeanInfo {

    /**
     *
     */
    public GetDetailHtmlDirUuidSetHtmlFileNameBeanInfo() {
        super();
     }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor detailHtmlFileNameProp =
                new PropertyDescriptor("detailHtmlFileNameProp", GetDetailHtmlDirUuidSetHtmlFileName.class);
            detailHtmlFileNameProp.setBound(true);
            detailHtmlFileNameProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            detailHtmlFileNameProp.setDisplayName("<html><font color='green'>File Name <br> Dup Details");
            detailHtmlFileNameProp.setShortDescription("Dup Html <br> Detail File Name");
            
            PropertyDescriptor potUuidListPropName =
                new PropertyDescriptor("potUuidListPropName", GetDetailHtmlDirUuidSetHtmlFileName.class);
            potUuidListPropName.setBound(true);
            potUuidListPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            potUuidListPropName.setDisplayName("<html><font color='green'>Uuid of Dup");
            potUuidListPropName.setShortDescription("Uuid of Pot Dup");
                 	
            PropertyDescriptor htmlDirPropName =
                new PropertyDescriptor("htmlDirPropName", GetDetailHtmlDirUuidSetHtmlFileName.class);
            htmlDirPropName.setBound(true);
            htmlDirPropName.setPropertyEditorClass(PropertyNameLabelEditor.class);
            htmlDirPropName.setDisplayName("<html><font color='green'>Dir Html File");
            htmlDirPropName.setShortDescription("Detail Html File Name");

            PropertyDescriptor rv[] = { detailHtmlFileNameProp, potUuidListPropName, htmlDirPropName };
            return rv;
        } catch (IntrospectionException e) {
             throw new Error(e.toString());
        }
    }
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(GetDetailHtmlDirUuidSetHtmlFileName.class);
        bd.setDisplayName("<html><font color='green'><center>Get Detail Pot Dup <br>Html Dir, Uuid <br> Set Html Detail File Name");
        return bd;
    }

}
