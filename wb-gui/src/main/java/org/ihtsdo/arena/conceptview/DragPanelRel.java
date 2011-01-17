package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;

import javax.swing.TransferHandler;

import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class DragPanelRel extends ComponentVersionDragPanel<RelationshipVersionBI> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelRel(ConceptViewSettings settings,
           CollapsePanel parentCollapsePanel) {
		super(settings, parentCollapsePanel);
	}

	public DragPanelRel(LayoutManager layout, ConceptViewSettings settings,
           CollapsePanel parentCollapsePanel) {
		super(layout, settings, parentCollapsePanel);
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

	public RelationshipVersionBI getThingToDrag() {
		return thingToDrag;
	}

	public RelationshipVersionBI getDraggedThing() {
		return thingToDrag;
	}
	

	public void setDraggedThing(RelationshipVersionBI rel) {
		// TODO handle drop.
	}
}
