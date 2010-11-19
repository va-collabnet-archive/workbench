package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import javax.swing.TransferHandler;

import org.ihtsdo.tk.spec.RelSpec;

//FROM THIS public class DragPanelRelTemplate extends DragPanel<RelSpec> {
public class DragPanelRelTemplate extends DragPanel<RelSpec> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelRelTemplate(ConceptViewSettings settings) {
		super(settings);
	}

	public DragPanelRelTemplate(LayoutManager layout, 
			ConceptViewSettings settings) {
		super(layout, settings);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DragPanelDataFlavors.relVersionFlavor }; //from desc
	}
	
	@Override
	public DataFlavor getNativeDataFlavor() {
		return DragPanelDataFlavors.relVersionFlavor ; //from desc
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return false;
	}

	@Override
	protected int getTransferMode() {
		return TransferHandler.COPY;
	}
	
	public RelSpec getThingToDrag() {
		return thingToDrag;
	}
	
	public RelSpec getDraggedThing() {
		return thingToDrag;
	}
	
	@Override
	public String getUserString(RelSpec obj) {
		return obj.toString();
	}
}
