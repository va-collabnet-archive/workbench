/*
 * Created on Apr 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.termviewer.DefaultTermViewerModel;
import org.dwfa.termviewer.view.concept.ConceptLabel;


/**
 * @author kec
 *
 */
public class QueueTypeEditor extends PropertyEditorSupport 
	implements PropertyChangeListener {

 
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    
    private ConceptLabel editor = new ConceptLabel(DefaultTermViewerModel.get());
    /**
     * @param arg0
     */
    public QueueTypeEditor() throws Exception {
        super();
        this.editor.addPropertyChangeListener("concept", this);
    }
    
    /**
     * @return TermEntry
     * @see java.beans.PropertyEditor#getValue()
     */
    public TermEntry getValue() {
        return (TermEntry) super.getValue();
    }
    

    /**
     * Must be a <code>TermEntry</code>. 
     * @see java.beans.PropertyEditor#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
    	
    	if (value != null) {
    		if (value.equals(getValue())) {
    			//nothing to do...
    		} else {
        	   	super.setValue(value);
        	   	TermEntry entry = (TermEntry) value;
                 try {
					this.editor.setConcept(entry.getLocalConcept());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	} else {
    	   	super.setValue(value);
    	   	try {
				editor.setConcept(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
        return "TermEntry.getByName(" +  this.getAsText() + ")";
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
        throw new UnsupportedOperationException();
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
	public void propertyChange(PropertyChangeEvent evt) {
        I_ConceptualizeLocally c = editor.getConcept();
        try {
			TermEntry entry = new TermEntry(c.getUids());
			setValue(entry);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setValue(null);
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setValue(null);
		}
	}

}
