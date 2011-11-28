package org.ihtsdo.tk.api.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;
import java.util.UUID;

public interface RefexVersionBI<A extends RefexAnalogBI<A>>
        extends ComponentVersionBI, RefexChronicleBI<A>, AnalogGeneratorBI<A> {
   RefexCAB getRefexEditSpec() throws IOException;

   TkRefsetAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
           Map<UUID, UUID> conversionMap)
           throws ContraditionException, IOException;
   
   boolean refexFieldsEqual (RefexVersionBI another);
}
