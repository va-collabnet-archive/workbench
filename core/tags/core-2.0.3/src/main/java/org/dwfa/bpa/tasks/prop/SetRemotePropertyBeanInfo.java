/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Created on Feb 22, 2006
 */
package org.dwfa.bpa.tasks.prop;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ProcessDataIdEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class SetRemotePropertyBeanInfo extends SimpleBeanInfo {

    public SetRemotePropertyBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor remotePropertyName =
                    new PropertyDescriptor("remotePropName",
                        SetRemoteProperty.class);
            remotePropertyName.setBound(true);
            remotePropertyName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            remotePropertyName
                .setDisplayName("<html><font color='green'>Remote property:");
            remotePropertyName
                .setShortDescription("Name of the remote property to get. ");

            PropertyDescriptor localPropertyName =
                    new PropertyDescriptor("localPropName",
                        SetRemoteProperty.class);
            localPropertyName.setBound(true);
            localPropertyName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName
                .setDisplayName("<html><font color='blue'>Local property:");
            localPropertyName
                .setShortDescription("Name of the local property to set. ");

            PropertyDescriptor process =
                    new PropertyDescriptor("processDataId",
                        SetRemoteProperty.class);
            process.setBound(true);
            process.setPropertyEditorClass(ProcessDataIdEditor.class);
            process.setDisplayName("<html><font color='green'>Process:");
            process
                .setShortDescription("A data id for the process container to launch. Only data containers that contain I_EncodeBusinessProcess objects can be dropped. ");

            PropertyDescriptor rv[] =
                    { process, localPropertyName, remotePropertyName };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetRemoteProperty.class);
        bd
            .setDisplayName("<html>Set <font color='green'>Remote</font> Property");
        return bd;
    }

}
