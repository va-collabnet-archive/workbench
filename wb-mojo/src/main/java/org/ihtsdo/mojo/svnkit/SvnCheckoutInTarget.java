/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.mojo.svnkit;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnTarget;

/**
 * Goal which touches a timestamp file.
 *
 * @goal svn-checkout-in-target
 *
 * @phase process-sources
 */public class SvnCheckoutInTarget     extends AbstractMojo {

    /**
     * username on the scm server.
     *
     * @parameter
     */

    protected String username;

    /**
     * password on the scm server.
     *
     * @parameter
     */

    protected String password;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.basedir}"
     * @required
     */
    protected File baseDirectory;

    /**
     * Location of the origin directory to copy/clone.
     * @parameter
     * @required
     */
    protected String sourceDirectory;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File targetDirectory;

    /**
     * Location to copy the directory to.
     * @parameter
     * @required
     */
    protected String destinationDirectory;


    public void execute()
            throws MojoExecutionException {

        try {
            setupLibrary();

            File origin = new File(baseDirectory, sourceDirectory);
            File destination = new File(targetDirectory, destinationDirectory);

            SVNClientManager clientManager = SVNClientManager.newInstance();
            ISVNAuthenticationManager authManager = null;

            if (username != null && !username.isEmpty()) {
                authManager = new BasicAuthenticationManager(username, password);
            }
            clientManager.setAuthenticationManager(authManager);

            SVNInfo svnInfo = clientManager.getWCClient().doInfo(baseDirectory, SVNRevision.UNDEFINED);

            getLog().info("Base directory: " + baseDirectory);
            getLog().info("Source directory: " + sourceDirectory);
            getLog().info("Base directory url: " + svnInfo.getURL());

            svnInfo = clientManager.getWCClient().doInfo(new File(baseDirectory, sourceDirectory), SVNRevision.UNDEFINED);

            getLog().info("Source directory url: " + svnInfo.getURL());

            SvnCheckout checkout = clientManager.getUpdateClient().getOperationsFactory().createCheckout();
            checkout.setSource(SvnTarget.fromURL(svnInfo.getURL()));
            checkout.addTarget(SvnTarget.fromFile(destination));
            checkout.run();



//            SvnExport export = clientManager.getWCClient().getOperationsFactory().createExport();
//            export.setSource(SvnTarget.fromFile(origin));
//            export.addTarget(SvnTarget.fromFile(destination));
//            export.run();

//            SvnCopy copy = clientManager.getCopyClient().getOperationsFactory().createCopy();
//            copy.addCopySource(SvnCopySource.create(SvnTarget.fromFile(origin), SVNRevision.WORKING));
//            copy.addTarget(SvnTarget.fromFile(destination));
//            copy.setDisjoint(true);
//            copy.setMove(false);
//            copy.run();


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