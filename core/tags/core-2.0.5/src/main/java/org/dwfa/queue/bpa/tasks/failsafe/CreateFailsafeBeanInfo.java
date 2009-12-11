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
 * Created on Jun 9, 2005
 */
package org.dwfa.queue.bpa.tasks.failsafe;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.DataIdEditorOneLine;
import org.dwfa.bpa.tasks.editor.RelativeTimeEditor;

public class CreateFailsafeBeanInfo extends SimpleBeanInfo {
    protected Class getBeanClass() {
        return CreateFailsafe.class;
    }

    public CreateFailsafeBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor relativeTimeProp =
                    new PropertyDescriptor("relativeTimeInMins", getBeanClass());
            relativeTimeProp.setBound(true);
            relativeTimeProp.setPropertyEditorClass(RelativeTimeEditor.class);
            relativeTimeProp.setDisplayName("failsafe interval");
            relativeTimeProp
                .setShortDescription("The interval to launch failsafe processes.");

            PropertyDescriptor failsafeDataId =
                    new PropertyDescriptor("failsafeDataId", getBeanClass());
            failsafeDataId.setBound(true);
            failsafeDataId.setPropertyEditorClass(DataIdEditorOneLine.class);
            failsafeDataId.setDisplayName("UUID Data Id:");
            failsafeDataId
                .setShortDescription("The String representation of the failsafe UUID.");

            PropertyDescriptor rv[] = { relativeTimeProp, failsafeDataId };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(getBeanClass());
        bd.setDisplayName("<html><font color='green'><center>Create Failsafe");
        return bd;
    }

}
