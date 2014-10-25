package org.ihtsdo.mojo.svnkit;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Goal which touches a timestamp file.
 *
 * @goal geturl
 * 
 * @phase process-sources
 */
public class SvnGetUrl
    extends AbstractMojo
{
   /**
     * Location of the build directory.
     *
     * @parameter expression="${project.basedir}"
     * @required
     */
    protected File baseDirectory;

  /**
     * Location of the working directory of interest.
     * @parameter
     * @required
     */
    protected String workingDirectory;

    public void execute()
        throws MojoExecutionException
    {
        try {
            setupLibrary();
            SVNClientManager clientManager = SVNClientManager.newInstance();
            clientManager.getLookClient();
            SVNInfo svnInfo = clientManager.getWCClient().doInfo(baseDirectory, SVNRevision.UNDEFINED);

            
            getLog().info("Base directory: " + baseDirectory);
            getLog().info("Working directory: " + workingDirectory);
            getLog().info("Base directory url: " + svnInfo.getURL());
            
            svnInfo = clientManager.getWCClient().doInfo(new File(baseDirectory, workingDirectory), SVNRevision.UNDEFINED);
            
            getLog().info("Working directory url: " + svnInfo.getURL());
        } catch (SVNException ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }
    
    /*
     * Initializes the library to work with a repository via 
     * different protocols.
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();
        
        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }
}
