/*
 * Created on Jun 8, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
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
     public PropertyDescriptorWithTarget(String propertyName, Object target) throws IntrospectionException {
         super(propertyName, target.getClass());
         this.target = target;
     }
     public PropertyDescriptorWithTarget(String propertyName, Object target, 
             String readMethodName, String writeMethodName) throws IntrospectionException {
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
            return new PropertySpec(PropertySpec.SourceType.TASK, t
                    .getId(), this.getName());           
        } else if (I_ContainData.class.isAssignableFrom(target.getClass())) {
            I_ContainData dc = (I_ContainData) target;
            return new PropertySpec(PropertySpec.SourceType.DATA_CONTAINER, dc
                    .getId(), this.getName());
        }
        return new PropertySpec(PropertySpec.SourceType.ATTACHMENT, -1, this.getName());           
    }
    
    public String getDisplayNameNoHTML() {
        if (this.getDisplayName().startsWith("<html>")) {
            return this.getDisplayName().substring(6);
        }
        return this.getDisplayName();
    }

}
