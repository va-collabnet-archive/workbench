package org.dwfa.maven.sctid;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal runSqlUuidSctidMapDb
 */
public class RunSql extends AbstractMojo {
    /**
     * DB file
     *
     * @parameter
     * @required
     */
    File dbMapDirectory;

    /**
     * Array of SQL to run.
     *
     * @parameter
     * @required
     */
    String[] sqlArray;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            UuidSctidMapDb.getInstance().openDb(dbMapDirectory);
            for (String sql : sqlArray) {
                UuidSctidMapDb.getInstance().runAndCommitSql(sql);
            }
        } catch (SQLException e) {
            throw new MojoExecutionException("SQLException ", e);
        } catch (IOException e) {
            throw new MojoExecutionException("IOException ", e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("ClassNotFoundException ", e);
        } finally {
            try {
                UuidSctidMapDb.getInstance().close();
            } catch (SQLException e) {
                throw new MojoExecutionException("SQLException ", e);
            }
        }
    }

}
