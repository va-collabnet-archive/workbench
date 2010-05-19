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
 * Created on Jul 26, 2005
 */
package org.dwfa.bpa.tasks.util;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.ProbabilityEditor;

public class RandomBranchBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor relativeTimeProp = new PropertyDescriptor("branchFrequency", RandomBranch.class);
            relativeTimeProp.setBound(true);
            relativeTimeProp.setPropertyEditorClass(ProbabilityEditor.class);
            relativeTimeProp.setDisplayName("branch probability");
            relativeTimeProp.setShortDescription("The probability this task evaluates to true.");

            PropertyDescriptor rv[] = { relativeTimeProp };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(RandomBranch.class);
        bd.setDisplayName("<html><font color='green'><center>Random Branch");
        return bd;
    }

}
