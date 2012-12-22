/*
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
package org.ihtsdo.translation.tasks.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.search.AbstractSearchTest;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;

/**
 * The Class SearchLangRefsetStatusDateUser.
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN)/*,
		@Spec(directory = "search", type = BeanType.TASK_BEAN)*/ })
		public class SearchLangRefsetStatusDateUser extends AbstractSearchTest {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/**
	 * Status concept for the term component to test.
	 */
	private TermEntry langRefsetTerm = new TermEntry(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
	
	/** The status term. */
	private TermEntry statusTerm = new TermEntry(ArchitectonicAuxiliary.Concept.STATUS.getUids());
	
	/** The day. */
	private Integer day = 1;
	
	/** The month. */
	private Integer month = 1;
	
	/** The year. */
	private Integer year = 2010;
	
	/** The author term. */
	private TermEntry authorTerm = new TermEntry(ArchitectonicAuxiliary.Concept.USER.getUids());;

	/**
	 * Write object.
	 *
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(this.statusTerm);
	}

	/**
	 * Read object.
	 *
	 * @param in the in
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			this.statusTerm = (TermEntry) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.ace.task.search.AbstractSearchTest#test(org.dwfa.ace.api.I_AmTermComponent, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	@Override
	public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
		try {
			I_GetConceptData langRefsetConcept = AceTaskUtil.getConceptFromObject(langRefsetTerm);
			I_GetConceptData statusToMatch = AceTaskUtil.getConceptFromObject(statusTerm);
			I_GetConceptData author = AceTaskUtil.getConceptFromObject(authorTerm);

			I_GetConceptData conceptToTest;
			if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
				conceptToTest = (I_GetConceptData) component;
			} else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
				I_DescriptionVersioned desc = (I_DescriptionVersioned) component;
				conceptToTest = Terms.get().getConcept(desc.getConceptNid());
			} else {
				return applyInversion(false);
			}

			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine("### testing status for: " + conceptToTest);
			}

			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			LanguageMembershipRefset langRefset;
			try {
				langRefset = new LanguageMembershipRefset(langRefsetConcept, config);
			} catch (Exception e) {
				return applyInversion(false);
			}
			PromotionRefset promotionRefset = langRefset.getPromotionRefset(config);
			
			if (promotionRefset == null) {
				return applyInversion(false);
			}

			int dateToMatch = Integer.MAX_VALUE;
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd" );
				Date javaDate = dateFormat.parse(year + "/" + month + "/" + day);
				dateToMatch = tf.convertToThinVersion(javaDate.getTime());
			} catch (ParseException e) {
				//return applyInversion(false);
			}
			
			Set<PositionBI> positionSet = new HashSet<PositionBI>();
			for (PositionBI loopPos : config.getViewPositionSetReadOnly()) {
				//TODO use input date
				positionSet.add(tf.newPosition(loopPos.getPath(), dateToMatch));
			}

			List<? extends I_ExtendByRef> members = tf.getAllExtensionsForComponent(conceptToTest.getConceptNid());
			I_ExtendByRefVersion lastTuple = null;
			for (I_ExtendByRef promotionMember : members) {
				if (promotionMember.getRefsetId() == promotionRefset.getRefsetId()) {
					List<? extends I_ExtendByRefVersion> tuples = promotionMember.getTuples(
							config.getAllowedStatus(), 
							new PositionSetReadOnly(positionSet), 
							Precedence.TIME, 
							config.getConflictResolutionStrategy());
					if (tuples != null && !tuples.isEmpty()) {
						for (I_ExtendByRefVersion loopTuple : tuples) {
							if (lastTuple == null || lastTuple.getTime() < loopTuple.getTime()) {
								lastTuple = loopTuple;
							}
						}
					}
				}
			}
			
			if (lastTuple == null) {
				return applyInversion(false);
			}
			
			if (lastTuple.getStatusNid() == statusToMatch.getConceptNid()) {
				return applyInversion(true);
			} else {
				return applyInversion(false);
			}

		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
	}

	/**
	 * Gets the status term.
	 *
	 * @return the status term
	 */
	public TermEntry getStatusTerm() {
		return statusTerm;
	}

	/**
	 * Sets the status term.
	 *
	 * @param statusTerm the new status term
	 */
	public void setStatusTerm(TermEntry statusTerm) {
		this.statusTerm = statusTerm;
	}

	/**
	 * Gets the lang refset term.
	 *
	 * @return the lang refset term
	 */
	public TermEntry getLangRefsetTerm() {
		return langRefsetTerm;
	}

	/**
	 * Sets the lang refset term.
	 *
	 * @param langRefsetTerm the new lang refset term
	 */
	public void setLangRefsetTerm(TermEntry langRefsetTerm) {
		this.langRefsetTerm = langRefsetTerm;
	}

	/**
	 * Gets the day.
	 *
	 * @return the day
	 */
	public Integer getDay() {
		return day;
	}

	/**
	 * Sets the day.
	 *
	 * @param day the new day
	 */
	public void setDay(Integer day) {
		this.day = day;
	}

	/**
	 * Gets the month.
	 *
	 * @return the month
	 */
	public Integer getMonth() {
		return month;
	}

	/**
	 * Sets the month.
	 *
	 * @param month the new month
	 */
	public void setMonth(Integer month) {
		this.month = month;
	}

	/**
	 * Gets the year.
	 *
	 * @return the year
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * Sets the year.
	 *
	 * @param year the new year
	 */
	public void setYear(Integer year) {
		this.year = year;
	}

	/**
	 * Gets the author term.
	 *
	 * @return the author term
	 */
	public TermEntry getAuthorTerm() {
		return authorTerm;
	}

	/**
	 * Sets the author term.
	 *
	 * @param authorTerm the new author term
	 */
	public void setAuthorTerm(TermEntry authorTerm) {
		this.authorTerm = authorTerm;
	}

}
