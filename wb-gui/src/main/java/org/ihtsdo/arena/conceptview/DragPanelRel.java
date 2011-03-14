package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import javax.swing.TransferHandler;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;

import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class DragPanelRel extends ComponentVersionDragPanel<RelationshipVersionBI> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel relLabel;

    public DragPanelRel(ConceptViewSettings settings,
            CollapsePanel parentCollapsePanel, RelationshipVersionBI rel)
            throws TerminologyException, IOException {
        super(settings, parentCollapsePanel, rel);
        layoutRel();
    }

    public DragPanelRel(LayoutManager layout, ConceptViewSettings settings,
            CollapsePanel parentCollapsePanel, RelationshipVersionBI rel)
            throws TerminologyException, IOException {
        super(layout, settings, parentCollapsePanel, rel);
        layoutRel();
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
                dpd.setInactiveBackground();
                panelList.add(dpd);
            }
        }
        return panelList;
    }

    private void layoutRel() throws TerminologyException, IOException {
        boolean canDrop = false;
        if (getRel().getTime() == Long.MAX_VALUE) {
            setOpaque(true);
            setBackground(Color.YELLOW);
            canDrop = true;
        }
        setupDrag(getRel());
        setBorder(BorderFactory.createRaisedBevelBorder());
        relLabel = getJLabel(" ");
        relLabel.setBackground(Color.BLUE);
        relLabel.setOpaque(true);
        setDropPopupInset(relLabel.getPreferredSize().width);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(relLabel, gbc);
        gbc.weightx = 1;
        gbc.gridx++;
        TermComponentLabel typeLabel = getLabel(getRel().getTypeNid(), canDrop);
        typeLabel.addPropertyChangeListener("termComponent",
                new PropertyChangeManager<RelationshipAnalogBI>(
                (RelationshipAnalogBI) getRel()) {

                    @Override
                    protected void changeProperty(I_GetConceptData newValue) {
                        try {
                            getComponent().setTypeNid(newValue.getNid());
                            if (getComponent().isUncommitted()) {
                                Terms.get().addUncommitted(Terms.get().getConcept(
                                        getComponent().getOriginNid()));
                            }
                        } catch (PropertyVetoException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        } catch (TerminologyException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        } catch (IOException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                    }
                });
        add(typeLabel, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.weightx = 1;
        gbc.gridx++;
        TermComponentLabel destLabel = getLabel(getRel().getDestinationNid(), canDrop);
        typeLabel.addPropertyChangeListener("termComponent",
                new PropertyChangeManager<RelationshipAnalogBI>(
                (RelationshipAnalogBI) getRel()) {

                    @Override
                    protected void changeProperty(I_GetConceptData newValue) {
                        try {
                            getComponent().setDestinationNid(newValue.getNid());
                            if (getComponent().isUncommitted()) {
                                Terms.get().addUncommitted(Terms.get().getConcept(
                                        getComponent().getOriginNid()));
                            }
                        } catch (PropertyVetoException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        } catch (TerminologyException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        } catch (IOException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                    }
                });
        add(destLabel, gbc);
        JButton collapseExpandButton = getCollapseExpandButton();
        gbc.weightx = 0;
        gbc.gridx++;
        add(collapseExpandButton, gbc);
        addSubPanels(gbc);
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

    public RelationshipVersionBI getRel() {
        return thingToDrag;
    }

    public RelationshipVersionBI getDraggedThing() {
        return thingToDrag;
    }

    public void setDraggedThing(RelationshipVersionBI rel) {
        // TODO handle drop.
    }

    private void setInactiveBackground() {
        relLabel.setBackground(relLabel.getBackground().darker());
    }
}
