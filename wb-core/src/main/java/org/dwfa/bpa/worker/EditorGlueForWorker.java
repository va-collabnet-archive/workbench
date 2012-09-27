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
 * Created on Mar 8, 2006
 */
package org.dwfa.bpa.worker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.bpa.process.I_Work;

public class EditorGlueForWorker implements PropertyChangeListener {

    private Method writeMethod;

    private PropertyEditor editor;

    private Object target;

    private I_Work worker;

    public EditorGlueForWorker(PropertyEditor editor, Method writeMethod, Object target, I_Work worker) {
        this.editor = editor;
        this.writeMethod = writeMethod;
        this.target = target;
        this.worker = worker;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            Object newValue = editor.getValue();
            if (newValue != null) {
                if (Set.class.isAssignableFrom(newValue.getClass())) {
                    newValue = new HashSet((Collection) newValue);
                } else if (List.class.isAssignableFrom(newValue.getClass())) {
                    newValue = new ArrayList((Collection) newValue);
                }
            }
            writeMethod.invoke(target, new Object[] { newValue });
        } catch (Exception ex) {
            this.worker.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

}
