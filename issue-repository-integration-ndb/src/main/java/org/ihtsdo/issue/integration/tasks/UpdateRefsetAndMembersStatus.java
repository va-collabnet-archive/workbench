/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.issue.integration.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The Class UpdateRefsetAndMembersStatus.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class UpdateRefsetAndMembersStatus extends AbstractTask {


	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/** The new status. */
	private TermEntry newStatus;

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(newStatus);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			newStatus = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		getLogger().info("Entering update refsets..");
		I_ConfigAceFrame config = (I_ConfigAceFrame) worker
		.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

		I_TermFactory tf = Terms.get();

		try {
			I_GetConceptData newStatusConcept = tf.getConcept(newStatus.ids);
			I_GetConceptData refsetConcept = null;
			if (process.readAttachement("REFSET_UUID") == null) {
				refsetConcept = config.getRefsetInSpecEditor();
			} else {
				refsetConcept = tf.getConcept((UUID) process.readAttachement("REFSET_UUID"));
			}
			RefsetSpec refsetSpec = new RefsetSpec(refsetConcept, true, config);
			
			I_GetConceptData promotionRefset = refsetSpec.getPromotionRefsetConcept();

			RefsetUtil refsetUtil = new RefsetUtilImpl();
			tf.getSpecRefsetHelper(config);
			I_HelpSpecRefset specRefsetHelper = tf.getSpecRefsetHelper(config);

			getLogger().info("Refset = " + refsetConcept.toString());
			getLogger().info("newStatus = " + newStatusConcept.toString());
			getLogger().info("PromotionRefset = " + promotionRefset.toString());

			Collection<? extends I_ExtendByRef> members = tf.getRefsetExtensionMembers(refsetConcept.getConceptNid());

			for (I_ExtendByRef member : members) {
				getLogger().info("updating member");
				
				I_GetConceptData memberConcept = tf.getConcept(member.getComponentNid());
				
				I_ExtendByRef currentMembershipToPromotionRefset = null;
				for (I_ExtendByRef membershipToOtherRefsets : memberConcept.getExtensions()) {
					if (membershipToOtherRefsets.getRefsetId() == promotionRefset.getConceptNid()) {
						currentMembershipToPromotionRefset = membershipToOtherRefsets;
					}
				}
				
				if (currentMembershipToPromotionRefset == null) {
					//Create new membership to promotion refset
					specRefsetHelper.newRefsetExtension(promotionRefset.getConceptNid(),
                            member.getComponentNid(), newStatusConcept.getConceptNid()); 
					tf.addUncommittedNoChecks(member);
				} else {
					//Add new version
					I_ExtendByRefPart memberLastPart = refsetUtil.getLatestVersion(member, tf);
					if (memberLastPart instanceof I_ExtendByRefPartCid) {
	                    // found a member to retire
						specRefsetHelper.newConceptExtensionPart(
                   			 promotionRefset.getConceptNid(), memberConcept.getConceptNid(), 
                   			 newStatusConcept.getConceptNid());
						tf.addUncommittedNoChecks(member);
//	                    for (I_Path editPath : config.getEditingPathSet()) {
//	                        I_ThinExtByRefPartConcept clone = (I_ThinExtByRefPartConcept) memberLastPart.duplicate();
//	                        clone.setStatusId(newStatusConcept.getConceptId());
//	                        clone.setVersion(Integer.MAX_VALUE);
//	                        clone.setPathId(editPath.getConceptId());
//	                        member.addVersion(clone);
//	                    }
	                }
				}
			}

				/*I_ConceptAttributePart lastAttributePart = refsetUtil.getLastestAttributePart(refsetConcept);

			I_ConceptAttributePart newAttributePart = lastAttributePart.duplicate();
			newAttributePart.setStatusId(newStatusConcept.getConceptId());
			newAttributePart.setVersion(Integer.MAX_VALUE);
			newAttributePart.setPathId(config.getEditingPathSet().iterator().next().getConceptId());

			refsetConcept.getConceptAttributes().addVersion(newAttributePart);
			tf.addUncommitted(refsetConcept);

			List<I_ThinExtByRefVersioned> members = tf.getRefsetExtensionMembers(refsetConcept.getConceptId());

			for (I_ThinExtByRefVersioned member : members) {
				getLogger().info("updating member");
				I_ThinExtByRefPart memberLastPart = refsetUtil.getLatestVersion(member, tf);
				I_ThinExtByRefPart newMemberLastPart = memberLastPart.duplicate();
				newMemberLastPart.setStatus(newStatusConcept.getConceptId());
				newMemberLastPart.setVersion(Integer.MAX_VALUE);
				newAttributePart.setPathId(config.getEditingPathSet().iterator().next().getConceptId());

				member.addVersion(newMemberLastPart);
				tf.addUncommitted(member);
			}*/

				getLogger().info("Before commit");
				tf.commit();
				getLogger().info("After commit");


			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return Condition.CONTINUE;
		}

		/**
		 * Gets the new status.
		 * 
		 * @return the new status
		 */
		public TermEntry getNewStatus() {
			return newStatus;
		}

		/**
		 * Sets the new status.
		 * 
		 * @param newStatus the new new status
		 */
		public void setNewStatus(TermEntry newStatus) {
			this.newStatus = newStatus;
		}

		/* (non-Javadoc)
		 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
		 */
		public void complete(I_EncodeBusinessProcess process, I_Work worker)
		throws TaskFailedException {

		}

		/* (non-Javadoc)
		 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
		 */
		public Collection<Condition> getConditions() {
			return CONTINUE_CONDITION;
		}

		/* (non-Javadoc)
		 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
		 */
		public int[] getDataContainerIds() {
			return new int[] {  };
		}

	}