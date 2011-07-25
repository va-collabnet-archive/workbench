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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.concept.component.description.DescriptionBinder;
import org.ihtsdo.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.PositionMapper;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.WfHxIndexGenerator;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * Goal which loads an EConcept.jbin file into a bdb.
 * 
 * @goal load-wf-econcept
 * 
 * @phase process-sources
 */
public class LoadWfToBdb extends AbstractMojo {
	/**
	 * workflow history text file to speed up lucene indexing.
	 * 
	 * @parameter
	 */
	private String inputWfHxFilePath;

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
	 * @parameter expression="${project.build.directory}/workflow"
	 */
	private File wfLuceneDir;

	public void execute() throws MojoExecutionException {
		try {
            Bdb.allowWfLuceneSetup(true);
			Bdb.setup(berkeleyDir.getAbsolutePath());

			LuceneManager.setLuceneRootDir(wfLuceneDir, LuceneSearchType.WORKFLOW_HISTORY);
	        WfHxIndexGenerator.setSourceInputFile(new File(inputWfHxFilePath));
	        LuceneManager.createLuceneIndex(LuceneSearchType.WORKFLOW_HISTORY, DefaultConfig.newProfile().getViewCoordinate());

			Bdb.close();
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		} catch (Throwable ex) {
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}

	}


}
