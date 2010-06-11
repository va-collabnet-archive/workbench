package org.ihtsdo.arena.editor;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import org.dwfa.ace.ACE;
import org.dwfa.util.EnumMap;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

public class ArenaEditor extends BasicGraphEditor
{
	public enum CELL_ATTRIBUTES { LINKED_TAB, COMPONENT_KIND };

	public enum COMPONENT_KIND { CONCEPT, TAXONOMY };

    /**
     * 
     */
    private static final long serialVersionUID = -7007225006753337933L;

    /**
     * 
     */
    public ArenaEditor(ACE ace)
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
        }, ace)

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
        graphOutline.setVisible(true);
        addPaletteTemplate(editorPalette, "tab 1", "view", 1);
        addPaletteTemplate(editorPalette, "tab 2", "view", 2);
        addPaletteTemplate(editorPalette, "tab 3", "view", 3);
        addPaletteTemplate(editorPalette, "tab 4", "view", 4);
        addPaletteTemplate(editorPalette, "taxonomy", "text_tree", 1, 
        		new EnumMap().put(CELL_ATTRIBUTES.COMPONENT_KIND, COMPONENT_KIND.TAXONOMY));

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
    	addPaletteTemplate(palette,  label, imageName, link, new EnumMap().put(CELL_ATTRIBUTES.LINKED_TAB, link));
    }
    private void addPaletteTemplate(EditorPalette palette,  String label, String imageName, int link, EnumMap map) {
        mxCell tableTemplate = new mxCell(label, new mxGeometry(20, 20, 200, 200), null);
        tableTemplate.setValue(map);
        tableTemplate.getGeometry().setAlternateBounds(new mxRectangle(0, 0, 200, 20));
        tableTemplate.setVertex(true);
        ImageIcon icon = new ImageIcon(ArenaEditor.class.getResource("/24x24/plain/" + imageName + ".png"));
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

}
