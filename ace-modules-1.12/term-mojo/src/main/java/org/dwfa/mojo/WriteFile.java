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
package org.dwfa.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal to create a propeties file dynamically.
 * If a properties value, passed to this goal is TIMESTAMP,
 * it will be replaced with a current timestamp, in long date format.
 * 
 * @goal write-file
 * 
 * @phase process-sources
 */
public class WriteFile extends AbstractMojo {
    /**
     * Map to pass property key and value pair to, for setting DB properties
     * from a project.
     * 
     * @parameter
     */
    private Map<String, String> propertyMap;

    /**
     * Location of the file.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * File name
     * 
     * @parameter expression="default.txt"
     * @required
     */
    private String fileName;

    public void execute() throws MojoExecutionException {
        try {
            File f = outputDirectory;

            if (!f.exists()) {
                f.mkdirs();
            }

            File file = new File(f, fileName);

            if (file.exists()) {
                HashMap<String, String> existingProps = readExistingFile(file);
                writeProperties(file, existingProps);
            } else {
                file.createNewFile();
                writeProperties(file, null);
            }// End if/else

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }// End method execute

    private HashMap<String, String> readExistingFile(File file) {
        HashMap<String, String> currentProperties = new HashMap<String, String>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();

            while (line != null) {
                String[] tokens = line.split("=");
                currentProperties.put(tokens[0], tokens[1]);
                line = reader.readLine();
            }// End while loop

            reader.close();

        } catch (Exception e) {
            return null;
        }

        return currentProperties;
    }// End method readExistingFile

    private void writeProperties(File outputFile, HashMap<String, String> existingProps) throws MojoExecutionException {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));

            if (existingProps == null)
                existingProps = new HashMap<String, String>();

            if (propertyMap != null) {
                for (String key : propertyMap.keySet()) {

                    String value = propertyMap.get(key);

                    if (value != null && value.equalsIgnoreCase("TIMESTAMP")) {
                        value = new Date().toString();
                    }

                    existingProps.put(key, value);

                }// End for loop
            }// End if

            for (String key : existingProps.keySet()) {

                String value = existingProps.get(key);

                writer.append(key + "=" + value);
                writer.newLine();
            }// End for loop

            writer.close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }// End method writeProperties
}// End class writeFile
