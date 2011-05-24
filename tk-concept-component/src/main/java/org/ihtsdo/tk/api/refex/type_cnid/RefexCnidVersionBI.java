package org.ihtsdo.tk.api.refex.type_cnid;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface RefexCnidVersionBI <A extends RefexCnidAnalogBI<A>> 
        extends RefexVersionBI<A> {
    
     int getCnid1();

}
