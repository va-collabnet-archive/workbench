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

/**
 * @author Adam Flinton
 */

package org.ihtsdo.mojo.maven.importer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Extended class must have a maven goal and phase annotation
 * 
 * @goal CommonAceImporter
 * @phase process-resources
 */

public class CommonAceImporter extends AbstractMojo {
	// TODO Setup writers within ... Make PathFS set the IFW
	/**
	 * Location of the data directory to read from.
	 * 
	 * @parameter
	 * @required
	 */
	public File srcDir;

	/**
	 * Location of the ace data directory to write to.
	 * 
	 * @parameter
	 * @required
	 */
	public File aceDir;

	/**
	 * The effective date to associate with all changes.
	 * 
	 * @parameter
	 * @required
	 */
	public String effectiveDate;

	/**
	 * The default path name Used to setup the default ImportFileWriter
	 * 
	 * @parameter
	 * @required
	 */
	public String path;
	/**
	 *  Default File Writing class automatically init's by the 
	 *  value set via the maven path parameter above
	 */
	public ImportFileWriters ifw = new ImportFileWriters();
	/**
	 * A Hashtable for containing all the writers which have been asked for and which is then used
	 * at the end to close all writers.
	 */
	public Hashtable<String, Writer> writerStore = new Hashtable();

	/**
	 * A boolean used to decide if everything is OK just prior to calling writeToFiles() such that if an error has been
	 * found the process won't write out files
	 */
	public boolean OK2Write = false;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// setup
		try {
			getLog().info("CommonAceImporter srcDir = " + srcDir);
			getLog().info("ACE dir: " + aceDir);
			getLog().info("path: " + path);
			getLog().info("Effective Date: " + effectiveDate);
			aceDir.mkdirs();
			setLocalStrings();
			localSetup();

		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
		// Process
		try {
			getLog().info("CommonAceImporter about to call localProcess");
			localProcess();
			if (isOK2Write()) {
				writeToFiles();
			}
			closeWriters();
			postWriting();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

	}

	/**
	 * Called After closeReadersWriters Allows for writing of log files etc.
	 */
	public void postWriting() {

	}
	/**
	 * Sets up any local variables at the start of execute(). Usually overridden
	 * 
	 */
	public void localSetup() throws Exception {

	}

	/**
	 * Where the specific processing for a specific Importer will go
	 * 
	 * @throws Exception
	 */

	public void localProcess() throws Exception {

	}

	/**
	 * Called after the concepts and terms have been processed but defore the
	 * files are written Used to fill in missing values (for example if a
	 * preferred term in is not in the terms hashtable)
	 * 
	 * @throws Exception
	 */
	public void postProcessing() throws Exception {
	}

	/**
	 * Writes the relevant files out e.g. relationships.txt etc. 
	 * @throws Exception
	 */
	public void writeToFiles() throws Exception {

	}

	/**
	 * Allows a subclass to set all the local strings
	 * 
	 * @throws Exception
	 */
	public void setLocalStrings() throws Exception {
		

	}

	// Utility methods

	/**
	 * Closes the reader and writers to ensure that the buffers are clear
	 * 
	 * @throws Exception
	 */
	public void closeWriters() throws Exception {
		// getLog().info("closeWriters called ");
		for (Iterator<Writer> it1 = writerStore.values().iterator(); it1
				.hasNext();) {
			Writer writer = it1.next();
			writer.close();
		}

	}

	/**
	 * Gets a Writer for writing to relationships.txt
	 * @return
	 */
	public Writer getRelationshipsW() {
		return getWriter(ImportStatics.RELT);
	}
	/**
	 * Gets a Writer for writing to concepts.txt in aceDir
	 * @return
	 */
	public Writer getConceptsW() {
		return getWriter(ImportStatics.CONCEPTT);
	}
	/**
	 * Gets a Writer for writing to descriptions.txt in aceDir
	 * @return
	 */
	public Writer getDescW() {
		return getWriter(ImportStatics.DESCT);
	}
	/**
	 * Gets a Writer for writing to ids.txt in aceDir
	 * @return
	 */
	public Writer getIdsW() {
		return getWriter(ImportStatics.IDST);
	}
	/**
	 * Gets a Writer for writing to illicit_words.txt in aceDir
	 * @return
	 */
	public Writer getIllicitW() {
		return getWriter(ImportStatics.ILLICIT);
	}
	/**
	 * Gets a Writer for writing to licit_words.txt in aceDir
	 * @return
	 */
	public Writer getLicitW() {
		return getWriter(ImportStatics.LICIT);
	}
	/**
	 * Gets a Writer for writing to boolean.refset in aceDir
	 * @return
	 */
	public Writer getBoolRefsetW() {
		return getWriter(ImportStatics.BREF);
	}
	/**
	 * Gets a Writer for writing to concept.refset in aceDir
	 * @return
	 */
	public Writer getConceptRefsetW() {
		return getWriter(ImportStatics.CREF);
	}
	/**
	 * Gets a Writer for writing to conint.refset in aceDir
	 * @return
	 */
	public Writer getConceptIntRefsetW() {
		return getWriter(ImportStatics.CIREF);
	}
	/**
	 * Gets a Writer for writing to measurement.refset in aceDir
	 * @return
	 */
	public Writer getMeasureRefsetW() {
		return getWriter(ImportStatics.MREF);
	}
	/**
	 * Gets a Writer for writing to integer.refset in aceDir
	 * @return
	 */
	public Writer getIntRefsetW() {
		return getWriter(ImportStatics.IREF);
	}
	/**
	 * Gets a Writer for writing to string.refset in aceDir
	 * @return
	 */
	public Writer getStringRefsetW() {
		return getWriter(ImportStatics.SREF);
	}
	/**
	 * Gets a Writer for writing to language.refset in aceDir
	 * @return
	 */
	public Writer getLangRefsetW() {
		return getWriter(ImportStatics.LREF);
	}

	/**
	 * Get Writer - Assumes directory is aceDir
	 * @param key
	 * @return
	 */
	public Writer getWriter(String key) {
		return getWriter(aceDir,key);
	}
	
	/**
	 * Get Writer supply both target Directory and file name
	 * @param targetDir
	 * @param key
	 * @return
	 */

	public Writer getWriter(File targetDir, String key) {

		Writer bw = null;
		if (writerStore.get(key) == null) {
			try {
				bw = new BufferedWriter(new FileWriter(new File(targetDir, key),true));
				writerStore.put(key, bw);
				getLog().error("getWriter Creating Writer using "+key +" Current writerStore size = "+writerStore.size());
			} catch (IOException e) {
				getLog().error("getWriter IOException thrown ", e);
			}
		} else {
			bw = writerStore.get(key);
		}
		return bw;

	}
	
	
	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public boolean isOK2Write() {
		return OK2Write;
	}

	public void setOK2Write(boolean oK2Write) {
		OK2Write = oK2Write;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path1) {
		this.path = path1;
		getLog().error("setPath called Path = " + this.path);
		ifw.init(effectiveDate, path);
	}

}
