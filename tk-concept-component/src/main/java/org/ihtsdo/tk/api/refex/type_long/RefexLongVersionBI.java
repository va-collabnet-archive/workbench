package org.ihtsdo.tk.api.refex.type_long;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface RefexLongVersionBI <A extends RefexLongAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     long getLong1();

}
