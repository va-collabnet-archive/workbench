package org.ihtsdo.arena.conceptview;

import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.TransferHandler;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class DragPanelRel extends ComponentVersionDragPanel<RelationshipVersionBI> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DragPanelRel(ConceptViewSettings settings,
            CollapsePanel parentCollapsePanel, RelationshipVersionBI rel) {
        super(settings, parentCollapsePanel, rel);
    }

    public DragPanelRel(LayoutManager layout, ConceptViewSettings settings,
            CollapsePanel parentCollapsePanel, RelationshipVersionBI rel) {
        super(layout, settings, parentCollapsePanel, rel);
    }

         
    @Override
    public Collection<ComponentVersionDragPanel<RelationshipVersionBI>> getOtherVersionPanels() 
            throws IOException, TerminologyException {
        Collection<ComponentVersionDragPanel<RelationshipVersionBI>> panelList =
                new ArrayList<ComponentVersionDragPanel<RelationshipVersionBI>>();
        Collection<RelationshipVersionBI> versions = thingToDrag.getChronicle().getVersions();
        for (RelationshipVersionBI dav : versions) {
            if (!thingToDrag.equals(dav)) {
                DragPanelRel dpd = new DragPanelRel(
                        new GridBagLayout(), 
                        getSettings(),
                        null,
                        dav);
                panelList.add(dpd);
            }
        }
        return panelList;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DragPanelDataFlavors.relVersionFlavor};
    }

    @Override
    public DataFlavor getNativeDataFlavor() {
        return DragPanelDataFlavors.relVersionFlavor;
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
