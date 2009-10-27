package org.dwfa.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN), 
        @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class IsKindOf extends AbstractSearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;
    

    /**
     * Property name for the term component to test. 
     */
    private TermEntry parentTerm = new TermEntry(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.parentTerm);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
                this.parentTerm = (TermEntry) in.readObject();
         } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }
    }

    @Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        try {
            I_GetConceptData parent = AceTaskUtil.getConceptFromObject(parentTerm);
            
            I_GetConceptData potChild;
            if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
                potChild = (I_GetConceptData) component;
            } else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
                I_DescriptionVersioned desc = (I_DescriptionVersioned) component;
                potChild = LocalVersionedTerminology.get().getConcept(desc.getConceptId());
            } else {
                return applyInversion(false);
            }
            
            return applyInversion(parent.isParentOf(potChild, frameConfig.getAllowedStatus(), 
                                     frameConfig.getDestRelTypes(), frameConfig.getViewPositionSet(), false));
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

    public TermEntry getParentTerm() {
        return parentTerm;
    }

    public void setParentTerm(TermEntry parentTerm) {
        this.parentTerm = parentTerm;
    }


}
