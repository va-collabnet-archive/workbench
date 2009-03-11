package org.dwfa.bpa.tasks.editor;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class IncrementEditor implements PropertyEditor, ChangeListener {
	
	private class EditorComponent extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param text
		 */
		public EditorComponent() {
	        super(new FlowLayout());
	        this.setOpaque(false);
	        IncrementEditor.this.numberSpinnerModel = new SpinnerNumberModel(0, //initial value
	                0, //min
	                Integer.MAX_VALUE, //max
	                1); //step
	        IncrementEditor.this.numberSpinnerModel.addChangeListener(IncrementEditor.this);
	        spinner = new JSpinner(IncrementEditor.this.numberSpinnerModel);
	        this.add(spinner);
		}
		
	}
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private EditorComponent editor = new EditorComponent();

    private SpinnerNumberModel numberSpinnerModel;
    private JSpinner spinner;
    


    public IncrementEditor()  {
	    JComponent editor = spinner.getEditor();
	    ((JSpinner.DefaultEditor)editor).getTextField().setColumns(8);
    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent arg0) {
        this.firePropertyChange();

    }
    
    
	/**
	 * @return probability between 0 and 100. 
	 * @see java.beans.PropertyEditor#getValue()
	 */
	public Integer getValue() {
		return (Integer) this.numberSpinnerModel.getValue();
	}
    
    

	/**
	 * Must be a <code>Integer</code> representing probability between 0 and 100. 
	 * @see java.beans.PropertyEditor#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		Integer intVal = (Integer) value;
        this.numberSpinnerModel.setValue(intVal);
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
		this.editor.setBounds(box);
		this.editor.paintAll(gfx);
	}

	/**
	 * @see java.beans.PropertyEditor#getJavaInitializationString()
	 */
	public String getJavaInitializationString() {
		return "new Integer(" +  this.getValue() + ")";
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
		return this.editor;
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
        PropertyChangeEvent evt = new PropertyChangeEvent(this.editor, "value", null, null);

        for (PropertyChangeListener l: targets) {
            l.propertyChange(evt);
        }
    }

    private java.util.Vector<PropertyChangeListener> listeners;
   
}

