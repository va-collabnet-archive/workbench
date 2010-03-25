/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.vodb.VodbEnv;

import java.io.IOException;

/**
 * Creates a lucene index from a populated berkeley database. The indexing was separated from
 * {@code LoadBdb} for due to a problem where the index was not correctly generated from
 * freshly-populated data. [TCP-2267]
 *
 * The database has to be opened for this mojo to succeed. @see VodbOpen
 * @goal create-lucene-index
 */
public final class CreateLuceneIndexMojo extends AbstractMojo {

    @Override public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ((VodbEnv) LocalVersionedTerminology.get()).createLuceneDescriptionIndex();
        } catch (IOException e) {
            throw new MojoFailureException("There was an error creating the lucene index", e);
        }
    }
}
