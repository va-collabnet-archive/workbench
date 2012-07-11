package org.ihtsdo.tk.api.refex;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;
import java.util.UUID;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;

public interface RefexVersionBI<A extends RefexAnalogBI<A>>
        extends ComponentVersionBI, RefexChronicleBI<A>, AnalogGeneratorBI<A>, Comparable<RefexVersionBI<A>> {

    @Override
    RefexCAB makeBlueprint(ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException;

    TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate viewCoordinate, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap)
            throws ContradictionException, IOException;

    boolean refexFieldsEqual(RefexVersionBI another);
    
    
}
