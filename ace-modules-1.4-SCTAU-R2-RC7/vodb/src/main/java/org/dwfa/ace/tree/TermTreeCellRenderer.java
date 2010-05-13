/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_OverrideTaxonomyRenderer;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.PathManager;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.bind.ThinVersionHelper.MAX_VALUE_TYPE;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;

public class TermTreeCellRenderer extends DefaultTreeCellRenderer implements PropertyChangeListener,
        I_RenderAndFocusOnBean {

    private static ImageIcon multiParentClosed = new ImageIcon(ACE.class.getResource("/16x16/plain/nav_up_green.png"));

    private static ImageIcon multiParentOpen = new ImageIcon(
        ACE.class.getResource("/16x16/plain/nav_up_right_green.png"));

    private static ImageIcon focusMultiParentOpen = new ImageIcon(
        ACE.class.getResource("/16x16/plain/nav_up_right_blue.png"));

    private static ImageIcon multiParentRoot = new ImageIcon(ACE.class.getResource("/16x16/plain/pin_green.png"));

    private I_ConfigAceFrame aceConfig;

    private boolean drawsFocusBorderAroundIcon = false;

    private boolean drawDashedFocusIndicator;

    // If drawDashedFocusIndicator is true, the following are used.
    /**
     * Background color of the tree.
     */
    private Color treeBGColor;

    /**
     * Color to draw the focus indicator in, determined from the background.
     * color.
     */
    private Color focusBGColor;

    private ConceptBean focusBean;

    private boolean showRefsetInfoInTaxonomy;

    @SuppressWarnings("unused")
    private boolean variableHeightTaxonomyView;

    private boolean showViewerImagesInTaxonomy;

    private IntSet viewerImageTypes = new IntSet();

    private I_IntList refsetsToShow;

    private Boolean highlightConflictsInTaxonomyView;

    private boolean showPathInfoInTaxonomy;

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    public TermTreeCellRenderer(I_ConfigAceFrame aceConfig) throws TerminologyException, IOException {
        super();
        this.aceConfig = aceConfig;
        showViewerImagesInTaxonomy = this.aceConfig.getShowViewerImagesInTaxonomy();
        variableHeightTaxonomyView = this.aceConfig.getVariableHeightTaxonomyView();
        showRefsetInfoInTaxonomy = this.aceConfig.getShowRefsetInfoInTaxonomy();
        showPathInfoInTaxonomy = this.aceConfig.getShowPathInfoInTaxonomy();
        highlightConflictsInTaxonomyView = this.aceConfig.getHighlightConflictsInTaxonomyView();
        this.aceConfig.addPropertyChangeListener(this);
        refsetsToShow = this.aceConfig.getRefsetsToShowInTaxonomy();
        setLeafIcon(null);
        setClosedIcon(null);
        setOpenIcon(null);
        Object value = UIManager.get("Tree.drawDashedFocusIndicator");
        drawDashedFocusIndicator = (value != null && ((Boolean) value).booleanValue());
        ConceptBean viewerImageType = ConceptBean.get(ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getUids());
        viewerImageTypes.add(viewerImageType.getConceptId());
    }

    @SuppressWarnings("deprecation")
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        this.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));

        try {
            if (node.getUserObject() != null) {
                if (I_GetConceptDataForTree.class.isAssignableFrom(node.getUserObject().getClass())) {
                    I_GetConceptDataForTree cb = (I_GetConceptDataForTree) node.getUserObject();
                    List<Color> pathColors = new ArrayList<Color>();
                    List<I_ConceptAttributeTuple> attributes = cb.getConceptAttributeTuples(
                        aceConfig.getAllowedStatus(), aceConfig.getViewPositionSet(), true);
                    for (I_ConceptAttributeTuple t : attributes) {
                        Color pathColor = aceConfig.getColorForPath(t.getPathId());
                        if (pathColor != null) {
                            pathColors.add(pathColor);
                        }
                    }
                    for (Color pathColor : pathColors) {
                        this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                            BorderFactory.createMatteBorder(0, 2, 0, 0, pathColor)));
                        this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                            BorderFactory.createEmptyBorder(0, 2, 0, 0)));
                    }

                   
                    Rectangle iconRect = getIconRect(cb.getParentDepth());
                    I_DescriptionTuple tdt = cb.getDescTuple(aceConfig);
                    if (tdt != null) {
                        List<String> htmlPrefixes = new ArrayList<String>();
                        List<String> htmlSuffixes = new ArrayList<String>();

                        if (highlightConflictsInTaxonomyView) {
                            if (aceConfig.getConflictResolutionStrategy().isInConflict(cb, true)) {
                                htmlPrefixes.add("<strong><em style=\"color:red\">");
                                htmlSuffixes.add("</em></strong>");
                            }
                        }

                        if (showViewerImagesInTaxonomy) {
                            for (I_ImageTuple imageTuple : cb.getImageTuples(aceConfig.getAllowedStatus(),
                                viewerImageTypes, aceConfig.getViewPositionSet())) {
                                htmlPrefixes.add("<img src='ace:" + imageTuple.getImageId() + "$"
                                    + imageTuple.getConceptId() + "' align=center>");
                            }
                        }

                        if (showPathInfoInTaxonomy) {
                            PathManager pathMan = new PathManager();
                            if (pathMan.exists(cb.getConceptId())) {
                                I_Position latestInheritedViewPosition = null;

                                for (I_Path editPath : aceConfig.getEditingPathSet()) {
                                    if (editPath.getConceptId() == cb.getConceptId()) {
                                        String colour = sel ? "white" : "red";
                                        htmlSuffixes.add("<font color=" + colour + ">&nbsp;[Editing]</font>");
                                    }
                                }

                                for (I_Position viewPosition : aceConfig.getViewPositionSet()) {
                                    if (viewPosition.getPath().getConceptId() == cb.getConceptId()) {
                                        String version = 
                                            ThinVersionHelper.format(viewPosition.getVersion(), MAX_VALUE_TYPE.LATEST);
                                        String colour = sel ? "white" : "#007FAE";
                                        htmlSuffixes.add(
                                            "<font color='" + colour + "'>&nbsp;[Viewing:" + version + "]</font>");
                                    }
                                    for (I_Position origin : viewPosition.getPath().getNormalisedOrigins()) {
                                        if (origin.getPath().getConceptId() == cb.getConceptId()) {
                                            if ((latestInheritedViewPosition == null)
                                                || (origin.getVersion() > latestInheritedViewPosition.getVersion())) {
                                                latestInheritedViewPosition = origin;
                                            }
                                        }
                                    }
                                }

                                if (latestInheritedViewPosition != null) {
                                    String version = 
                                        ThinVersionHelper.format(latestInheritedViewPosition.getVersion(), MAX_VALUE_TYPE.LATEST);
                                    String colour = sel ? "white" : "#967F49";
                                    htmlSuffixes.add(
                                        "<font color='" + colour + "'>&nbsp;[Inherited view:" + version + "]</font>");
                                }
                            }
                        }

                        if (showRefsetInfoInTaxonomy) {
                            List<I_GetExtensionData> extensions = 
                                AceConfig.getVodb().getExtensionsForComponent(cb.getConceptId());
                            
                            for (int i : refsetsToShow.getListArray()) {
                                for (I_GetExtensionData ext : extensions) {
                                    if (ext != null && ext.getExtension() != null
                                        && ext.getExtension().getRefsetId() == i) {
                                        I_ThinExtByRefVersioned ebr = ext.getExtension();
                                        List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
                                        switch (ThinExtBinder.getExtensionType(ebr)) {
                                        case BOOLEAN:
                                            ebr.addTuples(aceConfig.getAllowedStatus(), aceConfig.getViewPositionSet(),
                                                returnTuples, false);
                                            for (I_ThinExtByRefTuple t : returnTuples) {
                                                boolean extValue = ((I_ThinExtByRefPartBoolean) t.getPart()).getValue();

                                                try {
                                                    ConceptBean booleanImageBean = ConceptBean.get(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.getUids());
                                                    if (extValue) {
                                                        booleanImageBean = ConceptBean.get(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.getUids());
                                                    }
                                                    for (I_ImageTuple imageTuple : booleanImageBean.getImageTuples(
                                                        aceConfig.getAllowedStatus(), viewerImageTypes,
                                                        aceConfig.getViewPositionSet())) {
                                                        htmlPrefixes.add("<img src='ace:" + imageTuple.getImageId()
                                                            + "$" + imageTuple.getConceptId() + "' align=center>");
                                                    }
                                                } catch (TerminologyException e) {
                                                    AceLog.getAppLog().alertAndLogException(e);
                                                }
                                            }
                                            break;
                                        case CONCEPT:
                                            ebr.addTuples(aceConfig.getAllowedStatus(), aceConfig.getViewPositionSet(),
                                                returnTuples, false);
                                            for (I_ThinExtByRefTuple t : returnTuples) {
                                                ConceptBean ebrCb = ConceptBean.get(((I_ThinExtByRefPartConcept) t.getPart()).getConceptId());
                                                for (I_ImageTuple imageTuple : ebrCb.getImageTuples(
                                                    aceConfig.getAllowedStatus(), viewerImageTypes,
                                                    aceConfig.getViewPositionSet())) {
                                                    htmlPrefixes.add("<img src='ace:" + imageTuple.getImageId() + "$"
                                                        + imageTuple.getConceptId() + "' align=center>");
                                                }
                                            }
                                            break;
                                        case INTEGER:
                                            ebr.addTuples(aceConfig.getAllowedStatus(), aceConfig.getViewPositionSet(),
                                                returnTuples, false);
                                            for (I_ThinExtByRefTuple t : returnTuples) {
                                                int extValue = ((I_ThinExtByRefPartInteger) t.getPart()).getValue();
                                                htmlPrefixes.add("<font color=blue>&nbsp;" + extValue + "&nbsp;</font>");
                                            }
                                            break;
                                        case LANGUAGE:
                                            break;
                                        case MEASUREMENT:
                                            break;
                                        case SCOPED_LANGUAGE:
                                            break;
                                        case STRING:
                                            ebr.addTuples(aceConfig.getAllowedStatus(), aceConfig.getViewPositionSet(),
                                                returnTuples, false);
                                            for (I_ThinExtByRefTuple t : returnTuples) {
                                                String strExt = ((I_ThinExtByRefPartString) t.getPart()).getStringValue();
                                                htmlSuffixes.add("<code><strong>" + strExt + "'</strong></code>");
                                            }
                                            break;
                                        }

                                    }
                                }
                            }

                        }

                        if (aceConfig.getContext() != null) {
                            if (aceConfig.getContext().getConceptId() == cb.getConceptId()) {
                                String colour = sel ? "white" : "#967F49";
                                htmlSuffixes.add("<font color='" + colour + "'>&nbsp;[Working Context]</font>");
                            }
                        }
                        
                        StringBuffer buff = new StringBuffer();
                        if (htmlPrefixes.size() > 0 || htmlSuffixes.size() > 0) {
                            buff.append("<html>");
                            for (String prefix : htmlPrefixes) {
                                buff.append(prefix);
                            }
                            String text = cb.getDescTuple(aceConfig).getText();
                            if (text.toLowerCase().startsWith("<html>")) {
                                buff.append(text.substring(5));
                            } else {
                                buff.append(text);
                            }
                            // if (htmlSuffixes.size() > 0) {
                            // buff.append("<br>");
                            // }
                            for (String suffix : htmlSuffixes) {
                                buff.append(suffix);
                            }

                        } else {
                            buff.append(cb.getDescTuple(aceConfig).getText());
                        }

                        setText(buff.toString());

                    } else {
                        setText("null desc: " + cb.getInitialText());
                    }
                    ;
                    int sourceRelTupleSize = cb.getSourceRelTuples(aceConfig.getAllowedStatus(),
                        aceConfig.getDestRelTypes(), aceConfig.getViewPositionSet(), false).size();
                    if (sourceRelTupleSize > 1) {
                        if (cb.isParentOpened()) {
                            this.setIcon(multiParentOpen);
                        } else {
                            this.setIcon(multiParentClosed);
                        }
                        this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                            BorderFactory.createMatteBorder(0, iconRect.x, 0, 0, getBackgroundNonSelectionColor())));

                    } else {
                        if (cb.isSecondaryParentNode()) {
                            this.setBorder(BorderFactory.createCompoundBorder(this.getBorder(),
                                BorderFactory.createMatteBorder(0, iconRect.x, 0, 0, getBackgroundNonSelectionColor())));
                            if (sourceRelTupleSize == 0) {
                                this.setIcon(multiParentRoot);
                            } else {
                                if (cb.isParentOpened()) {
                                    if (focusBean != null && cb.getConceptId() == focusBean.getConceptId()) {
                                        this.setIcon(focusMultiParentOpen);
                                    } else {
                                        this.setIcon(multiParentOpen);
                                    }
                                } else {
                                    if (focusBean != null && cb.getConceptId() == focusBean.getConceptId()) {
                                        this.setIcon(focusMultiParentOpen);
                                    } else {
                                        this.setIcon(multiParentClosed);
                                    }
                                }
                            }
                        } else {
                            this.setIcon(null);
                        }
                    }
                    for (I_OverrideTaxonomyRenderer override : aceConfig.getTaxonomyRendererOverrideList()) {
                        override.overrideTreeCellRendererComponent(this, tree, cb, sel, expanded, leaf, row, hasFocus,
                            aceConfig);
                    }
                } else {
                    this.setIcon(null);
                    this.setText(node.getUserObject().toString());
                }
            } else {
                this.setText("ROOT? (User object is null)");
            }

        } catch (IOException e) {
            this.setText(e.toString());
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            this.setText(e.toString());
            AceLog.getAppLog().alertAndLogException(e);
        }
        return this;
    }

    public Rectangle getIconRect(int parentDepth) {
        int indent = (multiParentOpen.getIconWidth() * parentDepth);
        if (indent > 0) {
            indent = indent + 4;
        }
        return new Rectangle(indent, 0, multiParentOpen.getIconWidth(), multiParentOpen.getIconHeight());
    }

    /**
     * Paints the value. The background is filled based on selected.
     */
    public void paint(Graphics g) {
        Color bColor;

        if (selected) {
            bColor = getBackgroundSelectionColor();
        } else {
            bColor = getBackgroundNonSelectionColor();
            if (bColor == null)
                bColor = getBackground();
        }
        int imageOffset = -1;
        if (bColor != null) {

            imageOffset = getLabelStart();
            g.setColor(bColor);
            if (getComponentOrientation().isLeftToRight()) {
                g.fillRect(imageOffset, 0, getWidth() - imageOffset, getHeight());
            } else {
                g.fillRect(0, 0, getWidth() - imageOffset, getHeight());
            }
        }

        if (hasFocus) {
            if (drawsFocusBorderAroundIcon) {
                imageOffset = 0;
            } else if (imageOffset == -1) {
                imageOffset = getLabelStart();
            }
            if (getComponentOrientation().isLeftToRight()) {
                paintFocus(g, imageOffset, 0, getWidth() - imageOffset, getHeight());
            } else {
                paintFocus(g, 0, 0, getWidth() - imageOffset, getHeight());
            }
        }
        super.paint(g);
    }

    private void paintFocus(Graphics g, int x, int y, int w, int h) {
        Color bsColor = getBorderSelectionColor();

        if (bsColor != null && (selected || !drawDashedFocusIndicator)) {
            g.setColor(bsColor);
            g.drawRect(x, y, w - 1, h - 1);
        }
        if (drawDashedFocusIndicator) {
            Color color;
            if (selected) {
                color = getBackgroundSelectionColor();
            } else {
                color = getBackgroundNonSelectionColor();
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

    private int getLabelStart() {
        Icon currentI = getIcon();
        if (currentI != null && getText() != null) {
            if (getBorder() != null) {
                return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1)
                    + getBorder().getBorderInsets(this).left;
            }
            return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
        }
        return 0;
    }

    public ConceptBean getFocusBean() {
        return focusBean;
    }

    public void setFocusBean(ConceptBean focusBean) {
        this.focusBean = focusBean;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("showRefsetInfoInTaxonomy")) {
            showRefsetInfoInTaxonomy = aceConfig.getShowRefsetInfoInTaxonomy();
        } else if (evt.getPropertyName().equals("variableHeightTaxonomyView")) {
            variableHeightTaxonomyView = aceConfig.getVariableHeightTaxonomyView();
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

}
