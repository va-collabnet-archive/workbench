package org.ihtsdo.tk.api.refex.type_nid_float;

import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_float.RefexFloatVersionBI;

public interface RefexNidFloatVersionBI <A extends RefexNidFloatAnalogBI<A>>
    extends RefexNidVersionBI<A>, RefexFloatVersionBI<A> {
     
}
