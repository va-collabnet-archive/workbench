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
 * Created on Jun 1, 2005
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author kec
 * 
 */
public class SelectDestinationBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public SelectDestinationBeanInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

    public PropertyDescriptor[] getPropertyDescriptors() {

        return new PropertyDescriptor[] {};
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SetDestination.class);
        bd.setDisplayName("<html><font color='green'><center>Select Destination");
        return bd;
    }

}