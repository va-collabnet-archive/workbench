/*
 * Created on Jan 12, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
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

import org.dwfa.bpa.gui.DataIdPanel;
import org.dwfa.bpa.gui.TargetAndProcessForEditor;

public class DataIdEditor implements PropertyEditor,
        PropertyChangeListener {

    private static Logger logger = Logger.getLogger(DataIdEditor.class
            .getName());

    DataIdPanel dataIdPanel;
    
    public Class getAcceptableClass() {
        return Object.class;
    }

    public DataIdEditor(Object obj) throws ClassNotFoundException {
        TargetAndProcessForEditor tpfe = (TargetAndProcessForEditor) obj;
        this.dataIdPanel = new DataIdPanel(-1, tpfe.getProcess(), getAcceptableClass());
        this.dataIdPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.dataIdPanel.addPropertyChangeListener("id", this);
    }

    /**
     * @return true or false
     * @see java.beans.PropertyEditor#getValue()
     */
    public Integer getValue() {
        return dataIdPanel.getId();
    }

    /**
     * Must be a <code>Integer</code> representing data id.
     * 
     * @see java.beans.PropertyEditor#setValue(java.lang.Object)
     */
    public void setValue(Integer value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("DataIdEditor setValue: " + value);
        }
        this.dataIdPanel.setId(value);
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
        this.dataIdPanel.setBounds(box);
        this.dataIdPanel.paintAll(gfx);
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
        return this.dataIdPanel;
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
            logger.fine("Property changed for DataIdEditor: " + evt);
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
    public synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
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
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
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
        PropertyChangeEvent evt = new PropertyChangeEvent(this.dataIdPanel, "value", null, null);

        for (PropertyChangeListener l: targets) {
            l.propertyChange(evt);
        }
    }

    private java.util.Vector<PropertyChangeListener> listeners;

    public void setValue(Object value) {
        this.setValue((Integer)value);
    }

}
