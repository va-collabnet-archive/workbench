package org.ihtsdo.tk.api.refex.type_cnid_cnid;

import org.ihtsdo.tk.api.refex.type_cnid.*;

public interface RefexCnidCnidVersionBI <A extends RefexCnidCnidAnalogBI<A>> 
    extends RefexCnidVersionBI<A> {
    
     int getCnid2();
     
}
