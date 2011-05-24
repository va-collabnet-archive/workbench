package org.ihtsdo.mojo.mojo;

import java.io.File;
import java.util.Collection;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.lucene.LuceneManager.LuceneSearchType;
import org.ihtsdo.tk.Ts;

/**
 * Goal which loads an EConcept.jbin file into a bdb.
 * 
 * @goal vodb-mutable-to-readonly
 * 
 * @phase process-resources
 */

public class VodbMutableToReadOnly extends AbstractMojo {

    /**
     * Berkeley directory.
     * 
     * @parameter expression="${project.build.directory}/berkeley-db"
     * @required
     */
    private File berkeleyDir;

    public void execute() throws MojoExecutionException {
        executeMojo(berkeleyDir);

    }

    void executeMojo(File berkeleyDir) throws MojoExecutionException {
        try {
        	LuceneManager.init(LuceneSearchType.DESCRIPTION);
            LuceneManager.setDbRootDir(berkeleyDir, LuceneSearchType.DESCRIPTION);
            FileIO.recursiveDelete(new File(berkeleyDir, "read-only"));
            File dirToMove = new File(berkeleyDir, "mutable");
            dirToMove.renameTo(new File(berkeleyDir, "read-only"));
            new File(berkeleyDir, "mutable").mkdir();
            Terms.createFactory(berkeleyDir, false, 0L, new DatabaseSetupConfig());
            LuceneManager.writeToLucene((Collection<Description>) 
                    Ts.get().getConcept(ReferenceConcepts.CURRENT.getNid()).getDescs(), LuceneSearchType.DESCRIPTION);
            I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) Terms.get();
            termFactoryImpl.close();

        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        } catch (Throwable ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }
}
