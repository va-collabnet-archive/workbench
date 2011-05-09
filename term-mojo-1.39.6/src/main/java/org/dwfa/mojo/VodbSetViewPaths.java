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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * 
 * @goal vodb-set-view-paths
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbSetViewPaths extends AbstractMojo {

    /**
     * View path UUIDs
     * 
     * @parameter
     * @required
     */
    private List<ConceptDescriptor> viewPaths;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            I_ConfigAceFrame activeConfig = LocalVersionedTerminology.get().getActiveAceFrameConfig();
            I_TermFactory tf = LocalVersionedTerminology.get();
            activeConfig.getViewPositionSet().clear();
            for (ConceptDescriptor path : viewPaths) {
            	if (path.getUuid() == null) {
            		path.setUuid(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, 
            				path.getDescription()).toString());
            	}
                I_Path viewPath = tf.getPath(path.getVerifiedConcept().getUids());
                activeConfig.addViewPosition(tf.newPosition(viewPath, Integer.MAX_VALUE));
            }
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
    }

}
