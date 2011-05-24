package org.ihtsdo.tk.api.refex.type_int;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface RefexIntVersionBI <A extends RefexIntAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     int getInt1();

}
