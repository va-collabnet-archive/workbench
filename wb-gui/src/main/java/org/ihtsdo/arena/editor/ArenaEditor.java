package org.ihtsdo.arena.editor;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class ArenaEditor extends BasicGraphEditor
{

    /**
     * 
     */
    private static final long serialVersionUID = -7007225006753337933L;

    /**
     * 
     */
    public ArenaEditor()
    {
        super("mxGraph for JFC/Swing", new ArenaGraphComponent(new mxGraph()
        {
            /**
             * Allows expanding tables
             */
            public boolean isCellFoldable(Object cell, boolean collapse)
            {
                return model.isVertex(cell);
            }
        })

        {
            /**
             * 
             */
            private static final long serialVersionUID = -1194463455177427496L;

            /**
             * Disables folding icons.
             */
            public ImageIcon getFoldingIcon(mxCellState state)
            {
                return null;
            }

        });

        // Creates a single shapes palette
        EditorPalette shapesPalette = insertPalette("panels");
        graphOutline.setVisible(false);
        addPaletteTemplate(shapesPalette, "tab 1");
        addPaletteTemplate(shapesPalette, "tab 2");
        addPaletteTemplate(shapesPalette, "tab 3");
        addPaletteTemplate(shapesPalette, "tab 4");
        addPaletteTemplate(shapesPalette, "unconnected");
        addPaletteTemplate(shapesPalette, "search results");
        addPaletteTemplate(shapesPalette, "taxonomy");
        
        EditorPalette viewPalette = insertPalette("views");
        addPaletteTemplate(viewPalette, "lineage");
        addPaletteTemplate(viewPalette, "parents");
        addPaletteTemplate(viewPalette, "siblings");
        addPaletteTemplate(viewPalette, "children");
        addPaletteTemplate(viewPalette, "refset members");
        addPaletteTemplate(viewPalette, "src rels");
        addPaletteTemplate(viewPalette, "dest rels");
        addPaletteTemplate(shapesPalette, "refset spec");
        addPaletteTemplate(shapesPalette, "refset members");
       
        EditorPalette history = insertPalette("history");
        addPaletteTemplate(history, "history 1");
        addPaletteTemplate(history, "history 2");

        getGraphComponent().getGraph().setCellsResizable(false);
        getGraphComponent().setConnectable(false);
        getGraphComponent().getGraphHandler().setCloneEnabled(false);
        getGraphComponent().getGraphHandler().setImagePreview(false);

        // Prefers default JComponent event-handling before mxCellHandler handling
        //getGraphComponent().getGraphHandler().setKeepOnTop(false);

        mxGraph graph = getGraphComponent().getGraph();
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try
        {
            mxCell v1 = (mxCell) graph.insertVertex(parent, null, "Customers",
                    20, 20, 200, 280);
            v1.getGeometry().setAlternateBounds(new mxRectangle(0, 0, 200, 20));
            mxCell v2 = (mxCell) graph.insertVertex(parent, null, "Orders",
                    280, 20, 200, 280);
            v2.getGeometry().setAlternateBounds(new mxRectangle(0, 0, 200, 20));
        }
        finally
        {
            graph.getModel().endUpdate();
        }
    }

    private void addPaletteTemplate(EditorPalette shapesPalette,  String label) {
        mxCell tableTemplate = new mxCell(label, new mxGeometry(0, 0, 150, 300), null);
        tableTemplate.getGeometry().setAlternateBounds( new mxRectangle(0, 0, 150, 25));
        tableTemplate.setVertex(true);
        shapesPalette.addTemplate(label,
                        new ImageIcon(
                                GraphEditor.class
                                        .getResource("/com/mxgraph/examples/swing/images/rectangle.png")),
                        tableTemplate);
    }

    /**
     * 
     */
    protected void installToolBar()
    {
        add(new ArenaEditorToolBar(this, JToolBar.HORIZONTAL),
                BorderLayout.NORTH);
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

        ArenaEditor editor = new ArenaEditor();
        editor.createFrame(new ArenaEditorMenuBar(editor)).setVisible(true);
    }

}
