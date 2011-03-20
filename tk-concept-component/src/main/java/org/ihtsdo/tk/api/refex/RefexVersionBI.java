package org.ihtsdo.tk.api.refex;

import java.io.IOException;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.RefexCUB;

public interface RefexVersionBI <A extends RefexAnalogBI<A>> 
        extends ComponentVersionBI,
        RefexChronicleBI<A>,
        AnalogGeneratorBI<A> {
	
	RefexCUB getRefexEditSpec() throws IOException;
	
}
