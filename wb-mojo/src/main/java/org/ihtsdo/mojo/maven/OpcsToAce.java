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
package org.ihtsdo.mojo.maven;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * Convert an OPCS-4 file to a set of <a href=
 * 'https://mgr.cubit.aceworkspace.net/apps/dwfa/ace-mojo/dataimport.html'>ACE
 * formatted</a> files.
 * 
 * @goal opcs-to-ace
 * @phase process-resources
 */

public class OpcsToAce extends AbstractMojo {

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the opcs data directory to read from.
     * 
     * @parameter expression=
     * 
     * 
     * 
     * 
     *            
     *            "${project.build.directory}/generated-resources/net/nhs/uktc/opcs-4"
     * @required
     */
    private File opcsDir;

    /**
     * Location of the ace data directory to write to.
     * 
     * @parameter expression="${project.build.directory}/classes/ace"
     * @required
     */
    private File aceDir;

    /**
     * The effective date to associate with all changes.
     * 
     * @parameter
     * @required
     */
    private String effectiveDate;

    /**
     * The path name to associate with all changes.
     * 
     * @parameter
     * @required
     */
    private String pathFsDesc;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("OPCS dir: " + opcsDir);
            getLog().info("ACE dir: " + aceDir);
            aceDir.mkdirs();
            BufferedReader r = new BufferedReader(new FileReader(new File(opcsDir, "OPCS Codes and titles v2.text")));

            Writer concepts = new BufferedWriter(new FileWriter(new File(aceDir, "concepts.txt")));
            Writer descriptions = new BufferedWriter(new FileWriter(new File(aceDir, "descriptions.txt")));
            Writer relationships = new BufferedWriter(new FileWriter(new File(aceDir, "relationships.txt")));
            Writer ids = new BufferedWriter(new FileWriter(new File(aceDir, "ids.txt")));

            UUID pathUUID = Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, pathFsDesc);

            writeRoot(concepts, descriptions, relationships, ids, pathUUID);

            while (r.ready()) {
                String line = r.readLine();
                if (line.length() > 0) {
                    processRow(concepts, descriptions, relationships, ids, pathUUID, line);
                }
            }

            r.close();
            concepts.close();
            descriptions.close();
            relationships.close();
            ids.close();
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    private void processRow(Writer concepts, Writer descriptions, Writer relationships, Writer ids, UUID pathUUID,
            String line) throws IOException, NoSuchAlgorithmException, UnsupportedEncodingException {

        String id = line.substring(0, 8).trim();
        String desc = line.substring(8).trim();

        ids.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id).toString()); // concept
        // id
        ids.append("\t");
        ids.append(ArchitectonicAuxiliary.Concept.UNSPECIFIED_STRING.getUids().iterator().next().toString()); // source
        ids.append("\t");
        ids.append(id); // source id
        ids.append("\t");
        ids.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString()); // status
        ids.append("\t");
        ids.append(effectiveDate);
        ids.append("\t");
        ids.append(pathUUID.toString()); // path id
        ids.append("\n");

        concepts.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id).toString()); // concept
        // id
        concepts.append("\t");
        concepts.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString()); // status
        concepts.append("\t");
        concepts.append("1"); // primitive
        concepts.append("\t");
        concepts.append(effectiveDate);
        concepts.append("\t");
        concepts.append(pathUUID.toString()); // path id
        concepts.append("\n");

        descriptions.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_DESC_ID, id).toString());
        descriptions.append("\t");
        descriptions.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
        descriptions.append("\t");
        descriptions.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id).toString());
        descriptions.append("\t");
        descriptions.append(desc);
        descriptions.append("\t");
        descriptions.append("1");
        descriptions.append("\t");
        descriptions.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next()
            .toString()); // status
        descriptions.append("\t");
        descriptions.append("en"); // primitive
        descriptions.append("\t");
        descriptions.append(effectiveDate);
        descriptions.append("\t");
        descriptions.append(pathUUID.toString()); // path id
        descriptions.append("\n");

        String parentId = "opcs";
        if (id.contains(".")) {
            parentId = id.substring(0, 3);
        }

        relationships.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_REL_ID, id + parentId).toString());
        relationships.append("\t");
        relationships.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
        relationships.append("\t");
        relationships.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id).toString());
        relationships.append("\t");
        relationships.append(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids().iterator().next().toString());
        relationships.append("\t");
        relationships.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, parentId).toString());
        relationships.append("\t");
        relationships.append(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids().iterator().next().toString());
        relationships.append("\t");
        relationships.append(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids().iterator().next().toString());
        relationships.append("\t");
        relationships.append("0");
        relationships.append("\t");
        relationships.append(effectiveDate);
        relationships.append("\t");
        relationships.append(pathUUID.toString()); // path id
        relationships.append("\n");
    }

    private void writeRoot(Writer concepts, Writer descriptions, Writer relationships, Writer ids, UUID pathUUID)
            throws IOException, NoSuchAlgorithmException, UnsupportedEncodingException {
        String id = "opcs";
        String desc = "OPCS Concept";

        ids.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id).toString()); // concept
        // id
        ids.append("\t");
        ids.append(ArchitectonicAuxiliary.Concept.UNSPECIFIED_STRING.getUids().iterator().next().toString()); // source
        ids.append("\t");
        ids.append(id); // source id
        ids.append("\t");
        ids.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString()); // status
        ids.append("\t");
        ids.append(effectiveDate);
        ids.append("\t");
        ids.append(pathUUID.toString()); // path id
        ids.append("\n");

        concepts.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id).toString()); // concept
        // id
        concepts.append("\t");
        concepts.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString()); // status
        concepts.append("\t");
        concepts.append("1"); // primitive
        concepts.append("\t");
        concepts.append(effectiveDate);
        concepts.append("\t");
        concepts.append(pathUUID.toString()); // path id
        concepts.append("\n");

        descriptions.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_DESC_ID, id).toString());
        descriptions.append("\t");
        descriptions.append(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString());
        descriptions.append("\t");
        descriptions.append(Type5UuidFactory.get(Type5UuidFactory.OPCS_CONCEPT_ID, id).toString());
        descriptions.append("\t");
        descriptions.append(desc);
        descriptions.append("\t");
        descriptions.append("1");
        descriptions.append("\t");
        descriptions.append(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()
            .iterator()
            .next()
            .toString()); // status
        descriptions.append("\t");
        descriptions.append("en"); // primitive
        descriptions.append("\t");
        descriptions.append(effectiveDate);
        descriptions.append("\t");
        descriptions.append(pathUUID.toString()); // path id
        descriptions.append("\n");

    }
}
