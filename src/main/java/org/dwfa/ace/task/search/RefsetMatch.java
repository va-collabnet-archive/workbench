package org.dwfa.ace.task.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Test criteria that tests if a component is in a refset.
 * 
 * @author Dion McMurtrie
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/search", type = BeanType.TASK_BEAN),
        @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class RefsetMatch extends AbstractSearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * refset concept the component must be a member of.
     */
    private TermEntry refset = new TermEntry(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.refset);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.refset = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        try {
            I_GetConceptData refsetToMatch = AceTaskUtil.getConceptFromObject(refset);
            int refsetId = refsetToMatch.getConceptId();            
            
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
            	return isComponentInRefset(frameConfig, termFactory, ((I_GetConceptData) component).getConceptId(), refsetId);            	
            } else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {            	
            	I_DescriptionVersioned description = (I_DescriptionVersioned) component;
            	
            	if (isComponentInRefset(frameConfig, termFactory, description.getDescId(), refsetId)) {
            		return true;
            	}
            	
            	return isComponentInRefset(frameConfig, termFactory, description.getConceptId(), refsetId);
            } else if (I_RelVersioned.class.isAssignableFrom(component.getClass())) {
            	return isComponentInRefset(frameConfig, termFactory, ((I_RelVersioned) component).getRelId(), refsetId);
            }
            
            return false;
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

	private boolean isComponentInRefset(I_ConfigAceFrame frameConfig,
			I_TermFactory termFactory, int componentId, int refsetId)
			throws IOException {
		List<I_ThinExtByRefVersioned> extensions = termFactory.getAllExtensionsForComponent(componentId);
		            
		for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
			List<I_ThinExtByRefTuple> returnTuples = new ArrayList<I_ThinExtByRefTuple>();
			thinExtByRefVersioned.addTuples(frameConfig.getAllowedStatus(), frameConfig.getViewPositionSet(), returnTuples, false);
			for (I_ThinExtByRefTuple thinExtByRefTuple : returnTuples) {
				if (thinExtByRefTuple.getRefsetId() == refsetId) {
					return true;
				}
			}
		}
		
		return false;
	}

	public TermEntry getRefset() {
        return refset;
    }

    public void setRefset(TermEntry refset) {
        this.refset = refset;
    }

}
