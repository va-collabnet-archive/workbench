package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.ihtsdo.arena.context.action.I_HandleContext;
import org.ihtsdo.tk.api.ComponentBI;

public class DragPanelDescription extends DragPanel<I_DescriptionTuple> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelDescription(I_HandleContext context) {
		super(context);
	}

	public DragPanelDescription(LayoutManager layout, I_HandleContext context) {
		super(layout, context);
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
	@Override
	protected Collection<Action> getActions(ComponentBI targetComponent, ComponentBI droppedComponent) {
		return context.dropOnDesc(targetComponent.getNid(), droppedComponent.getNid());
	}

}
