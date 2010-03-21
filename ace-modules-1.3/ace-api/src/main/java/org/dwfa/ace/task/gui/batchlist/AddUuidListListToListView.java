/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.ace.task.gui.batchlist;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.util.MultiMap;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import javax.swing.JList;
import javax.swing.SwingUtilities;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@BeanList(specs = {@Spec(directory = "tasks/ide/listview", type = BeanType.TASK_BEAN)})
public class AddUuidListListToListView extends AbstractTask {

	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	/**
	 * Property name for a list of uuid lists, typically used to represent a
	 * list of concepts in a transportable way.
	 */
	private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST
			.getAttachmentKey();

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(uuidListListPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			uuidListListPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do...

	}

	@SuppressWarnings("unchecked")
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
							.name());
			final I_TermFactory tf = LocalVersionedTerminology.get();

			JList conceptList = config.getBatchConceptList();
			final I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList
					.getModel();

			final List<List<UUID>> idListList = (ArrayList<List<UUID>>) process
					.readProperty(uuidListListPropName);
			AceLog.getAppLog()
					.info("Adding list of size: " + idListList.size());

			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {

					I_GetConceptData[] elements = getConceptsForList(
							idListList, tf);

					for (I_GetConceptData concept : elements) {
						model.addElement(concept);
					}
				}
			});

			return Condition.CONTINUE;
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		}
	}

	private I_GetConceptData[] getConceptsForList(List<List<UUID>> idListList,
			I_TermFactory tf) {

		I_GetConceptData[] elements = new I_GetConceptData[idListList.size()];
		Map idPositions = new MultiMap();
		int inc = -1;

		for (List<UUID> idList : idListList) {

			inc++;

			try {
				try {
					// Concept
					elements[inc] = getConcept(idList);
				} catch (TerminologyException e) {

					// Not a concept... try a description or
					// relationship
					final int nid = tf.uuidToNative(idList);
					List<UUID> uuids = tf.getId(nid).getUIDs();

					if (uuids != null && !uuids.isEmpty()) {
						try {
							// Description
							elements[inc] = getDescriptionParent(tf, uuids);
						} catch (Exception e1) {
							// Relationship
							idPositions.put(nid, inc);
						}
					}
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
				return new I_GetConceptData[0];
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
				return new I_GetConceptData[0];
			}
		}

		if (!idPositions.isEmpty()) {
			getRelationshipSources(tf, idPositions, elements);
		}

		return elements;
	}

	private void getRelationshipSources(I_TermFactory tf,
			final Map idPositions, final I_GetConceptData[] elements) {
		try {
			tf.iterateConcepts(new I_ProcessConcepts() {
				public void processConcept(I_GetConceptData concept)
						throws Exception {

					if (idPositions.isEmpty()) {
						return;
					}

					List<I_RelVersioned> rels = concept.getSourceRels();
					for (I_RelVersioned rel : rels) {

						if (idPositions.containsKey(rel.getRelId())) {

							ArrayList<Integer> positions = (ArrayList<Integer>) idPositions
									.remove(rel.getRelId());
							for (Integer position : positions) {
								elements[position.intValue()] = concept;
							}
						}
					}
				}
			});
		} catch (Exception e) {
			String logMessage = "An error occurred. An id exists that cannot be resolved as a concept, "
					+ "description or relationship";
			AceLog.getAppLog().severe(logMessage, e);
			AceLog.getAppLog().alertAndLogException(
					new TaskFailedException(logMessage));
		}
	}

	private I_GetConceptData getDescriptionParent(I_TermFactory tf,
			List<UUID> uuids) throws Exception {
		I_GetConceptData conceptInList;
		I_DescriptionVersioned desc = tf
				.getDescription(uuids.get(0).toString());
		conceptInList = tf.getConcept(desc.getConceptId());
		return conceptInList;
	}

	private I_GetConceptData getConcept(List<UUID> idList)
			throws TerminologyException, IOException {
		I_GetConceptData conceptInList = null;
		conceptInList = AceTaskUtil.getConceptFromObject(idList);

		// TODO - Review: Is there a better way to confirm it is not a concept?
		// We need to test as it could be a desc
		if (conceptInList.getConceptAttributes() == null) {
			throw new TerminologyException("No concept attributes found");
		}
		return conceptInList;
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[]{};
	}

	public String getUuidListListPropName() {
		return uuidListListPropName;
	}

	public void setUuidListListPropName(String uuidListListPropName) {
		this.uuidListListPropName = uuidListListPropName;
	}
}
