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
     * DB file
     *
     * @parameter
     * @required
     */
    File dbMapDirectory;

    /**
     * Fixed id map file
     *
     * @parameter
     * @required
     */
    File fixIdMapDirectory;

    /**
     * List of read write map File
     *
     * @required
     */
    File readWriteMapDirectory;

    /**
     * Validates the map files and logs error message if duplicate UUIDs are found.
     *
     * @parameter
     */
    boolean validate = false;;

    /**
     * Append files to database.
     *
     * @parameter
     */
    boolean appendToDb = false;;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            UuidSctidMapDb.getInstance().openDb(dbMapDirectory, fixIdMapDirectory, readWriteMapDirectory, validate, appendToDb);
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
