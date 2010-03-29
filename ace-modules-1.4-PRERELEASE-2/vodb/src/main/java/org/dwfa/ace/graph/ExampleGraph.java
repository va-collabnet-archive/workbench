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

import javax.swing.JFrame;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.StringLabeller.UniqueLabelException;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.utils.GraphUtils;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.Renderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class ExampleGraph {

    public static void main(String[] args) {
        DirectedGraph dag = new DirectedSparseGraph();
        StringLabeller sl = StringLabeller.getLabeller(dag);
        Vertex v1 = dag.addVertex(new SparseVertex());
        Vertex v2 = dag.addVertex(new SparseVertex());
        Vertex v3 = dag.addVertex(new SparseVertex());
        Vertex v4 = dag.addVertex(new SparseVertex());
        Vertex v5 = dag.addVertex(new SparseVertex());
        Vertex v6 = dag.addVertex(new SparseVertex());
        try {
            sl.setLabel(v1, "V1");
            sl.setLabel(v2, "V2");
            sl.setLabel(v3, "V3");
            sl.setLabel(v4, "V4");
            sl.setLabel(v5, "V5");
            sl.setLabel(v6, "V6");
        } catch (UniqueLabelException e) {
            e.printStackTrace();
        }
        GraphUtils.addEdge(dag, v2, v1);
        GraphUtils.addEdge(dag, v3, v1);
        GraphUtils.addEdge(dag, v4, v1);
        GraphUtils.addEdge(dag, v5, v1);
        GraphUtils.addEdge(dag, v6, v1);

        Graph g = dag;
        Layout l = new FRLayout(g);
        Renderer r = new PluggableRenderer();
        VisualizationViewer vv = new VisualizationViewer(l, r);
        JFrame jf = new JFrame();
        jf.getContentPane().add(vv);
        jf.setVisible(true);
    }
}
