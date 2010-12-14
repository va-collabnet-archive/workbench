package org.ihtsdo.arena.editor;


import java.awt.Component;

import org.dwfa.ace.ACE;
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

public class ArenaGraphComponent extends mxGraphComponent
{
	
    /**
     * 
     */
    private static final long serialVersionUID = -1152655782652932774L;
	private ACE ace;

    /**
     * 
     * @param graph
     */
    public ArenaGraphComponent(mxGraph graph, ACE ace) {
        super(graph);
        this.ace = ace;
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
    public Component[] createComponents(mxCellState state) {
        if (getGraph().getModel().isVertex(state.getCell())) {
        	mxCell cell = (mxCell) state.getCell();
        	Object cellValue = cell.getValue();
        	if (cellValue != null) {
            	if (ConceptViewSettings.class.isAssignableFrom(cellValue.getClass())) {
                    return new Component[] { new ConceptViewRenderer(state.getCell(), this, ace) };
            	} else  if (TaxonomyViewSettings.class.isAssignableFrom(cellValue.getClass())) {
                    return new Component[] { new TaxonomyViewRenderer(state.getCell(), this, ace) };
            	}
        	}
        }
        return null;
    }
}
