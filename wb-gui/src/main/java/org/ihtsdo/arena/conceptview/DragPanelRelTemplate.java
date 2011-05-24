package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.util.EnumSet;

import javax.swing.TransferHandler;
import org.ihtsdo.arena.conceptview.ComponentVersionDragPanel.SubPanelTypes;

import org.ihtsdo.tk.spec.RelSpec;

public class DragPanelRelTemplate extends DragPanel<RelSpec> implements I_ToggleSubPanels {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	public DragPanelRelTemplate(ConceptViewLayout viewLayout, RelSpec rs) {
		super(viewLayout, rs);
	}

	public DragPanelRelTemplate(LayoutManager layout,
			ConceptViewLayout viewLayout, RelSpec rs) {
		super(layout, viewLayout, rs);
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

        @Override
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

    @Override
    public void showSubPanels(EnumSet<SubPanelTypes> panels) {
        // nothing to do...;
    }


    @Override
    public void hideSubPanels(EnumSet<SubPanelTypes> panels) {
        // nothing to do...;
    }


   @Override
   public boolean isExpanded() {
      return false;
   }

}
