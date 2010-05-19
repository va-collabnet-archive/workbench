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
public class AceVertexLabelShapeFunction extends AbstractVertexShapeFunction {
    public AceVertexLabelShapeFunction() {
    }

    public AceVertexLabelShapeFunction(VertexSizeFunction vsf, VertexAspectRatioFunction varf) {
        super(vsf, varf);
    }

    public Shape getShape(Vertex v) {
        if (v.containsUserDatumKey("shape")) {
            return (Rectangle) v.getUserDatum("shape");
        }
        return factory.getRectangle(v);
    }
}
