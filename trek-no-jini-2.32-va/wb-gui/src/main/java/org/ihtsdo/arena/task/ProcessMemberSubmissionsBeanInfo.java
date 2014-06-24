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
package org.ihtsdo.arena.task;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;

public class ProcessMemberSubmissionsBeanInfo extends SimpleBeanInfo {

    /**
     * Bean info for ProcessMemberSubmissions class.
     * 
     * @author akf
     * 
     */
    public ProcessMemberSubmissionsBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor stringSetProp = new PropertyDescriptor("stringSetProp",
                ProcessMemberSubmissions.class);
            PropertyDescriptor msFileProp = new PropertyDescriptor("msFileProp",
                ProcessMemberSubmissions.class);
            
            stringSetProp.setBound(true);
            stringSetProp.setDisplayName("<html><font color='green'>String set:");
            stringSetProp.setShortDescription("string set");
            stringSetProp.setPropertyEditorClass(PropertyNameLabelEditor.class);
            
            msFileProp.setBound(true);
            msFileProp.setDisplayName("<html><font color='green'>File path:");
            msFileProp.setShortDescription("file path");
            msFileProp.setPropertyEditorClass(PropertyNameLabelEditor.class);

            PropertyDescriptor rv[] = { stringSetProp, msFileProp };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ProcessMemberSubmissions.class);
        bd.setDisplayName("<html><font color='green'><center>Process Member Submissions");
        return bd;
    }

}
