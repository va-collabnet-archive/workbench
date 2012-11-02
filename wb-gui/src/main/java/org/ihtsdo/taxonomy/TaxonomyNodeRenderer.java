
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.I_RenderAndFocusOnBean;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.bind.ThinVersionHelper.MAX_VALUE_TYPE;
import org.dwfa.vodb.types.IntSet;

import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;

import sun.swing.DefaultLookup;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.TreeCellRenderer;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author kec
 */
public class TaxonomyNodeRenderer extends JLabel
        implements TreeCellRenderer, PropertyChangeListener, I_RenderAndFocusOnBean {

    protected static final int EXTRA_PARENT_WIDTH = 17;
    private static final long serialVersionUID = 1L;
    //~--- fields --------------------------------------------------------------
    private Icon closedIcon = null;
    private boolean drawsFocusBorderAroundIcon = false;
    private int labelStart = 0;
    private IntSet viewerImageTypes = new IntSet();
    private DescTypeToRender typeToRender = DescTypeToRender.FSN;
    private boolean selected = false;
    private I_ConfigAceFrame aceConfig;
    private Color backgroundNonSelectionColor;
    private Color backgroundSelectionColor;
    private Color borderSelectionColor;
    private boolean drawDashedFocusIndicator;
    /**
     * Color to draw the focus indicator in, determined from the background. color.
     */
    private Color focusBGColor;
    private I_GetConceptData focusBean;
    private boolean hasFocus;
    private TaxonomyHelper helper;
    private Boolean highlightConflictsInTaxonomyView;
    private Icon leafIcon;
    private Icon openIcon;
    private I_IntList refsetsToShow;
    private boolean showPathInfoInTaxonomy;
    private boolean showRefsetInfoInTaxonomy;
    private boolean showViewerImagesInTaxonomy;
    private final Color textNonSelectionColor;
    private final Color textSelectionColor;
    /**
     * Background color of the tree.
     */
    private Color treeBGColor;
    private int viewerImageTypeNid;

    ViewCoordinate getViewCoordinate() {
        return this.aceConfig.getViewCoordinate();
    }

    //~--- constant enums ------------------------------------------------------
    protected enum DescTypeToRender {

        PREFERRED, FSN
    }

    //~--- constructors --------------------------------------------------------
    public TaxonomyNodeRenderer(I_ConfigAceFrame aceConfig, TaxonomyHelper helper) throws IOException {
        super();
        this.aceConfig = aceConfig;
        this.helper = helper;
        showViewerImagesInTaxonomy = this.aceConfig.getShowViewerImagesInTaxonomy();
        showRefsetInfoInTaxonomy = this.aceConfig.getShowRefsetInfoInTaxonomy();
        showPathInfoInTaxonomy = this.aceConfig.getShowPathInfoInTaxonomy();
        highlightConflictsInTaxonomyView = this.aceConfig.getHighlightConflictsInTaxonomyView();
        this.aceConfig.addPropertyChangeListener(this);
        refsetsToShow = this.aceConfig.getRefsetsToShowInTaxonomy();
        backgroundSelectionColor = DefaultLookup.getColor(this, ui, "Tree.selectionBackground");
        backgroundNonSelectionColor = DefaultLookup.getColor(this, ui, "Tree.textBackground");
        borderSelectionColor = DefaultLookup.getColor(this, ui, "Tree.selectionBorderColor");
        textSelectionColor = DefaultLookup.getColor(this, ui, "Tree.selectionForeground");
        textNonSelectionColor = DefaultLookup.getColor(this, ui, "Tree.textForeground");

        Object value = UIManager.get("Tree.drawDashedFocusIndicator");

        drawDashedFocusIndicator = ((value != null) && ((Boolean) value).booleanValue());

        ConceptChronicleBI viewerImageType =
                Ts.get().getConcept(ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getUids());

        viewerImageTypes.add(viewerImageType.getConceptNid());

        try {
            viewerImageTypeNid = ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.localize().getNid();
        } catch (TerminologyException ex) {
            throw new IOException(ex);
        }
    }

    //~--- enums ---------------------------------------------------------------
    public enum NodeIcon {

        PRIMITIVE_SINGLE_PARENT(new ImageIcon(ACE.class.getResource("/16x16/plain/taxonomy/primitive-single-parent.png"))),
        PRIMITIVE_MULTI_PARENT_CLOSED(new ImageIcon(ACE.class.getResource("/16x16/plain/taxonomy/primitive-multi-parent-closed.png"))),
        PRIMITIVE_MULTI_PARENT_OPEN(new ImageIcon(ACE.class.getResource("/16x16/plain/taxonomy/primitive-multi-parent-opened.png"))),
        DEFINED_SINGLE_PARENT(new ImageIcon(ACE.class.getResource("/16x16/plain/taxonomy/defined-single-parent.png"))),
        DEFINED_MULTI_PARENT_CLOSED(new ImageIcon(ACE.class.getResource("/16x16/plain/taxonomy/defined-multi-parent-closed.png"))),
        DEFINED_MULTI_PARENT_OPEN(new ImageIcon(ACE.class.getResource("/16x16/plain/taxonomy/defined-multi-parent-opened.png"))),
        MULTI_PARENT_ROOT(new ImageIcon(ACE.class.getResource("/16x16/plain/pin_green.png")));
        ImageIcon icon;

        //~--- constructors -----------------------------------------------------
        private NodeIcon(ImageIcon icon) {
            this.icon = icon;
        }
    }

    //~--- methods -------------------------------------------------------------
    private void addChildrenToolTipText(ConceptVersionBI cv) throws IOException {
        StringBuilder toolTipText = new StringBuilder();

        toolTipText.append("<html>");

        int originCount = 0;

        for (PositionBI child : Ts.get().getPath(cv.getConceptNid()).getOrigins()) {
            originCount++;
            toolTipText.append("<font color=blue>origin:</font> ");
            toolTipText.append(child.toString());
            toolTipText.append("<br>");
        }

        if (originCount == 0) {
            toolTipText.append("no origins<br><br>");
        } else {
            toolTipText.append("<br>");
        }

        int childCount = 0;

        for (PathBI child : Ts.get().getPathChildren(cv.getConceptNid())) {
            childCount++;
            toolTipText.append("<font color=green>child:</font> ");
            toolTipText.append(child.toString());
            toolTipText.append("<br>");
        }

        if (childCount == 0) {
            toolTipText.append("no children");
        }

        this.setToolTipText(toolTipText.toString());
    }

    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
    }

    @Override
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {

        // Strings get interned...
        if ((propertyName == "text")
                || (((propertyName == "font") || (propertyName == "foreground")) && (oldValue != newValue)
                && (getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null))) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    @Override
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {
    }

    @Override
    public void invalidate() {
    }

    /**
     * Paints the value. The background is filled based on selected.
     */
    @Override
    public void paint(Graphics g) {
        Color bColor;

        if (selected) {
            bColor = backgroundSelectionColor;
        } else {
            bColor = backgroundNonSelectionColor;

            if (bColor == null) {
                bColor = getBackground();
            }
        }

        if (bColor != null) {
            g.setColor(bColor);

            if (getComponentOrientation().isLeftToRight()) {
                g.fillRect(labelStart, 0, getWidth() - labelStart, getHeight());
            } else {
                g.fillRect(0, 0, getWidth() - labelStart, getHeight());
            }
        }

        if (hasFocus) {
            if (getComponentOrientation().isLeftToRight()) {
                paintFocus(g, labelStart, 0, getWidth() - labelStart, getHeight());
            } else {
                paintFocus(g, 0, 0, getWidth() - labelStart, getHeight());
            }
        }

        super.paint(g);
    }

    private void paintFocus(Graphics g, int x, int y, int w, int h) {
        Color bsColor = borderSelectionColor;

        if ((bsColor != null) && (selected || !drawDashedFocusIndicator)) {
            g.setColor(bsColor);
            g.drawRect(x, y, w - 1, h - 1);
        }

        if (drawDashedFocusIndicator) {
            Color color;

            if (selected) {
                color = backgroundSelectionColor;
            } else {
                color = backgroundNonSelectionColor;

                if (color == null) {
                    color = getBackground();
                }
            }

            if (treeBGColor != color) {
                treeBGColor = color;
                focusBGColor = new Color(~color.getRGB());
            }

            g.setColor(focusBGColor);
            BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("showRefsetInfoInTaxonomy")) {
            showRefsetInfoInTaxonomy = aceConfig.getShowRefsetInfoInTaxonomy();
        } else if (evt.getPropertyName().equals("highlightConflictsInTaxonomyView")) {
            highlightConflictsInTaxonomyView = aceConfig.getHighlightConflictsInTaxonomyView();
        } else if (evt.getPropertyName().equals("showViewerImagesInTaxonomy")) {
            showViewerImagesInTaxonomy = aceConfig.getShowViewerImagesInTaxonomy();
        } else if (evt.getPropertyName().equals("refsetsToShow")) {
            refsetsToShow = this.aceConfig.getRefsetsToShowInTaxonomy();
        } else if (evt.getPropertyName().equals("showPathInfoInTaxonomy")) {
            showPathInfoInTaxonomy = this.aceConfig.getShowPathInfoInTaxonomy();
        }
    }

    @Override
    public void repaint() {
    }

    @Override
    public void repaint(Rectangle r) {
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    @Override
    public void revalidate() {
    }

    public void setupTaxonomyNode(TaxonomyNode node, ConceptVersionBI cv) throws IOException {
        List<String> htmlPrefixes = new ArrayList<String>();
        List<String> htmlSuffixes = new ArrayList<String>();
        boolean defined = false;
        try {
            if (cv.getConceptAttributesActive() != null) {
                defined = cv.getConceptAttributesActive().isDefined();
            }
        } catch (ContradictionException ex) {
            defined = cv.getConceptAttributes().getVersions(cv.getViewCoordinate()).iterator().next().isDefined();
        }
        Set<Color> colors = new HashSet<Color>();
        for (int sapNid : cv.getAllStampNids()) {
            colors.add(aceConfig.getColorForPath(Ts.get().getPathNidForStampNid(sapNid)));
        }
        List<Color> pathColors = new ArrayList<Color>(colors);
        node.setPathColors(pathColors);
        if (showViewerImagesInTaxonomy) {
            try {
                for (MediaVersionBI media : cv.getMediaActive()) {
                    if (media.getTypeNid() == viewerImageTypeNid) {
                        htmlPrefixes.add("<img src='ace:" + media.getNid() + "$" + media.getConceptNid()
                                + "' align=center>");
                    }
                }
            } catch (ContradictionException ex) {
                htmlPrefixes.add("media in conflict");
            }
        }

        if (showPathInfoInTaxonomy) {
            if ((aceConfig.getClassificationRoot() != null)
                    && (aceConfig.getClassificationRoot().getConceptNid() == node.getConceptNid())) {
                htmlSuffixes.add("<font color='#CC3300'>&nbsp;[Classification Root]</font>");
            }

            if ((aceConfig.getClassificationRoleRoot() != null)
                    && (aceConfig.getClassificationRoleRoot().getConceptNid() == node.getConceptNid())) {
                htmlSuffixes.add("<font color='#CC3300'>&nbsp;[Classifier Role Root]</font>");
            }

            showPathInfoInTaxonomy(cv, htmlSuffixes);
        }

        if (showRefsetInfoInTaxonomy) {
            showRefsetInfoInTaxonomy(cv, htmlPrefixes, htmlSuffixes);
        }

        StringBuilder buff = new StringBuilder();
        String conceptDesc;

        switch (typeToRender) {
            case FSN:
                try {
                    DescriptionVersionBI desc = cv.getDescriptionFullySpecified();

                    if (desc != null) {
                        conceptDesc = desc.getText();
                        node.setSortComparable(conceptDesc.toLowerCase());
                    } else {
                        conceptDesc = "no fsn";
                        node.setSortComparable(conceptDesc.toLowerCase());
                    }
                } catch (ContradictionException ex) {
                    conceptDesc = cv.getDescriptionsFullySpecifiedActive().iterator().next().getText();
                    node.setSortComparable(conceptDesc.toLowerCase());
                }

                break;

            case PREFERRED:
                try {
                    DescriptionVersionBI desc = cv.getDescriptionPreferred();

                    if (desc != null) {
                        conceptDesc = desc.getText();
                        node.setSortComparable(conceptDesc.toLowerCase());
                    } else {
                        conceptDesc = "no fsn";
                        node.setSortComparable(conceptDesc.toLowerCase());
                    }

                    node.setSortComparable(conceptDesc.toLowerCase());
                } catch (ContradictionException ex) {
                    conceptDesc = cv.getDescriptionsPreferredActive().iterator().next().getText();
                    node.setSortComparable(conceptDesc.toLowerCase());
                }

                break;

            default:
                throw new UnsupportedOperationException("Can't handle: " + typeToRender);
        }

        if (conceptDesc.toLowerCase().startsWith("<html>")) {
            conceptDesc = conceptDesc.substring(5);
        }

        if ((htmlPrefixes.size() > 0) || (htmlSuffixes.size() > 0)) {
            buff.append("<html>");

            for (String prefix : htmlPrefixes) {
                buff.append(prefix);
            }

            String text = conceptDesc;

            if (text.toLowerCase().startsWith("<html>")) {
                buff.append(text.substring(5));
            } else {
                buff.append(text);
            }

            for (String suffix : htmlSuffixes) {
                buff.append(suffix);
            }
        } else {
            buff.append(conceptDesc);
        }

        node.setText(buff.toString());

        if (defined) {
            node.setIcon(NodeIcon.DEFINED_SINGLE_PARENT);
        } else {
            node.setIcon(NodeIcon.PRIMITIVE_SINGLE_PARENT);
        }
        Rectangle iconRect = getIconRect(node.getParentDepth());

        if (node.hasExtraParents() && !node.isSecondaryParentNode()) {
            if (node.isSecondaryParentOpened()) {
                if (defined) {
                    node.setIcon(NodeIcon.DEFINED_MULTI_PARENT_OPEN);
                } else {
                    node.setIcon(NodeIcon.PRIMITIVE_MULTI_PARENT_OPEN);
                }

            } else {
                if (defined) {
                    node.setIcon(NodeIcon.DEFINED_MULTI_PARENT_CLOSED);
                } else {
                    node.setIcon(NodeIcon.PRIMITIVE_MULTI_PARENT_CLOSED);
                }
            }

            this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                    BorderFactory.createMatteBorder(0, iconRect.x, 0, 0, backgroundNonSelectionColor)));
        } else {
            if (node.isSecondaryParentNode()) {
                this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                        BorderFactory.createMatteBorder(0, iconRect.x, 0, 0, backgroundNonSelectionColor)));

                if (cv.getRelationshipsOutgoingTargetNidsActiveIsa().length == 0) {
                    node.setIcon(NodeIcon.MULTI_PARENT_ROOT);
                } else {
                    if (node.isSecondaryParentOpened()) {
                        if (defined) {
                            node.setIcon(NodeIcon.DEFINED_MULTI_PARENT_OPEN);
                        } else {
                            node.setIcon(NodeIcon.PRIMITIVE_MULTI_PARENT_OPEN);
                        }
                    } else {
                        if (defined) {
                            node.setIcon(NodeIcon.DEFINED_MULTI_PARENT_CLOSED);
                        } else {
                            node.setIcon(NodeIcon.PRIMITIVE_MULTI_PARENT_CLOSED);
                        }
                    }
                }
            }
        }
    }

    private void showPathInfoInTaxonomy(ConceptVersionBI cv, List<String> htmlSuffixes) throws IOException {
        if (Ts.get().hasPath(cv.getConceptNid())) {
            addChildrenToolTipText(cv);

            PositionBI latestInheritedViewPosition = null;

            for (PathBI editPath : aceConfig.getEditingPathSet()) {
                if (editPath.getConceptNid() == cv.getConceptNid()) {
                    htmlSuffixes.add("<font color=red>&nbsp;[Editing]</font>");
                }
            }

            for (PathBI promotionPath : aceConfig.getPromotionPathSet()) {
                if (promotionPath.getConceptNid() == cv.getConceptNid()) {
                    htmlSuffixes.add("<font color='#669900'>&nbsp;[Promotion]</font>");
                }
            }

            for (PositionBI viewPosition : aceConfig.getViewPositionSet()) {
                if (viewPosition.getPath().getConceptNid() == cv.getConceptNid()) {
                    String version = ThinVersionHelper.format(viewPosition.getVersion(), MAX_VALUE_TYPE.LATEST);

                    htmlSuffixes.add("<font color='#007FAE'>&nbsp;[Viewing:" + version + "]</font>");
                }

                for (PositionBI origin : viewPosition.getPath().getNormalisedOrigins()) {
                    if (origin.getPath().getConceptNid() == cv.getConceptNid()) {
                        if ((latestInheritedViewPosition == null)
                                || (origin.getVersion() > latestInheritedViewPosition.getVersion())) {
                            latestInheritedViewPosition = origin;
                        }
                    }
                }
            }

            if (latestInheritedViewPosition != null) {
                String version = ThinVersionHelper.format(latestInheritedViewPosition.getVersion(),
                        MAX_VALUE_TYPE.LATEST);

                htmlSuffixes.add("<font color='#967F49'>&nbsp;[Inherited view:" + version + "]</font>");
            }
        } else {
            setToolTipText(null);
        }
    }

    private void showRefsetInfoInTaxonomy(ConceptVersionBI cv, List<String> htmlPrefixes,
            List<String> htmlSuffixes)
            throws IOException {
        Collection<RefexChronicleBI> extensions = new ArrayList<RefexChronicleBI>();

        extensions.addAll(cv.getRefexes());

        HashSet<Integer> refexAlreadyHandled = new HashSet<Integer>();

        for (int i : refsetsToShow.getListArray()) {
            for (RefexChronicleBI ebr : extensions) {
                if (!refexAlreadyHandled.contains(i)) {
                    if ((ebr != null) && (ebr.getRefexNid() == i)) {
                        if (ebr instanceof RefexBooleanVersionBI) {
                            for (RefexVersionBI t : ebr.getRefexesActive(cv.getViewCoordinate())) {
                                boolean extValue = ((RefexBooleanVersionBI) t).getBoolean1();

                                refexAlreadyHandled.add(i);

                                try {
                                    I_GetConceptData booleanImageBean =
                                            Terms.get().getConcept(
                                            RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.getUids());

                                    if (extValue) {
                                        booleanImageBean = Terms.get().getConcept(
                                                RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.getUids());
                                    }

                                    for (I_ImageTuple imageTuple :
                                            booleanImageBean.getImageTuples(aceConfig.getAllowedStatus(),
                                            viewerImageTypes, aceConfig.getViewPositionSetReadOnly(),
                                            aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy())) {
                                        htmlPrefixes.add("<img src='ace:" + imageTuple.getNid() + "$"
                                                + imageTuple.getConceptNid() + "' align=center>");
                                    }
                                } catch (TerminologyException e) {
                                    AceLog.getAppLog().alertAndLogException(e);
                                }
                            }
                        } else if (ebr instanceof RefexNidVersionBI) {
                            RefexNidVersionBI extVersion = (RefexNidVersionBI) ebr;
                            ConceptVersionBI ebrCb = Ts.get().getConceptVersion(cv.getViewCoordinate(),
                                    extVersion.getNid1());

                            refexAlreadyHandled.add(i);

                            try {
                                for (MediaChronicleBI imageTuple : ebrCb.getMediaActive()) {
                                    htmlPrefixes.add("<img src='ace:" + imageTuple.getNid() + "$"
                                            + imageTuple.getConceptNid() + "' align=center>");
                        }
                            } catch (ContradictionException ex) {
                                for (MediaChronicleBI imageTuple : ebrCb.getMedia()) {
                                    htmlPrefixes.add("<img src='ace:" + imageTuple.getNid() + "$"
                                            + imageTuple.getConceptNid() + "' align=center>");
                                }
                            }
                        } else if (ebr instanceof RefexIntVersionBI) {
                            int extValue = ((RefexIntVersionBI) ebr).getInt1();

                            htmlPrefixes.add("<font color=blue>&nbsp;" + extValue + "&nbsp;</font>");
                        } else if (ebr instanceof RefexStringVersionBI) {
                            String strExt = ((RefexStringVersionBI) ebr).getString1();

                            refexAlreadyHandled.add(i);
                            htmlSuffixes.add("<code><strong>" + strExt + "'</strong></code>");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void validate() {
    }

    //~--- get methods ---------------------------------------------------------
    public Icon getClosedIcon() {
        return closedIcon;
    }

    @Override
    public I_GetConceptData getFocusBean() {
        return focusBean;
    }

    @Override
    public Rectangle getIconRect(int parentDepth) {
        int indent = (EXTRA_PARENT_WIDTH * parentDepth);

        return new Rectangle(indent, 0, NodeIcon.DEFINED_MULTI_PARENT_OPEN.icon.getIconWidth(),
                NodeIcon.DEFINED_MULTI_PARENT_OPEN.icon.getIconHeight());
    }

    public Icon getLeafIcon() {
        return leafIcon;
    }

    public Icon getOpenIcon() {
        return openIcon;
    }

    public String getOrder(ConceptVersionBI cv) throws IOException {
        switch (typeToRender) {
            case FSN:
                try {
                    return cv.getDescriptionFullySpecified().getText() + '\u039A';
                } catch (ContradictionException ex) {
                    return cv.getDescriptionsFullySpecifiedActive().iterator().next().getText() + '\u039A';
                }
            case PREFERRED:
                try {
                    return cv.getDescriptionPreferred().getText() + '\u039A';
                } catch (ContradictionException ex) {
                    return cv.getDescriptionsPreferredActive().iterator().next().getText() + '\u039A';
                }
            default:
                throw new UnsupportedOperationException("Can't handle: " + typeToRender);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension retDimension = super.getPreferredSize();

        if (retDimension != null) {
            retDimension = new Dimension(retDimension.width + 3, retDimension.height);
        }

        return retDimension;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

        this.hasFocus = hasFocus;
        setText(stringValue);

        Color fg = null;
        JTree.DropLocation dropLocation = tree.getDropLocation();

        if ((dropLocation != null) && (dropLocation.getChildIndex() == -1)
                && (tree.getRowForPath(dropLocation.getPath()) == row)) {
            Color col = DefaultLookup.getColor(this, ui, "Tree.dropCellForeground");

            if (col != null) {
                fg = col;
            } else {
                fg = textSelectionColor;
            }
        } else if (sel) {
            fg = textSelectionColor;
        } else {
            fg = textNonSelectionColor;
        }

        setForeground(fg);

        Icon icon = null;

        if (leaf) {
            icon = getLeafIcon();
        } else if (expanded) {
            icon = getOpenIcon();
        } else {
            icon = getClosedIcon();
        }

        if (!tree.isEnabled()) {
            setEnabled(false);

            LookAndFeel laf = UIManager.getLookAndFeel();
            Icon disabledIcon = laf.getDisabledIcon(tree, icon);

            if (disabledIcon != null) {
                icon = disabledIcon;
            }

            setDisabledIcon(icon);
        } else {
            setEnabled(true);
            setIcon(icon);
        }

        setComponentOrientation(tree.getComponentOrientation());
        selected = sel;

        if (value instanceof TaxonomyNode) {
            TaxonomyNode node = (TaxonomyNode) value;

            labelStart = EXTRA_PARENT_WIDTH * node.getParentDepth();
            this.setBorder(BorderFactory.createEmptyBorder(1, labelStart, 1, 0));

            try {
                for (Color pathColor : node.getPathColors()) {
                    this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                            BorderFactory.createMatteBorder(0, 3, 0, 0, pathColor)));
                    this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                            BorderFactory.createEmptyBorder(0, 1, 0, 0)));
                }

                if (node.getIcon() != null) {
                    this.setIcon(node.getIcon().icon);
                } else {
                    this.setIcon(null);
                }
            } catch (IOException e) {
                this.setText(e.toString());
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

        return this;
    }
    
    public RelAssertionType getAssertionType(){
        return helper.getAssertionType();
    }

    //~--- set methods ---------------------------------------------------------
    public void setClosedIcon(Icon closedIcon) {
        this.closedIcon = closedIcon;
    }

    @Override
    public void setFocusBean(I_GetConceptData focusBean) {
        this.focusBean = focusBean;
    }

    public void setLeafIcon(Icon leafIcon) {
        this.leafIcon = leafIcon;
    }

    public void setOpenIcon(Icon openIcon) {
        this.openIcon = openIcon;
    }

    protected void setTypeToRender(DescTypeToRender typeToRender) {
        this.typeToRender = typeToRender;
    }
}
