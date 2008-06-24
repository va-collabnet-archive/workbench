package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;

import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_SupportClassifier;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.classify.I_SnorocketFactory.I_Callback;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/classify", type = BeanType.TASK_BEAN) })
public class ClassifyCurrentEditing extends AbstractTask {

	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		final int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {

		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
	}

	public Condition evaluate(final I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		long startTime = System.currentTimeMillis();

		try {

			// get uncommitted editing concept
			final I_HostConceptPlugins host = (I_HostConceptPlugins) worker
					.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS
							.name());
			final I_GetConceptData termComponent = (I_GetConceptData) host
					.getTermComponent();

			final I_SnorocketFactory rocket = (I_SnorocketFactory) process
					.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());
			final I_TermFactory termFactory = LocalVersionedTerminology.get();

			// load the new concept into classifer
			worker.getLogger().info("** Classifying: " + termComponent);
			int isaId = ((I_SupportClassifier) LocalVersionedTerminology.get())
					.uuidToNative(SNOMED.Concept.IS_A.getUids());
			worker.getLogger().info(
					"**** isaId: " + isaId + ": " + SNOMED.Concept.IS_A);

			processConcept(termComponent, rocket, worker);

			rocket.classify();

			worker.getLogger().info(
					"Classified! " + (System.currentTimeMillis() - startTime)
							+ "s");

			rocket.getResults(new I_Callback() {
				public void addRelationship(int conceptId1, int roleId,
						int conceptId2, int group) {
					System.err.println("###### " + conceptId1 + " " + roleId
							+ " " + conceptId2);
				}
			});
		} catch (IOException e) {
			handleException(e);
		} catch (TerminologyException e) {
			handleException(e);
		}

		return Condition.CONTINUE;
	}

	private void handleException(Exception e) throws TaskFailedException {
		throw new TaskFailedException(e);
	}

	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	private void processConcept(final I_GetConceptData concept,
			final I_SnorocketFactory rocket, final I_Work worker)
			throws IOException, TerminologyException {
		final int conceptId = concept.getConceptId();

		// rocket.addConcept(111, false);
		// rocket.addConcept(222, false);
		// rocket.addConcept(333, false);
		// rocket.addConcept(444, false);
		// rocket.addRelationship(111, -2147468542, 222, 0);
		// rocket.addRelationship(222, 333, 444, 0);

		//firstly check if this is a brand new concept
		//worker.getLogger().info("ming c" + concept.getConceptAttributes().toString());
		//worker.getLogger().info("ming d" + concept.getDescriptions().toString());
		//worker.getLogger().info("ming s" + concept.getSourceRels().toString());
		//worker.getLogger().info("ming uc" + concept.getUncommittedConceptAttributes().toString());
		//worker.getLogger().info("ming ud " + concept.getUncommittedDescriptions().toString());
		//worker.getLogger().info("ming us" + concept.getUncommittedSourceRels().toString());
		

		/**
		 * a little tricky here to judge if this is a brand new concept 
		 */
		if (concept.getConceptAttributes().getTuples().size() == 1
				&& concept.getUncommittedConceptAttributes() != null) {
			// this is a brand new concept
			// add concept then add rels

			boolean isDefined = concept.getUncommittedConceptAttributes()
					.getTuples().get(0).isDefined();

			worker.getLogger().info(
					"Add concept: " + conceptId + " : " + concept.getId()
							+ ": " + isDefined);

			rocket.addConcept(conceptId, isDefined);

			// add rels suppose all the rels are for classficaiton

			for (I_RelVersioned rel : concept.getUncommittedSourceRels()) {
				worker.getLogger().info(
						"Add relationship: "
								+ rel.getLastTuple().getRelTypeId() + " "
								+ rel.getLastTuple().getC2Id());
				rocket.addRelationship(rel.getLastTuple().getC1Id(), rel
						.getLastTuple().getRelTypeId(), rel.getLastTuple()
						.getC2Id(), rel.getLastTuple().getGroup());
			}
		}
	}

}
