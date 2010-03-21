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
package org.dwfa.bpa.gui.glue;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;

/**
 * @author kec
 * 
 */
public class ItemListenerGlue implements ItemListener {
    private String methodName;
    private Class<?>[] methodArgClasses;
    private Object target;

    /**
     * @param property
     * @param target
     */
    public ItemListenerGlue(String methodName, Object target) {
        this.methodName = methodName;
        this.methodArgClasses = new Class[] { boolean.class };
        this.target = target;
    }

    /**
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent evt) {
        try {
            Method targetMethod = target.getClass().getMethod(this.methodName, this.methodArgClasses);
            targetMethod.invoke(target, new Object[] { new Boolean(evt.getStateChange() == ItemEvent.SELECTED) });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
