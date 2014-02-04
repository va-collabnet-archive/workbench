package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.helper.cswords.CsWordsHelper;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.CaseSensitive;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import org.dwfa.ace.DynamicWidthTermComponentLabel;
import org.ihtsdo.arena.editor.ArenaEditor;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;

public class DragPanelDescription extends DragPanelComponentVersion<DescriptionAnalogBI> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ConceptViewLayout viewLayout;
    private JTextArea textPane;

    //~--- constructors --------------------------------------------------------
    public DragPanelDescription(ConceptViewLayout viewLayout, CollapsePanel parentCollapsePanel,
            DescriptionAnalogBI desc)
            throws TerminologyException, IOException {
        super(viewLayout, parentCollapsePanel, desc);
        this.viewLayout = viewLayout;
        layoutDescription();
    }

    public DragPanelDescription(LayoutManager layout, ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel, DescriptionAnalogBI desc)
            throws TerminologyException, IOException {
        super(layout, viewLayout, parentCollapsePanel, desc);
        this.viewLayout = viewLayout;
        layoutDescription();
    }

    //~--- methods -------------------------------------------------------------
    private void layoutDescription() throws TerminologyException, IOException {
        boolean canDrop = false;

        if (!ArenaEditor.diffColor.isEmpty() && viewLayout.getSettings().isForPromotion()){
            if(ArenaEditor.diffColor.containsKey(getThingToDrag().getNid())){
                Color color = ArenaEditor.diffColor.get(getThingToDrag().getNid());
                setBackground(color);
                
            }
        }
        
        if (getDesc().getTime() == Long.MAX_VALUE) {
            setOpaque(true);
            setBackground(Color.YELLOW);
            canDrop = true;
        }
        
        setupDrag(getDesc());
        setBorder(BorderFactory.createRaisedBevelBorder());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 3;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        if ((getParentCollapsePanel() == null)
                || !getSettings().getView().getConfig().getAllowedStatus().contains(getDesc().getStatusNid())) {
            JLabel descLabel = getJLabel(" ");
            descLabel.setOpaque(true);
            descLabel.setMinimumSize(new Dimension(16, 28));
            descLabel.setPreferredSize(new Dimension(16, 28));
            setDropPopupInset(descLabel.getPreferredSize().width);
            descLabel.setBackground(Color.ORANGE.darker());
            add(descLabel, gbc);
        } else {
            gbc.gridheight = 1;
            JButton button = getComponentActionMenuButton();
            button.setMinimumSize(new Dimension(16, 16));
            button.setPreferredSize(new Dimension(16, 16));
            button.setBackground(Color.ORANGE);
            button.setOpaque(true);
            add(button, gbc);
            gbc.gridy++;
            gbc.gridheight = GridBagConstraints.REMAINDER;
            gbc.weighty = 1;
            JLabel descLabel = getJLabel(" ");
            descLabel.setOpaque(true);
            descLabel.setMinimumSize(new Dimension(16, 12));
            descLabel.setPreferredSize(new Dimension(16, 12));
            setDropPopupInset(descLabel.getPreferredSize().width);
            descLabel.setBackground(Color.ORANGE);
            add(descLabel, gbc);
            gbc.gridy = 0;
            gbc.gridheight = 3;
            gbc.weighty = 0;
        }
        gbc.gridx++;
        if (!getDesc().isActive(getSettings().getConfig().getAllowedStatus())) {
            gbc.gridheight = 1;
            add(new JLabel(getGhostIcon()), gbc);
            gbc.gridheight = 3;
            gbc.gridx++;
        }

        add(conflictLabel, gbc);
        conflictLabel.setVisible(false);
        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;

        DynamicWidthTermComponentLabel typeLabel = getDynamicLabel(getDesc().getTypeNid(), canDrop, getSettings().getDescType());
        typeLabel.setSize(new Dimension(5,5));
        typeLabel.setBorder(BorderFactory.createEmptyBorder(0,3,0,0));
        add(typeLabel, gbc);
        
        if (getDesc().isUncommitted()
                && (getDesc().getStatusNid() == SnomedMetadataRfx.getSTATUS_RETIRED_NID())) {
            typeLabel.setFrozen(true);
        }

        if (getDesc().isUncommitted()
                && (getDesc().getStatusNid() != SnomedMetadataRfx.getSTATUS_RETIRED_NID())) {
            typeLabel.addPropertyChangeListener("termComponent",
                    new PropertyChangeManager<DescriptionAnalogBI>(getDesc()) {

                        @Override
                        protected void changeProperty(I_GetConceptData newValue) {
                            try {
                                getComponent().setTypeNid(newValue.getNid());

                                if (getComponent().isUncommitted()) {
                                    Terms.get().addUncommitted(Terms.get().getConcept(getComponent().getConceptNid()));
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
        gbc.anchor = GridBagConstraints.NORTHEAST;
        // check for case sensitivity
        String caseStr = "ci ";
        String descText = getDesc().getText();
        String initialWord = null;

        if (descText.indexOf(" ") != -1) {
            initialWord = descText.substring(0, descText.indexOf(" "));
        } else {
            initialWord = descText;
        }

        if (CsWordsHelper.isIcTypeSignificant(initialWord,
                CaseSensitive.MAYBE_IC_SIGNIFICANT.getLenient().getNid()) && (getDesc().isInitialCaseSignificant() == false) && getDesc().isUncommitted()) {
            caseStr = "<html><font color = 'red'>Cs";
        } else if (getDesc().isInitialCaseSignificant()) {
            caseStr = "Cs";
        }

        JLabel caseLabel = getJLabel(caseStr);
        add(caseLabel, gbc);
        
        gbc.gridx--;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        String lang = getDesc().getLang();

        if ((lang != null) && (lang.length() > 2)) {
            lang = lang.substring(0, 2);
        }

        StringBuilder sb = new StringBuilder();
        if (getDesc().getTypeNid() == SnomedMetadataRfx.getDES_SYNONYM_NID()) {
            for (RefexChronicleBI<?> refex : getDesc().getAnnotations()) {
                if (refex.getRefexNid() == SnomedMetadataRfx.getUS_DIALECT_REFEX_NID()) {
                    for (RefexVersionBI<?> v : refex.getVersions(getSettings().getConfig().getViewCoordinate())) {
                        if(RefexNidVersionBI.class.isAssignableFrom(v.getClass())){
                            RefexNidVersionBI cv = (RefexNidVersionBI) v;
                            if (cv.getNid1() == SnomedMetadataRfx.getDESC_PREFERRED_NID()) {
                                sb.append(" <font color='red'>US</font>");
                            } 
                        }
                    }

                } else if (refex.getRefexNid() == SnomedMetadataRfx.getGB_DIALECT_REFEX_NID()) {
                    for (RefexVersionBI<?> v : refex.getVersions(getSettings().getConfig().getViewCoordinate())) {
                        if(RefexNidVersionBI.class.isAssignableFrom(v.getClass())){
                            RefexNidVersionBI cv = (RefexNidVersionBI) v;
                            if (cv.getNid1() == SnomedMetadataRfx.getDESC_PREFERRED_NID()) {
                                sb.append(" <font color='blue'>GB</font>");
                            }
                        }
                        
                    }
                } else {
                    for (RefexVersionBI<?> v : refex.getVersions(getSettings().getConfig().getViewCoordinate())) {
                        if(RefexNidVersionBI.class.isAssignableFrom(v.getClass())){
                            RefexNidVersionBI cv = (RefexNidVersionBI) v;
                            if (cv.getNid1() == SnomedMetadataRfx.getDESC_PREFERRED_NID()) {
                                sb.append(" <font color='blue'> </font>");
                            }
                        }
                    }
                }
            }
        }
        if (sb.length() > 0) {
            lang = "<html>" + lang + ":pt" + sb.toString();
        }
        if(lang.length() == 2){
            if(getDesc().isUncommitted()){
                lang = "<html>" + lang + ":pt <font color='yellow'>US GB</font>"; 
            }else{
               lang = "<html>" + lang + ":pt <font color='rgb(234,234,234)'>US GB</font>"; 
            }
        }else if(lang.length() == 40 || lang.length() == 39){
            if(getDesc().isUncommitted()){
                lang = lang + "<font color='yellow'> GB</font>";
            }else{
                lang = lang + "<font color=rgb(234,234,234)'> GB</font>";
            }
        }
        JLabel langLabel = getJLabel(lang);
        langLabel.setBorder(BorderFactory.createEmptyBorder(0,3,0,2));
        add(langLabel, gbc);
        //add additional length
        

        gbc.gridx++;
        gbc.gridx++;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        
        add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        
        textPane = new JTextArea();
        textPane.setLineWrap(true);
        textPane.setWrapStyleWord(true);
        textPane.setEditable(canDrop);
        textPane.setOpaque(false);
        textPane.setFont(textPane.getFont().deriveFont(getSettings().getFontSize()));
        textPane.setText(getDesc().getText());
        textPane.setToolTipText(textPane.getText());
        if(canDrop && viewLayout.cView.focus){
            textPane.selectAll();
        }

        add(textPane, gbc);
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.gridx++;

        JButton collapseExpandButton = getCollapseExpandButton();
        gbc.gridheight = 2;
        add(collapseExpandButton, gbc);
             
        if (getDesc().isUncommitted()
                && (getDesc().getStatusNid() == SnomedMetadataRfx.getSTATUS_RETIRED_NID())) {
            textPane.setEditable(false);
        }

        if (getDesc().isUncommitted()
                && (getDesc().getStatusNid() != SnomedMetadataRfx.getSTATUS_RETIRED_NID())) {
            textPane.getDocument().addDocumentListener(new UpdateTextDocumentListener(textPane, getDesc()));
        }
        gbc.gridy = 3;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        addSubPanels(gbc);
    }

    //~--- get methods ---------------------------------------------------------
    private DescriptionAnalogBI getDesc() {
        return getComponentVersion();
    }

    public DescriptionVersionBI getDraggedThing() {
        return thingToDrag;
    }

    @Override
    public DataFlavor getNativeDataFlavor() {
        return DragPanelDataFlavors.descVersionFlavor;
    }

    public JTextArea getTextPane() {
        return textPane;
    }
    
    @Override
    public Collection<DragPanelComponentVersion<DescriptionAnalogBI>> getOtherVersionPanels()
            throws IOException, TerminologyException {
        Collection<DragPanelComponentVersion<DescriptionAnalogBI>> panelList =
                new ArrayList<DragPanelComponentVersion<DescriptionAnalogBI>>();
        Collection<DescriptionAnalogBI> versions = thingToDrag.getChronicle().getVersions();

        for (DescriptionAnalogBI dav : versions) {
            if (!thingToDrag.equals(dav)) {
                DragPanelDescription dpd = new DragPanelDescription(new GridBagLayout(), viewLayout, null, dav);

                panelList.add(dpd);
            }
        }

        return panelList;
    }

    @Override
    public DescriptionAnalogBI getThingToDrag() {
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
