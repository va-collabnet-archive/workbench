package org.dwfa.ace.graph;

import java.awt.Rectangle;
import java.awt.Shape;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.AbstractVertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.VertexSizeFunction;

/**
 * 
 * @author Joshua O'Madadhain
 */
public class AceVertexLabelShapeFunction extends AbstractVertexShapeFunction
{
    public AceVertexLabelShapeFunction() 
    {
    }
    public AceVertexLabelShapeFunction(VertexSizeFunction vsf, VertexAspectRatioFunction varf)
    {
        super(vsf, varf);
    }
    
    public Shape getShape(Vertex v)
    {
        if (v.containsUserDatumKey("shape")) {
			return (Rectangle) v.getUserDatum("shape");
        }
        return factory.getRectangle(v);
    }
}
