/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.TransferHandler;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.conattr.ConAttrAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

/**
 *
 * @author kec
 */
public class DragPanelConceptAttributes extends DragPanelComponentVersion<ConAttrAnalogBI> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DragPanelConceptAttributes(ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel,
            ConAttrAnalogBI attr)
            throws TerminologyException, IOException {
        super(viewLayout, parentCollapsePanel, attr);
        layoutConceptAttrs();
    }

    public DragPanelConceptAttributes(LayoutManager layout, ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel,
            ConAttrAnalogBI attr)
            throws TerminologyException, IOException {
        super(layout, viewLayout, parentCollapsePanel, attr);
        layoutConceptAttrs();
    }

    private ConAttrAnalogBI getAttr() {
        return getComponentVersion();
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
    public ConAttrAnalogBI getThingToDrag() {
        return thingToDrag;
    }

    public ConAttrAnalogBI getDraggedThing() {
        return thingToDrag;
    }

    public void setDraggedThing(DescriptionVersionBI desc) {
        // handle drop...;
    }

    private void layoutConceptAttrs() throws TerminologyException, IOException {
        if (getAttr() == null) {
            return;
        }
        if (getAttr().getTime() == Long.MAX_VALUE) {
            setOpaque(true);
            setBackground(Color.YELLOW);
        }
        setupDrag(getAttr());
        setBorder(BorderFactory.createRaisedBevelBorder());

        JLabel attrLabel = getJLabel(" ");
        attrLabel.setBackground(Color.CYAN);
        attrLabel.setOpaque(true);
        setDropPopupInset(attrLabel.getPreferredSize().width);


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        add(attrLabel, gbc);
        if (!getThingToDrag().isActive(getSettings().getConfig().getAllowedStatus())) {
            add(new JLabel(getGhostIcon()));
            gbc.gridx++;
        }
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx++;
        gbc.weightx = 1;


        String statedDefinedStr = "primitive";
        if (getAttr().isDefined()) {
            statedDefinedStr = "defined";
        }
        JLabel statedDefinedLabel = getJLabel(statedDefinedStr);
        statedDefinedLabel.setOpaque(false);
        add(statedDefinedLabel, gbc);

        gbc.weightx = 0;
        gbc.gridx++;

        add(getComponentActionMenuButton(), gbc);
        gbc.gridx++;

        JButton collapseExpandButton = getCollapseExpandButton();
        add(collapseExpandButton, gbc);

        addSubPanels(gbc);
    }

    @Override
    public Collection<DragPanelComponentVersion<ConAttrAnalogBI>> getOtherVersionPanels()
            throws IOException, TerminologyException {
        Collection<DragPanelComponentVersion<ConAttrAnalogBI>> panelList =
                new ArrayList<DragPanelComponentVersion<ConAttrAnalogBI>>();
        Collection<ConAttrAnalogBI> versions = thingToDrag.getChronicle().getVersions();
        for (ConAttrAnalogBI dav : versions) {
            if (!thingToDrag.equals(dav)) {
                DragPanelConceptAttributes dpd = new DragPanelConceptAttributes(
                        new GridBagLayout(),
                        viewLayout,
                        null,
                        dav);
                panelList.add(dpd);
            }
        }
        return panelList;
    }
}
