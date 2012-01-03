package org.ihtsdo.tk.api.media;

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TypedComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface MediaVersionBI<A extends MediaAnalogBI>
        extends TypedComponentVersionBI,
		MediaChronicleBI, AnalogGeneratorBI<A> {

    public byte[] getMedia();

    public String getTextDescription();

    public String getFormat();

    public int getConceptNid();
    
    @Override
    public MediaCAB makeBlueprint(ViewCoordinate vc) throws IOException, ContradictionException, InvalidCAB;

}
