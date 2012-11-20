/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.maven.rf2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * Read a file of SCTID with corresponding UUIDs and determines if the UUIDs need to be re-mapped.
 *
 * @author Marc E. Campbell
 *
 * @goal sct-rf2-uuid-xmap-gen
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class Rf2UuidXmapGenMojo extends AbstractMojo implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    /**
     * A partial file name is sufficient for matching to 1 or more files.
     *
     * @parameter
     */
    private String inputFileName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        BufferedReader br = null;
        try {
            System.out.println("BEGIN: Rf2UuidXmapGenMojo");
            //        String current = new java.io.File(".").getCanonicalPath();
            //        System.out.println("Current dir:" + current);
            String currentDir = System.getProperty("user.dir");
            System.out.println("Current dir using System:" + currentDir);

            // DATA COLUMNS
            int Partition_ID = 0;
            int Namespace_ID = 1;
            int Release_ID = 2;
            int SCTID = 3;
            int UUID_CODE = 4;

            File f = new File("src/main/resources/org/ihtsdo/Other/SCTID_UUID_20120131.txt");
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            int count = 0;
            br.readLine(); // Header row
            while (br.ready()) {
                String[] line = br.readLine().split(TAB_CHARACTER);
                String sctidUuidStr = Rf2x.convertSctIdToUuidStr(line[SCTID]);

                if (sctidUuidStr.equalsIgnoreCase(line[UUID_CODE])) {
                    System.out.println(line[UUID_CODE] + "\t" + line[UUID_CODE]);
                }
                count++;
            }
            System.out.println("SCTID UUID pairs reviewed = " + count);
        } catch (IOException ex) {
            Logger.getLogger(Rf2UuidXmapGenMojo.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Rf2UuidXmapGenMojo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }
}

// The Rf2UuidXmapGenMojo above only tests to see if the computed UUID matches the actual ID.
// If the UUID computed from the SCTID does not match the assigned UUID then
// a UUID to UUID mapped mojo will need to be created.
//
// SAMPLE PROJECT
//<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
//    <modelVersion>4.0.0</modelVersion>
//    <groupId>org.ihtsdo.sct.baseline</groupId>
//    <artifactId>sct-rf2-uuid-xmap-gen</artifactId>
//    <version>1.0-SNAPSHOT</version>
//    <packaging>pom</packaging>
//    <name>sct-rf2-uuid-xmap-gen</name>
//
//    <properties>
//        <org.ihtsdo.wb-toolkit.version>2.6.0-trek-122-SNAPSHOT</org.ihtsdo.wb-toolkit.version>
//    </properties>
//
//
//    <build>
//        <plugins>
//            <plugin>
//                <groupId>org.ihtsdo</groupId>
//                <artifactId>wb-mojo</artifactId>
//                <version>${org.ihtsdo.wb-toolkit.version}</version>
//                <executions>
//                    <execution>
//                        <id>attach-database</id>
//                        <phase>process-sources</phase>
//                        <configuration>
//                            <inputFileName>
//                                src/main/resources/org/ihtsdo/Other/SCTID_UUID_20120131.txt
//                            </inputFileName>
//                        </configuration>
//                        <goals>
//                            <goal>sct-rf2-uuid-xmap-gen</goal>
//                        </goals>
//                    </execution>
//                </executions>
//            </plugin>
//        </plugins>
//    </build>
//
//
//</project>
