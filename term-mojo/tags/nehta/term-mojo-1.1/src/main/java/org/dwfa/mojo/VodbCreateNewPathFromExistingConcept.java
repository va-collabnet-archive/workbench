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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;

/**
 *This goal will add all the child concepts of given concept to path.
 * 
 * @goal vodb-create-new-path-from-parent
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 * @author Ming Zhang
 *
 */

public class VodbCreateNewPathFromExistingConcept extends AbstractMojo {

    /**
     * Path origins
     * 
     * @parameter
     */
    SimpleUniversalAcePosition[] origins;

    /**
     * Parent of the new pathes.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor pathParent;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
		// Use the architectonic branch for all path editing.
		try {
			try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() +  pathParent.getDescription(), 
						this.getClass(), targetDirectory)) {
					return;
				}
			} catch (NoSuchAlgorithmException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			} 
			I_TermFactory tf = LocalVersionedTerminology.get();
			Set<I_Position> pathOrigins = null;
			if (origins != null) {
				pathOrigins = new HashSet<I_Position>(origins.length);
				for (SimpleUniversalAcePosition pos : origins) {
					I_Path originPath = tf.getPath(pos.getPathId());
					pathOrigins.add(tf.newPosition(originPath, pos.getTime()));
				}
			}
			I_GetConceptData parent = pathParent.getVerifiedConcept();
			I_IntSet allowedStatus = tf.newIntSet();
			allowedStatus.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
			
			I_IntSet allowedTypes = tf.newIntSet();
			
			allowedTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<I_GetConceptData> s = parent.getDestRelOrigins(allowedStatus, allowedTypes, null, false);
			if (s.size() >0)
			{
				for (I_GetConceptData child :s)
				{
					if (tf.hasId(child.getUids())) 
					{
						getLog().info("Find path to add" + child.toString());							
						tf.newPath(pathOrigins, child);
					}
				}
			}
		} catch (TerminologyException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (ParseException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}
