package org.ihtsdo.tk.api.refex.type_string;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface RefexStringVersionBI <A extends RefexStringAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     String getString1();

}
