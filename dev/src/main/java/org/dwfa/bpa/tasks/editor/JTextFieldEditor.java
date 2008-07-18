/*
 * Created on Dec 2, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.editor;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author kec
 * 
 */
public class JTextFieldEditor implements PropertyEditor, ItemListener {

  private class UpdateFieldDocumentListener implements DocumentListener {

    /**
     * @param setMethod
     * @param task
     * @param textField
     */
    public UpdateFieldDocumentListener() {
      super();
    }

    public void insertUpdate(DocumentEvent e) {
      JTextFieldEditor.this.firePropertyChange();
    }

    public void removeUpdate(DocumentEvent e) {
      JTextFieldEditor.this.firePropertyChange();
    }

    public void changedUpdate(DocumentEvent e) {
      JTextFieldEditor.this.firePropertyChange();
    }
  }

  private class EditorComponent extends JTextArea {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param text
     */
    public EditorComponent() {
      super(3, 10);
      this.setLineWrap(true);
      this.setBorder(BorderFactory.createLoweredBevelBorder());
      this.getDocument().addDocumentListener(new UpdateFieldDocumentListener());

    }

  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private EditorComponent   editor           = new EditorComponent();

  /**
   */
  public JTextFieldEditor() {
    super();
  }

  /**
   * @return String
   * @see java.beans.PropertyEditor#getValue()
   */
  public Object getValue() {
    return editor.getText();
  }

  /**
   * Must be a <code>String</code>.
   * 
   * @see java.beans.PropertyEditor#setValue(java.lang.Object)
   */
  public void setValue(Object value) {
    String message = (String) value;
    if (editor.getText().equals(message)) {
      //
    } else {
      editor.setText(message);
      this.firePropertyChange();
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
    return "\"" + this.getValue() + "\"";
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
    JScrollPane textScroller = new JScrollPane(this.editor);
    textScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    return textScroller;
  }

  /**
   * Returns true since this editor provides a custom GUI component.
   * 
   * @see java.beans.PropertyEditor#supportsCustomEditor()
   */
  public boolean supportsCustomEditor() {
    return true;
  }

  public void itemStateChanged(ItemEvent e) {
    this.firePropertyChange();
  }

  /**
   * Register a listener for the PropertyChange event. The class will fire a
   * PropertyChange value whenever the value is updated.
   * 
   * @param listener
   *          An object to be invoked when a PropertyChange event is fired.
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
   *          The PropertyChange listener to be removed.
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
    PropertyChangeEvent evt = new PropertyChangeEvent(this.editor, "value",
        null, null);

    for (PropertyChangeListener l : targets) {
      l.propertyChange(evt);
    }
  }

  private java.util.Vector<PropertyChangeListener> listeners;

}