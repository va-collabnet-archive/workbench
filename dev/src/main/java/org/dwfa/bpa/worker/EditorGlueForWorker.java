/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
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

    public EditorGlueForWorker(PropertyEditor editor, Method writeMethod,
            Object target, I_Work worker) {
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
