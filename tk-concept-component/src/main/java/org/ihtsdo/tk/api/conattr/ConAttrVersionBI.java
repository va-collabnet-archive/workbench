package org.ihtsdo.tk.api.conattr;

import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;

public interface ConAttrVersionBI 
	extends ComponentVersionBI, ConAttrChronicleBI, AnalogGeneratorBI<ConAttrAnalogBI> {

    public boolean isDefined();

}
