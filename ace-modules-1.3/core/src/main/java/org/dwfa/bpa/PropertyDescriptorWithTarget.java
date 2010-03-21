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
 * Created on Jun 8, 2005
 */
package org.dwfa.bpa;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.PropertySpec;
import org.dwfa.bpa.process.PropertySpec.SourceType;

/**
 * 
 * @author kec
 *         TODO consider replacing the target on this with a named
 *         value/property on the feature descriptor...
 */
public class PropertyDescriptorWithTarget extends PropertyDescriptor {

    public static enum VALUE {
        PROPERTY_SPEC
    };

    private Object target;

    /**
     * @param propertyName
     * @param readMethod
     * @param writeMethod
     * @throws IntrospectionException
     */
    public PropertyDescriptorWithTarget(String propertyName, Object target) throws IntrospectionException {
        super(propertyName, target.getClass());
        this.target = target;
    }

    public PropertyDescriptorWithTarget(String propertyName, Object target, String readMethodName,
            String writeMethodName) throws IntrospectionException {
        super(propertyName, target.getClass(), readMethodName, writeMethodName);
        this.target = target;
    }

    public PropertyDescriptorWithTarget(String propertyName, Object target, Method readMethod, Method writeMethod)
            throws IntrospectionException {
        super(propertyName, readMethod, writeMethod);
        this.target = target;
    }

    /**
     * @return Returns the obj.
     */
    public Object getTarget() {
        return target;
    }

    public String getLabel() {
        if (I_DefineTask.class.isAssignableFrom(target.getClass())) {
            I_DefineTask t = (I_DefineTask) target;
            return "<html>T" + t.getId() + ": " + this.getDisplayNameNoHTML();
        }
        return "<html>" + this.getDisplayNameNoHTML();

    }

    public String getKey() {
        PropertySpec spec = this.getSpec();
        if (spec != null) {
            return spec.getKey();
        }
        return null;
    }

    public PropertySpec getSpec() {
        return (PropertySpec) this.getValue(PropertyDescriptorWithTarget.VALUE.PROPERTY_SPEC.name());
    }

    public String getDisplayNameNoHTML() {
        if (this.getDisplayName().toLowerCase().startsWith("<html>")) {
            return this.getDisplayName().substring(6);
        }
        return this.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        if (this.getSpec() == null || this.getSpec().getType().equals(SourceType.TASK)) {
            return super.getShortDescription();
        }
        return this.getSpec().getShortDescription();
    }

    @Override
    public void setShortDescription(String desc) {
        super.setShortDescription(desc);
        if (this.getSpec() != null) {
            this.getSpec().setShortDescription(desc);
        }
    }
}
