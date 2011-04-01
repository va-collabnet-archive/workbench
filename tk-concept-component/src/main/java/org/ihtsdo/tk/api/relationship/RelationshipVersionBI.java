package org.ihtsdo.tk.api.relationship;

import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.TypedComponentVersionBI;

public interface RelationshipVersionBI<A extends RelationshipAnalogBI>
        extends TypedComponentVersionBI,
        RelationshipChronicleBI,
        AnalogGeneratorBI<A> {

    public int getRefinabilityNid();

    public int getCharacteristicNid();

    public int getGroup();

    public boolean isInferred();
    
    public boolean isStated();

}
