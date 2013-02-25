package org.ihtsdo.mojo.schema;

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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.dto.concept.TkConcept;

/**
 * Goal that runs transformation of the current open database, creating a binary EConcepts file as a result.
 *
 * @goal transform
 * 
 * @phase process-sources
 */
public class TransformEConceptMojo
extends AbstractMojo
{
	/**
	 * Location of the file.
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * transformerId
	 * 
	 * @parameter
	 * @required
	 */
	private String transformerId;

	/**
	 * xmlFile
	 * 
	 * @parameter
	 * @required
	 */
	private String xmlFile;
	public static HashMap<String,Object> supportMap;
	private final Semaphore writeSemaphore = new Semaphore(1);

    @Override
	public void execute() throws MojoExecutionException {
		try {
			I_TermFactory tf = Terms.get();
			TerminologyStoreDI ts = Ts.get();
			DataOutputStream eConceptDOS;
            
			TransformersConfigApi api = new TransformersConfigApi(xmlFile);

			String className = api.getValueAt(api.getIntId(transformerId), "class");

			Class theClass  = Class.forName(className);
			AbstractTransformer transformer = (AbstractTransformer)theClass.newInstance();
			transformer.setupFromXml(xmlFile);

			File eConceptsFile = new File(targetDirectory, "/eConcepts.jbin");
			eConceptsFile.getParentFile().mkdirs();
			BufferedOutputStream eConceptsBos = new BufferedOutputStream(new FileOutputStream(eConceptsFile));
			eConceptDOS = new DataOutputStream(eConceptsBos);
			
			transformer.preProcessIteration();
			
			TransformProcessor processor = new TransformProcessor(transformer, eConceptDOS);

			ts.iterateConceptDataInSequence(processor);

			List<TkConcept> postProcessList = transformer.postProcessIteration();
			
			processor.writeList(postProcessList);

			eConceptDOS.close();
			eConceptsBos.close();

		} catch (Exception e1) {
			throw new MojoExecutionException( "Error in transformation", e1 );
		}
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	public String getTransfomerId() {
		return transformerId;
	}

	public void setTransfomerId(String transfomerId) {
		this.transformerId = transfomerId;
	}
}
