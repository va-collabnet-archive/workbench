package org.ihtsdo.tk.example.binding;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class TermAux {

    public static ConceptSpec IS_A =
            new ConceptSpec("is a (relationship type)",
            UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));
    public static ConceptSpec CURRENT =
            new ConceptSpec("current (active status type)",
            UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66"));
    public static ConceptSpec RETIRED =
            new ConceptSpec("retired (inactive status type)",
            UUID.fromString("e1956e7b-08b4-3ad0-ab02-b411869f1c09"));
    public static ConceptSpec INACTIVE_STATUS =
            new ConceptSpec("inactive (inactive status type)",
            UUID.fromString("1464ec56-7118-3051-9d21-0f95c1a39080"));
    public static ConceptSpec MOVED_TO =
            new ConceptSpec("moved elsewhere (inactive status type)",
            UUID.fromString("76367831-522f-3250-83a4-8609ab298436"));
    public static ConceptSpec REL_QUALIFIER_CHAR =
            new ConceptSpec("qualifier (characteristic type)",
            UUID.fromString("416ad0e4-b6bc-386c-900e-121c58b20f55"));
    public static ConceptSpec REL_HISTORIC =
            new ConceptSpec("historical (characteristic type)",
            UUID.fromString("1d054ca3-2b32-3004-b7af-2701276059d5"));
    public static ConceptSpec REL_STATED_CHAR =
            new ConceptSpec("stated (defining characteristic type)",
            UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d"));
    public static ConceptSpec REL_INFERED_CHAR =
            new ConceptSpec("defining",
            UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66"));
    public static ConceptSpec REL_NOT_REFINABLE =
            new ConceptSpec("not refinable (refinability type)",
            UUID.fromString("e4cde443-8fb6-11db-b606-0800200c9a66"));
    public static ConceptSpec REL_OPTIONALLY_REFINABLE =
            new ConceptSpec("optional (refinability type)",
            UUID.fromString("c3d997d3-b0a4-31f8-846f-03fa874f5479"));
}
