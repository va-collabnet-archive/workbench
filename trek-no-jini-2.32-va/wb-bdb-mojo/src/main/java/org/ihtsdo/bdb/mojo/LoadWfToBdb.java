package org.ihtsdo.bdb.mojo;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 * Goal which loads an EConcept.jbin file into a bdb.
 * 
 * @goal load-wf-econcept
 * 
 * @phase process-sources
 */
public class LoadWfToBdb extends AbstractMojo {
	/**
	 * Generated resources directory.
	 * 
	 * @parameter expression="${project.build.directory}/generated-resources/berkeley-db"
	 * @required
	 */
	private File berkeleyDir;

	/**
	 * Generated resources directory.
	 * 
	 * @parameter expression="${project.build.directory}/workflow/lucene"
	 */
	private File wfLuceneDir;

	public void execute() throws MojoExecutionException {
        ViewCoordinate vc;
		try {
	        try {
	        	// Setup Database
				Bdb.setup(berkeleyDir.getAbsolutePath());
	        	vc = DefaultConfig.newProfile().getViewCoordinate();
	        } catch (Exception e) {
				throw new MojoExecutionException("Failed to setup or read database successfully from directory: " + berkeleyDir.getAbsolutePath(), e);
	        }
        
	        // Init Lucene
			LuceneManager.setLuceneRootDir(wfLuceneDir, LuceneSearchType.WORKFLOW_HISTORY);

	        // Generate Lucene Index
	        LuceneManager.createLuceneIndex(LuceneSearchType.WORKFLOW_HISTORY, vc);

	        // Close
			LuceneManager.close(LuceneSearchType.WORKFLOW_HISTORY);
			Bdb.close();
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}


}
