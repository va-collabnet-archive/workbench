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
 * Created on Feb 18, 2006
 */
package org.dwfa.bpa.tasks.editor;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;

import org.dwfa.bpa.gui.TargetAndProcessForEditor;
import org.dwfa.bpa.gui.TaskIdPanel;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;

public class ProcessTaskIdEditor implements PropertyEditor, I_OnlyWantOneLine, PropertyChangeListener {

    private static Logger logger = Logger.getLogger(ProcessTaskIdEditor.class.getName());

    TaskIdPanel idPanel;

    public Class<I_EncodeBusinessProcess> getAcceptableClass() {
        return I_EncodeBusinessProcess.class;
    }

    public ProcessTaskIdEditor(Object obj) throws ClassNotFoundException {
        super();
        TargetAndProcessForEditor tpfe = (TargetAndProcessForEditor) obj;
        this.idPanel = new TaskIdPanel(-1, tpfe.getProcess(), true);
        this.idPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.idPanel.addPropertyChangeListener("id", this);
    }

    /**
     * @return true or false
     * @see java.beans.PropertyEditor#getValue()
     */
    public Integer getValue() {
        return idPanel.getId();
    }

    /**
     * Must be a <code>Integer</code> representing data id.
     * 
     * @see java.beans.PropertyEditor#setValue(java.lang.Object)
     */
    public void setValue(Integer value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ProcessTaskIdEditor setValue: " + value);
        }
        this.idPanel.setId(value);
        this.firePropertyChange();
    }

    /**
     * @see java.beans.PropertyEditor#isPaintable()
     */
    public boolean isPaintable() {
        return true;
    }

    /**
     * Calls the paint method on this swing component.
     * 
     * @see java.beans.PropertyEditor#paintValue(java.awt.Graphics,
     *      java.awt.Rectangle)
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        this.idPanel.setBounds(box);
        this.idPanel.paintAll(gfx);
    }

    /**
     * @see java.beans.PropertyEditor#getJavaInitializationString()
     */
    public String getJavaInitializationString() {
        return "new Integer(" + this.getValue().toString() + ")";
    }

    /**
     * @see java.beans.PropertyEditor#getAsText()
     */
    public String getAsText() {
        return this.getValue().toString();
    }

    /**
     * @see java.beans.PropertyEditor#setAsText(java.lang.String)
     */
    public void setAsText(String text) throws IllegalArgumentException {
        this.setValue(new Integer(text));
    }

    /**
     * Returns null since this editor provides a custom GUI component.
     * 
     * @see java.beans.PropertyEditor#getTags()
     */
    public String[] getTags() {
        return null;
    }

    /**
     * Returns swing component to edit the check box.
     * 
     * @see java.beans.PropertyEditor#getCustomEditor()
     */
    public Component getCustomEditor() {
        return this.idPanel;
    }

    /**
     * Returns true since this editor provides a custom GUI component.
     * 
     * @see java.beans.PropertyEditor#supportsCustomEditor()
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Property changed for ProcessTaskIdEditor: " + evt);
        }
        this.firePropertyChange();
    }

    /**
     * Register a listener for the PropertyChange event. The class will fire a
     * PropertyChange value whenever the value is updated.
     * 
     * @param listener
     *            An object to be invoked when a PropertyChange event is fired.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new java.util.Vector<PropertyChangeListener>();
        }
        listeners.addElement(listener);
    }

    /**
     * Remove a listener for the PropertyChange event.
     * 
     * @param listener
     *            The PropertyChange listener to be removed.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(listener);
    }

    /**
     * Report that we have been modified to any interested listeners.
     */
    public void firePropertyChange() {
        Vector<PropertyChangeListener> targets;
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            targets = new Vector<PropertyChangeListener>(listeners);
        }
        // Tell our listeners that "everything" has changed.
        PropertyChangeEvent evt = new PropertyChangeEvent(this.idPanel, "value", null, null);

        for (PropertyChangeListener l : targets) {
            l.propertyChange(evt);
        }
    }

    private java.util.Vector<PropertyChangeListener> listeners;

    public void setValue(Object value) {
        this.setValue((Integer) value);
    }

}
