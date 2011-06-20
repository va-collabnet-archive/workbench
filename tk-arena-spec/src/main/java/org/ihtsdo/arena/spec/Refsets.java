package org.ihtsdo.arena.spec;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class Refsets {

    public static ConceptSpec TEST =
            new ConceptSpec("Terminology Auxiliary concept",
            UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029"));
    
    public static ConceptSpec TEST2 =
            new ConceptSpec("Refset Auxiliary Concept",
            UUID.fromString("1c698388-c309-3dfa-96f0-86248753fac5"));
    
    public static ConceptSpec EN_GB_LANG =
            new ConceptSpec("GB English Dialect Subset",
            UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd"));
    
    public static ConceptSpec EN_US_LANG =
            new ConceptSpec("US English Dialect Subset",
            UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6"));
    
    public static ConceptSpec NON_HUMAN =
            new ConceptSpec("Non-human Subset",
            UUID.fromString("0e2687b7-db28-5a01-b968-b98865648f2b"));
    
    public static ConceptSpec VMP =
            new ConceptSpec("VMP subset",
            UUID.fromString("d085216e-e34d-52e8-9785-d8af93939f99"));
    
    public static ConceptSpec VTM =
            new ConceptSpec("VTM subset",
            UUID.fromString("0a2904aa-e393-5e64-b354-7daf9266ca81"));
    
    public static ConceptSpec DEGREE_OF_SYNONYMY =
            new ConceptSpec("Degree of Synonymy Refset",
            UUID.fromString("a8dd0021-4994-36b2-a0f5-567b7e007847"));
    
    public static ConceptSpec REFERS_TO =
            new ConceptSpec("Refers To Refset",
            UUID.fromString("1b122b8f-172f-53d5-a2e2-eb1161737c2a"));
}
