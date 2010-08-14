package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.I_DescriptionTuple;

public class DragPanelDescription extends DragPanel<I_DescriptionTuple> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelDescription() {
		super();
	}

	public DragPanelDescription(LayoutManager layout) {
		super(layout);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DragPanelDataFlavors.descVersionFlavor };
	}
	
	@Override
	public DataFlavor getNativeDataFlavor() {
		return DragPanelDataFlavors.descVersionFlavor ;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return false;
	}

	@Override
	protected int getTransferMode() {
		return TransferHandler.COPY;
	}
	
	public I_DescriptionTuple getThingToDrag() {
		return thingToDrag;
	}

	public I_DescriptionTuple getDraggedDesc() {
		return thingToDrag;
	}
	
	public void setDraggedDesc(I_DescriptionTuple desc) {
		setThingToDrag(desc);
	}

	@Override
	public String getDragPropertyString() {
		return "draggedDesc";
	}
	protected void addToDropPopupMenu(JPopupMenu popup) {};

}
