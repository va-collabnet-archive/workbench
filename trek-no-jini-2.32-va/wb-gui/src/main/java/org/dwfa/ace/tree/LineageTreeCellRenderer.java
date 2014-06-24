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

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;

public class LineageTreeCellRenderer extends DefaultTreeCellRenderer {

    private static ImageIcon multiParentOpen = new ImageIcon(
        ACE.class.getResource("/16x16/plain/nav_up_right_green.png"));

    private static ImageIcon multiParentRoot = new ImageIcon(ACE.class.getResource("/16x16/plain/pin_green.png"));

    private static ImageIcon focusMultiParentOpen = new ImageIcon(
        ACE.class.getResource("/16x16/plain/nav_up_right_blue.png"));

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

    private I_GetConceptData focusBean;

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    public LineageTreeCellRenderer(I_ConfigAceFrame aceConfig) {
        super();
        this.aceConfig = aceConfig;
        setLeafIcon(null);
        setClosedIcon(null);
        setOpenIcon(null);
        Object value = UIManager.get("Tree.drawDashedFocusIndicator");
        drawDashedFocusIndicator = (value != null && ((Boolean) value).booleanValue());
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        setHorizontalTextPosition(JLabel.LEFT);
        setHorizontalAlignment(JLabel.RIGHT);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        try {
            if (node.getUserObject() != null) {
                if (I_GetConceptData.class.isAssignableFrom(node.getUserObject().getClass())) {
                    I_GetConceptData cb = (I_GetConceptData) node.getUserObject();
                    if (aceConfig.getHighlightConflictsInComponentPanel()
                        && aceConfig.getConflictResolutionStrategy().isInConflict(cb, true)) {
                        this.setForeground(Color.red);
                    } else {
                        this.setForeground(Color.black);
                    }
                    I_DescriptionTuple tdt = cb.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
                    if (tdt != null) {
                        setText(tdt.getText());
                    } else {
                        setText("null desc: " + cb.getInitialText());
                    }

                    if ((node.getParent() != null) && (node.getParent().getParent() == null)) {
                        this.setIcon(multiParentRoot);
                    } else {
                        if (cb.getConceptNid() == focusBean.getConceptNid()) {
                            this.setIcon(focusMultiParentOpen);
                        } else {
                            this.setIcon(multiParentOpen);
                        }
                    }
                } else {
                    this.setText(node.getUserObject().toString());
                }
            } else {
                this.setText("ROOT? (User object is null)");
            }

        } catch (Exception e) {
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
   @Override
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

    public I_GetConceptData getFocusBean() {
        return focusBean;
    }

    public void setFocusBean(I_GetConceptData focusBean) {
        this.focusBean = focusBean;
    }

}
