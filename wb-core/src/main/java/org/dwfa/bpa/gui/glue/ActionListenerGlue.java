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
 * Created on Feb 8, 2005
 */
package org.dwfa.bpa.gui.glue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

/**
 * @author kec
 * 
 */
public class ActionListenerGlue implements ActionListener {

    private String methodName;
    private Class<?>[] methodArgClasses;
    private Object[] methodArgs;
    private Object target;

    /**
     * @param property
     * @param target
     */
    public ActionListenerGlue(String methodName, boolean stateToSet, Object target) {
        this.methodName = methodName;
        this.methodArgClasses = new Class[] { boolean.class };
        this.methodArgs = new Object[] { new Boolean(stateToSet) };
        this.target = target;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        try {
            Method targetMethod = target.getClass().getMethod(this.methodName, this.methodArgClasses);
            targetMethod.invoke(target, this.methodArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
