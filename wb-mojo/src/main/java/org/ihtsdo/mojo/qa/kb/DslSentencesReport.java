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
package org.ihtsdo.mojo.qa.kb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The mojo creates a new knowledge base reference and lnks it to a context
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Vahram
 * @goal dsl-sentence-report
 * @phase process-resources
 */
public class DslSentencesReport extends AbstractMavenReport {

	/**
	 * The Maven Project Object
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Specifies the directory where the report will be generated
	 * 
	 * @parameter default-value="${project.reporting.outputDirectory}"
	 * @required
	 */
	private String outputDirectory;

	/**
	 * delpoyment package reference name.
	 * 
	 * @parameter expression="inputfiles"
	 */
	private String brlinputfolder;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * @component
	 * @required
	 * @readonly
	 */
	private Renderer siteRenderer;

	private static Logger log = Logger.getLogger(DslSentencesReport.class);

	private static HashMap<String, String> map;

	@Override
	protected void executeReport(Locale arg0) throws MavenReportException {
		try {
			transformInHTMLAllInOne();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void transformInHTMLAllInOne() throws Exception {
		try {
			String fileOut = targetDirectory.getAbsolutePath() + "/output/allinone";
			log.info("Output folder path " + fileOut);
			File outFolder = new File(fileOut);
			outFolder.mkdirs();

			File inFolder = new File(brlinputfolder);
			inFolder.mkdirs();
			File[] dir = inFolder.listFiles();
			log.info("Fils in brlinputFolder " + dir.length);
			map = new HashMap<String, String>();

			BufferedWriter dwriter = null;
			String endDroolFile = fileOut + "/drl.html";
			dwriter = new BufferedWriter(new FileWriter(new File(endDroolFile)));
			File tempFolder = new File(targetDirectory, "/temp");
			tempFolder.mkdir();

			for (File file : dir) {
				if (!file.isFile())
					continue;
				if (file.getName().endsWith(".enumeration")) {
					BufferedReader reader;
					reader = new BufferedReader(new FileReader(file));
					String line;
					while ((line = reader.readLine()) != null) {
						String lines[] = line.split("\'");
						for (int i = 3; i < lines.length; i++) {
							if (i % 2 == 0)
								continue;
							try {
								if (lines[i].contains("=")) {
									String[] aux = lines[i].split("=");
									map.put(lines[1] + aux[0], aux[1]);
								} else {
									map.put(lines[1] + (i / 2), lines[i]);
								}
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
						}
					}
					reader.close();
				}

			} // end of for each file on resources
			dwriter.close();

			Sink sink = getSink();

			sink.head();
			sink.title();
			sink.text("DSL Sentences");
			sink.title_();
			sink.head_();

			sink.body();
			sink.section1();
			sink.sectionTitle1();
			sink.text("DSL Sentences");
			sink.sectionTitle1_();

			sink.lineBreak();

			sink.table();
			sink.tableRow();
			sink.tableHeaderCell();
			sink.text("Rules");
			sink.tableHeaderCell_();
			sink.tableHeaderCell();
			sink.text("Sentence");
			sink.tableHeaderCell_();
			sink.tableRow_();

			for (File file : dir) {
				if (file.getName().endsWith(".brl")) {
					String line;
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					// Using factory get an instance of document builder
					DocumentBuilder db = dbf.newDocumentBuilder();

					// parse using builder to get DOM representation of the XML
					// file
					Document dom = db.parse(file.getAbsolutePath());

					NodeList dslSentence = dom.getElementsByTagName("dslSentence");
					if (dslSentence.getLength() > 0) {
						sink.tableRow();
						sink.tableCell();
						sink.text(file.getName().substring(0, file.getName().length() - 5));
						sink.tableCell_();

						Node sentence = dslSentence.item(0);
						line = sentence.getTextContent();
						String[] parts = line.split("\\{");
						for (int k = 0; k < parts.length; k++) {
							if (parts[k].contains("ENUM")) {
								String target = parts[k].substring(0, parts[k].indexOf("}"));
								String part = target.split(":")[2].concat(target.split(":")[0]);
								try {
									if (map.containsKey(part)) {
										part = map.get(part);
										line = line.replace("{" + target + "}", part);
									}
								} catch (IllegalArgumentException e) {
									System.out.println("Error getting enum in " + file.getName());
								}
							}
						}
						sink.tableCell();
						sink.text(line);
						sink.tableCell_();
						sink.tableRow_();
					}
				}

			}
			sink.table_();
			sink.section1_();
			sink.body_();
			System.out.println("Removing temp folder");
			tempFolder.deleteOnExit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected MavenProject getProject() {
		return project;
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory;
	}

	@Override
	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

	@Override
	public String getDescription(Locale locale) {
		return getBundle(locale).getString("report.description");
	}

	@Override
	public String getName(Locale locale) {
		return getBundle(locale).getString("report.name");
	}

	@Override
	public String getOutputName() {
		return "dsl-sentence-Rules-Report";
	}

	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("dsl-sentence-report");
	}
}
