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
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import javax.swing.TransferHandler;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_cnid.RefexCnidCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;

public class DragPanelExtension
        extends ComponentVersionDragPanel<RefexVersionBI<?>> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel extensionLabel;

    public DragPanelExtension(ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel, RefexVersionBI<?> refex)
            throws IOException, TerminologyException {
        super(viewLayout, parentCollapsePanel, refex);
        layoutExtension();
    }

    public DragPanelExtension(LayoutManager layout, ConceptViewLayout viewLayout,
            CollapsePanel parentCollapsePanel, RefexVersionBI<?> refex)
            throws IOException, TerminologyException {
        super(layout, viewLayout, parentCollapsePanel, refex);
        layoutExtension();
    }

    @Override
    public Collection<ComponentVersionDragPanel<RefexVersionBI<?>>> getOtherVersionPanels()
            throws IOException, TerminologyException {
        Collection<ComponentVersionDragPanel<RefexVersionBI<?>>> panelList =
                new ArrayList<ComponentVersionDragPanel<RefexVersionBI<?>>>();
        Collection<RefexVersionBI<?>> versions = thingToDrag.getChronicle().getVersions();
        for (RefexVersionBI<?> dav : versions) {
            if (!thingToDrag.equals(dav)) {
                DragPanelExtension dpd = new DragPanelExtension(
                        new GridBagLayout(),
                        viewLayout,
                        null,
                        dav);
                dpd.setInactiveBackground();
                panelList.add(dpd);
            }
        }
        return panelList;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DragPanelDataFlavors.conceptFlavor};
    }

    @Override
    public DataFlavor getNativeDataFlavor() {
        return DragPanelDataFlavors.conceptFlavor;
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
    public RefexVersionBI<?> getThingToDrag() {
        return thingToDrag;
    }

    public RefexVersionBI<?> getRefexV() {
        return thingToDrag;
    }

    public ComponentVersionBI getDraggedThing() {
        return thingToDrag;
    }

    public void setDraggedThing(ComponentVersionBI component) {
        // handle drop...;
    }

    public final void layoutExtension() throws IOException, TerminologyException {
        setLayout(new GridBagLayout());
        boolean canDrop = false;
        TerminologyStoreDI ts = Ts.get();
        if (getRefexV().getTime() == Long.MAX_VALUE) {
            setOpaque(true);
            setBackground(Color.YELLOW);
            canDrop = true;
        }

        setBorder(BorderFactory.createRaisedBevelBorder());
        extensionLabel = getJLabel(" ");
        extensionLabel.setBackground(Color.RED);
        extensionLabel.setOpaque(true);
        setDropPopupInset(extensionLabel.getPreferredSize().width);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(extensionLabel, gbc);
        gbc.weightx = 1;
       
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx++;
        TermComponentLabel typeLabel = getLabel(getRefexV().getCollectionNid(), canDrop);
        add(typeLabel, gbc);
        gbc.gridx++;
        add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.weightx = 1;
        gbc.gridx++;

        boolean classFound = false;
        if (RefexCnidVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
            int cnid = ((RefexCnidVersionBI) getRefexV()).getCnid1();
            TermComponentLabel ext = getLabel(cnid, canDrop);
            ext.setOpaque(false);
            ext.setBackground(getBackground());
            add(ext, gbc);
            gbc.gridx++;
            classFound = true;
        }
        if (RefexCnidCnidVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
            int cnid = ((RefexCnidCnidVersionBI) getRefexV()).getCnid1();
            TermComponentLabel ext = getLabel(cnid, canDrop);
            ext.setOpaque(false);
            ext.setBackground(getBackground());
            add(ext, gbc);
            gbc.gridx++;
            classFound = true;
        }
        if (RefexCnidCnidCnidVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
            int cnid = ((RefexCnidCnidCnidVersionBI) getRefexV()).getCnid1();
            TermComponentLabel ext = getLabel(cnid, canDrop);
            ext.setOpaque(false);
            ext.setBackground(getBackground());
            add(ext, gbc);
            gbc.gridx++;
            classFound = true;
        }

        if (RefexStrVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
            String text = ((RefexStrVersionBI) getRefexV()).getStr1();
            FixedWidthJEditorPane textPane = new FixedWidthJEditorPane();
            textPane.setEditable(canDrop);
            textPane.setOpaque(false);
            textPane.setBackground(getBackground());
            textPane.setFont(textPane.getFont().deriveFont(getSettings().getFontSize()));
            textPane.setText(text);
            add(textPane, gbc);
            gbc.gridx++;
            classFound = true;
        }

        if (RefexIntVersionBI.class.isAssignableFrom(getRefexV().getClass())) {
            int value = ((RefexIntVersionBI) getRefexV()).getInt1();
            JLabel valueLabel = new JLabel(Integer.toString(value));
            valueLabel.setOpaque(false);
            valueLabel.setBackground(getBackground());
            valueLabel.setFont(valueLabel.getFont().deriveFont(getSettings().getFontSize()));
            valueLabel.setText(Integer.toString(value));
            add(valueLabel, gbc);
            gbc.gridx++;
            classFound = true;
        }
        if (!classFound) {
            FixedWidthJEditorPane textPane = new FixedWidthJEditorPane();
            textPane.setEditable(canDrop);
            textPane.setOpaque(false);
            textPane.setBackground(getBackground());
            textPane.setFont(textPane.getFont().deriveFont(getSettings().getFontSize()));
            textPane.setText(getRefexV().toUserString());
            add(textPane, gbc);
            gbc.gridx++;
            classFound = true;
        }
        
        gbc.weightx = 0;
        gbc.gridx++;

        add(getComponentActionMenuButton(), gbc);

        gbc.gridx++;


        JButton collapseExpandButton = getCollapseExpandButton();
        add(collapseExpandButton, gbc);
        addSubPanels(gbc);
    }

    void setInactiveBackground() {
        extensionLabel.setBackground(extensionLabel.getBackground().darker());
    }
}
