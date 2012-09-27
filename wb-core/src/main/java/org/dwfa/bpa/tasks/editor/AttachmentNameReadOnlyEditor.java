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
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.dwfa.bpa.gui.DnDropTextLabel;

public class AttachmentNameReadOnlyEditor implements PropertyEditor, PropertyChangeListener {

    private static Logger logger = Logger.getLogger(PropertyNameLabelEditor.class.getName());

    private JLabel propertyName;

    public Class<?> getAcceptableClass() {
        return Object.class;
    }

    public AttachmentNameReadOnlyEditor(Object obj) throws ClassNotFoundException {
        System.out.println("Creating AttachmentNameReadOnlyEditor for " + obj);
        // TargetAndProcessForEditor tpfe = (TargetAndProcessForEditor) obj;
        this.propertyName = new DnDropTextLabel();
        this.propertyName.setBorder(BorderFactory.createLoweredBevelBorder());
        this.propertyName.addPropertyChangeListener("text", this);
        this.setFrozen(true);
    }

    /**
     * @return true or false
     * @see java.beans.PropertyEditor#getValue()
     */
    public String getValue() {
        return this.propertyName.getText();
    }

    /**
     * Must be a <code>Integer</code> representing data id.
     * 
     * @see java.beans.PropertyEditor#setValue(java.lang.Object)
     */
    public void setValue(String value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("AttachmentNameLabelEditor setValue: " + value);
        }
        this.propertyName.setText(value);
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
        this.propertyName.setBounds(box);
        this.propertyName.paintAll(gfx);
    }

    /**
     * @see java.beans.PropertyEditor#getJavaInitializationString()
     */
    public String getJavaInitializationString() {
        return "new String(\"" + this.getValue().toString() + "\")";
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
        return this.propertyName;
    }

    /**
     * Returns true since this editor provides a custom GUI component.
     * 
     * @see java.beans.PropertyEditor#supportsCustomEditor()
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    private static String defaultText = null;

    public void propertyChange(PropertyChangeEvent evt) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Property changed for PropertyNameLabelEditor: " + evt);
        }
        if (defaultText == null) {
            try {
                defaultText = new DnDropTextLabel().getText();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        if (isFrozen() && (getValue() != null)
            && ((evt.getOldValue() != null) && evt.getOldValue().equals(defaultText) == false)) {
            logger.info("Cannot change property for embededded process to: " + evt.getNewValue() + " from "
                + evt.getOldValue());
            JOptionPane.showMessageDialog(propertyName, "Cannot change property for embededded process to: "
                + evt.getNewValue() + " from " + evt.getOldValue());
            propertyName.removePropertyChangeListener("text", this);
            propertyName.setText(evt.getOldValue().toString());
            propertyName.addPropertyChangeListener("text", this);
            return;
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
        PropertyChangeEvent evt = new PropertyChangeEvent(this.propertyName, "value", null, null);

        for (PropertyChangeListener l : targets) {
            l.propertyChange(evt);
        }
    }

    private java.util.Vector<PropertyChangeListener> listeners;

    public void setValue(Object value) {
        this.setValue((String) value);
        this.firePropertyChange();
    }

    private boolean frozen;

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

}
