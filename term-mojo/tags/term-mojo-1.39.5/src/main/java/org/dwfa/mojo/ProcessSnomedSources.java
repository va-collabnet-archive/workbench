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
package org.dwfa.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

public abstract class ProcessSnomedSources extends AbstractMojo {
	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}/generated-resources"
	 * @required
	 */
	File outputDirectory;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 * @required
	 */
	private File sourceDirectory;

	/**
	* Project instance, used to add new source directory to the build.
	* @parameter default-value="${project}"
	* @required
	* @readonly
	*/
	private MavenProject project;
	
	/**
	* project-helper instance, used to make addition of resources
	* simpler.
	* @component
	*/
	private MavenProjectHelper helper;
	
	@SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException {
		File snomedDir = new File(sourceDirectory.getAbsolutePath()
				+ "/../SNOMED/");
		try {
			String directory = outputDirectory.getCanonicalPath();
			List includes = Collections.singletonList("**/*");
			List excludes = null;
			helper.addResource(project, directory, includes, excludes);
			setup();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			
			for (File releaseDir : snomedDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith(".") == false;
				}})) {
				getLog().info(releaseDir.getName());
				Date releaseDate = dateFormat.parse(releaseDir.getName());
				
				for (File contentFile : releaseDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.startsWith("sct");
					}})) {
					getLog().info(contentFile.getName());
					FileReader fr;
					fr = new FileReader(contentFile);
					BufferedReader br = new BufferedReader(fr);
					if (contentFile.getName().startsWith("sct_concepts_")) {
						readConcepts(br, releaseDate);
					} else if (contentFile.getName().startsWith(
							"sct_descriptions_")) {
						readDescriptions(br, releaseDate);
					} else if (contentFile.getName().startsWith(
							"sct_relationships_")) {
						readRelationships(br, releaseDate);
					}
					br.close();
				}
			}
			
			cleanup();
		} catch (Exception e) {
			throw new MojoExecutionException("Processing file " + snomedDir, e);
		}
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
			// CONCEPTID
			long conceptKey = Long.parseLong(st.sval);
			// CONCEPTSTATUS
			tokenType = st.nextToken();
			int conceptStatus = Integer.parseInt(st.sval);
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
			int defChar = Integer.parseInt(st.sval);

			writeConcept(releaseDate, conceptKey, conceptStatus,
					defChar);
			concepts++;

			// CR
			tokenType = st.nextToken();
			// LF
			tokenType = st.nextToken();

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
			long relID = Long.parseLong(st.sval);
			// CONCEPTID1
			tokenType = st.nextToken();
			long conceptOneID = Long.parseLong(st.sval);
			// RELATIONSHIPTYPE
			tokenType = st.nextToken();
			long relationshipTypeConceptID = Long.parseLong(st.sval);
			// CONCEPTID2
			tokenType = st.nextToken();
			long conceptTwoID = Long.parseLong(st.sval);
			// CHARACTERISTICTYPE
			tokenType = st.nextToken();
			long characteristic = Long.parseLong(st.sval);
			// REFINABILITY
			tokenType = st.nextToken();
			long refinability = Long.parseLong(st.sval);
			// RELATIONSHIPGROUP
			tokenType = st.nextToken();
			int group = Integer.parseInt(st.sval);

			writeRelationship(releaseDate, relID, conceptOneID,
					relationshipTypeConceptID, conceptTwoID, characteristic,
					refinability, group);
			rels++;

			// CR
			tokenType = st.nextToken();
			// LF
			tokenType = st.nextToken();

			// Beginning of loop
			tokenType = st.nextToken();
		}
		getLog().info(
				"Process time: " + (System.currentTimeMillis() - start)
						+ " Parsed relationsips: " + rels);
	}

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
			long descriptionId = Long.parseLong(st.sval);
			// DESCRIPTIONSTATUS
			tokenType = st.nextToken();

			int status = Integer.parseInt(st.sval);
			// CONCEPTID
			tokenType = st.nextToken();
			long conceptId = Long.parseLong(st.sval);
			// TERM
			tokenType = st.nextToken();
			String text = st.sval;
			// INITIALCAPITALSTATUS
			tokenType = st.nextToken();
			int capStatus = Integer.parseInt(st.sval);

			// DESCRIPTIONTYPE
			tokenType = st.nextToken();
			int typeInt = Integer.parseInt(st.sval);

			// LANGUAGECODE
			tokenType = st.nextToken();
			String lang = st.sval;

			writeDescription(releaseDate, descriptionId, status,
					conceptId, text, capStatus, typeInt, lang);
			descriptions++;

			// CR
			tokenType = st.nextToken();
			// LF
			tokenType = st.nextToken();

			// Beginning of loop
			tokenType = st.nextToken();
		}
		getLog().info(
				"Process time: " + (System.currentTimeMillis() - start)
						+ " Parsed descriptions: " + descriptions);
	}

	private void skipLineOne(StreamTokenizer st) throws IOException {
		int tokenType = st.nextToken();
		while (tokenType != StreamTokenizer.TT_EOL) {
			tokenType = st.nextToken();
		}

	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public abstract void setup() throws Exception;
	public abstract void cleanup() throws Exception;
	public abstract void writeConcept(
			Date releaseDate, long conceptKey, int conceptStatus, int defChar)
			throws Exception;

	public abstract void writeRelationship(
			Date releaseDate, long relID, long conceptOneID,
			long relationshipTypeConceptID, long conceptTwoID,
			long characteristic, long refinability, int group)
			throws Exception;

	public abstract void writeDescription(
			Date releaseDate, long descriptionId, int status, long conceptId,
			String text, int capStatus, int typeInt, String lang)
			throws Exception;
}
