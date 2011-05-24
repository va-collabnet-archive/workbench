package org.ihtsdo.tk.api.refex.type_str;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface RefexStrVersionBI <A extends RefexStrAnalogBI<A>>
        extends RefexVersionBI<A> {
    
     String getStr1();

}
