package org.ihtsdo.batch;

import java.io.IOException;
import java.util.UUID;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
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

    public abstract boolean execute(ConceptVersionBI c, EditCoordinate ec, ViewCoordinate vc) throws Exception;

    /**
     * Call once prior to execution of task to setup common values used to process all tasks.
     * @param tc
     * @throws IOException 
     */
    public static void setup(EditCoordinate ec, ViewCoordinate vc) throws IOException {
        RETIRED_NID = TermAux.RETIRED.getLenient().getNid();
        CURRENT_NID = TermAux.CURRENT.getLenient().getNid();
        termConstructor = Ts.get().getTerminologyConstructor(ec, vc);
    }

    public static String nidToName(int nid) throws IOException {
        return Ts.get().getComponent(nid).toUserString();
    }

    public static String uuidToName(UUID uuid) throws IOException {
        return Ts.get().getComponent(uuid).toUserString();
    }
}
