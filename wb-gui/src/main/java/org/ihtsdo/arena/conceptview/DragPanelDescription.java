package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import javax.swing.TransferHandler;

import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class DragPanelDescription extends ComponentVersionDragPanel<DescriptionVersionBI> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelDescription(ConceptViewSettings settings) {
		super(settings);
	}

	public DragPanelDescription(LayoutManager layout, ConceptViewSettings settings) {
		super(layout, settings);
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
	
	public DescriptionVersionBI getThingToDrag() {
		return thingToDrag;
	}
	
	public DescriptionVersionBI getDraggedThing() {
		return thingToDrag;
	}

	public void setDraggedThing(DescriptionVersionBI desc) {
		// handle drop...;
	}

}
