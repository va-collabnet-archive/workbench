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
package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.vodb.bind.ThinVersionHelper;

/**
 * 
 * Produce an HTML report of all refset metadata in the ACE db.
 * 
 * <p>
 * Specify the report file using the list_file parameter.
 * 
 * @goal refset-listing
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class RefsetListing extends AbstractMojo {

	/**
	 * The uuid of the path.
	 * 
	 * @parameter
	 */
	private String path_uuid;

	private I_Path path;

	/**
	 * List refset to file
	 * 
	 * @parameter
	 */
	private File list_file;

	private I_GetConceptData refset;

	private I_Path processConfig() throws Exception {
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_Path path = tf.getPath(Arrays.asList(UUID.fromString(path_uuid)));
		return path;
	}

	private void listRefset() throws Exception {
		getLog().info("refset list");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				list_file)));
		I_TermFactory tf = LocalVersionedTerminology.get();
		this.path = tf.getPath(Arrays.asList(UUID.fromString(path_uuid)));
		I_Path path = processConfig();
		// I_IntSet allowed_status = tf.newIntSet();
		// allowed_status.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
		I_GetConceptData refset_con = tf.getConcept(Arrays.asList(UUID
				.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da")));
		out.println("<html>");
		out.println("<body>");
		for (Integer con_id : getDescendants(refset_con.getConceptId(), null,
				Integer.MAX_VALUE)) {
			I_GetConceptData con = tf.getConcept(con_id);
			// I_GetConceptData con = tf.getConcept(Arrays.asList(UUID
			// .fromString("fb98edf7-779f-4560-b410-4675b94a9d0a")));

			out.println("<h1>");
			out.println(con.getInitialText());
			out.println("</h1>");
			out.println("<table border=\"1\">");
			for (UUID id : con.getUids()) {
				out.println("<tr>");
				out.println("<td>");
				out.println("ID");
				out.println("<td>");
				out.println(id);
			}
			for (String r : Arrays.asList(
					"dd413e49-c124-3b05-8c25-0da5922379d3",
					"7a981930-621f-3935-b26c-47f54413a59d",
					"41fbef7f-7210-3288-97cb-c860dfc90601")) {
				I_GetConceptData r_con = tf.getConcept(Arrays.asList(UUID
						.fromString(r)));
				boolean found = false;
				if (r_con != null) {
					for (int val_id : getRelationship(con.getConceptId(), r_con
							.getConceptId(), null, Integer.MAX_VALUE)) {
						I_GetConceptData val_con = tf.getConcept(val_id);
						if (val_con != null) {
							found = true;
							out.println("<tr>");
							out.println("<td>");
							out.println(r_con.getInitialText());
							out.println("<td>");
							out.println(val_con.getInitialText());
						}
					}
				}
				if (!found) {
					out.println("<tr>");
					out.println("<td>");
					out.println(r_con.getInitialText());
					out.println("<td>");
				}
			}
			// comments rel "ff1b55d3-2b7b-382c-ae42-eceffcc47c71"
			// promotion rel "9a801240-b3b0-3475-8a7b-07111d3ff564"
			out.println("</table>");
		}
		out.println("</html>");
		out.println("</body>");
		out.close();
	}

	private HashSet<Integer> getDescendants(int concept_id, I_Path path,
			int version) throws Exception {
		HashSet<Integer> ret = new HashSet<Integer>();
		getDescendants1(concept_id, path, version, ret);
		return ret;
	}

	private void getDescendants1(int concept_id, I_Path path, int version,
			HashSet<Integer> ret) throws Exception {
		if (ret.contains(concept_id))
			return;
		ret.add(concept_id);
		for (int ch : getChildren(concept_id, path, version)) {
			getDescendants1(ch, path, version, ret);
		}
	}

	private ArrayList<Integer> getChildren(int concept_id, I_Path path,
			int version) throws Exception {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_GetConceptData c = tf.getConcept(concept_id);
		for (I_RelVersioned d : c.getDestRels()) {
			I_RelPart dm = null;
			for (I_RelPart dd : d.getVersions()) {
				if (path != null && dd.getPathId() != path.getConceptId())
					continue;
				if (!(dd.getTypeId() == tf.getConcept(
						SNOMED.Concept.IS_A.getUids()).getConceptId() || dd
						.getTypeId() == tf.getConcept(
						ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
						.getConceptId()))
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= version
						&& (dm == null || dm.getVersion() < dd.getVersion()))
					dm = dd;
			}
			if (dm != null)
//					&& dm.getStatusId() == tf.getConcept(
//							ArchitectonicAuxiliary.Concept.CURRENT.getUids())
//							.getConceptId())
				ret.add(d.getC1Id());
		}
		return ret;
	}

	private ArrayList<Integer> getRelationship(int concept_id,
			int relationship_id, I_Path path, int version) throws Exception {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		I_TermFactory tf = LocalVersionedTerminology.get();
		I_GetConceptData c = tf.getConcept(concept_id);
		for (I_RelVersioned d : c.getSourceRels()) {
			// ret.add(d.getC2Id());
			I_RelPart dm = null;
			for (I_RelPart dd : d.getVersions()) {
				if (path != null && dd.getPathId() != path.getConceptId())
					continue;
				if (dd.getTypeId() != relationship_id)
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= version
						&& (dm == null || dm.getVersion() < dd.getVersion()))
					dm = dd;
			}
			if (dm != null)
				// && dm.getStatusId() == tf.getConcept(
				// ArchitectonicAuxiliary.Concept.CURRENT.getUids())
				// .getConceptId())
				ret.add(d.getC2Id());
		}
		return ret;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			this.listRefset();
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}

	// generated UUID fb98edf7-779f-4560-b410-4675b94a9d0a current

}
