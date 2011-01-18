package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyVetoException;
import java.io.IOException;
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
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;

import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class DragPanelDescription extends ComponentVersionDragPanel<DescriptionVersionBI> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    DescriptionAnalogBI desc;

    public DragPanelDescription(ConceptViewSettings settings,
            CollapsePanel parentCollapsePanel, DescriptionAnalogBI desc)
            throws TerminologyException, IOException {
        super(settings, parentCollapsePanel);
        this.desc = desc;
        layoutDescription();
    }

    public DragPanelDescription(LayoutManager layout, ConceptViewSettings settings,
            CollapsePanel parentCollapsePanel, DescriptionAnalogBI desc)
            throws TerminologyException, IOException {
        super(layout, settings, parentCollapsePanel);
        this.desc = desc;
        layoutDescription();
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
    public DescriptionVersionBI getThingToDrag() {
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
        if (desc.getTime() == Long.MAX_VALUE) {
            setOpaque(true);
            setBackground(Color.YELLOW);
            canDrop = true;
        }
        setupDrag(desc);
        setBorder(BorderFactory.createRaisedBevelBorder());

        JLabel descLabel = getJLabel(" ");
        descLabel.setBackground(Color.ORANGE);
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
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx++;
        TermComponentLabel typeLabel = getLabel(desc.getTypeNid(), canDrop);
        add(typeLabel, gbc);
        if (desc.isUncommitted()) {
            typeLabel.addPropertyChangeListener("termComponent",
                    new PropertyChangeManager<DescriptionAnalogBI>(desc) {

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
        add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.weightx = 1;
        gbc.gridx++;
        FixedWidthJEditorPane textPane = new FixedWidthJEditorPane();
        textPane.setEditable(canDrop);
        textPane.setOpaque(false);
        textPane.setFont(textPane.getFont().deriveFont(getSettings().getFontSize()));
        textPane.setText(desc.getText());
        add(textPane, gbc);
        gbc.weightx = 0;
        gbc.gridx++;
        String lang = desc.getLang();
        if (lang != null && lang.length() > 2) {
            lang = lang.substring(0, 2);
        }
        JLabel langLabel = getJLabel(lang);
        langLabel.setOpaque(true);
        add(langLabel, gbc);
        gbc.gridx++;
        String caseStr = "ci";
        if (desc.isInitialCaseSignificant()) {
            caseStr = "Cs";
        }
        JLabel caseLabel = getJLabel(caseStr);
        langLabel.setOpaque(true);
        add(caseLabel, gbc);
        gbc.gridx++;


        JButton collapseExpandButton = getCollapseExpandButton();
        add(collapseExpandButton, gbc);

        if (desc.isUncommitted()) {
            textPane.getDocument().addDocumentListener(
                    new UpdateTextDocumentListener(textPane, desc));
        }
    }
}
