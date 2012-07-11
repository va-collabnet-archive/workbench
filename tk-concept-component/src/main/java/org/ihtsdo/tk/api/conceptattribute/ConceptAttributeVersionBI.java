package org.ihtsdo.tk.api.conceptattribute;

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface ConceptAttributeVersionBI<A extends ConceptAttributeAnalogBI>
	extends ComponentVersionBI,
        ConceptAttributeChronicleBI,
        AnalogGeneratorBI<A> {

    public boolean isDefined();
    
    @Override
    public ConceptAttributeAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, ContradictionException, InvalidCAB;
}
