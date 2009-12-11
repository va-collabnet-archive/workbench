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
 * Created on Mar 8, 2006
 */
package org.dwfa.queue.bpa.tasks.sync;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ProcessDataIdEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class LoadFromQueueBeanInfo extends SimpleBeanInfo {

    public LoadFromQueueBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {

            PropertyDescriptor localPropertyName =
                    new PropertyDescriptor("localPropName", LoadFromQueue.class);
            localPropertyName.setBound(true);
            localPropertyName
                .setPropertyEditorClass(PropertyNameLabelEditor.class);
            localPropertyName
                .setDisplayName("<html><font color='blue'>Queue entry");
            localPropertyName
                .setShortDescription("Name of the local property that holds the queue entry record. ");

            PropertyDescriptor process =
                    new PropertyDescriptor("processDataId", LoadFromQueue.class);
            process.setBound(true);
            process.setPropertyEditorClass(ProcessDataIdEditor.class);
            process.setDisplayName("<html><font color='green'>Process");
            process
                .setShortDescription("A data id for the process container to launch. Only data containers that contain I_EncodeBusinessProcess objects can be dropped. ");

            PropertyDescriptor rv[] = { localPropertyName, process };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LoadFromQueue.class);
        bd.setDisplayName("<html>Load From Queue");
        return bd;
    }

}
