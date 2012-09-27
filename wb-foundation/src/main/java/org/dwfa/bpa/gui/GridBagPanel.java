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
package org.dwfa.bpa.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.util.bean.PropertyChangeSupportWithPropagationId;

/**
 * @author kec
 * 
 */
public class GridBagPanel extends JPanel {
    private static Logger logger = Logger.getLogger(GridBagPanel.class.getName());

    public static class GridBagPanelConstraints extends GridBagConstraints {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public boolean showInLayout;
        public int layer;
        public int positionInLayer;

        /**
         * 
         */
        public GridBagPanelConstraints() {
            super();
        }

        /**
         * @param gridx
         * @param gridy
         * @param gridwidth
         * @param gridheight
         * @param weightx
         * @param weighty
         * @param anchor
         * @param fill
         * @param insets
         * @param ipadx
         * @param ipady
         */
        public GridBagPanelConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx,
                double weighty, int anchor, int fill, Insets insets, int ipadx, int ipady, boolean showInLayout,
                int layer, int positionInLayer) {
            super(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady);
            this.showInLayout = showInLayout;
            this.layer = layer;
            this.positionInLayer = positionInLayer;
        }

    }

    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 2485090515541759981L;

    private GridBagPanelConstraints gb = new GridBagPanelConstraints(0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 1, 1, true,
        JLayeredPane.DEFAULT_LAYER, 0);

    private String title;

    private boolean addedToLayout = false;

    private PropertyChangeSupport gridBagPropertySupport;

    private JInternalFrame internalFrame;

    private I_Workspace workspace;

    public GridBagPanel(String title, I_Workspace workspace) {
        this(new FlowLayout(), title, workspace);
    }

    /**
     * @param layout
     */
    public GridBagPanel(LayoutManager layout, String title, I_Workspace workspace) {
        super(layout);
        this.workspace = workspace;
        gridBagPropertySupport = new PropertyChangeSupportWithPropagationId(this);
        this.setTitle(title);
        this.setName(title);
        this.setOpaque(true);
    }

    /**
     * @see java.awt.Component#getMinimumSize()
     */
    public Dimension getMinimumSize() {
        return this.getPreferredSize();
    }

    public void addGridBagConstraintsListener(PropertyChangeListener listener) {
        this.gridBagPropertySupport.addPropertyChangeListener(listener);
    }

    public void removeGridBagConstraintsListener(PropertyChangeListener listener) {
        this.gridBagPropertySupport.removePropertyChangeListener(listener);
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle(String title) {
        String oldTitle = this.title;
        this.title = title;
        this.gridBagPropertySupport.firePropertyChange("title", oldTitle, this.title);
    }

    /**
     * @return Returns the visible.
     */
    public boolean isShownInLayout() {
        return this.gb.showInLayout;
    }

    /**
     * @param visible
     *            The visible to set.
     */
    public void setShowInLayout(boolean includedInLayout) {
        Boolean oldValue = new Boolean(this.gb.showInLayout);
        this.gb.showInLayout = includedInLayout;
        this.gridBagPropertySupport.firePropertyChange("showInLayout", oldValue, new Boolean(includedInLayout));
    }

    public String toString() {
        return this.title;
    }

    public GridBagPanelConstraints getConstraints() {
        if (this.getInternalFrame() != null) {
            JInternalFrame intFrm = this.getInternalFrame();
            if ((intFrm.getParent() != null) && (JLayeredPane.class.isAssignableFrom(intFrm.getParent().getClass()))) {
                JLayeredPane parent = (JLayeredPane) intFrm.getParent();
                this.gb.layer = JLayeredPane.getLayer(intFrm);
                this.gb.positionInLayer = parent.getPosition(intFrm);
            }
        } else {
            if ((this.getParent() != null) && (JLayeredPane.class.isAssignableFrom(this.getParent().getClass()))) {
                JLayeredPane parent = (JLayeredPane) this.getParent();
                this.gb.layer = JLayeredPane.getLayer(this);
                this.gb.positionInLayer = parent.getPosition(this);
            }
        }
        return new GridBagPanelConstraints(this.gb.gridx, this.gb.gridy, this.gb.gridwidth, this.gb.gridheight,
            this.gb.weightx, this.gb.weighty, this.gb.anchor, this.gb.fill, this.gb.insets, this.gb.ipadx,
            this.gb.ipady, this.gb.showInLayout, this.gb.layer, this.gb.positionInLayer);

    }

    public void setConstraints(GridBagPanelConstraints gb) {
        if (this.gb.gridx != gb.gridx) {
            this.setGridx(gb.gridx);
        }
        if (this.gb.gridy != gb.gridy) {
            this.setGridy(gb.gridy);
        }
        if (this.gb.gridwidth != gb.gridwidth) {
            this.setGridwidth(gb.gridwidth);
        }
        if (this.gb.gridheight != gb.gridheight) {
            this.setGridheight(gb.gridheight);
        }
        if (this.gb.weightx != gb.weightx) {
            this.setWeightx(gb.weightx);
        }
        if (this.gb.weighty != gb.weighty) {
            this.setWeighty(gb.weighty);
        }
        if (this.gb.weighty != gb.weighty) {
            this.setWeighty(gb.weighty);
        }
        if (this.gb.anchor != gb.anchor) {
            this.setAnchor(gb.anchor);
        }
        if (this.gb.fill != gb.fill) {
            this.setFill(gb.fill);
        }
        if (this.gb.insets.equals(gb.insets) == false) {
            this.setInsets(gb.insets);
        }
        if (this.gb.ipadx != gb.ipadx) {
            this.setIpadx(gb.ipadx);
        }
        if (this.gb.ipady != gb.ipady) {
            this.setIpadx(gb.ipady);
        }
        if (this.gb.showInLayout != gb.showInLayout) {
            this.setShowInLayout(gb.showInLayout);
        }
        if ((this.gb.layer != gb.layer) || (this.gb.positionInLayer != gb.positionInLayer)) {
            this.gb.layer = gb.layer;
            this.gb.positionInLayer = gb.positionInLayer;
            if (this.getInternalFrame() != null) {
                JInternalFrame intFrm = this.getInternalFrame();
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("setConstraints internalFrame != null");
                }
                if ((intFrm.getParent() != null)
                    && (JLayeredPane.class.isAssignableFrom(intFrm.getParent().getClass()))) {
                    JLayeredPane parent = (JLayeredPane) intFrm.getParent();
                    parent.setLayer(intFrm, this.gb.layer, this.gb.positionInLayer);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("setConstraints set layer and position.");
                    }
                } else {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("setConstraints failed to set layer and position.");
                    }

                }
            } else {
                if ((this.getParent() != null) && (JLayeredPane.class.isAssignableFrom(this.getParent().getClass()))) {
                    JLayeredPane parent = (JLayeredPane) this.getParent();
                    parent.setLayer(this, this.gb.layer, this.gb.positionInLayer);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("setConstraints set layer and position (no internal frame).");
                    }
                }
            }
        }
    }

    /**
     * @return Returns the anchor.
     */
    public int getAnchor() {
        return this.gb.anchor;
    }

    /**
     * @param anchor
     *            The anchor to set.
     */
    public void setAnchor(int anchor) {
        Integer oldValue = new Integer(this.gb.anchor);
        this.gb.anchor = anchor;
        this.firePropertyChange("anchor", oldValue, new Integer(this.gb.anchor));
    }

    /**
     * @return Returns the fill.
     */
    public int getFill() {
        return this.gb.fill;
    }

    /**
     * @param fill
     *            The fill to set.
     */
    public void setFill(int fill) {
        Integer oldValue = new Integer(this.gb.fill);
        this.gb.fill = fill;
        this.gridBagPropertySupport.firePropertyChange("fill", oldValue, new Integer(this.gb.fill));
    }

    /**
     * @return Returns the gridheight.
     */
    public int getGridheight() {
        return this.gb.gridheight;
    }

    /**
     * @param gridheight
     *            The gridheight to set.
     */
    public void setGridheight(int gridheight) {
        Integer oldValue = new Integer(this.gb.gridheight);
        this.gb.gridheight = gridheight;
        this.gridBagPropertySupport.firePropertyChange("gridheight", oldValue, new Integer(this.gb.gridheight));
    }

    /**
     * @return Returns the gridwidth.
     */
    public int getGridwidth() {
        return this.gb.gridwidth;
    }

    /**
     * @param gridwidth
     *            The gridwidth to set.
     */
    public void setGridwidth(int gridwidth) {
        Integer oldValue = new Integer(this.gb.gridwidth);
        this.gb.gridwidth = gridwidth;
        this.gridBagPropertySupport.firePropertyChange("gridwidth", oldValue, new Integer(this.gb.gridwidth));
    }

    /**
     * @return Returns the gridx.
     */
    public int getGridx() {
        return this.gb.gridx;
    }

    /**
     * @param gridx
     *            The gridx to set.
     */
    public void setGridx(int gridx) {
        Integer oldValue = new Integer(this.gb.gridx);
        this.gb.gridx = gridx;
        this.gridBagPropertySupport.firePropertyChange("gridx", oldValue, new Integer(this.gb.gridx));
    }

    /**
     * @return Returns the gridy.
     */
    public int getGridy() {
        return this.gb.gridy;
    }

    /**
     * @param gridy
     *            The gridy to set.
     */
    public void setGridy(int gridy) {
        Integer oldValue = new Integer(this.gb.gridy);
        this.gb.gridy = gridy;
        this.gridBagPropertySupport.firePropertyChange("gridy", oldValue, new Integer(this.gb.gridy));
    }

    /**
     * @return Returns the insets.
     */
    public Insets getInsets() {
        return this.gb.insets;
    }

    /**
     * @param insets
     *            The insets to set.
     */
    public void setInsets(Insets insets) {
        Insets oldValue = this.gb.insets;
        this.gb.insets = insets;
        this.gridBagPropertySupport.firePropertyChange("insets", oldValue, insets);
    }

    /**
     * @return Returns the ipadx.
     */
    public int getIpadx() {
        return this.gb.ipadx;
    }

    /**
     * @param ipadx
     *            The ipadx to set.
     */
    public void setIpadx(int ipadx) {
        Integer oldValue = new Integer(this.gb.ipadx);
        this.gb.ipadx = ipadx;
        this.gridBagPropertySupport.firePropertyChange("ipadx", oldValue, new Integer(this.gb.ipadx));
    }

    /**
     * @return Returns the ipady.
     */
    public int getIpady() {
        return this.gb.ipady;
    }

    /**
     * @param ipady
     *            The ipady to set.
     */
    public void setIpady(int ipady) {
        Integer oldValue = new Integer(this.gb.ipady);
        this.gb.ipady = ipady;
        this.gridBagPropertySupport.firePropertyChange("ipady", oldValue, new Integer(this.gb.ipady));
    }

    /**
     * @return Returns the weightx.
     */
    public double getWeightx() {
        return this.gb.weightx;
    }

    /**
     * @param weightx
     *            The weightx to set.
     */
    public void setWeightx(double weightx) {
        Double oldValue = new Double(this.gb.weightx);
        this.gb.weightx = weightx;
        this.gridBagPropertySupport.firePropertyChange("weightx", oldValue, new Double(this.gb.weightx));
    }

    /**
     * @return Returns the weighty.
     */
    public double getWeighty() {
        return this.gb.weighty;
    }

    /**
     * @param weighty
     *            The weighty to set.
     */
    public void setWeighty(double weighty) {
        Double oldValue = new Double(this.gb.weighty);
        this.gb.weighty = weighty;
        this.gridBagPropertySupport.firePropertyChange("weighty", oldValue, new Double(this.gb.weighty));
    }

    /**
     * @return Returns the addedToLayout.
     */
    public boolean isAddedToLayout() {
        return addedToLayout;
    }

    /**
     * @param addedToLayout
     *            The addedToLayout to set.
     */
    public void setAddedToLayout(boolean addedToLayout) {
        this.addedToLayout = addedToLayout;
    }

    /**
     * @return Returns the internalFrame.
     */
    public JInternalFrame getInternalFrame() {
        return internalFrame;
    }

    /**
     * @param internalFrame
     *            The internalFrame to set.
     */
    public void setInternalFrame(JInternalFrame internalFrame) {
        this.internalFrame = internalFrame;
        if (this.internalFrame != null) {
            this.setBorder(null);
        }
    }

    public I_Workspace getWorkspace() {
        return workspace;
    }

    public Object getWsAttachement(String key) {
        return workspace.getAttachment(key);
    }

}
