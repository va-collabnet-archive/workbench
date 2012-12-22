/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.project.tasks;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * The Class ListWorkSetMembersToSignpostBeanInfo.
 */
public class ListWorkSetMembersToSignpostBeanInfo extends SimpleBeanInfo {

    /**
     * Instantiates a new list work set members to signpost bean info.
     */
    public ListWorkSetMembersToSignpostBeanInfo() {
        super();
    }

    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return null;
    }

    /* (non-Javadoc)
     * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ListWorkSetMembersToSignpost.class);
        bd.setDisplayName("<html><font color='green'><center>List WorkSetMembers<br>of selected WorkSet<br>to Signpost");
        return bd;
    }
}
