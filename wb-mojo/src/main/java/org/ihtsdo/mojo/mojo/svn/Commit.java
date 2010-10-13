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
package org.ihtsdo.mojo.mojo.svn;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.svn.Svn;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.mojo.maven.MojoUtil;

/**
 * Commit the changes to svn.
 * 
 * @goal svn-commit
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class Commit extends AbstractSvnMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + workingCopyStr + repositoryUrlStr,
                this.getClass(), targetDirectory)) {
                return;
            }
            if (workingCopyStr != null && workingCopyStr.length() > 1) {
                workingCopyStr = FileIO.getNormalizedRelativePath(new File(workingCopyStr));
            } else {
                workingCopyStr = System.getProperty("user.dir");
            }
            Svn.setConnectedToSvn(true);
            if ( username == null )
            {
                Svn.setUseCachedCredentials(true);
            }
            Svn svn = new Svn();
            SubversionData svd = new SubversionData(repositoryUrlStr, workingCopyStr);
            getLog().info("Connecting to: " + repositoryUrlStr + " as: " + username);
            svd.setUsername(username);
            svd.setPassword(password);
            svn.svnCommit(svd, username != null ? this : null, false);
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (TaskFailedException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
