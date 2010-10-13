package org.ihtsdo.mojo.mojo.svn;

import org.apache.maven.plugin.AbstractMojo;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.tigris.subversion.javahl.PromptUserPassword3;

import java.io.File;

public abstract class AbstractSvnMojo
    extends AbstractMojo
    implements PromptUserPassword3
{
    /**
     * Location of the svn working copy
     *
     * @parameter
     */
    String workingCopyStr;

    /**
     * The svn repository url
     *
     * @parameter
     */
    String repositoryUrlStr;

    /**
     * The svn username
     *
     * @parameter
     */
    String username;

    /**
     * The svn password
     *
     * @parameter
     */
    String password;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File targetDirectory;

    /**
     * @component
     */
    private Prompter prompter;

    /**
     * Subversion configuration directory. If not specified, the default (~/.subversion) will be used.
     * @parameter
     */
    protected String subversionConfigDirectory;

    /**
     * Check if Maven is operating in interactive mode.
     *
     * @parameter expression="${settings.interactiveMode}"
     * @readonly
     */
    private boolean interactive;

    public String askQuestion(String arg0, String arg1, boolean arg2, boolean arg3) {
        throw new UnsupportedOperationException();
    }

    public boolean prompt(String realm, String username, boolean maySave) {
        if ( !interactive )
        {
            getLog().info( "Not prompting for username/password - non-interactive mode" );
            return false;
        }

        try
        {
            if ( this.username == null || this.password == null )
            {
                this.username = prompter.prompt( realm + " Username", username );
                this.password = prompter.promptForPassword( realm + " Password" );
            }
        }
        catch ( PrompterException e )
        {
            getLog().error( e.getMessage() );
            return false;
        }
        return true;
    }

    public boolean userAllowedSave() {
        return true;
    }

    public int askTrustSSLServer(String arg0, boolean arg1) {
        throw new UnsupportedOperationException();
    }

    public String askQuestion(String arg0, String arg1, boolean arg2) {
        throw new UnsupportedOperationException();
    }

    public boolean askYesNo(String arg0, String arg1, boolean arg2) {
        throw new UnsupportedOperationException();
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean prompt(String arg0, String arg1) {
        throw new UnsupportedOperationException();
    }
}
