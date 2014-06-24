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
package org.ihtsdo.project.view.dnd;

import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.dwfa.ace.log.AceLog;

/**
 * The listener interface for receiving listConceptDragSource events. The class
 * that is interested in processing a listConceptDragSource event implements
 * this interface, and the object created with that class is registered with a
 * component using the component's
 * <code>addListConceptDragSourceListener<code> method. When
 * the listConceptDragSource event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see ListConceptDragSourceEvent
 */
public class ListConceptDragSourceListener implements DragSourceListener {

	/** The j list. */
	private JList jList;

	/** The remove item. */
	private I_RemoveItemFromModel removeItem;

	/**
	 * Instantiates a new list concept drag source listener.
	 * 
	 * @param jList
	 *            the j list
	 * @param removeItem
	 *            the remove item
	 */
	public ListConceptDragSourceListener(JList jList, I_RemoveItemFromModel removeItem) {

		this.jList = jList;
		this.removeItem = removeItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent
	 * )
	 */
	public void dragDropEnd(DragSourceDropEvent dsde) {
		if (dsde.getDropSuccess()) {
			Component sourceComponent = dsde.getDragSourceContext().getComponent();
			Point location = dsde.getLocation();
			Component parentComp = null;
			Component nextParent = sourceComponent.getParent();
			// while(parentComp == null){
			// if(nextParent.getParent().getClass().equals(RefsetPartitionerPanel.class)){
			// parentComp = nextParent.getParent();
			// }else{
			// nextParent = nextParent.getParent();
			// }
			// }
			if (sourceComponent instanceof JList) {
				if (dsde.getDropAction() == DnDConstants.ACTION_MOVE) {
					jList = (JList)sourceComponent;
					int[] indices = jList.getSelectedIndices();
					Object[] objs = jList.getSelectedValues();
					if (indices != null && indices.length > 0) {
						try {
							DefaultListModel model = (DefaultListModel) jList.getModel();
							if (removeItem != null) {
								removeItem.removeItemFromObject(objs[0]);
							}
							model.remove(indices[0]);
						} catch (Exception e) {
							AceLog.getAppLog().alertAndLogException(e);
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent
	 * )
	 */
	public void dragEnter(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
	 */
	public void dragExit(DragSourceEvent dse) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent
	 * )
	 */
	public void dragOver(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.
	 * DragSourceDragEvent)
	 */
	public void dropActionChanged(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub
	}

}
