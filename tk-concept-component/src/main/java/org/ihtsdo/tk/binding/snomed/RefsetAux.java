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
    public static ConceptSpec SV_REFEX =
            new ConceptSpec("Swedish [International Organization for Standardization 639-1 code sv] language reference set (foundation metadata concept)",
            UUID.fromString("e57ec728-742f-56b3-9b53-9613670fb24d"));
    public static ConceptSpec EN_US_REFEX =
            new ConceptSpec("United States of America English language reference set (foundation metadata concept)",
            UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));
    public static ConceptSpec EN_GB_REFEX =
            new ConceptSpec("Great Britain English language reference set",
            UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));
    public static ConceptSpec PATH_REFSET =
            new ConceptSpec("Path reference set",
            UUID.fromString("fd9d47b7-c0a4-3eea-b3ab-2b5a3f9e888f"));
    public static ConceptSpec PATH_ORIGIN_REFEST =
            new ConceptSpec("Path origin reference set",
            UUID.fromString("1239b874-41b4-32a1-981f-88b448829b4b"));
}
