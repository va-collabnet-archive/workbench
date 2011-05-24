package org.ihtsdo.arena.editor;


import java.awt.Component;

import org.ihtsdo.arena.conceptview.ConceptViewRenderer;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.arena.taxonomyview.TaxonomyViewRenderer;
import org.ihtsdo.arena.taxonomyview.TaxonomyViewSettings;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;
import org.dwfa.ace.api.I_ConfigAceFrame;

public class ArenaGraphComponent extends mxGraphComponent
{

    /**
     *
     */
    private static final long serialVersionUID = -1152655782652932774L;
	private I_ConfigAceFrame config;

    /**
     *
     * @param graph
     */
    public ArenaGraphComponent(mxGraph graph, I_ConfigAceFrame config) {
        super(graph);
        this.config = config;
        mxGraphView graphView = new mxGraphView(graph);

        graph.setView(graphView);
    }

    /**
     *
     * @param edge
     * @param isSource
     * @return the column number the edge is attached to
     */
    public int getColumn(mxCellState state, boolean isSource)
    {
        if (state != null) {
            if (isSource) {
                return mxUtils.getInt(state.getStyle(), "sourceRow", -1);
            } else {
                return mxUtils.getInt(state.getStyle(), "targetRow", -1);
            }
        }
        return -1;
    }


    /**
     *
     */
   @Override
    public Component[] createComponents(mxCellState state) {
        if (getGraph().getModel().isVertex(state.getCell())) {
        	mxCell cell = (mxCell) state.getCell();
        	Object cellValue = cell.getValue();
        	if (cellValue != null) {
            	if (ConceptViewSettings.class.isAssignableFrom(cellValue.getClass())) {
                    return new Component[] { new ConceptViewRenderer(state.getCell(), this, config) };
            	} else  if (TaxonomyViewSettings.class.isAssignableFrom(cellValue.getClass())) {
                    return new Component[] { new TaxonomyViewRenderer(state.getCell(), this, config) };
            	}
        	}
        }
        return null;
    }
}
