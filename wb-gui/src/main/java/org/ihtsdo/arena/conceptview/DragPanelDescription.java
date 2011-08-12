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
import org.ihtsdo.helper.cswords.CsWordsHelper;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;

import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.CaseSensitive;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

public class DragPanelDescription extends DragPanelComponentVersion<DescriptionAnalogBI> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DragPanelDescription(ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel,
            DescriptionAnalogBI desc)
            throws TerminologyException, IOException {
        super(viewLayout, parentCollapsePanel, desc);
        layoutDescription();
    }

    public DragPanelDescription(LayoutManager layout, ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel,
            DescriptionAnalogBI desc)
            throws TerminologyException, IOException {
        super(layout, viewLayout, parentCollapsePanel, desc);
        layoutDescription();
    }

    private DescriptionAnalogBI getDesc() {
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
    public DescriptionAnalogBI getThingToDrag() {
        return thingToDrag;
    }

    public DescriptionVersionBI getDraggedThing() {
        return thingToDrag;
    }

    public void setDraggedThing(DescriptionVersionBI desc) {
        // handle drop...;
    }

    private void layoutDescription() throws TerminologyException, IOException {
        boolean canDrop = false;
        if (getDesc().getTime() == Long.MAX_VALUE) {
            setOpaque(true);
            setBackground(Color.YELLOW);
            canDrop = true;
        }
        setupDrag(getDesc());
        setBorder(BorderFactory.createRaisedBevelBorder());

        JLabel descLabel = getJLabel(" ");
        if (getParentCollapsePanel() == null || 
                !getSettings().getView().getConfig().getAllowedStatus().contains(getDesc().getStatusNid())) {
            descLabel.setBackground(Color.ORANGE.darker());
        } else {
            descLabel.setBackground(Color.ORANGE);
        }
        descLabel.setOpaque(true);
        setDropPopupInset(descLabel.getPreferredSize().width);


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(descLabel, gbc);
        if (!getDesc().isActive(getSettings().getConfig().getAllowedStatus())) {
            add(new JLabel(getGhostIcon()));
            gbc.gridx++;
        }
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx++;
        TermComponentLabel typeLabel = getLabel(getDesc().getTypeNid(), canDrop, getSettings().getDescType());
        add(typeLabel, gbc);
        
        if (getDesc().isUncommitted() 
                && getDesc().getStatusNid()== SnomedMetadataRfx.getSTATUS_RETIRED_NID()) {
            typeLabel.setFrozen(true);
        }
        if (getDesc().isUncommitted() && 
                getDesc().getStatusNid()!= SnomedMetadataRfx.getSTATUS_RETIRED_NID()) {
            typeLabel.addPropertyChangeListener("termComponent",
                    new PropertyChangeManager<DescriptionAnalogBI>(getDesc()) {

                        @Override
                        protected void changeProperty(I_GetConceptData newValue) {
                            try {
                                getComponent().setTypeNid(newValue.getNid());
                                if (getComponent().isUncommitted()) {
                                    Terms.get().addUncommitted(
                                            Terms.get().getConcept(getComponent().getConceptNid()));
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
        }
        gbc.gridx++;
        add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.weightx = 1;
        gbc.gridx++;
        FixedWidthJEditorPane textPane = new FixedWidthJEditorPane();
        textPane.setEditable(canDrop);
        textPane.setOpaque(false);
        textPane.setFont(textPane.getFont().deriveFont(getSettings().getFontSize()));
        textPane.setText(getDesc().getText());
        add(textPane, gbc);
        gbc.weightx = 0;
        gbc.gridx++;
        String lang = getDesc().getLang();
        if (lang != null && lang.length() > 2) {
            lang = lang.substring(0, 2);
        }
        JLabel langLabel = getJLabel(lang);
        add(langLabel, gbc);
        gbc.gridx++;
        //check for case sensitivity
        String caseStr = "ci";
        String descText = getDesc().getText();
        String initialWord = null;
        if (descText.indexOf(" ") != -1) {
            initialWord = descText.substring(0, descText.indexOf(" "));
        } else {
            initialWord = descText;
        }

        if (CsWordsHelper.isIcTypeSignificant(initialWord, 
                CaseSensitive.MAYBE_IC_SIGNIFICANT.getLenient().getNid())
                && getDesc().isInitialCaseSignificant() == false && getDesc().isUncommitted()) {
            caseStr = "<html><font color = 'red'>Cs";
        } else if (getDesc().isInitialCaseSignificant()) {
            caseStr = "Cs";
        }
        JLabel caseLabel = getJLabel(caseStr);
        add(caseLabel, gbc);


        gbc.gridx++;

        add(getComponentActionMenuButton(), gbc);

        gbc.gridx++;

        JButton collapseExpandButton = getCollapseExpandButton();
        add(collapseExpandButton, gbc);
        
        if (getDesc().isUncommitted() 
                && getDesc().getStatusNid()== SnomedMetadataRfx.getSTATUS_RETIRED_NID()) {
            textPane.setEditable(false);
        }

        if (getDesc().isUncommitted() 
                && getDesc().getStatusNid()!= SnomedMetadataRfx.getSTATUS_RETIRED_NID()) {
            textPane.getDocument().addDocumentListener(
                    new UpdateTextDocumentListener(textPane, getDesc()));
        }
        addSubPanels(gbc);
    }

    @Override
    public Collection<DragPanelComponentVersion<DescriptionAnalogBI>> getOtherVersionPanels()
            throws IOException, TerminologyException {
        Collection<DragPanelComponentVersion<DescriptionAnalogBI>> panelList =
                new ArrayList<DragPanelComponentVersion<DescriptionAnalogBI>>();
        Collection<DescriptionAnalogBI> versions = thingToDrag.getChronicle().getVersions();
        for (DescriptionAnalogBI dav : versions) {
            if (!thingToDrag.equals(dav)) {
                DragPanelDescription dpd = new DragPanelDescription(
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
