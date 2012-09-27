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
package org.ihtsdo.ace.task.workflow.search;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathWorkflowHistoryBeanInfo extends AbstractWorkflowHistorySearchTestBeanInfo {
    private static PathWorkflowHistorySearchInfo searchInfo = new PathWorkflowHistorySearchInfo();

    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] parentDescriptors = super.getPropertyDescriptors();
        PropertyDescriptor[] searchDescriptors = searchInfo.getPropertyDescriptors();

        List<PropertyDescriptor> descriptorList = new ArrayList<PropertyDescriptor>(Arrays.asList(parentDescriptors));
        descriptorList.addAll(Arrays.asList(searchDescriptors));

        return descriptorList.toArray(new PropertyDescriptor[descriptorList.size()]);
    }

    public BeanDescriptor getBeanDescriptor() {
        return searchInfo.getBeanDescriptor();
    }

}
