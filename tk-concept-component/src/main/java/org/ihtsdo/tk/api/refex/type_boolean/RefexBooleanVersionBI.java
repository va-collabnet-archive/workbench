package org.ihtsdo.tk.api.refex.type_boolean;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface RefexBooleanVersionBI <A extends RefexBooleanAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     boolean getBoolean1();

}
