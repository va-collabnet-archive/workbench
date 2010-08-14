package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.I_RelTuple;

public class DragPanelRelGroup extends DragPanel<RelGroupForDragPanel> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelRelGroup() {
		super();
	}

	public DragPanelRelGroup(LayoutManager layout) {
		super(layout);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DragPanelDataFlavors.relGroupFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(DragPanelDataFlavors.relGroupFlavor);
	}

	@Override
	public DataFlavor getNativeDataFlavor() {
		return DragPanelDataFlavors.relGroupFlavor ;
	}

	@Override
	protected int getTransferMode() {
		return TransferHandler.COPY;
	}

	public RelGroupForDragPanel getDraggedRelGroup() {
		return thingToDrag;
	}
	
	public void setDraggedRelGroup(RelGroupForDragPanel relGroup) {
		setThingToDrag(relGroup);
	}
	public void setDraggedRelGroup(I_RelTuple[] relGroup) {
		setThingToDrag(new RelGroupForDragPanel(relGroup));
	}

	@Override
	public String getDragPropertyString() {
		return "draggedRelGroup";
	}

	@Override
	protected void addToDropPopupMenu(JPopupMenu popup) {
		popup.add(new JMenuItem("Add to Rel Group"));
		popup.add(new JMenuItem("Move to Rel Group"));
		
	}


}
