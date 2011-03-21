package org.ihtsdo.batch;

import java.io.IOException;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.example.binding.TermAux;

// :!!!: should BatchActionTask be abstract or interface?
public abstract class BatchActionTask {

    public static int RETIRED_NID;
    public static int CURRENT_NID;
    public static TerminologyConstructorBI termConstructor;

    public enum BatchActionTaskType {

        PARENT_ADD_NEW,
        PARENT_REPLACE,
        PARENT_RETIRE,
        REFSET_ADD_MEMBER,
        REFSET_MOVE_MEMBER,
        REFSET_REPLACE_VALUE,
        REFSET_RETIRE_MEMBER,
        ROLE_REPLACE_VALUE,
        SIMPLE
    }

    public BatchActionTask() {
    }

    public abstract boolean execute(ConceptVersionBI c) throws Exception;

    public static void setup(TerminologyConstructorBI tc) throws IOException {
        RETIRED_NID = TermAux.RETIRED.getLenient().getNid();
        CURRENT_NID = TermAux.CURRENT.getLenient().getNid();

        termConstructor = tc;
    }
    // 
}
