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
 * Created on Feb 7, 2005
 */
package org.dwfa.bpa.gui.glue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kec
 * 
 */
public class PropertyListenerGlue implements PropertyChangeListener {
    private String methodName;
    private Class<?>[] methodArgClasses;
    private Object target;

    /**
     * @param property
     * @param target
     */
    public PropertyListenerGlue(String methodName, Class<?> methodArgClass, Object target) {
        this.methodName = methodName;
        this.methodArgClasses = new Class[] { methodArgClass };
        this.target = target;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            Method targetMethod = target.getClass().getMethod(this.methodName, this.methodArgClasses);
            Object newValue = evt.getNewValue();
            if (newValue != null) {
                if (newValue.getClass().isAssignableFrom(this.methodArgClasses[0]) == false) {
                    if (Set.class.isAssignableFrom(this.methodArgClasses[0])) {
                        newValue = new HashSet((Collection) newValue);
                    } else if (List.class.isAssignableFrom(this.methodArgClasses[0])) {
                        newValue = new ArrayList((Collection) newValue);
                    }
                }
            }
            targetMethod.invoke(target, new Object[] { newValue });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("  method name: " + this.methodName);
            System.out.println("  arg classes: " + this.methodArgClasses[0].getName());
            System.out.println("  new value: " + evt.getNewValue());
            if (evt.getNewValue() != null) {
                System.out.println("  new value class: " + evt.getNewValue().getClass().getName());
            }
        }

    }

}
