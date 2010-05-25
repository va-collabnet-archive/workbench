package org.ihtsdo.arena.editor;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.dwfa.util.EnumMap;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class ArenaEditor extends BasicGraphEditor
{
	public enum CELL_ATTRIBUTES { LINKED_TAB };

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
        addPaletteTemplate(shapesPalette, "tab 1", "potion_green", 1);
        addPaletteTemplate(shapesPalette, "tab 2", "potion_red", 2);
        addPaletteTemplate(shapesPalette, "tab 3", "text_formula", 3);
        addPaletteTemplate(shapesPalette, "tab 4", "view", 4);
        addPaletteTemplate(shapesPalette, "unconnected", "graph_edge_directed", 5);
        addPaletteTemplate(shapesPalette, "search results", "flag_yellow", 6);
        addPaletteTemplate(shapesPalette, "taxonomy", "elements_selection", 7);
        
        EditorPalette viewPalette = insertPalette("views");
        addPaletteTemplate(viewPalette, "lineage", "potion_green", 1);
        addPaletteTemplate(viewPalette, "parents", "potion_green", 2);
        addPaletteTemplate(viewPalette, "siblings", "potion_green", 3);
        addPaletteTemplate(viewPalette, "children", "potion_green", 4);
        addPaletteTemplate(viewPalette, "refset members", "potion_green", 5);
        addPaletteTemplate(viewPalette, "src rels", "potion_green", 6);
        addPaletteTemplate(viewPalette, "dest rels", "potion_green", 7);
        addPaletteTemplate(shapesPalette, "refset spec", "potion_green", 8);
        addPaletteTemplate(shapesPalette, "refset members", "potion_green", 9);
       
        EditorPalette history = insertPalette("history");
        addPaletteTemplate(history, "history 1", "potion_green", 1);
        addPaletteTemplate(history, "history 2", "potion_green", 2);

        getGraphComponent().getGraph().setCellsResizable(false);
        getGraphComponent().setConnectable(false);
        getGraphComponent().getGraphHandler().setCloneEnabled(false);
        getGraphComponent().getGraphHandler().setImagePreview(false);

        // Prefers default JComponent event-handling before mxCellHandler handling
        //getGraphComponent().getGraphHandler().setKeepOnTop(false);

        mxGraph graph = getGraphComponent().getGraph();
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            mxCell v1 = (mxCell) graph.insertVertex(parent, null, "Customers", 20, 20, 200, 280);
            v1.getGeometry().setAlternateBounds(new mxRectangle(0, 0, 200, 20));
            v1.setValue(new EnumMap().put(CELL_ATTRIBUTES.LINKED_TAB, 1));
            mxCell v2 = (mxCell) graph.insertVertex(parent, null, "Orders", 280, 20, 200, 280);
            v2.getGeometry().setAlternateBounds(new mxRectangle(0, 0, 200, 20));
            v2.setValue(new EnumMap().put(CELL_ATTRIBUTES.LINKED_TAB, 2));
        } finally {
            graph.getModel().endUpdate();
        }
    }

    private void addPaletteTemplate(EditorPalette palette,  String label, String imageName, int link) {
        mxCell tableTemplate = new mxCell(label, new mxGeometry(20, 20, 200, 200), null);
        tableTemplate.setValue(new EnumMap().put(CELL_ATTRIBUTES.LINKED_TAB, link));
        tableTemplate.getGeometry().setAlternateBounds( new mxRectangle(0, 0, 200, 20));
        tableTemplate.setVertex(true);
        ImageIcon icon = new ImageIcon(GraphEditor.class.getResource("/24x24/plain/" + imageName + ".png"));
        palette.addTemplate(label, icon, tableTemplate);
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
        try  {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        ArenaEditor editor = new ArenaEditor();
        editor.createFrame(new ArenaEditorMenuBar(editor)).setVisible(true);
    }

}
