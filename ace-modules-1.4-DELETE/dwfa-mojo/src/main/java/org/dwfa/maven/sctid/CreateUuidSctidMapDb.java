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
package org.dwfa.maven.sctid;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Creates a new Uuid-Sctid map database optionally initialised with provided map data.
 * 
 * @goal createUuidSctidMapDb
 */
public class CreateUuidSctidMapDb extends AbstractMojo {

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
     * Validates the map files and logs error message if duplicate UUIDs are
     * found.
     * 
     * @parameter
     */
    boolean validate = false;

    /**
     * Append files to database.
     * 
     * @parameter
     */
    boolean appendToDb = false;
    
    /**
     * Indicates if a existing database is found whether it should be overwritten - defaults to false
     * 
     * @parameter
     */
    boolean createFreshDatabase = false;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        UuidSctidMapDb.setDatabaseProperties(dbDriver, dbConnectionUrl, dbUsername, dbPassword);
        UuidSctidMapDb mapDbInstance = UuidSctidMapDb.getInstance(true);
        
        try {
            boolean databaseExists = mapDbInstance.isDatabaseInitialised();
            
            if (!createFreshDatabase && databaseExists) {
                throw new MojoFailureException("Existing database found, proceeding would overwrite! Cannot proceed!");
            }
            
            if (databaseExists) {
                getLog().warn("Existing database found but instructed to overwrite - dropping database now");
                mapDbInstance.openDb();
                mapDbInstance.dropDb();
                mapDbInstance.close();
            }
            
            if (fixIdMapDirectory != null) {
                mapDbInstance.createDb(fixIdMapDirectory, readWriteMapDirectory, validate);
            } else {
                mapDbInstance.createDb();
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
