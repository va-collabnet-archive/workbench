package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.util.EnumSet;

import javax.swing.TransferHandler;
import org.ihtsdo.arena.conceptview.DragPanelComponentVersion.SubPanelTypes;

import org.ihtsdo.tk.spec.DescriptionSpec;

public class DragPanelDescTemplate extends DragPanel<DescriptionSpec> implements I_ToggleSubPanels {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DragPanelDescTemplate(ConceptViewLayout viewLayout,
            DescriptionSpec ds) {
        super(viewLayout, ds);
    }

    public DragPanelDescTemplate(LayoutManager layout,
            ConceptViewLayout viewLayout, DescriptionSpec ds) {
        super(layout, viewLayout, ds);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DragPanelDataFlavors.descVersionFlavor};
    }

    @Override
    public DataFlavor getNativeDataFlavor() {
        return DragPanelDataFlavors.descVersionFlavor;
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

    @Override
    public boolean isExpanded() {
        return false;
    }
}
