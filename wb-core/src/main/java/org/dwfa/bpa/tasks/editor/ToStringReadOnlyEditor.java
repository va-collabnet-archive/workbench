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
 * Created on Feb 20, 2006
 */
package org.dwfa.bpa.tasks.editor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class ToStringReadOnlyEditor implements PropertyEditor {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JLabel editor = new JLabel();

    /**
     * 
     */

    /**
     * @param arg0
     */
    public ToStringReadOnlyEditor() {
        editor.setMinimumSize(new Dimension(40, 10));
        editor.setBorder(BorderFactory.createLoweredBevelBorder());

    }

    /**
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent evt) {
        this.firePropertyChange();

    }

    /**
     * @return String
     * @see java.beans.PropertyEditor#getValue()
     */
    public String getValue() {
        return editor.getText();
    }

    /**
     * Must be a <code>Level</code> representing logging level for the logger.
     * 
     * @see java.beans.PropertyEditor#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        if (value == null) {
            this.editor.setText("null");
        } else {
            this.editor.setText(value.toString());
        }
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
        this.editor.setBounds(box);
        this.editor.paintAll(gfx);
    }

    /**
     * @see java.beans.PropertyEditor#getJavaInitializationString()
     */
    public String getJavaInitializationString() {
        return "new String(" + this.getValue() + ")";
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
        this.setValue(text);
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
        return this.editor;
    }

    /**
     * Returns true since this editor provides a custom GUI component.
     * 
     * @see java.beans.PropertyEditor#supportsCustomEditor()
     */
    public boolean supportsCustomEditor() {
        return true;
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
        PropertyChangeEvent evt = new PropertyChangeEvent(this.editor, "value", null, null);

        for (PropertyChangeListener l : targets) {
            l.propertyChange(evt);
        }
    }

    private java.util.Vector<PropertyChangeListener> listeners;
}
