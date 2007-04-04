package org.dwfa.vodb;


/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.vodb.bind.ThinVersionHelper;

import com.sleepycat.je.DatabaseException;

public abstract class ProcessSources  {

	public abstract Logger getLog();
		
	boolean skipFirstLine;
	private List<Date> releaseDates = new ArrayList<Date>();
	protected ThinVersionHelper vh = new ThinVersionHelper();

	public ProcessSources(boolean skipFirstLine) throws DatabaseException {
		super();
		this.skipFirstLine = skipFirstLine;
	}

	protected void readConcepts(Reader r,
			Date releaseDate) throws Exception {
		// CONCEPTID CONCEPTSTATUS FULLYSPECIFIEDNAME CTV3ID SNOMEDID
		// ISPRIMITIVE
		long start = System.currentTimeMillis();

		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.wordChars('\u001F', '\u00FF');
		st.whitespaceChars('\t', '\t');
		st.eolIsSignificant(true);
		int concepts = 0;

		skipLineOne(st);
		int tokenType = st.nextToken();
		while (tokenType != StreamTokenizer.TT_EOF) {
			Object conceptKey = getId(st);
			// CONCEPTSTATUS
			tokenType = st.nextToken();
			Object conceptStatus =  getStatus(st);
			// FULLYSPECIFIEDNAME
			// Present in the descriptions table, so can ignore
			tokenType = st.nextToken();
			// CTV3ID
			// Do nothing with the legacy CTV3ID
			tokenType = st.nextToken();
			// SNOMEDID
			// Do nothing with the legacy SNOMED id
			tokenType = st.nextToken();
			// ISPRIMITIVE
			tokenType = st.nextToken();
			//convert to "defined"
			boolean defChar = !parseBoolean(st);
			writeConcept(releaseDate, conceptKey, conceptStatus,
					defChar);
			concepts++;

			// CR or LF
			tokenType = st.nextToken();
			if (tokenType == 13) { // is CR
				// LF
				tokenType = st.nextToken();
			}

			// Beginning of loop
			tokenType = st.nextToken();
		}
		getLog().info(
				"Process time: " + (System.currentTimeMillis() - start)
						+ " Parsed SNOMED concepts: " + concepts);
	}

	protected void readRelationships(Reader r,
			Date releaseDate) throws Exception {
		// RELATIONSHIPID
		// CONCEPTID1
		// RELATIONSHIPTYPE
		// CONCEPTID2
		// CHARACTERISTICTYPE
		// REFINABILITY
		// RELATIONSHIPGROUP

		long start = System.currentTimeMillis();
		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.wordChars('\u001F', '\u00FF');
		st.whitespaceChars('\t', '\t');
		st.eolIsSignificant(true);
		int rels = 0;

		skipLineOne(st);
		int tokenType = st.nextToken();
		while (tokenType != StreamTokenizer.TT_EOF) {
			// RELATIONSHIPID
			Object relID = getId(st);
			// CONCEPTID1
			tokenType = st.nextToken();
			Object conceptOneID = getId(st);
			// RELATIONSHIPTYPE
			tokenType = st.nextToken();
			Object relationshipTypeConceptID = getId(st);
			// CONCEPTID2
			tokenType = st.nextToken();
			Object conceptTwoID = getId(st);
			// CHARACTERISTICTYPE
			tokenType = st.nextToken();
			Object characteristic = getCharacteristic(st);
			// REFINABILITY
			tokenType = st.nextToken();
			Object refinability = getRefinability(st);
			// RELATIONSHIPGROUP
			tokenType = st.nextToken();
			int group = Integer.parseInt(st.sval);

			writeRelationship(releaseDate, relID, conceptOneID,
					relationshipTypeConceptID, conceptTwoID, characteristic,
					refinability, group);
			rels++;

			// CR or LF
			tokenType = st.nextToken();
			if (tokenType == 13) { // is CR
				// LF
				tokenType = st.nextToken();
			}

			// Beginning of loop
			tokenType = st.nextToken();
		}
		getLog().info(
				"Process time: " + (System.currentTimeMillis() - start)
						+ " Parsed relationsips: " + rels);
	}

	protected abstract Object getId(StreamTokenizer st);
	

	protected void readDescriptions(Reader r,
			Date releaseDate) throws Exception {
		// DESCRIPTIONID
		// DESCRIPTIONSTATUS
		// CONCEPTID
		// TERM
		// INITIALCAPITALSTATUS
		// DESCRIPTIONTYPE
		// LANGUAGECODE
		long start = System.currentTimeMillis();

		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.wordChars('\u001F', '\u00FF');
		st.whitespaceChars('\t', '\t');
		st.eolIsSignificant(true);
		int descriptions = 0;

		skipLineOne(st);
		int tokenType = st.nextToken();

		while (tokenType != StreamTokenizer.TT_EOF) {
			// DESCRIPTIONID
			Object descriptionId = getId(st);

			// DESCRIPTIONSTATUS
			tokenType = st.nextToken();

			Object status = getStatus(st);
			// CONCEPTID
			tokenType = st.nextToken();
			Object conceptId = getId(st);
			// TERM
			tokenType = st.nextToken();
			String text = st.sval;
			// INITIALCAPITALSTATUS
			tokenType = st.nextToken();
			boolean capSignificant = parseBoolean(st);

			// DESCRIPTIONTYPE
			tokenType = st.nextToken();
			Object typeInt = getDescType(st);

			// LANGUAGECODE
			tokenType = st.nextToken();
			String lang = st.sval;

			writeDescription(releaseDate, descriptionId, status,
					conceptId, text, capSignificant, typeInt, lang);
			descriptions++;

			// CR or LF
			tokenType = st.nextToken();
			if (tokenType == 13) { // is CR
				// LF
				tokenType = st.nextToken();
			}

			// Beginning of loop
			tokenType = st.nextToken();
		}
		getLog().info(
				"Process time: " + (System.currentTimeMillis() - start)
						+ " Parsed descriptions: " + descriptions);
	}

	protected abstract boolean parseBoolean(StreamTokenizer st);

	protected abstract Object getStatus(StreamTokenizer st);

	protected abstract Object getDescType(StreamTokenizer st);

	protected abstract Object getRefinability(StreamTokenizer st);
	
	protected abstract Object getCharacteristic(StreamTokenizer st);

	private void skipLineOne(StreamTokenizer st) throws IOException {
		if (skipFirstLine) {
			int tokenType = st.nextToken();
			while (tokenType != StreamTokenizer.TT_EOL) {
				tokenType = st.nextToken();
			}
		}
	}

	public void addReleaseDate(Date releaseDate) {
		releaseDates.add(releaseDate);
	}

	public int[] getReleaseDates() {
		int[] releases = new int[releaseDates.size()];
		int i = 0;
		for (Date rdate: releaseDates) {
			releases[i] = ThinVersionHelper.convert(rdate.getTime());
			i++;
		}
		return releases;
	}
	public abstract void execute(File snomedDir) throws Exception;
	public abstract void cleanup(I_IntSet relsToIgnore) throws Exception;
	public abstract void writeConcept(
			Date releaseDate, Object conceptKey, Object conceptStatus, boolean defChar)
			throws Exception;

	public abstract void writeRelationship(
			Date releaseDate, Object relID, Object conceptOneID,
			Object relationshipTypeConceptID, Object conceptTwoID,
			Object characteristic, Object refinability, int group)
			throws Exception;

	public abstract void writeDescription(
			Date releaseDate, Object descriptionId, Object status, Object conceptId,
			String text, boolean capStatus, Object typeInt, String lang)
			throws Exception;
}
