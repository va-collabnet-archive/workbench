package org.ihtsdo.tk.api.refex.type_nid;

import org.ihtsdo.tk.api.refex.RefexVersionBI;

public interface RefexNidVersionBI <A extends RefexNidAnalogBI<A>> 
        extends RefexVersionBI<A> {
    
     int getNid1();

}
