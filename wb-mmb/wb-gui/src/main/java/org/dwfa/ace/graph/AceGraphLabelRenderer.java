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
package org.dwfa.ace.graph;

/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 * Created on Apr 14, 2005
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.GraphLabelRenderer;

/**
 * DefaultGraphLabelRenderer is similar to the cell renderers
 * used by the JTable and JTree jfc classes.
 * 
 * @author Tom Nelson - RABA Technologies
 * 
 * 
 */

public class AceGraphLabelRenderer extends JLabel implements GraphLabelRenderer, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected static Border noFocusBorder = BorderFactory.createRaisedBevelBorder();

    protected Color pickedVertexLabelColor = Color.black;
    protected Color pickedEdgeLabelColor = Color.BLUE;
    protected boolean rotateEdgeLabels;

    public AceGraphLabelRenderer(Color pickedVertexLabelColor, Color pickedEdgeLabelColor) {
        this(pickedVertexLabelColor, pickedEdgeLabelColor, true);
    }

    /**
     * Creates a default table cell renderer.
     */
    public AceGraphLabelRenderer(Color pickedVertexLabelColor, Color pickedEdgeLabelColor, boolean rotateEdgeLabels) {
        super();
        this.pickedVertexLabelColor = pickedVertexLabelColor;
        this.pickedEdgeLabelColor = pickedEdgeLabelColor;
        this.rotateEdgeLabels = rotateEdgeLabels;
        setOpaque(true);
        setBorder(noFocusBorder);
    }

    /**
     * @return Returns the rotateEdgeLabels.
     */
    public boolean isRotateEdgeLabels() {
        return rotateEdgeLabels;
    }

    /**
     * @param rotateEdgeLabels The rotateEdgeLabels to set.
     */
    public void setRotateEdgeLabels(boolean rotateEdgeLabels) {
        this.rotateEdgeLabels = rotateEdgeLabels;
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign
     * the unselected-foreground color to the specified color.
     * 
     * @param c set the foreground color to this value
     */
    public void setForeground(Color c) {
        super.setForeground(c);
    }

    /**
     * Overrides <code>JComponent.setBackground</code> to assign
     * the unselected-background color to the specified color.
     * 
     * @param c set the background color to this value
     */
    public void setBackground(Color c) {
        super.setBackground(c);
    }

    /**
     * Notification from the <code>UIManager</code> that the look and feel
     * [L&F] has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     * 
     * @see JComponent#updateUI
     */
    public void updateUI() {
        super.updateUI();
        setForeground(null);
        setBackground(null);
    }

    /**
     * 
     * Returns the default label renderer for a Vertex
     * 
     * @param vv the <code>VisualizationViewer</code> to render on
     * @param value the value to assign to the label for <code>Vertex</code>
     * @param vertex the <code>Vertex</code>
     * @return the default label renderer
     */
    public Component getGraphLabelRendererComponent(JComponent vv, Object value, Font font, boolean isSelected,
            Vertex vertex) {

        super.setForeground(vv.getForeground());
        if (isSelected)
            setForeground(pickedVertexLabelColor);
        super.setBackground(vv.getBackground());
        if (font != null) {
            // setFont(vv.getFont());
            setFont(font);
        } else {
            setFont(vv.getFont());
        }
        setIcon(null);
        if (vertex.containsUserDatumKey("border")) {
            setBorder(BorderFactory.createCompoundBorder(noFocusBorder, (Border) vertex.getUserDatum("border")));
        } else {
            setBorder(noFocusBorder);
        }
        setValue(value);
        return this;
    }

    /**
     * 
     * Returns the default label renderer for an Edge
     * 
     * @param vv the <code>VisualizationViewer</code> to render on
     * @param value the value to assign to the label for <code>Edge</code>
     * @param edge the <code>Edge</code>
     * @return the default label renderer
     */
    public Component getGraphLabelRendererComponent(JComponent vv, Object value, Font font, boolean isSelected,
            Edge edge) {

        super.setForeground(vv.getForeground());
        if (isSelected)
            setForeground(pickedEdgeLabelColor);
        super.setBackground(vv.getBackground());

        if (font != null) {
            // setFont(vv.getFont());
            setFont(font);
        } else {
            setFont(vv.getFont());
        }
        setIcon(null);
        setBorder(noFocusBorder);
        setValue(value);
        return this;
    }

    /*
     * The following methods are overridden as a performance measure to
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary. Great care should be taken
     * when writing your own renderer to weigh the benefits and
     * drawbacks of overriding methods like these.
     */

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public boolean isOpaque() {
        /*
         * Color back = getBackground();
         * Component p = getParent();
         * if (p != null) {
         * p = p.getParent();
         * }
         * boolean colorMatch = (back != null) && (p != null) &&
         * back.equals(p.getBackground()) &&
         * p.isOpaque();
         * return !colorMatch && super.isOpaque();
         */
        return true;
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void validate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void revalidate() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void repaint(Rectangle r) {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName == "text") {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    /**
     * Sets the <code>String</code> object for the cell being rendered to
     * <code>value</code>.
     * 
     * @param value the string value for this cell; if value is
     *            <code>null</code> it sets the text value to an empty string
     * @see JLabel#setText
     * 
     */
    protected void setValue(Object value) {
        setText((value == null) ? "" : value.toString());
    }

}
