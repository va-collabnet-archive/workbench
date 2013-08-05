
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Dimension;
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
import org.ihtsdo.arena.editor.ArenaEditor;

/**
 *
 * @author kec
 */
public class DragPanelConceptAttributes extends DragPanelComponentVersion<ConceptAttributeAnalogBI> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //~--- constructors --------------------------------------------------------
    public DragPanelConceptAttributes(ConceptViewLayout viewLayout, CollapsePanel parentCollapsePanel,
            ConceptAttributeAnalogBI attr)
            throws TerminologyException, IOException {
        super(viewLayout, parentCollapsePanel, attr);
        layoutConceptAttrs();
    }

    public DragPanelConceptAttributes(LayoutManager layout, ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel, ConceptAttributeAnalogBI attr)
            throws TerminologyException, IOException {
        super(layout, viewLayout, parentCollapsePanel, attr);
        layoutConceptAttrs();
    }

    //~--- methods -------------------------------------------------------------
    private void layoutConceptAttrs() throws TerminologyException, IOException {
        if (getAttr() == null) {
            return;
        }
        
        if (!ArenaEditor.diffColor.isEmpty() && viewLayout.getSettings().isForPromotion()){
            if(ArenaEditor.diffColor.containsKey(getThingToDrag().getNid())){
                Color color = ArenaEditor.diffColor.get(getThingToDrag().getNid());
                if(getThingToDrag().isDefined()){
                    color = color.darker();
                }
                setBackground(color);
                
            }
        }
        
        if (getAttr().getTime() == Long.MAX_VALUE) {
            setOpaque(true);
            setBackground(Color.YELLOW);
        }

        setupDrag(getAttr());
        setBorder(BorderFactory.createRaisedBevelBorder());

        JLabel attrLabel = getJLabel(" ");

        if ((getParentCollapsePanel() == null)
                || !getSettings().getView().getConfig().getAllowedStatus().contains(getAttr().getStatusNid())) {
            attrLabel.setBackground(Color.CYAN.darker());
        } else {
            attrLabel.setBackground(Color.CYAN);
        }

        attrLabel.setOpaque(true);
        attrLabel.setMinimumSize(new Dimension(20, 28));
        attrLabel.setPreferredSize(new Dimension(20, 28));
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
        gbc.gridx++;
        if (!getThingToDrag().isActive(getSettings().getConfig().getAllowedStatus())) {
            add(new JLabel(getGhostIcon()), gbc);
            gbc.gridx++;
        }

        add(conflictLabel, gbc);
        conflictLabel.setVisible(false);
        gbc.gridx++;
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

    //~--- get methods ---------------------------------------------------------
    private ConceptAttributeAnalogBI getAttr() {
        return getComponentVersion();
    }

    public ConceptAttributeAnalogBI getDraggedThing() {
        return thingToDrag;
    }

    @Override
    public DataFlavor getNativeDataFlavor() {
        return DragPanelDataFlavors.descVersionFlavor;
    }

    @Override
    public Collection<DragPanelComponentVersion<ConceptAttributeAnalogBI>> getOtherVersionPanels()
            throws IOException, TerminologyException {
        Collection<DragPanelComponentVersion<ConceptAttributeAnalogBI>> panelList =
                new ArrayList<DragPanelComponentVersion<ConceptAttributeAnalogBI>>();
        Collection<ConceptAttributeAnalogBI> versions = thingToDrag.getChronicle().getVersions();

        for (ConceptAttributeAnalogBI dav : versions) {
            if (!thingToDrag.equals(dav)) {
                DragPanelConceptAttributes dpd = new DragPanelConceptAttributes(new GridBagLayout(), viewLayout,
                        null, dav);

                panelList.add(dpd);
            }
        }

        return panelList;
    }

    @Override
    public ConceptAttributeAnalogBI getThingToDrag() {
        return thingToDrag;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DragPanelDataFlavors.descVersionFlavor};
    }

    @Override
    protected int getTransferMode() {
        return TransferHandler.COPY;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return false;
    }

    //~--- set methods ---------------------------------------------------------
    public void setDraggedThing(DescriptionVersionBI desc) {
        // handle drop...;
    }
}
