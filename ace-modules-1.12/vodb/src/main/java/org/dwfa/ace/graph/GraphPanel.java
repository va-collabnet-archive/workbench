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
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.graph.GraphPlugin.GRAPH_LAYOUTS;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ConceptBean;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.ToStringLabeller;
import edu.uci.ics.jung.graph.decorators.StringLabeller.UniqueLabelException;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.utils.GraphUtils;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.contrib.DAGLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.LayoutScalingControl;
import edu.uci.ics.jung.visualization.control.LensMagnificationGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer;
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.MagnifyTransformer;
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.MagnifyShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.ViewLensSupport;

public class GraphPanel extends JPanel {

    private static class VertexRec {
        private int from;
        private int to;

        public VertexRec(int from, int to) {
            super();
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object obj) {
            VertexRec another = (VertexRec) obj;
            return this.from == another.from && this.to == another.to;
        }

        @Override
        public int hashCode() {
            return to;
        }

    }

    private class GraphLayoutAction implements ActionListener {
        GRAPH_LAYOUTS gl;

        public GraphLayoutAction(GRAPH_LAYOUTS gl) {
            super();
            this.gl = gl;
        }

        public void actionPerformed(ActionEvent evt) {
            parentPlugin.setGraphLayout(gl);
            try {
                parentPlugin.update();
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

    }

    private class GraphLayoutActionListener implements ActionListener {
        JPopupMenu graphLayoutPopup;

        public void actionPerformed(ActionEvent evt) {
            if (graphLayoutPopup == null) {
                graphLayoutPopup = new JPopupMenu();
                for (GRAPH_LAYOUTS gl : GRAPH_LAYOUTS.values()) {
                    JMenuItem layoutItem = new JMenuItem(gl.name());
                    layoutItem.addActionListener(new GraphLayoutAction(gl));
                    graphLayoutPopup.add(layoutItem);
                }
            }
            graphLayoutPopup.show((Component) evt.getSource(), 0, 0);
        }
    }

    public class LensActionListener implements ActionListener {
        JPopupMenu lensPopup;

        public void actionPerformed(ActionEvent evt) {
            if (lensPopup == null) {
                lensPopup = new JPopupMenu();
                JMenuItem noLens = new JMenuItem("no lens");
                noLens.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        if (hyperbolicViewSupport != null) {
                            hyperbolicViewSupport.deactivate();
                        }
                        if (hyperbolicLayoutSupport != null) {
                            hyperbolicLayoutSupport.deactivate();
                        }
                        if (magnifyViewSupport != null) {
                            magnifyViewSupport.deactivate();
                        }
                        if (magnifyLayoutSupport != null) {
                            magnifyLayoutSupport.deactivate();
                        }
                        scaler = crossoverScaler;
                    }

                });
                lensPopup.add(noLens);

                JMenuItem hyperbolicView = new JMenuItem("hyperbolic view");
                hyperbolicView.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        hyperbolicViewSupport.activate(true);
                        scaler = layoutScaler;
                    }

                });
                lensPopup.add(hyperbolicView);

                JMenuItem hyperbolicLayout = new JMenuItem("hyperbolic layout");
                hyperbolicLayout.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        hyperbolicLayoutSupport.activate(true);
                        scaler = layoutScaler;
                    }

                });
                lensPopup.add(hyperbolicLayout);

                JMenuItem magnifyView = new JMenuItem("magnify view");
                magnifyView.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        magnifyViewSupport.activate(true);
                        scaler = layoutScaler;
                    }

                });
                lensPopup.add(magnifyView);

                JMenuItem magnifyLayout = new JMenuItem("magnify layout");
                magnifyLayout.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        magnifyLayoutSupport.activate(true);
                        scaler = layoutScaler;
                    }

                });
                lensPopup.add(magnifyLayout);

            }

            lensPopup.show((Component) evt.getSource(), 0, 0);

        }

    }

    public class MouseModeActionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            JToggleButton modeToggle = (JToggleButton) evt.getSource();
            if (modeToggle.isSelected()) {
                graphMouse.setMode(Mode.PICKING);
            } else {
                graphMouse.setMode(Mode.TRANSFORMING);
            }

        }

    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
     * the graph
     */
    DirectedSparseGraph graph;

    Layout graphLayout;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer vv;

    /**
     * provides a Hyperbolic lens for the view
     */
    LensSupport hyperbolicViewSupport;
    /**
     * provides a magnification lens for the view
     */
    LensSupport magnifyViewSupport;

    /**
     * provides a Hyperbolic lens for the model
     */
    LensSupport hyperbolicLayoutSupport;
    /**
     * provides a magnification lens for the model
     */
    LensSupport magnifyLayoutSupport;

    ScalingControl scaler;

    private DefaultModalGraphMouse graphMouse;

    private CrossoverScalingControl crossoverScaler;

    private LayoutScalingControl layoutScaler;

    private GraphPlugin parentPlugin;

    /**
     * create an instance of a simple graph with controls to demo the zoom and
     * hyperbolic features.
     * 
     * @throws IOException
     * 
     */
    public GraphPanel(GraphPlugin.GRAPH_LAYOUTS layout, GraphPlugin parentPlugin, Dimension size) throws IOException {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.parentPlugin = parentPlugin;

        final PluggableRenderer graphRenderer = new PluggableRenderer();
        graphRenderer.setGraphLabelRenderer(new AceGraphLabelRenderer(Color.blue, Color.cyan));
        graphRenderer.setVertexLabelCentering(true);
        graphRenderer.setVertexShapeFunction(new AceVertexLabelShapeFunction());
        // Need to generate a rectangle here...

        this.graph = getLineageGraph(graphRenderer);

        graphLayout = new AceGraphLayout(graph);
        switch (layout) {
        case DAGLayout:
            graphLayout = new DAGLayout(graph);
            break;
        case FRLayout:
            graphLayout = new FRLayout(graph);
            ((FRLayout) graphLayout).setMaxIterations(5000);
            break;
        case ISOMLayout:
            graphLayout = new ISOMLayout(graph);
            break;
        case KKLayout:
            graphLayout = new KKLayout(graph);
            break;
        case SpringLayout:
            graphLayout = new SpringLayout(graph);
            break;
        case AceGraphLayout:
            graphLayout = new AceGraphLayout(graph);
            break;

        default:
            break;
        }

        Dimension preferredSize = new Dimension(size.width - 60, 600);

        final VisualizationModel visualizationModel = new DefaultVisualizationModel(graphLayout, preferredSize);
        vv = new VisualizationViewer(visualizationModel, graphRenderer, preferredSize);
        graphRenderer.setScreenDevice(vv);
        vv.setPickSupport(new ShapePickSupport());
        graphRenderer.setEdgeShapeFunction(new EdgeShape.QuadCurve());
        PickedState ps = vv.getPickedState();
        graphRenderer.setEdgePaintFunction(new PickableEdgePaintFunction(ps, Color.black, Color.red));
        vv.setBackground(Color.white);

        // add a listener for ToolTips
        vv.setToolTipFunction(new DefaultToolTipFunction());

        /**
         * the regular graph mouse for the normal view
         */
        graphMouse = new DefaultModalGraphMouse();

        vv.setGraphMouse(graphMouse);

        hyperbolicViewSupport = new ViewLensSupport(vv, new HyperbolicShapeTransformer(vv), new ModalLensGraphMouse());
        hyperbolicLayoutSupport = new LayoutLensSupport(vv, new HyperbolicTransformer(vv, vv.getLayoutTransformer()),
            new ModalLensGraphMouse());
        magnifyViewSupport = new ViewLensSupport(vv, new MagnifyShapeTransformer(vv), new ModalLensGraphMouse(
            new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));
        magnifyLayoutSupport = new LayoutLensSupport(vv, new MagnifyTransformer(vv, vv.getLayoutTransformer()),
            new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));
        hyperbolicLayoutSupport.getLensTransformer()
            .setEllipse(hyperbolicViewSupport.getLensTransformer().getEllipse());
        magnifyViewSupport.getLensTransformer().setEllipse(hyperbolicLayoutSupport.getLensTransformer().getEllipse());
        magnifyLayoutSupport.getLensTransformer().setEllipse(magnifyViewSupport.getLensTransformer().getEllipse());

        crossoverScaler = new CrossoverScalingControl();
        layoutScaler = new LayoutScalingControl();
        scaler = crossoverScaler;

        JButton plus = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/zoom_in.png")));
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/zoom_out.png")));
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1 / 1.1f, vv.getCenter());
            }
        });

        graphMouse.addItemListener(hyperbolicLayoutSupport.getGraphMouse().getModeListener());
        graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());
        graphMouse.addItemListener(magnifyLayoutSupport.getGraphMouse().getModeListener());
        graphMouse.addItemListener(magnifyViewSupport.getGraphMouse().getModeListener());

        JMenuBar menubar = new JMenuBar();
        menubar.add(graphMouse.getModeMenu());
        // gzsp.setCorner(menubar);

        JToggleButton selection = new JToggleButton(new ImageIcon(
            ACE.class.getResource("/24x24/plain/elements_selection.png")));
        selection.addActionListener(new MouseModeActionListener());

        JButton graphLayoutButton = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/text_formula.png")));
        graphLayoutButton.addActionListener(new GraphLayoutActionListener());

        JButton lens = new JButton(new ImageIcon(ACE.class.getResource("/24x24/plain/view.png")));
        lens.addActionListener(new LensActionListener());

        JPanel zoomControls = makeControlStrip(new JComponent[] { lens, plus, minus, selection, graphLayoutButton });
        add(zoomControls);

        // add(new GraphZoomScrollPane(vv));
        add(vv);

    }

    private JPanel makeControlStrip(JComponent[] components) {
        JPanel zoomControls = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        for (JComponent comp : components) {
            zoomControls.add(comp, gbc);
            gbc.gridy++;
        }
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.VERTICAL;

        zoomControls.add(new JPanel(), gbc);
        return zoomControls;
    }

    private DirectedSparseGraph getLineageGraph(PluggableRenderer r) throws IOException {

        ConceptBean bean = (ConceptBean) parentPlugin.getHost().getTermComponent();
        if (bean != null) {
            DirectedSparseGraph dag = new DirectedSparseGraph();
            HashSet<VertexRec> vertexRecs = new HashSet<VertexRec>();
            HashMap<Integer, Vertex> vertexMap = new HashMap<Integer, Vertex>();
            StringLabeller sl = ToStringLabeller.getLabeller(dag);
            r.setVertexStringer(sl);
            Vertex v1 = dag.addVertex(new SparseVertex());
            v1.addUserDatum("border", BorderFactory.createLineBorder(Color.BLUE, 2), UserData.SHARED);

            /*
             * if (vertex.containsUserDatumKey("shape")) {
             * vertex.setUserDatum("shape", this.getBounds(), UserData.SHARED);
             * }
             * else { vertex.addUserDatum("shape", this.getBounds(),
             * UserData.SHARED); }
             */
            vertexMap.put(bean.getConceptId(), v1);
            String labelText = getBeanLabelText(bean);
            try {
                sl.setLabel(v1, labelText);
            } catch (UniqueLabelException e) {
                e.printStackTrace();
            }
            addShapeToVertex(v1, labelText);

            addLineageToNode(dag, v1, bean, vertexMap, vertexRecs, sl, 0);
            return dag;
        }
        return null;

    }

    private void addShapeToVertex(Vertex vertex, String labelText) {
        Rectangle labelRect;
        int height = 16;
        if (BasicHTML.isHTMLString(labelText)) {
            View v = BasicHTML.createHTMLView(this, labelText);
            v.setSize(0, 16);
            float prefXSpan = v.getPreferredSpan(View.X_AXIS);
            if (prefXSpan < 60) {
                prefXSpan = 60;
            }
            int width = (int) prefXSpan;
            labelRect = new Rectangle(0 - (width / 2), 0 - (height / 2), width, height);
        } else {
            View v = BasicHTML.createHTMLView(this, "<html>" + labelText);
            v.setSize(0, 16);
            float prefXSpan = v.getPreferredSpan(View.X_AXIS);
            if (prefXSpan < 60) {
                prefXSpan = 60;
            }
            int width = (int) prefXSpan;
            labelRect = new Rectangle(0 - (width / 2), 0 - (height / 2), width, height);
        }
        if (vertex.containsUserDatumKey("shape")) {
            vertex.setUserDatum("shape", labelRect, UserData.SHARED);
        } else {
            vertex.addUserDatum("shape", labelRect, UserData.SHARED);
        }
    }

    private String getBeanLabelText(ConceptBean bean) throws IOException {
        return bean.getInitialText();
    }

    private void addLineageToNode(DirectedGraph dag, Vertex childVertex, ConceptBean child,
            HashMap<Integer, Vertex> vertexMap, HashSet<VertexRec> vertexRecs, StringLabeller sl, int depth)
            throws IOException {
        if (depth > 100) {
            return;
        }
        I_ConfigAceFrame config = parentPlugin.getHost().getConfig();
        List<I_RelTuple> sourceRelTuples = child.getSourceRelTuples(config.getAllowedStatus(),
            config.getDestRelTypes(), config.getViewPositionSet(), false);
        try {
            boolean root = true;
            for (I_RelTuple srcRelTuple : sourceRelTuples) {
                root = false;
                ConceptBean parent = ConceptBean.get(srcRelTuple.getC2Id());
                Vertex parentVertex = vertexMap.get(srcRelTuple.getC2Id());
                if (parentVertex == null) {
                    parentVertex = dag.addVertex(new SparseVertex());
                    vertexMap.put(srcRelTuple.getC2Id(), parentVertex);
                    String labelText = getBeanLabelText(parent);
                    try {
                        sl.setLabel(parentVertex, labelText);
                    } catch (UniqueLabelException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }
                    addShapeToVertex(parentVertex, labelText);
                }
                try {
                    if (vertexRecs.contains(new VertexRec(srcRelTuple.getC1Id(), srcRelTuple.getC2Id())) == false) {
                        vertexRecs.add(new VertexRec(srcRelTuple.getC1Id(), srcRelTuple.getC2Id()));
                        GraphUtils.addEdge(dag, childVertex, parentVertex);
                        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                            AceLog.getAppLog().info("Edge from: " + child + " to " + parent);
                            AceLog.getAppLog().info("Edge from: " + childVertex + " to " + parentVertex);
                        }
                    }
                } catch (RuntimeException e) {
                    AceLog.getAppLog().warning("Exception child: " + child + " parent: " + parent);
                    AceLog.getAppLog().warning("Exception child vertex: " + childVertex + " parent: " + parentVertex);
                    AceLog.getAppLog().alertAndLogException(e);
                }
                addLineageToNode(dag, parentVertex, parent, vertexMap, vertexRecs, sl, depth + 1);
            }

            if (root) {
                if (childVertex.containsUserDatumKey("border")) {
                    childVertex.setUserDatum("border", BorderFactory.createLineBorder(Color.green, 2), UserData.SHARED);
                } else {
                    childVertex.addUserDatum("border", BorderFactory.createLineBorder(Color.green, 2), UserData.SHARED);
                }
            }

        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

}
