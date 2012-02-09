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
package org.ihtsdo.project.tasks;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.DescCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class promotes target language contento to release path.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/workflow2", type = BeanType.TASK_BEAN)})
public class PromoteTargetContentToReleasePath extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
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
		if (!(objDataVersion == 1)) {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/**
	 * Instantiates a new creates the new project.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public PromoteTargetContentToReleasePath() throws MalformedURLException {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
			.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
			TerminologyStoreDI ts = Ts.get();

			int activeNid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();

			WfInstance instance = (WfInstance) process.readAttachement("WfInstance");

			I_TerminologyProject iproject = TerminologyProjectDAO.getProjectForWorklist(instance.getWorkList(), config);
			TranslationProject project = (TranslationProject) iproject;
			int targetLangNid = project.getTargetLanguageRefset().getNid();
			I_GetConceptData pathConcept = project.getReleasePath();
			EditCoordinate ec = new EditCoordinate(config.getDbConfig().getUserConcept().getNid(), 
					pathConcept.getNid());

			TerminologyBuilderBI tc = ts.getTerminologyBuilder(ec, config.getViewCoordinate());

			ConceptChronicleBI concept = Ts.get().getConcept(instance.getComponentId());

			for (DescriptionChronicleBI loopDescription : concept.getDescs()) {
				DescriptionVersionBI lastDescVersion = loopDescription.getVersion(config.getViewCoordinate());
				if (lastDescVersion != null) {
					DescCAB dcab = lastDescVersion.makeBlueprint(config.getViewCoordinate());
					boolean written = false;
					for (RefexChronicleBI<?> loopAnnotChronicle : loopDescription.getAnnotations()) {
						if (loopAnnotChronicle.getCollectionNid() == targetLangNid) {
							if (!written) {
								tc.construct(dcab);
								written = true;
							}
							RefexVersionBI loopAnnotV = loopAnnotChronicle.getVersion(config.getViewCoordinate());
							RefexCnidVersionBI loopAnnotC = (RefexCnidVersionBI) loopAnnotV;
							RefexCAB acab = loopAnnotC.makeBlueprint(config.getViewCoordinate());
							RefexChronicleBI<?> newRefexForProm = tc.construct(acab);
							concept.addAnnotation(newRefexForProm);
						}
					}
				}
			}

			Terms.get().addUncommittedNoChecks((I_GetConceptData) concept);
			Terms.get().commit();

			return Condition.CONTINUE;
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
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