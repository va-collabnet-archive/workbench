package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.TransferHandler;

import org.ihtsdo.arena.context.action.I_HandleContext;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;

public class DragPanelRelGroup extends DragPanel<RelGroupChronicleBI> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelRelGroup(I_HandleContext context) {
		super(context);
	}

	public DragPanelRelGroup(LayoutManager layout, I_HandleContext context) {
		super(layout, context);
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

	public RelGroupChronicleBI getDraggedRelGroup() {
		return thingToDrag;
	}
	
	public void setDraggedRelGroup(RelGroupChronicleBI relGroup) {
		setDraggedThing(relGroup);
	}

	@Override
	protected Collection<Action> getActions(ComponentBI targetComponent, ComponentBI droppedComponent) {
		return context.dropOnRelGroup(targetComponent.getNid(), droppedComponent.getNid());
	}

}
