package org.ihtsdo.tk.api.conattr;

import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;

public interface ConAttrVersionBI extends ComponentVersionBI, AnalogGeneratorBI<ConAttrAnalogBI> {

    public boolean isDefined();

}
