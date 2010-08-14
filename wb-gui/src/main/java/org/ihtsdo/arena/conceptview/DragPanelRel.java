package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.I_RelTuple;

public class DragPanelRel extends DragPanel<I_RelTuple> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelRel() {
		super();
	}

	public DragPanelRel(LayoutManager layout) {
		super(layout);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DragPanelDataFlavors.relVersionFlavor };
	}
	
	@Override
	public DataFlavor getNativeDataFlavor() {
		return DragPanelDataFlavors.relVersionFlavor ;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return false;
	}

	@Override
	protected int getTransferMode() {
		return TransferHandler.COPY;
	}

	public I_RelTuple getThingToDrag() {
		return thingToDrag;
	}

	public I_RelTuple getDraggedRel() {
		return thingToDrag;
	}
	
	public void setDraggedRel(I_RelTuple rel) {
		setThingToDrag(rel);
	}

	@Override
	public String getDragPropertyString() {
		return "draggedRel";
	}

	protected void addToDropPopupMenu(JPopupMenu popup) {};
	

}
