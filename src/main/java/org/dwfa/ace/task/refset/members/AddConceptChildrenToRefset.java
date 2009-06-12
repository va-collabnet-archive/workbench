package org.dwfa.ace.task.refset.members;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.refset.MemberRefsetHelper;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Adds a single concept as a member of the working refset 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/refset/membership", type = BeanType.TASK_BEAN) })
public class AddConceptChildrenToRefset extends AbstractTask {

	private static final long serialVersionUID = -2883696709930614625L;

	private static final int dataVersion = 1;
	
	/** the refset we are adding to */
    private String refsetConceptPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();

    /** the concept to be added to the refset */
    private String memberConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();
    
    /** the value to be given to the new concept extension */
    private String conceptExtValuePropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey(); 
    
    protected I_TermFactory termFactory;
    
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
        out.writeObject(this.refsetConceptPropName);
        out.writeObject(this.memberConceptPropName);
        out.writeObject(this.conceptExtValuePropName);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			this.refsetConceptPropName = (String) in.readObject();
			this.memberConceptPropName = (String) in.readObject();
			this.conceptExtValuePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		try {
			I_GetConceptData refset = (I_GetConceptData) process.readProperty(refsetConceptPropName);
			I_GetConceptData member = (I_GetConceptData) process.readProperty(memberConceptPropName);
			I_GetConceptData value  = (I_GetConceptData) process.readProperty(conceptExtValuePropName);
			
			if (refset == null) {
				throw new TerminologyException("A working refset has not been selected.");
			}
			
			if (member == null) {
				throw new TerminologyException("No member concept selected.");				
			}
			
			if (value == null) {
				throw new TerminologyException("No concept extension value selected.");
			}
			
			getLogger().info(
					"Adding children of concept '" + member.getInitialText() + 
					"' as member of refset '" + refset.getInitialText() +
					"' with a value '" + value.getInitialText() + "'.");
			
			MemberRefsetHelper helper = new MemberRefsetHelper(refset.getConceptId(), value.getConceptId());			
			Set<I_GetConceptData> newMembers = helper.getAllDescendants(member);
			
			helper.addAllToRefset(newMembers, "Adding children of concept " + member.getInitialText());
			
			return Condition.CONTINUE;
			
		} catch (Exception e) {
			throw new TaskFailedException("Unable to add children of concept to refset. " + e.getMessage(), e);
		}
	}

	

	
	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}


	public String getRefsetConceptPropName() {
		return refsetConceptPropName;
	}

	public void setRefsetConceptPropName(String refsetConceptPropName) {
		this.refsetConceptPropName = refsetConceptPropName;
	}

	public String getMemberConceptPropName() {
		return memberConceptPropName;
	}

	public void setMemberConceptPropName(String memberConceptPropName) {
		this.memberConceptPropName = memberConceptPropName;
	}

	public String getConceptExtValuePropName() {
		return conceptExtValuePropName;
	}

	public void setConceptExtValuePropName(String conceptExtValuePropName) {
		this.conceptExtValuePropName = conceptExtValuePropName;
	}
	
	

}
