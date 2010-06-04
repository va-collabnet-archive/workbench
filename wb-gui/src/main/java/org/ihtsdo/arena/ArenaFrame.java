package org.ihtsdo.arena;

import java.awt.HeadlessException;
import java.util.EventObject;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.SwingConstants;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class ArenaFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private mxCell morphology;
    private mxCell opacity;

    private ArenaFrame() throws HeadlessException {
        super("The Arena");
        mxGraph graph = new mxGraph() {
            // Overrides method to disallow edge label editing
            public boolean isCellEditable(Object cell) {
                return !getModel().isEdge(cell);
            }

            // Overrides method to provide a cell label in the display
            public String convertValueToString(Object cell) {
                if (cell instanceof mxCell) {
                    Object value = ((mxCell) cell).getValue();
                    if (value != null) {
                        return value.toString();
                    }

                    }
                return super.convertValueToString(cell);
            }

            // Overrides method to store a cell label in the model
            public void cellLabelChanged(Object cell, Object newValue, boolean autoSize) {
                super.cellLabelChanged(cell, newValue, autoSize);
            }
        };

        mxCell parent = (mxCell) graph.getDefaultParent();
        

        mxStylesheet stylesheet = graph.getStylesheet();
        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_SWIMLANE);
        style.put(mxConstants.STYLE_OPACITY, 50);
        style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
        stylesheet.putCellStyle("SWIMLANE", style);
        
        style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_OPACITY, 75);
        style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
        stylesheet.putCellStyle("ROLE", style);
        
        style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_OPACITY, 75);
        style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
        stylesheet.putCellStyle("RESTRICTION", style);
        
        style = new Hashtable<String, Object>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_SWIMLANE);
        style.put(mxConstants.STYLE_OPACITY, 75);
        style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
        stylesheet.putCellStyle("REL", style);
        
        graph.getModel().beginUpdate();
        graph.setHtmlLabels(true);
        
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setOrientation(SwingConstants.WEST);
        layout.setInterHierarchySpacing(2);
        layout.setInterRankCellSpacing(5);

        try {
            mxCell concept = (mxCell) graph.insertVertex(parent, null, "<html>concept", 20, 20, 80, 30, "SWIMLANE");
            
            mxCell group1 = (mxCell) graph.insertVertex(concept, null, "<html>group 1", 20, 20, 80, 30, "SWIMLANE");
            
            graph.setSwimlaneNesting(true);
            graph.updateCellSize(group1);

            mxCell r1 = (mxCell) graph.insertVertex(group1, null, "rel1", 20, 20, 80, 30, "REL");
            mxCell site = (mxCell) graph.insertVertex(r1, null, "finding site", 20, 20, 80, 30, "ROLE");
            graph.updateCellSize(site);
            mxCell cornea = (mxCell) graph.insertVertex(r1, null, "cornea", 20, 20, 80, 30, "RESTRICTION");
            graph.insertEdge(r1, null, "", site, cornea);
            graph.updateCellSize(cornea);
            
            graph.updateCellSize(r1);
            layout.execute(r1);
            graph.updateGroupBounds(new Object[] { r1 });
            
            mxCell r2 = (mxCell) graph.insertVertex(group1, null, "rel2", 20, 20, 80, 30, "REL");
            morphology = (mxCell) graph.insertVertex(r2, null, "morphology", 20, 20, 80, 30);
            graph.updateCellSize(morphology);
            opacity = (mxCell) graph.insertVertex(r2, null, "opacity", 20, 20, 80, 30);
     
            graph.insertEdge(r2, null, "", morphology, opacity);
            graph.updateCellSize(opacity);
            graph.updateCellSize(r2);
            layout.execute(r2);
            graph.updateGroupBounds(new Object[] { r2 });
            

            layout.execute(group1);
            graph.updateGroupBounds(new Object[] { group1 });
            layout.execute(concept);
            graph.updateGroupBounds(new Object[] { concept });
        } finally {
            graph.getModel().endUpdate();
        }

        // Overrides method to create the editing value
        mxGraphComponent graphComponent = new mxGraphComponent(graph) {
            /**
             * 
             */
            private static final long serialVersionUID = 6824440535661529806L;

            public String getEditingValue(Object cell, EventObject trigger) {
                return super.getEditingValue(cell, trigger);
            };

        };

        graphComponent.setCellWarning(opacity, "<html>1. Testing a warning<br>2. Another warning");
        getContentPane().add(graphComponent);

        // Stops editing after enter has been pressed instead
        // of adding a newline to the current editing value
        graphComponent.setEnterStopsCellEditing(true);
    }

    public static void main(String[] args) {
        ArenaFrame frame = new ArenaFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

}
