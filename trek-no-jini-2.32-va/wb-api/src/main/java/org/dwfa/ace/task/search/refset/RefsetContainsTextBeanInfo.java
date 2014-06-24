package org.dwfa.ace.task.search.refset;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dwfa.ace.task.search.AbstractSeachTestSearchInfo;
import org.dwfa.ace.task.search.AbstractSearchTestBeanInfo;

public class RefsetContainsTextBeanInfo extends AbstractSearchTestBeanInfo {
    private static AbstractSeachTestSearchInfo searchInfo = new RefsetContainsTextSearchInfo();

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