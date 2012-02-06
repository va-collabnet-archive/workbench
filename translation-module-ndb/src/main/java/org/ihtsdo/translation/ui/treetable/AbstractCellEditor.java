/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation.ui.treetable;
/*

 * %W% %E%
 *
 * Copyright 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *   
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution. 
 *   
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.  
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,   
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

/**
 * The Class AbstractCellEditor.
 *
 * @version %I% %G%
 * 
 * A base class for CellEditors, providing default implementations for all
 * methods in the CellEditor interface and support for managing a series
 * of listeners.
 * @author Philip Milne
 */

public class AbstractCellEditor implements CellEditor {

    /** The listener list. */
    protected EventListenerList listenerList = new EventListenerList();

    /* (non-Javadoc)
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    public Object getCellEditorValue() { return null; }
    
    /* (non-Javadoc)
     * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
     */
    public boolean isCellEditable(EventObject e) { return true; }
    
    /* (non-Javadoc)
     * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
     */
    public boolean shouldSelectCell(EventObject anEvent) { return false; }
    
    /* (non-Javadoc)
     * @see javax.swing.CellEditor#stopCellEditing()
     */
    public boolean stopCellEditing() { return true; }
    
    /* (non-Javadoc)
     * @see javax.swing.CellEditor#cancelCellEditing()
     */
    public void cancelCellEditing() {}

    /* (non-Javadoc)
     * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
     */
    public void addCellEditorListener(CellEditorListener l) {
	listenerList.add(CellEditorListener.class, l);
    }

    /* (non-Javadoc)
     * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
     */
    public void removeCellEditorListener(CellEditorListener l) {
	listenerList.remove(CellEditorListener.class, l);
    }

    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  
     * @see EventListenerList
     */
    protected void fireEditingStopped() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==CellEditorListener.class) {
		((CellEditorListener)listeners[i+1]).editingStopped(new ChangeEvent(this));
	    }	       
	}
    }

    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  
     * @see EventListenerList
     */
    protected void fireEditingCanceled() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==CellEditorListener.class) {
		((CellEditorListener)listeners[i+1]).editingCanceled(new ChangeEvent(this));
	    }	       
	}
    }
}

