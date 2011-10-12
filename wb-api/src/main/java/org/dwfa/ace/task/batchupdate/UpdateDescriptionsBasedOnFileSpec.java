/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.task.batchupdate;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.swing.JList;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

@BeanList(specs = { @Spec(directory = "tasks/ide/file", type = BeanType.TASK_BEAN) })
public class UpdateDescriptionsBasedOnFileSpec extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private String inputFilePropName = ProcessAttachmentKeys.PROCESS_FILENAME.getAttachmentKey();

	private int hostIndex = 3;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeInt(hostIndex);
		out.writeObject(inputFilePropName);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			hostIndex = in.readInt();
			inputFilePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do...

	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		try {
			String filename = (String) process.getProperty(inputFilePropName);
			File inputFile = new File(filename);
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			I_HelpRefsets refsetHelper = tf.getRefsetHelper(config);
			JList conceptList = config.getBatchConceptList();
			I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
			model.clear();
			int lines = 0;
			int modified = 0;
			int skipped = 0;

			HashSet<I_ShowActivity> activities = new HashSet<I_ShowActivity>();
			I_ShowActivity activity = null;
			if (!DwfaEnv.isHeadless()) {
				activity = Terms.get().newActivityPanel(true, config, 
						"<html>Batch descriptions change based on: " + filename, true);
				activities.add(activity);
				activity.setValue(0);
				activity.setIndeterminate(true);
				activity.setProgressInfoLower("Performing change...");
				Terms.get().getActiveAceFrameConfig().setStatusMessage("Batch descriptions change...");
			}
			long startTime = System.currentTimeMillis();

			try{
				FileInputStream fstream = new FileInputStream(inputFile);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				br.readLine(); // skip header
				while ((strLine = br.readLine()) != null)   {
					lines++;
					String columns[] =  strLine.split("\\|");
					String descriptionId = columns[0];
					String oldText = columns[1];
					String newText = columns[2];
					SearchResult results = tf.doLuceneSearch(descriptionId);
					int dnid = Integer.valueOf(results.searcher.doc(results.topDocs.scoreDocs[0].doc).get("dnid"));
					I_DescriptionVersioned oldDescription = tf.getDescription(dnid);
					I_GetConceptData concept = tf.getConcept(oldDescription.getConceptNid());
					I_DescriptionTuple tuple = null;
					for (I_DescriptionTuple loopTuple : concept.getDescriptionTuples(null, null, 
							config.getViewPositionSetReadOnly(), config.getPrecedence(), 
							config.getConflictResolutionStrategy())) {
						if (loopTuple.getDescId() == dnid) {
							tuple = loopTuple;
						}
					}

					if (tuple != null && tuple.getText().equals(oldText)) {

						I_DescriptionPart newPart= (I_DescriptionPart) tuple.makeAnalog(
								SnomedMetadataRfx.getSTATUS_RETIRED_NID(), 
								config.getDbConfig().getUserConcept().getNid(), 
								config.getEditingPathSet().iterator().next().getConceptNid(), 
								Long.MAX_VALUE);

						I_DescriptionVersioned newDescription = tf.newDescription(UUID.randomUUID(), 
								concept, 
								newPart.getLang(), 
								newText, 
								tf.getConcept(newPart.getTypeNid()), 
								config, 
								tf.getConcept(SnomedMetadataRfx.getSTATUS_CURRENT_NID()), 
								Long.MAX_VALUE);

						List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(
								oldDescription.getDescId(), true);
						for (I_ExtendByRef extension : extensions) {
							if (extension.getRefsetId() == SnomedMetadataRfx.getUS_DIALECT_REFEX_NID() ||
									extension.getRefsetId() == SnomedMetadataRfx.getGB_DIALECT_REFEX_NID()) {
								I_ExtendByRefVersion loopTuple = extension.getTuples(
										config.getAllowedStatus(), 
										config.getViewPositionSetReadOnly(), 
										config.getPrecedence(), 
										config.getConflictResolutionStrategy()).iterator().next();
								I_ExtendByRefPartCid newExtConceptPart = (I_ExtendByRefPartCid) 
								loopTuple.makeAnalog(SnomedMetadataRfx.getSTATUS_RETIRED_NID(), 
										config.getDbConfig().getUserConcept().getConceptNid(), 
										config.getEditingPathSetReadOnly().iterator().next().getConceptNid(),
										Long.MAX_VALUE);

								refsetHelper.newRefsetExtension(extension.getRefsetId(), 
										newDescription.getDescId(), EConcept.REFSET_TYPES.CID, 
										new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, 
												newExtConceptPart.getC1id()), config);
								
							}
						}
						boolean equalsSynonymExists = false;
						String semtagLessDescription = newDescription.getText().substring(0,
								newDescription.getText().lastIndexOf("(") - 1).trim();
						if (oldDescription.getTypeNid() == SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID()) {
							for (I_DescriptionTuple loopTuple : concept.getDescriptionTuples(
									config.getAllowedStatus(), 
									config.getDescTypes(), 
									config.getViewPositionSetReadOnly(), 
									config.getPrecedence(), 
									config.getConflictResolutionStrategy())) {
								if (loopTuple.getText().equals(semtagLessDescription)) {
									equalsSynonymExists = true;
								}
							}
						}

						if (!equalsSynonymExists) {
							I_DescriptionVersioned newSynonym = tf.newDescription(UUID.randomUUID(), 
									concept, 
									newPart.getLang(), 
									semtagLessDescription, 
									tf.getConcept(SnomedMetadataRfx.getDES_SYNONYM_NID()), 
									config, 
									tf.getConcept(SnomedMetadataRfx.getSTATUS_CURRENT_NID()), 
									Long.MAX_VALUE);

							for (I_ExtendByRef extension : extensions) {
								if (extension.getRefsetId() == SnomedMetadataRfx.getUS_DIALECT_REFEX_NID() ||
										extension.getRefsetId() == SnomedMetadataRfx.getGB_DIALECT_REFEX_NID()) {
									refsetHelper.newRefsetExtension(extension.getRefsetId(), 
											newSynonym.getDescId(), EConcept.REFSET_TYPES.CID, 
											new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, 
													SnomedMetadataRfx.getDESC_ACCEPTABLE_NID()), config);
								}
							}

						}

						model.addElement(concept);
						tf.addUncommittedNoChecks(concept);
						modified++;
					} else {
						skipped++;
					}
				}
				in.close();
				if (!DwfaEnv.isHeadless()) {
					long endTime = System.currentTimeMillis();
					long elapsed = endTime - startTime;
					String elapsedStr = getElapsedText(elapsed);
					activity.setProgressInfoUpper("<html>Batch update finished: Lines: " + lines + 
							" Modified: " + modified + " Skipped: " + skipped);
					activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";");
					activity.complete();
					Terms.get().getActiveAceFrameConfig().setStatusMessage("");
				}
				if (modified > 0) {
					config.setCommitEnabled(true);
				}
				JOptionPane.showMessageDialog(null,
						"Batch update finished: Lines: " + lines + 
						" Modified: " + modified + " Skipped: " + skipped );
			}catch (Exception e){
				System.err.println("Error: " + e.getMessage());
			}
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}

		return Condition.CONTINUE;
	}

	public double round1(double value) {
		return Math.round(value * 10.0) / 10.0;
	}

	public String getElapsedText(long elapsedMillis) {
		if(elapsedMillis < 60000) {
			double unit = round1(elapsedMillis / 1000.0); 
			return unit + (unit == 1 ? " second" : " seconds");
		}
		else if(elapsedMillis < 60000 * 60) {
			double unit = round1(elapsedMillis / 60000.0); 
			return unit + (unit == 1 ? " minute" : " minutes");
		}
		else if(elapsedMillis < 60000 * 60 * 24) {
			double unit = round1(elapsedMillis / (60000.0 * 60)); 
			return unit + (unit == 1 ? " hour" : " hours");
		}
		else {
			double unit = round1(elapsedMillis / (60000.0 * 60 * 24)); 
			return unit + (unit == 1 ? " day" : " days");
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Integer getHostIndex() {
		return hostIndex;
	}

	public void setHostIndex(Integer hostIndex) {
		this.hostIndex = hostIndex;
	}

	public String getInputFilePropName() {
		return inputFilePropName;
	}

	public void setInputFilePropName(String inputFilePropName) {
		this.inputFilePropName = inputFilePropName;
	}

}
