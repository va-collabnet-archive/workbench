package org.ihtsdo.tk.api.conattr;

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.blueprint.ConAttrAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface ConAttrVersionBI<A extends ConAttrAnalogBI>
	extends ComponentVersionBI,
        ConAttrChronicleBI,
        AnalogGeneratorBI<A> {

    public boolean isDefined();
    
    @Override
    public ConAttrAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB;
}
