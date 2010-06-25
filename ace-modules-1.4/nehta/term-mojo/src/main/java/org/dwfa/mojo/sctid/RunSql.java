package org.dwfa.mojo.sctid;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.maven.sctid.UuidSctidMapDb;

/**
 * @goal runSqlUuidSctidMapDb
 */
public class RunSql extends AbstractMojo {
    /**
     * URL used to connect to the database
     *
     * @parameter
     * @required
     */
    String dbConnectionUrl;

    /**
     * Database driver fully qualified class name
     *
     * @parameter
     * @required
     */
    String dbDriver;

    /**
     * Database user to optionally authenticate to the database
     *
     * @parameter
     */
    String dbUsername;

    /**
     * Database user's password optionally used to authenticate to the database
     *
     * @parameter
     */
    String dbPassword;

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
            UuidSctidMapDb.setDatabaseProperties(dbDriver, dbConnectionUrl, dbUsername, dbPassword);

            UuidSctidMapDb.getInstance().openDb();
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
