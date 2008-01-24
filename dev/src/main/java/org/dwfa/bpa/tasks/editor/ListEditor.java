/*
 * Created on Feb 20, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.editor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.bpa.data.ArrayListModel;


public class ListEditor implements PropertyEditor, ListDataListener  {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private JList list = new JList();
    private JScrollPane editorScroller;
    /**
     * 
     */

    /**
        * @param arg0
        */
       public ListEditor() {
           list.getModel().addListDataListener(this);
           editorScroller = new JScrollPane(list);
           editorScroller.setMinimumSize(new Dimension(150, 100));
           editorScroller.setPreferredSize(new Dimension(150, 100));

           list.setBorder(BorderFactory.createLoweredBevelBorder());
        }
       
    /**
     * @return true or false
     * @see java.beans.PropertyEditor#getValue()
     */
    public ArrayListModel<?> getValue() {
        return (ArrayListModel<?>) list.getModel();
    }
       
       

    /**
     * Must be a <code>ListModel</code>. 
     * @see java.beans.PropertyEditor#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        this.list.setModel((ArrayListModel<?>) value);
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
     * @see java.beans.PropertyEditor#paintValue(java.awt.Graphics, java.awt.Rectangle)
     */
    public void paintValue(Graphics gfx, Rectangle box) {
        this.list.setBounds(box);
        this.list.paintAll(gfx);
    }

    /**
     * @see java.beans.PropertyEditor#getJavaInitializationString()
     */
    public String getJavaInitializationString() {
        throw new UnsupportedOperationException();
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
     * @see java.beans.PropertyEditor#getTags()
     */
    public String[] getTags() {
        return null;
    }

    /**
     * Returns swing component to edit the check box. 
     * @see java.beans.PropertyEditor#getCustomEditor()
     */
    public Component getCustomEditor() {
        return this.editorScroller;
    }

    /**
     * Returns true since this editor provides a custom GUI component. 
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
           PropertyChangeEvent evt = new PropertyChangeEvent(this.editorScroller, "value", null, null);

           for (PropertyChangeListener l: targets) {
               l.propertyChange(evt);
           }
       }

       private java.util.Vector<PropertyChangeListener> listeners;
    public void intervalAdded(ListDataEvent e) {
        this.firePropertyChange();        
    }
    public void intervalRemoved(ListDataEvent e) {
        this.firePropertyChange();        
    }
    public void contentsChanged(ListDataEvent e) {
        this.firePropertyChange();        
    }
      

   }
