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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.tk.api.PositionBI;

/**
 * 
 * @goal change-report
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class ChangeReport extends DiffBase {

	/**
	 * Report directory.
	 * 
	 * @parameter expression="${project.build.directory}/generated-resources"
	 */
	private File report_dir;

	/**
	 * Concept per page
	 * 
	 * @parameter default-value=100
	 */
	private int concepts_per_page;

	private int concepts_on_page = 0;

	private int page_i = 0;

	private PrintWriter out = null;

	private String changes;

	String cur_page;

	private void getOut() throws Exception {
		if (concepts_on_page == concepts_per_page) {
			printHtmlEnd();
			out.close();
			out = null;
		}
		if (out == null) {
			concepts_on_page = 0;
			page_i++;
			cur_page = "page" + page_i + ".html";
			String file_name = report_dir + "/" + cur_page;
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file_name), "UTF-8")));
			printHtmlStart();
		}
		concepts_on_page++;
	}

	@Override
	protected void addedConcept(I_GetConceptData c) throws Exception {
		super.addedConcept(c);
		startChange(c);
		changes += "<tr><td>" + "Added concept" + "</td><td>" + " "
				+ "</td><td>" + " " + "</td></tr>";

	}

	private ArrayList<Integer> listed_changed_descriptions;

	private ArrayList<Integer> listed_changed_relationships;

	protected void startChange(I_GetConceptData c) throws Exception {
		if (changes.equals("")) {
			changes += "<caption>" + "<a name=\"" + c.getConceptNid() + "\">"
					+ getConceptPreferredDescription(c) + "</a>" + "</caption>";
			listed_changed_descriptions = new ArrayList<Integer>();
			listed_changed_relationships = new ArrayList<Integer>();
		}
	}

	@Override
	protected void changedConceptStatus(I_GetConceptData c, int v1, int v2)
			throws Exception {
		super.changedConceptStatus(c, v1, v2);
		startChange(c);
		changes += "<tr><td>" + "Status" + "</td><td>" + getConceptName(v1)
				+ "</td><td>" + getConceptName(v2) + "</td></tr>";
	}

	@Override
	protected void changedDefined(I_GetConceptData c, boolean v1, boolean v2)
			throws Exception {
		super.changedDefined(c, v1, v2);
		startChange(c);
		changes += "<tr><td>" + "Defined" + "</td><td>" + v1 + "</td><td>" + v2
				+ "</td></tr>";
	}

	@Override
	protected void addedDescription(I_GetConceptData c, I_DescriptionTuple d)
			throws Exception {
		super.addedDescription(c, d);
		startChange(c);
		changes += "<tr><td>" + "Added description" + "</td><td>" + ""
				+ "</td><td>" + d.getText() + "<br>"
				+ getConceptName(d.getTypeNid()) + " " + d.getLang()
				+ "</td></tr>";
	}

	protected void listChangedDescription(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		startChange(c);
		if (listed_changed_descriptions.contains(d1.getDescId()))
			return;
		changes += "<tr><td>" + "Changed description" + "</td><td>"
				+ d1.getText() + "</td><td>"
				+ (d1.getText().equals(d2.getText()) ? " " : d2.getText())
				+ "</td></tr>";
		changes += "<tr><td align = \"right\">"
				+ "status"
				+ "</td><td>"
				+ getConceptName(d1.getStatusNid())
				+ "</td><td>"
				+ (d1.getStatusNid() == d2.getStatusNid() ? " "
						: getConceptName(d2.getStatusNid())) + "</td></tr>";
		changes += "<tr><td align = \"right\">"
				+ "type"
				+ "</td><td>"
				+ getConceptName(d1.getTypeNid())
				+ "</td><td>"
				+ (d1.getTypeNid() == d2.getTypeNid() ? " " : getConceptName(d2
						.getTypeNid())) + "</td></tr>";
		changes += "<tr><td align = \"right\">" + "lang" + "</td><td>"
				+ d1.getLang() + "</td><td>"
				+ (d1.getLang().equals(d2.getLang()) ? " " : d2.getLang())
				+ "</td></tr>";
		changes += "<tr><td align = \"right\">"
				+ "case"
				+ "</td><td>"
				+ d1.isInitialCaseSignificant()
				+ "</td><td>"
				+ (d1.isInitialCaseSignificant() == d2
						.isInitialCaseSignificant() ? " " : d2
						.isInitialCaseSignificant()) + "</td></tr>";
		listed_changed_descriptions.add(d1.getDescId());
	}

	@Override
	protected void changedDescriptionStatus(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionStatus(c, d1, d2);
		listChangedDescription(c, d1, d2);
	}

	@Override
	protected void changedDescriptionTerm(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionTerm(c, d1, d2);
		listChangedDescription(c, d1, d2);
	}

	@Override
	protected void changedDescriptionLang(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionLang(c, d1, d2);
		listChangedDescription(c, d1, d2);
	}

	@Override
	protected void changedDescriptionCase(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionCase(c, d1, d2);
		listChangedDescription(c, d1, d2);
	}

	@Override
	protected void changedDescriptionType(I_GetConceptData c,
			I_DescriptionTuple d1, I_DescriptionTuple d2) throws Exception {
		super.changedDescriptionType(c, d1, d2);
		listChangedDescription(c, d1, d2);
	}

	@Override
	protected void addedRelationship(I_GetConceptData c, I_RelTuple d)
			throws Exception {
		super.addedRelationship(c, d);
		startChange(c);
		changes += "<tr><td>" + "Added relationship" + "</td><td>" + ""
				+ "</td><td>" + getConceptName(d.getTypeNid()) + "<br>"
				+ getConceptPreferredDescription(d.getDestinationNid())
				+ "</td></tr>";
	}

	protected void listChangedRelationship(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		startChange(c);
		if (listed_changed_relationships.contains(d1.getRelId()))
			return;
		changes += "<tr><td>"
				+ "Changed relationship"
				+ "</td><td>"
				+ getConceptPreferredDescription(d1.getDestinationNid())
				+ "</td><td>"
				+ (d1.getDestinationNid() == d2.getDestinationNid() ? " "
						: getConceptPreferredDescription(d2.getDestinationNid()))
				+ "</td></tr>";
		changes += "<tr><td align = \"right\">"
				+ "status"
				+ "</td><td>"
				+ getConceptName(d1.getStatusNid())
				+ "</td><td>"
				+ (d1.getStatusNid() == d2.getStatusNid() ? " "
						: getConceptName(d2.getStatusNid())) + "</td></tr>";
		changes += "<tr><td align = \"right\">"
				+ "type"
				+ "</td><td>"
				+ getConceptName(d1.getTypeNid())
				+ "</td><td>"
				+ (d1.getTypeNid() == d2.getTypeNid() ? " " : getConceptName(d2
						.getTypeNid())) + "</td></tr>";
		changes += "<tr><td align = \"right\">"
				+ "characteristic"
				+ "</td><td>"
				+ getConceptName(d1.getCharacteristicNid())
				+ "</td><td>"
				+ (d1.getCharacteristicNid() == d2.getCharacteristicNid() ? " "
						: getConceptName(d2.getCharacteristicNid()))
				+ "</td></tr>";
		changes += "<tr><td align = \"right\">"
				+ "refinability"
				+ "</td><td>"
				+ getConceptName(d1.getRefinabilityNid())
				+ "</td><td>"
				+ (d1.getRefinabilityNid() == d2.getRefinabilityNid() ? " "
						: getConceptName(d2.getRefinabilityNid()))
				+ "</td></tr>";
		changes += "<tr><td align = \"right\">" + "group" + "</td><td>"
				+ d1.getGroup() + "</td><td>"
				+ (d1.getGroup() == d2.getGroup() ? " " : d2.getGroup())
				+ "</td></tr>";
		listed_changed_relationships.add(d1.getRelId());
	}

	@Override
	protected void changedRelationshipStatus(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		super.changedRelationshipStatus(c, d1, d2);
		listChangedRelationship(c, d1, d2);
	}

	@Override
	protected void changedRelationshipType(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		super.changedRelationshipType(c, d1, d2);
		listChangedRelationship(c, d1, d2);
	}

	@Override
	protected void changedRelationshipCharacteristic(I_GetConceptData c,
			I_RelTuple d1, I_RelTuple d2) throws Exception {
		super.changedRelationshipCharacteristic(c, d1, d2);
		listChangedRelationship(c, d1, d2);
	}

	@Override
	protected void changedRelationshipRefinability(I_GetConceptData c,
			I_RelTuple d1, I_RelTuple d2) throws Exception {
		super.changedRelationshipRefinability(c, d1, d2);
		listChangedRelationship(c, d1, d2);
	}

	@Override
	protected void changedRelationshipGroup(I_GetConceptData c, I_RelTuple d1,
			I_RelTuple d2) throws Exception {
		super.changedRelationshipGroup(c, d1, d2);
		listChangedRelationship(c, d1, d2);
	}

	String config_html = "";

	protected void logConfig(String... str) throws Exception {
		super.logConfig(str);
		config_html += "<tr>";
		int i = 0;
		for (String s : str) {
			i++;
			if (str.length == i && i < 3) {
				config_html += "<td colspan=\"" + (3 - i + 1) + "\">" + s
						+ "</td>\n";
			} else {
				config_html += "<td>" + s + "</td>\n";
			}
		}
		config_html += "</tr>";
	}

	@Override
	protected List<Integer> buildConceptEnum(List<String> concepts, String tag)
			throws Exception {
		config_html += "<tr>" + "<td colspan=\"3\">" + tag + "</td>" + "</tr>"
				+ "\n";
		return super.buildConceptEnum(concepts, tag);
	}

	protected void sortConcepts(List<Integer> concepts) {
		Collections.sort(concepts, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				try {
					return getConceptName(o1).compareTo(getConceptName(o2));
				} catch (Exception ex) {
					ex.printStackTrace();
					return o1.compareTo(o2);
				}
			}
		});
	}

	protected void doChangeStat(ChangedValueCounter cvc) throws Exception {
		out.println("<table border=\"1\" width=\"700\">");
		out.println("<caption>" + "Changed " + cvc.getName() + "</caption>");
		out.println("<thead><th>" + "From" + "</th><th>" + "To" + "</th><th>"
				+ "Count" + "</th></thead>");
		ArrayList<Integer> cvcs = new ArrayList<Integer>(cvc.getValues());
		sortConcepts(cvcs);
		int[][] change_stat = cvc.getChanges();
		for (int v1 : cvcs) {
			boolean first_p = true;
			for (int v2 : cvcs) {
				out.println("<tr><td>"
						+ (first_p ? getConceptName(v1) : "")
						+ "</td><td>"
						+ getConceptName(v2)
						+ "</td><td align=\"right\"/>"
						+ change_stat[cvc.getValues().indexOf(v1)][cvc
								.getValues().indexOf(v2)] + "</td></tr>");
				first_p = false;
			}
		}
		out.println("</table>");
	}

	protected void printHtmlStart() {
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
		out.println("<html>");
		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\" />");
		out.println("<head>");
		out.println("<style type=\"text/css\">");
		out.println("body  {font-family:sans-serif}");
		out.println("table {font:10pt sans-serif; border-collapse:collapse; border: 1px black}");
		out.println("td    {padding:5px}");
		out.println("</style>");
		out.println("</head>");
		out.println("<body>");
	}

	protected void printHtmlEnd() {
		out.println("</body>");
		out.println("</html>");
	}

	protected void doSummaryReport() throws Exception {
		String file_name = report_dir + "/summary.html";
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file_name), "UTF-8")));
		printHtmlStart();
		out.println("<h3>Change Report</h3>");
		out.println("<p>");
		out.println("<br>Version 1: " + pos1.toString());
		out.println("<br>Version 2: " + pos2.toString());
		out.println("<br>Generated: " + new Date());
		out.println("<h3>Config</h3>");
		out.println("<table border=\"1\" width=\"700\">");
		out.println(config_html);
		out.println("</table>");
		out.println("<h4>Change counts</h4>");
		out.println("<p>");
		out.println("<table border=\"1\" width=\"700\">");
		out.println("<thead><th>" + "Change type" + "</th><th>" + "Count"
				+ "</th></thead>");
		out.println("<tr><td>" + "Changed concepts"
				+ "</td><td align=\"right\">" + changed_concepts.size()
				+ "</td></tr>");
		ArrayList<Integer> cvcs = new ArrayList<Integer>(
				this.diff_count.keySet());
		sortConcepts(cvcs);
		for (int cvc : cvcs) {
			out.println("<tr><td>" + getConceptName(cvc)
					+ "</td><td align=\"right\">" + this.diff_count.get(cvc)
					+ "</td></tr>");
		}
		out.println("</table>");
		out.println("<h4>Transition counts</h4>");
		out.println("<p>");
		doChangeStat(this.concept_status_change_stat);
		out.println("<p>");
		doChangeStat(this.description_status_change_stat);
		out.println("<p>");
		doChangeStat(this.description_type_change_stat);
		out.println("<p>");
		doChangeStat(this.relationship_status_change_stat);
		out.println("<p>");
		doChangeStat(this.relationship_type_change_stat);
		out.println("<p>");
		doChangeStat(this.relationship_characteristic_change_stat);
		out.println("<p>");
		doChangeStat(this.relationship_refinability_change_stat);
		printHtmlEnd();
		out.close();
	}

	protected void doConceptList(String file_name) throws Exception {
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file_name), "UTF-8")));
		printHtmlStart();
		out.println("<h3>Changed concepts list</h3>");
		for (int id : changed_concepts) {
			out.println("<br>" + "<a href=\"" + concept_to_page.get(id) + "#"
					+ id + "\">" + getConceptPreferredDescription(id) + "</a>");
		}
		printHtmlEnd();
		out.close();
	}

	private List<Integer> test_concepts = Arrays.asList(146773003);

	private ArrayList<Integer> changed_concepts = new ArrayList<Integer>();

	private HashMap<Integer, String> concept_to_page = new HashMap<Integer, String>();

	protected ArrayList<Integer> getAllConcepts() throws Exception {
		ArrayList<Integer> all_concepts;
		I_TermFactory tf = Terms.get();
		if (!test_p) {
			all_concepts = getDescendants(
					tf.getConcept(SNOMED.Concept.ROOT.getUids()).getNid(),
					this.allowed_position2);
			getLog().info("Retrieved hierarchical: " + all_concepts.size());
			return all_concepts;
		} else {
			all_concepts = super.getAllConcepts();
			for (int id : test_concepts) {
				UUID uuid = Type3UuidFactory.fromSNOMED(id);
				I_GetConceptData c = tf.getConcept(uuid);
				all_concepts.add(0, c.getConceptNid());
			}
		}
		return all_concepts;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			test_p = false;
			test_descendants_p = false;
			//
			super.execute();
			report_dir.mkdirs();
			I_TermFactory tf = Terms.get();
			ArrayList<Integer> all_concepts = getAllConcepts();
			getLog().info("Processing: " + all_concepts.size());
			int i = 0;
			for (int id : all_concepts) {
				I_GetConceptData c = tf.getConcept(id);
				changes = "";
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
			}
			if (out != null)
				out.close();
			doSummaryReport();
			doConceptList(report_dir + "/concepts.html");
			sortConcepts(changed_concepts);
			doConceptList(report_dir + "/alpha.html");
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}
}
