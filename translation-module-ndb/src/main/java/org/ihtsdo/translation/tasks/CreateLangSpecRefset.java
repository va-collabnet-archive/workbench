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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.refset.LanguageSpecRefset;

/**
 * The Class CreateNewContextualizedDescription.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class CreateLangSpecRefset extends AbstractTask {

	/** The Lang spec name. */
	private String LangSpecName;
	
	/** The parent concept. */
	private TermEntry parentConcept;

	/** The origin lang member refset. */
	private TermEntry originLangMemberRefset;

	/** The lang member refset. */
	private TermEntry langMemberRefset;


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
		out.writeObject(LangSpecName);
		out.writeObject(parentConcept);
		out.writeObject(originLangMemberRefset);
		out.writeObject(langMemberRefset);
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
			LangSpecName=(String)in.readObject();
			parentConcept = (TermEntry) in.readObject();
			originLangMemberRefset = (TermEntry) in.readObject();
			langMemberRefset = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		I_TermFactory tf = Terms.get();

		try{
			I_GetConceptData pConcept = tf.getConcept(parentConcept.ids);
			I_GetConceptData originConcept = tf.getConcept(originLangMemberRefset.ids);
			I_GetConceptData targetConcept = tf.getConcept(langMemberRefset.ids);

			LanguageSpecRefset langSpecRefset = LanguageSpecRefset.createNewLanguageSpecRefset(
					LangSpecName, pConcept.getConceptNid(), 
					targetConcept.getConceptNid(), originConcept.getConceptNid(),tf.getActiveAceFrameConfig());
			

			langSpecRefset.computeLanguageRefsetSpec(tf.getActiveAceFrameConfig());

			tf.commit();
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
		return Condition.CONTINUE;
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

	/**
	 * Gets the lang spec name.
	 *
	 * @return the lang spec name
	 */
	public String getLangSpecName() {
		return LangSpecName;
	}

	/**
	 * Sets the lang spec name.
	 *
	 * @param langSpecName the new lang spec name
	 */
	public void setLangSpecName(String langSpecName) {
		LangSpecName = langSpecName;
	}

	/**
	 * Gets the parent concept.
	 *
	 * @return the parent concept
	 */
	public TermEntry getParentConcept() {
		return parentConcept;
	}

	/**
	 * Sets the parent concept.
	 *
	 * @param parentConcept the new parent concept
	 */
	public void setParentConcept(TermEntry parentConcept) {
		this.parentConcept = parentConcept;
	}

	/**
	 * Gets the origin lang member refset.
	 *
	 * @return the origin lang member refset
	 */
	public TermEntry getOriginLangMemberRefset() {
		return originLangMemberRefset;
	}

	/**
	 * Sets the origin lang member refset.
	 *
	 * @param originLangMemberRefset the new origin lang member refset
	 */
	public void setOriginLangMemberRefset(TermEntry originLangMemberRefset) {
		this.originLangMemberRefset = originLangMemberRefset;
	}

	/**
	 * Gets the lang member refset.
	 *
	 * @return the lang member refset
	 */
	public TermEntry getLangMemberRefset() {
		return langMemberRefset;
	}

	/**
	 * Sets the lang member refset.
	 *
	 * @param langMemberRefset the new lang member refset
	 */
	public void setLangMemberRefset(TermEntry langMemberRefset) {
		this.langMemberRefset = langMemberRefset;
	}


}