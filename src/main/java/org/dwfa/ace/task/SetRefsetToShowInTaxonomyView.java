package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;


/**
 * Sets the list of reference sets to show in the taxonomy view to the reference set specified
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset", type = BeanType.TASK_BEAN) })
public class SetRefsetToShowInTaxonomyView extends AbstractTask {
	
	private static final long serialVersionUID = 0;
	private static final int dataVersion = 0;
	
	private String propName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
	
	private I_ConfigAceFrame config;
	
	private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(propName);
        
    }
	
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
        	propName = (String) in.readObject();        	
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    
	/**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process,final I_Work worker) throws TaskFailedException {
		try {
			config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
							.name());

			config.setShowRefsetInfoInTaxonomy(true);
			config.setShowViewerImagesInTaxonomy(true);
			
			I_IntList refsetsToShow = config.getRefsetsToShowInTaxonomy();

			refsetsToShow.clear();

			Object obj = process.readProperty(propName);
			I_GetConceptData refsetConcept = AceTaskUtil
					.getConceptFromObject(obj);

			refsetsToShow.add(refsetConcept.getConceptId());
			
			config.fireCommit();

		} catch (Exception e) {
			throw new TaskFailedException("Failed to set reference set to show to passed parameter", e);
		}

		return Condition.CONTINUE;
	}
 
    /**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
    public void complete( I_EncodeBusinessProcess process, I_Work worker ) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}
    
}