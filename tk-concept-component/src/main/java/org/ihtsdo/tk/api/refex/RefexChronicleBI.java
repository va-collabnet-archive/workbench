package org.ihtsdo.tk.api.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ComponentChronicleBI;

public interface RefexChronicleBI<A extends RefexAnalogBI<A>>
        extends ComponentChronicleBI<RefexVersionBI<A>> {
   int getRefexNid();

   int getReferencedComponentNid();
}
