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
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.concept.component.description.DescriptionBinder;
import org.ihtsdo.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.WfHxIndexGenerator;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;

/**
 * Goal which loads an EConcept.jbin file into a bdb.
 * 
 * @goal load-econcepts
 * 
 * @phase process-sources
 */
public class LoadBdb extends AbstractMojo {
	/**
	 * concepts file name.
	 * 
	 * @parameter default-value="eConcepts.jbin"
	 * @required
	 */
	private String conceptsFileName;
	/**
     * workflow history text file to speed up lucene indexing.
	 * 
	 * @parameter 
	 */
	private String inputWfHxFilePath;
	/**
	 * Generated resources directory.
	 * 
	 * @parameter expression="${project.build.directory}/generated-resources"
	 */
	private String generatedResources;
	    
    /**
     * Generated resources directory.
     * 
     * @parameter expression="${project.build.directory}/berkeley-db"
     * @required
     */
    private File berkeleyDir;
    
    /**
     * Generated resources directory.
     * 
     * @parameter expression="${project.build.directory}/workflow"
     */
    private File wfLuceneDir;
    
	/**
	 * 
	 * @parameter default-value=true
	 */
    private boolean moveToReadOnly;


	AtomicInteger conceptsRead = new AtomicInteger();
	AtomicInteger conceptsProcessed = new AtomicInteger();

	ExecutorService executors = Executors.newCachedThreadPool();
	LinkedBlockingQueue<I_ProcessEConcept> converters = new LinkedBlockingQueue<I_ProcessEConcept>();
	private int runtimeConverterSize = Runtime.getRuntime().availableProcessors() * 2;
	private int converterSize = 1;

	public void execute() throws MojoExecutionException {
		try {
			for (int i = 0; i < converterSize; i++) {
				converters.put(new ConvertConcept());
			}

			long startTime = System.currentTimeMillis();
			File conceptsFile = new File(generatedResources, conceptsFileName);
			getLog().info("Starting load from: " + conceptsFile.getAbsolutePath());    

			FileIO.recursiveDelete(berkeleyDir);
            
            if (inputWfHxFilePath != null) {
            	Bdb.allowWfLuceneSetup(true);
            }

			Bdb.setup(berkeleyDir.getAbsolutePath());

			FileInputStream fis = new FileInputStream(conceptsFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream in = new DataInputStream(bis);

			try {
				while (true) {
					EConcept eConcept = new EConcept(in);
					conceptsRead.incrementAndGet();
					I_ProcessEConcept conceptConverter = converters.take();
					conceptConverter.setEConcept(eConcept);
					executors.execute(conceptConverter);
				}
			} catch (EOFException e) {
				in.close();
			}
			// See if any exceptions in the last converters;
			while (converters.isEmpty() == false) {
				I_ProcessEConcept conceptConverter = converters.take();
				conceptConverter.setEConcept(null);
			}

			while (conceptsProcessed.get() < conceptsRead.get()) {
				Thread.sleep(1000);
			}

			getLog().info("finished load, start sync");
			getLog().info("Concept count: " + Bdb.getConceptDb().getCount());
			getLog().info(
					"Concept attributes encountered: "
							+ ConceptAttributesBinder.encountered
							+ " written: " + ConceptAttributesBinder.written);
			getLog().info(
					"Descriptions encountered: "
							+ DescriptionBinder.encountered + " written: "
							+ DescriptionBinder.written);
			getLog().info(
					"Relationships encountered: "
							+ RelationshipBinder.encountered + " written: "
							+ RelationshipBinder.written);
			getLog().info(
					"Refexes encountered: "
							+ RefsetMemberBinder.encountered + " written: "
							+ RefsetMemberBinder.written);

			getLog().info("Starting db sync.");
			Bdb.sync();
			getLog().info("Finished db sync, starting generate lucene index.");
			createLuceneIndices();
			getLog().info("Finished create index, starting close.");
			Bdb.close();
			getLog().info("db closed");
			getLog().info("elapsed time: "
									+ (System.currentTimeMillis() - startTime));

			if (moveToReadOnly) {
				FileIO.recursiveDelete(new File(berkeleyDir, "read-only"));
				File dirToMove = new File(berkeleyDir, "mutable");
				dirToMove.renameTo(new File(berkeleyDir, "read-only"));
			}
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		} catch (Throwable ex) {
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}

	}

	private interface I_ProcessEConcept extends Runnable {

		public void setEConcept(EConcept eConcept) throws Throwable;

	}

	private class ConvertConcept implements I_ProcessEConcept {
		Throwable exception = null;
		EConcept eConcept = null;
		Concept newConcept = null;
		NidCNidMapBdb nidCnidMap;

		@Override
		public void run() {
			if (nidCnidMap == null) {
				nidCnidMap = Bdb.getNidCNidMap();
			}
			try {
				newConcept = Concept.get(eConcept);
				Bdb.getConceptDb().writeConcept(newConcept);
				Collection<Integer> nids = newConcept.getAllNids();
				assert nidCnidMap.getCNid(newConcept.getNid()) == newConcept.getNid();
				for (int nid: nids) {
					assert nidCnidMap.getCNid(nid) == newConcept.getNid();
				}
				conceptsProcessed.incrementAndGet();
			} catch (Throwable e) {
				exception = e;
			}
			try {
				converters.put(this);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.ihtsdo.db.bdb.I_ProcessEConcept#setEConcept(org.ihtsdo.etypes
		 * .EConcept)
		 */
		public void setEConcept(EConcept eConcept) throws Throwable {
			if (exception != null) {
				throw exception;
			}
			this.eConcept = eConcept;
		}
	}

    public void createLuceneIndices() throws Exception {
        LuceneManager.setLuceneRootDir(berkeleyDir, LuceneSearchType.DESCRIPTION);
        LuceneManager.createLuceneIndex(LuceneSearchType.DESCRIPTION);

        if (inputWfHxFilePath != null) {
        	LuceneManager.setLuceneRootDir(wfLuceneDir, LuceneSearchType.WORKFLOW_HISTORY);
        	WfHxIndexGenerator.setSourceInputFile(new File(inputWfHxFilePath));
        	LuceneManager.createLuceneIndex(LuceneSearchType.WORKFLOW_HISTORY);
        }
    }
}
