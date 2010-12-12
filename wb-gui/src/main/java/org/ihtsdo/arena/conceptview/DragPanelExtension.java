package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import javax.swing.TransferHandler;

import org.ihtsdo.tk.api.ComponentVersionBI;

public class DragPanelExtension extends ComponentVersionDragPanel<ComponentVersionBI> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelExtension(ConceptViewSettings settings) {
		super(settings);
	}

	public DragPanelExtension(LayoutManager layout, ConceptViewSettings settings) {
		super(layout, settings);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DragPanelDataFlavors.conceptFlavor };
	}
	
	@Override
	public DataFlavor getNativeDataFlavor() {
		return DragPanelDataFlavors.conceptFlavor ;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return false;
	}

	@Override
	protected int getTransferMode() {
		return TransferHandler.COPY;
	}
	
	public ComponentVersionBI getThingToDrag() {
		return thingToDrag;
	}
	
	public ComponentVersionBI getDraggedThing() {
		return thingToDrag;
	}

	public void setDraggedThing(ComponentVersionBI component) {
		// handle drop...;
	}

}
