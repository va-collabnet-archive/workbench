package org.ihtsdo.tk.api.relationship;

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RelCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface RelationshipVersionBI<A extends RelationshipAnalogBI>
        extends TypedComponentVersionBI,
        RelationshipChronicleBI,
        AnalogGeneratorBI<A> {

    public int getRefinabilityNid();

    public int getCharacteristicNid();

    public int getGroup();

    public boolean isInferred();
    
    public boolean isStated();
    
    @Override
    public RelCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB;

}
