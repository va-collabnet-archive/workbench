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
package org.dwfa.ace.api;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * This class allows for fields (bean properties) to be defined along with an
 * expected value
 * value. The class can then be used to test an object matches these properties.
 * 
 * For example, to validate an object has the following properties
 * <ul>
 * <li>getName() returns "foo"
 * <li>getValue() returns 5
 * </ul>
 * this class can be created with:
 * <p>
 * <code>new BeanPropertyMap().with("name", "foo").with("value", 5).validate(object)</code>
 * 
 */
public class BeanPropertyMap {

    protected HashMap<String, Object> properties = new HashMap<String, Object>();

    protected Class<?> beanClass;

    /**
     * @deprecated Recommend using {@link #with(BeanProperty, Object)} with a
     *             well defined bean property.
     */
    @Deprecated
    public BeanPropertyMap with(String propertyName, Object propertyValue) {
        properties.put(propertyName, propertyValue);
        return this;
    }

    public <T extends BeanProperty> BeanPropertyMap with(T property, Object propertyValue) {
        properties.put(property.getPropertyName(), propertyValue);
        return this;
    }

    public BeanPropertyMap withType(Class<?> beanClass) {
        this.beanClass = beanClass;
        return this;
    }

    public boolean hasProperty(String propertyName) {
        return properties.containsKey(propertyName);
    }

    public Object getPropertyValue(String propertyName) {
        return properties.get(propertyName);
    }

    public boolean validate(Object bean) {

        if (beanClass != null) {
            if (!beanClass.isAssignableFrom(bean.getClass())) {
                return false;
            }
        }

        for (String name : properties.keySet()) {
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
                for (PropertyDescriptor propDesc : beanInfo.getPropertyDescriptors()) {
                    if (propDesc.getName().equals(name)) {
                        if (!properties.get(name).equals(propDesc.getReadMethod().invoke(bean))) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public void writeTo(Object bean) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        for (String name : properties.keySet()) {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            for (PropertyDescriptor propDesc : beanInfo.getPropertyDescriptors()) {
                if (propDesc.getName().equals(name)) {
                    propDesc.getWriteMethod().invoke(bean, properties.get(name));
                }
            }
        }
    }

}
