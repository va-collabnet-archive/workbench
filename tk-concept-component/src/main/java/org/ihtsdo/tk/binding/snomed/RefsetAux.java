package org.ihtsdo.tk.binding.snomed;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class RefsetAux {

    public static ConceptSpec NL_REFEX =
            new ConceptSpec("Dutch [International Organization for Standardization 639-1 code nl] language reference set (foundation metadata concept)",
            UUID.fromString("592fe43f-f07e-568b-90e1-96de4c33b2a8"));
    public static ConceptSpec GMDN_REFEX =
            new ConceptSpec("GMDN review status reference set (foundation metadata concept)",
            UUID.fromString("c5994e33-21d3-327c-ab25-06d2356d2d68"));
}
