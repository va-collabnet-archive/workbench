package org.ihtsdo.tk.api.refex.type_float;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface RefexFloatVersionBI <A extends RefexFloatAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     float getFloat1();

}
