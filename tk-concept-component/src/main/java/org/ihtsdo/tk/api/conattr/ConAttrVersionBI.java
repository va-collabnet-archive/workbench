package org.ihtsdo.tk.api.conattr;

import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;

public interface ConAttrVersionBI<A extends ConAttrAnalogBI>
	extends ComponentVersionBI,
        ConAttrChronicleBI,
        AnalogGeneratorBI<A> {

    public boolean isDefined();

}
