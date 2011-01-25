/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.mojo.mojo.diff;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;

/**
 *
 * @goal change-report
 *
 * @phase generate-resources
 *
 * @requiresDependencyResolution compile
 */
public class ChangeReport extends ChangeReportBase {
   @Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// test_p = false;
			test_descendants_p = false;
			//
			super.execute();
			report_dir.mkdirs();
			I_TermFactory tf = Terms.get();
			getLog().info("Getting concepts in DFS order.");
			ArrayList<Integer> all_concepts = getAllConcepts();
			getLog().info("Processing: " + all_concepts.size());
			String file_name = report_dir + "/" + "change_report.xml";
			out_xml = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file_name),
							"UTF-8")));
			out_xml.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out_xml.println(startElement("change_report"));
			int i = 0;
			for (int id : all_concepts) {
				I_GetConceptData c = tf.getConcept(id);
				// if (!c.getUUIDs()
				// .contains(
				// UUID.fromString("39f56845-f221-3ca7-9d21-8ed6d4e5e7de")))
				// continue;
				// if (!c.getUUIDs()
				// .contains(
				// UUID.fromString("e5be1abb-8e77-31c1-b8ff-8a11621d1762")))
				// continue;
				changes = "";
				changes_xml = "";
				if (debug_p)
					System.out.println("Concept: " + c.getInitialText());
				compareAttributes(c);
				compareDescriptions(c);
				compareRelationships(c);
				i++;
				if (i % 10000 == 0)
					System.out.println("Processed: " + i);
				if (changes.equals(""))
					continue;
				changed_concepts.add(id);
				getOut();
				concept_to_page.put(id, cur_page);
				out.println("<p><table border=\"1\" width=\"700\">"
						+ "<col width=\"140\"/><col width=\"270\"/><col width=\"270\"/>"
						+ changes + "</table>");
				out_xml.println(startElement("changed_concept") + "\n"
						+ changes_xml + endElement("changed_concept") + "\n");
			}
			if (out != null)
				out.close();
			out_xml.println(endElement("change_report"));
			out_xml.close();
			doSummaryReport();
			doConceptList(report_dir + "/concepts.html");
			sortConcepts(changed_concepts);
			doConceptList(report_dir + "/alpha.html");
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}
}
