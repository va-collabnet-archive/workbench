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
package org.dwfa.mojo.sctid;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.maven.sctid.UuidSctidMapDb;

/**
 * @goal updateUuidSctidMapDb
 */
public class UpdateUuidSctidMapDb extends AbstractMojo {
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
     * Fixed id map file
     *
     * @parameter
     */
    File fixIdMapDirectory;

    /**
     * List of read write map File
     *
     * @parameter
     */
    File readWriteMapDirectory;

    /**
     * rf2 id file
     *
     * @parameter
     */
    File[] aceIdFiles;

    /**
     * rf2 id file
     *
     * @parameter
     */
    File[] rf2IdFiles;

    /**
     * Validates the map files and logs error message if duplicate UUIDs are
     * found.
     *
     * @parameter
     */
    boolean validate = false;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        UuidSctidMapDb.setDatabaseProperties(dbDriver, dbConnectionUrl, dbUsername, dbPassword);
        UuidSctidMapDb mapDbInstance = UuidSctidMapDb.getInstance();

        try {
            if (!mapDbInstance.isDatabaseInitialised()) {
                throw new MojoFailureException("Database is not initialised - cannot update. Please create a database first");
            }

            if (fixIdMapDirectory != null) {
                mapDbInstance.openDb(fixIdMapDirectory, readWriteMapDirectory, validate);
            } else {
                mapDbInstance.openDb();
            }

            if (rf2IdFiles != null) {
                mapDbInstance.updateDbFromRf2IdFile(rf2IdFiles);
            } else if (aceIdFiles != null) {
                for (File rf2IdFile : aceIdFiles) {
                    mapDbInstance.updateDbFromAceIdFile(rf2IdFile);
                }
            }
        } catch (SQLException e) {
            throw new MojoExecutionException("SQLException ", e);
        } catch (IOException e) {
            throw new MojoExecutionException("IOException ", e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("ClassNotFoundException ", e);
        } finally {
            try {
                mapDbInstance.close();
            } catch (SQLException e) {
                throw new MojoExecutionException("SQLException ", e);
            }
        }
    }

}
