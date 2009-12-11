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

import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.PropertySpec;

public class PropertyDescriptorWithTarget extends PropertyDescriptor {

    private Object target;

    /**
     * @param propertyName
     * @param readMethod
     * @param writeMethod
     * @throws IntrospectionException
     */
    public PropertyDescriptorWithTarget(String propertyName, Object target)
            throws IntrospectionException {
        super(propertyName, target.getClass());
        this.target = target;
    }

    public PropertyDescriptorWithTarget(String propertyName, Object target,
            String readMethodName, String writeMethodName)
            throws IntrospectionException {
        super(propertyName, target.getClass(), readMethodName, writeMethodName);
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
        } else if (I_ContainData.class.isAssignableFrom(target.getClass())) {
            return "<html>DC: " + this.getDisplayNameNoHTML();
        }
        return "<html>A: " + this.getDisplayNameNoHTML();

    }

    public PropertySpec getSpec() {
        if (I_DefineTask.class.isAssignableFrom(target.getClass())) {
            I_DefineTask t = (I_DefineTask) target;
            return new PropertySpec(PropertySpec.SourceType.TASK, t.getId(),
                this.getName());
        } else if (I_ContainData.class.isAssignableFrom(target.getClass())) {
            I_ContainData dc = (I_ContainData) target;
            return new PropertySpec(PropertySpec.SourceType.DATA_CONTAINER, dc
                .getId(), this.getName());
        }
        return new PropertySpec(PropertySpec.SourceType.ATTACHMENT, -1, this
            .getName());
    }

    public String getDisplayNameNoHTML() {
        if (this.getDisplayName().startsWith("<html>")) {
            return this.getDisplayName().substring(6);
        }
        return this.getDisplayName();
    }

}
