package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.TransferHandler;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;

public class DragPanelRelGroup extends DragPanelComponentVersion<RelationshipGroupVersionBI> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DragPanelRelGroup(ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel, RelationshipGroupVersionBI rgv) {
        super(viewLayout, parentCollapsePanel, rgv);
    }

    public DragPanelRelGroup(LayoutManager layout, ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel, RelationshipGroupVersionBI rgv) {
        super(layout, viewLayout, parentCollapsePanel, rgv);
    }

    @Override
    public Collection<DragPanelComponentVersion<RelationshipGroupVersionBI>> getOtherVersionPanels()
            throws IOException, TerminologyException {
        Collection<DragPanelComponentVersion<RelationshipGroupVersionBI>> panelList =
                new ArrayList<DragPanelComponentVersion<RelationshipGroupVersionBI>>();
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

    public RelationshipGroupVersionBI getDraggedThing() {
        return thingToDrag;
    }

    public void setDraggedThing(RelationshipGroupVersionBI relGroup) {
        // TODO handle drop.
    }
}
