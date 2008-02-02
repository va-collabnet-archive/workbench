package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/commit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN)})
public class VerifySingleMemberPerConceptForRefset extends AbstractExtensionTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            //
         } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }
    }

    private static I_IntSet singleConceptRefsets;
    @Override
    public boolean test(I_ThinExtByRefVersioned extension, I_AlertToDataConstraintFailure alertObject, boolean forCommit)
            throws TaskFailedException {
        try {
            I_TermFactory tf = LocalVersionedTerminology.get();
            if (singleConceptRefsets == null) {
                singleConceptRefsets = tf.newIntSet();
                singleConceptRefsets.add(RefsetAuxiliary.Concept.ALLERGY_RXN_INCLUSION_SPEC.localize().getNid());
                singleConceptRefsets.add(RefsetAuxiliary.Concept.DISCHARGE_INCLUSION_SPEC.localize().getNid());
                singleConceptRefsets.add(RefsetAuxiliary.Concept.DOCUMENT_SECTION_ORDER.localize().getNid());
                singleConceptRefsets.add(RefsetAuxiliary.Concept.PATHOLOGY_INCLUSION_SPEC.localize().getNid());
                // etc..
                // @TODO  Need to gather this info from a machine readable concept model in the future...
            }
            if (singleConceptRefsets.contains(extension.getRefsetId())) {
                List<I_ThinExtByRefVersioned> matches = new ArrayList<I_ThinExtByRefVersioned>();
                
                for (I_ThinExtByRefVersioned ext: tf.getAllExtensionsForComponent(extension.getComponentId(), true)) {
                    if (ext.getRefsetId() == extension.getRefsetId()) {
                        matches.add(ext);
                    }
                }
                
                                
                if (matches.size() > 1) {
                    I_GetConceptData conceptWithDuplicate = tf.getConcept(extension.getComponentId());
                    I_GetConceptData refsetIdentity = tf.getConcept(extension.getRefsetId());
                	if (forCommit) {
                        alertObject.alert("Duplicate refset entries of identity: " + refsetIdentity.getInitialText()
                                + " for concept: " + conceptWithDuplicate.getInitialText()
                                + "\nPlease fix this up...");
                	} else {
                        // if not for commit, give option of rollback or continue
                		
                		String rollbackOpt = "don't add new refset member";
                		String letItGoOption = "let it go, I'll fix it before commit. ";
                        Object option = alertObject.alert("Duplicate refset entries of identity: " + refsetIdentity.getInitialText()
                                + " for concept: " + conceptWithDuplicate.getInitialText()
                                + "\nPlease fix this up...", new String[] {rollbackOpt, letItGoOption});
                        if (option == letItGoOption) {
                        	return true;
                        }
                        if (option == rollbackOpt) {
                        	((I_Transact) tf.getExtensionWrapper(extension.getMemberId())).abort();
                        }
                	}
                    return false;
                }

            }
            
            return true;
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

}
