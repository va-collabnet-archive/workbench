package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.TransferHandler;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;

public class DragPanelRelGroup extends ComponentVersionDragPanel<RelGroupVersionBI> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DragPanelRelGroup(ConceptViewSettings settings,
            CollapsePanel parentCollapsePanel, RelGroupVersionBI rgv) {
        super(settings, parentCollapsePanel, rgv);
    }

    public DragPanelRelGroup(LayoutManager layout, ConceptViewSettings settings,
            CollapsePanel parentCollapsePanel, RelGroupVersionBI rgv) {
        super(layout, settings, parentCollapsePanel, rgv);
    }

    @Override
    public Collection<ComponentVersionDragPanel<RelGroupVersionBI>> getOtherVersionPanels()
            throws IOException, TerminologyException {
        Collection<ComponentVersionDragPanel<RelGroupVersionBI>> panelList =
                new ArrayList<ComponentVersionDragPanel<RelGroupVersionBI>>();
        return panelList;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DragPanelDataFlavors.relGroupFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DragPanelDataFlavors.relGroupFlavor);
    }

    @Override
    public DataFlavor getNativeDataFlavor() {
        return DragPanelDataFlavors.relGroupFlavor;
    }

    @Override
    protected int getTransferMode() {
        return TransferHandler.COPY;
    }

    public RelGroupVersionBI getDraggedThing() {
        return thingToDrag;
    }

    public void setDraggedThing(RelGroupVersionBI relGroup) {
        // TODO handle drop.
    }
}
