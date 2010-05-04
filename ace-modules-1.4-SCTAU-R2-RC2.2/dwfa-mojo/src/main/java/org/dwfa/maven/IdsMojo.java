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
package org.dwfa.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.security.NoSuchAlgorithmException;

import org.apache.derby.tools.ij;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringInputStream;
import org.dwfa.util.io.FileIO;
import org.dwfa.maven.transform.UuidSnomedMap;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Goal which executes derby sql commands to generate a
 * database or perform other such tasks.
 * 
 * @goal generate-ids-file
 */
public class IdsMojo extends AbstractMojo {

    /**
     * @parameter
     * @required
     */
    File uuidsFile;
    /**
     * @parameter
     * @required
     */
    File snomedMappingFile;
    /**
     * @parameter
     * @required
     */
    File outputFile;
    /**
     * @parameter
     * @required
     */
    String path;
    /**
     * @parameter
     */
    boolean skipFirstLine = true;
    /**
     * @parameter
     */
    boolean writeHeader = false;
    /**
     * @parameter
     */
    boolean append = false;
    /**
     * @parameter
     * @required
     */
    File checkFile;
    /**
     * @parameter
     */
    File reportFile;
    Set<UUID> allowedUuids = new HashSet<UUID>();
    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {

        // calculate the SHA-1 hashcode for this mojo based on input
        Sha1HashCodeGenerator generator;
        String hashCode = "";
        try {
            generator = new Sha1HashCodeGenerator();
            generator.add(outputFile.getName());
            generator.add(snomedMappingFile.getName());
            generator.add(uuidsFile.getName());
            hashCode = generator.getHashCode();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }

        File goalFileDirectory = new File(targetDirectory, "completed-mojos");
        File goalFile = new File(goalFileDirectory, hashCode);

        // check to see if this goal has been executed previously
        if (!goalFile.exists()) {

            try {
                if (checkFile != null) {
                    BufferedReader reader = new BufferedReader(new FileReader(checkFile));
                    String line = reader.readLine();
                    while ((line = reader.readLine()) != null) {
                        allowedUuids.add(UUID.fromString(line.split("\t")[0]));
                    }
                    reader.close();
                }

                outputFile.getParentFile().mkdirs();
                reportFile.getParentFile().mkdirs();
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, append));
                BufferedWriter report = new BufferedWriter(new FileWriter(reportFile));

                if (writeHeader) {
                    writer.write("Primary UUID\tSource System UUID\tSource Id\tStatus Id\tEffective Date\tPath UUID");
                    writer.newLine();
                }

                UuidSnomedMap map = UuidSnomedMap.read(snomedMappingFile);
                String source = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next().toString();
                BufferedReader reader = new BufferedReader(new FileReader(uuidsFile));
                String line = reader.readLine();

                if (skipFirstLine) {
                    line = reader.readLine();
                }

                while (line != null) {
                    String[] parts = line.split("\t");
                    String uuid = parts[0];
                    String status = parts[1];
                    Long sctid = map.get(UUID.fromString(uuid));
                    if (sctid != null) {
                        if (allowedUuids.contains(UUID.fromString(uuid))) {
                            String effective_date = map.getEffectiveDate(sctid);
                            writer.write(uuid + "\t" + source + "\t" + sctid + "\t" + status + "\t" + effective_date
                                + "\t" + path);
                            writer.newLine();
                        } else {
                            report.write("UUID Not in release: " + uuid);
                            report.newLine();
                        }
                    }
                    line = reader.readLine();
                }

                writer.close();
                report.close();
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        } else {
            // skip execution as it has already been done previously
            getLog().info("Skipping goal - executed previously.");
        }
    }
}
