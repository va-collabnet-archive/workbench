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


import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal merge-snomed
 * 
 * @phase generate-resources
 */

public class GenerateSnomedBinary extends ProcessSnomedSources {

    /**
     * Location of the directory to output data files to.
     * KEC: I added this field, because the maven plugin plugin would 
     * crash unless there was at least one commented field. This field is
     * not actually used by the plugin. 
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @SuppressWarnings("unused")
    private String outputDirectory;

    private DataOutputStream con_dos;

	private DataOutputStream desc_dos;

	private DataOutputStream rel_dos;

	public void setup() throws IOException {
		getOutputDirectory().mkdirs();
		FileOutputStream con_fos = new FileOutputStream(new File(
				getOutputDirectory(), "sct_concepts"));
		BufferedOutputStream con_bos = new BufferedOutputStream(con_fos);
		con_dos = new DataOutputStream(con_bos);
		FileOutputStream desc_fos = new FileOutputStream(new File(
				getOutputDirectory(), "sct_descriptions"));
		BufferedOutputStream desc_bos = new BufferedOutputStream(desc_fos);
		desc_dos = new DataOutputStream(desc_bos);
		FileOutputStream rel_fos = new FileOutputStream(new File(
				getOutputDirectory(), "sct_relationships"));
		BufferedOutputStream rel_bos = new BufferedOutputStream(rel_fos);
		rel_dos = new DataOutputStream(rel_bos);

	}
	
	public void cleanup() throws IOException {
		con_dos.close();
		desc_dos.close();
		rel_dos.close();
	}


	public void writeConcept(Date releaseDate, long conceptKey,
			int conceptStatus, int defChar) throws IOException {
		con_dos.writeLong(conceptKey);
		con_dos.writeLong(releaseDate.getTime());
		con_dos.writeInt(conceptStatus);
		con_dos.writeInt(defChar);

	}

	public void writeRelationship(Date releaseDate, long relID,
			long conceptOneID, long relationshipTypeConceptID,
			long conceptTwoID, long characteristic, long refinability, int group)
			throws IOException {
		rel_dos.writeLong(relID);
		rel_dos.writeLong(releaseDate.getTime());
		rel_dos.writeLong(conceptOneID);
		rel_dos.writeLong(relationshipTypeConceptID);
		rel_dos.writeLong(conceptTwoID);
		rel_dos.writeLong(characteristic);
		rel_dos.writeLong(refinability);
		rel_dos.writeLong(group);

	}

	public void writeDescription(Date releaseDate, long descriptionId,
			int status, long conceptId, String text, int capStatus,
			int typeInt, String lang) throws IOException {
		desc_dos.writeLong(descriptionId);
		desc_dos.writeLong(releaseDate.getTime());
		desc_dos.writeInt(status);
		desc_dos.writeLong(conceptId);
		desc_dos.writeChars(text);
		desc_dos.writeInt(capStatus);
		desc_dos.writeInt(typeInt);
		desc_dos.writeChars(lang);

	}

}
