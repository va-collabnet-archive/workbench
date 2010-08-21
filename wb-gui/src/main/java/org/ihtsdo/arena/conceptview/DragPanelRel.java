package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.I_RelTuple;
import org.ihtsdo.arena.context.action.I_HandleContext;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class DragPanelRel extends DragPanel<RelationshipVersionBI> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelRel(I_HandleContext context) {
		super(context);
	}

	public DragPanelRel(LayoutManager layout, I_HandleContext context) {
		super(layout, context);
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

	public RelationshipVersionBI getDraggedRel() {
		return thingToDrag;
	}
	
	public void setDraggedRel(I_RelTuple rel) {
		setDraggedThing(rel);
	}

	@Override
	protected Collection<Action> getActions(ComponentBI targetComponent, ComponentBI droppedComponent) {
		return context.dropOnRel(targetComponent.getNid(), droppedComponent.getNid());
	}

}
