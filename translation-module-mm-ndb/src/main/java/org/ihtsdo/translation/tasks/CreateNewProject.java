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
package org.ihtsdo.translation.tasks;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.UnknownTransactionException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.refset.partition.RefsetSplitter;

/**
 * The Class CreateNewProject.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class CreateNewProject extends AbstractTask {
	
	/** The project name. */
	private String projectName;

	private String sequence;
	/** The Constant serialVersionUID. */

	private TermEntry sourceLanguageRefset;
	
	private TermEntry targetLanguageRefset;
	
	
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	private File bpFile;

	private WorkList workList2;

	private WorkList workList1;
	
	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(projectName);
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
			projectName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new creates the new project.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public CreateNewProject() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try{	
			String sourceUUID=(String) process.readAttachement("SourceRefsetUUID");
			String targetUUID=(String) process.readAttachement("TargetRefsetUUID");
			I_GetConceptData sourceLangConcept=null;
			I_GetConceptData targetLangConcept=null;
			if (sourceUUID ==null || sourceUUID.equals("")){

				sourceLangConcept = Terms.get().getConcept(sourceLanguageRefset.ids);
			}else{
				 sourceLangConcept=Terms.get().getConcept(UUID.fromString(sourceUUID));
			}

			if (targetUUID ==null || targetUUID.equals("")){
				targetLangConcept  = Terms.get().getConcept(targetLanguageRefset.ids);
			}else{

				 targetLangConcept=Terms.get().getConcept(UUID.fromString(targetUUID));
			}
			
			String seq=sequence;
			String name = projectName;
			I_ConfigAceFrame config=(I_ConfigAceFrame)Terms.get().getActiveAceFrameConfig();
			List<UUID> nullUid = null;
			TranslationProject tempProjectForMetadata = new TranslationProject(name, 0, nullUid);
			TranslationProject project = TerminologyProjectDAO.createNewTranslationProject(tempProjectForMetadata, config);

			I_TermFactory tf = Terms.get();


			I_GetConceptData rootConcept = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			I_GetConceptData colonoscopicPolypectomy = tf.getConcept(UUID.fromString("bd2ce7f7-20a5-3ce4-b044-141284395579"));
			I_GetConceptData pneumonitis = tf.getConcept(UUID.fromString("975420b8-e706-3744-a9f6-54180b5a5469"));

			I_IntSet allowedTypes = Terms.get().newIntSet();
			allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

			List<? extends I_RelTuple> relationships = rootConcept.getDestRelTuples(config.getAllowedStatus(), 
					allowedTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(),
					config.getConflictResolutionStrategy());
			//TODO review!!
			I_GetConceptData refset = tf.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE.getUids());
			for (I_RelTuple rel : relationships) {
				I_GetConceptData member = tf.getConcept(rel.getC1Id());
				tf.getRefsetHelper(config).newRefsetExtension(
						refset.getConceptNid(), 
						member.getConceptNid(), 
						EConcept.REFSET_TYPES.CID, 
						new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, member.getConceptNid())
						, config); 
			}
			tf.getRefsetHelper(config).newRefsetExtension(
					refset.getConceptNid(), 
					colonoscopicPolypectomy.getConceptNid(), 
					EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, colonoscopicPolypectomy.getConceptNid())
					, config);
			tf.getRefsetHelper(config).newRefsetExtension(
					refset.getConceptNid(), 
					pneumonitis.getConceptNid(), 
					EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, pneumonitis.getConceptNid())
					, config);


			tf.commit();

			project.addRefsetAsSourceLanguage(sourceLangConcept);
			sleep(1);

			project.setTargetLanguageRefset(targetLangConcept);

			sleep(1);

			WorkSet workSet1 = TerminologyProjectDAO.createNewWorkSet("WorkSet " + seq, 
					project.getUids().iterator().next(), config);

			sleep(1);

			workSet1.setSourceRefset(refset);

			sleep(1);

			workSet1.sync(config);



			PartitionScheme partitionScheme1 = TerminologyProjectDAO.createNewPartitionScheme(
					"partition Scheme " + seq, 
					workSet1.getUids().iterator().next(), config);
			sleep(1);

			RefsetSplitter refsetSplitter = new RefsetSplitter();

			List<Integer> portions = new ArrayList<Integer>();
			portions.add(50);
			portions.add(50);

			List<Partition> splitPartitions = refsetSplitter.splitRefset(partitionScheme1, 
					portions, "splitted partition " + seq, config);

			Partition partition1 = splitPartitions.get(0);
			Partition partition2 = splitPartitions.get(1);

			sleep(1);

			SwingUtilities.invokeAndWait(new Runnable() {


				public void run() {

					final JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setDialogTitle("Select BusinessProcess to attach...");

					class BPFilter extends javax.swing.filechooser.FileFilter {
						public boolean accept(File file) {
							String filename = file.getName();
							return filename.endsWith(".bp");
						}
						public String getDescription() {
							return "*.bp";
						}
					}

					fileChooser.addChoosableFileFilter(new BPFilter());


					int returnValue = fileChooser
					.showDialog(new Frame(), "Choose BP file");
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						bpFile = fileChooser.getSelectedFile();
					} else {
						try {
							throw new TaskFailedException("User failed to select a file.");
						} catch (TaskFailedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});

			BusinessProcess bp = getBusinessProcess( bpFile);

			 workList1 = TerminologyProjectDAO.generateWorkListFromPartition(
					partition1, "author.inbox", bp, "worklist "  + seq + ".1", config);
			sleep(1);


			 workList2 = TerminologyProjectDAO.generateWorkListFromPartition(
					partition2, "author.inbox", bp, "worklist "  + seq + ".2", config);
			sleep(1);




			return Condition.STOP;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	private static BusinessProcess getBusinessProcess(File f) {
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
			BusinessProcess processToLunch=(BusinessProcess) in.readObject();
			in.close();
			return processToLunch;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void sleep(int n){
		long t0, t1;
		t0 =  System.currentTimeMillis();
		do{
			t1 = System.currentTimeMillis();
		}
		while ((t1 - t0) < (n * 1000));
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {


		try {
			try {
				TerminologyProjectDAO.deliverWorklistBusinessProcessToOutbox(workList1, worker);
				TerminologyProjectDAO.deliverWorklistBusinessProcessToOutbox(workList2, worker);
			} catch (UnknownTransactionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CannotCommitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LeaseDeniedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PrivilegedActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return STOP_CONDITION;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {  };
	}
	
	/**
	 * Gets the project name.
	 * 
	 * @return the project name
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * Sets the project name.
	 * 
	 * @param projectName the new project name
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public TermEntry getSourceLanguageRefset() {
		return sourceLanguageRefset;
	}

	public void setSourceLanguageRefset(TermEntry sourceLanguageRefset) {
		this.sourceLanguageRefset = sourceLanguageRefset;
	}

	public TermEntry getTargetLanguageRefset() {
		return targetLanguageRefset;
	}

	public void setTargetLanguageRefset(TermEntry targetLanguageRefset) {
		this.targetLanguageRefset = targetLanguageRefset;
	}

}