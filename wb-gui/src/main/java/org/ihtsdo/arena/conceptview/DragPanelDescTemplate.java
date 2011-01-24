package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.util.EnumSet;

import javax.swing.TransferHandler;
import org.ihtsdo.arena.conceptview.ComponentVersionDragPanel.SubPanelTypes;

import org.ihtsdo.tk.spec.DescriptionSpec;

public class DragPanelDescTemplate extends DragPanel<DescriptionSpec> implements I_ToggleSubPanels {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public DragPanelDescTemplate(ConceptViewSettings settings, 
           DescriptionSpec ds) {
		super(settings, ds);
	}

	public DragPanelDescTemplate(LayoutManager layout, 
			ConceptViewSettings settings, DescriptionSpec ds) {
		super(layout, settings, ds);
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
	
	public DescriptionSpec getThingToDrag() {
		return thingToDrag;
	}
	
	public DescriptionSpec getDraggedThing() {
		return thingToDrag;
	}

	@Override
	public String getUserString(DescriptionSpec obj) {
		return obj.getDescText();
	}

    @Override
    public void showSubPanels(EnumSet<SubPanelTypes> panels) {
        // nothing to do...;
    }
	
    @Override
    public void hideSubPanels(EnumSet<SubPanelTypes> panels) {
        // nothing to do...;
    }
	
	
}
