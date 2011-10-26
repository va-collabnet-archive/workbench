package org.ihtsdo.project.panel.dnd;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class ListConceptDragSourceListener implements DragSourceListener{

	private JList jList;
	private I_RemoveItemFromModel removeItem;

	public ListConceptDragSourceListener(JList jList, I_RemoveItemFromModel removeItem){

		this.jList=jList;
		this.removeItem=removeItem;
	}
	public void dragDropEnd(DragSourceDropEvent dsde) {
		if (dsde.getDropSuccess()){
			if (dsde.getDropAction() == DnDConstants.ACTION_MOVE) {
				int[] indices =  jList.getSelectedIndices();
				Object[] objs=jList.getSelectedValues();
				if (indices != null && indices.length>0) {
					try {
						DefaultListModel model = (DefaultListModel)jList.getModel();
						if (removeItem!=null){
							removeItem.removeItemFromObject(objs[0]);
						}
						model.remove(indices[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void dragEnter(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub
	}

	public void dragExit(DragSourceEvent dse) {
		// TODO Auto-generated method stub
	}

	public void dragOver(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub
	}

}
