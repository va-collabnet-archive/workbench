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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributeSet;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.xml.sax.SAXException;

import com.googlecode.sardine.util.SardineException;

/**
 * The mojo creates a new knowledge base reference and lnks it to a context
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Vahram
 * @goal technical-rules-report
 * @phase process-resources
 */
public class TechnicalRulesReport extends AbstractMavenReport {

	private static final String BRLRULES = "drlrules";

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
	 * @component
	 * @required
	 * @readonly
	 */
	private Renderer siteRenderer;

	private static Logger log = Logger.getLogger(TechnicalRulesReport.class);

	private HashMap<String, String> map;

	@Override
	protected void executeReport(Locale arg0) throws MavenReportException {
		try {
			generateReport();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}

	private void generateReport() throws SardineException, Exception {

		try {
			File inFolder = new File(brlinputfolder);
			inFolder.mkdirs();
			File[] dir = inFolder.listFiles();
			log.info("Fils in brlinputFolder " + dir.length);
			map = new HashMap<String, String>();
			for (File file : dir) {
				if (!file.isFile()) {
					continue;
				} else if (file.getName().endsWith(".enumeration")) {
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
			}
			Sink sink = getSink();

			sink.head();
			sink.title();
			sink.text("Rules report");
			sink.title_();

			SinkEventAttributeSet jsatts = new SinkEventAttributeSet();
			jsatts.addAttribute(SinkEventAttributes.TYPE, "text/javascript");
			jsatts.addAttribute(SinkEventAttributes.SRC, "js/jquery.js");
			sink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, jsatts);
			sink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);

			SinkEventAttributeSet pagerAttr = new SinkEventAttributeSet();
			pagerAttr.addAttribute(SinkEventAttributes.TYPE, "text/javascript");
			pagerAttr.addAttribute(SinkEventAttributes.SRC, "js/jquery.pajinate.js");
			sink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, pagerAttr);
			sink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);

			SinkEventAttributeSet sorterAttr = new SinkEventAttributeSet();
			sorterAttr.addAttribute(SinkEventAttributes.TYPE, "text/javascript");
			sorterAttr.addAttribute(SinkEventAttributes.SRC, "js/tablesort.js");
			sink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, sorterAttr);
			sink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);

			SinkEventAttributeSet atts = new SinkEventAttributeSet();
			atts.addAttribute(SinkEventAttributes.TYPE, "text/javascript");
			atts.addAttribute(SinkEventAttributes.SRC, "js/page.js");
			sink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, atts);
			sink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);

			sink.head_();

			sink.body();

			technicalRules(dir, sink);

			sink.body_();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void technicalRules(File[] dir, Sink sink) throws ParserConfigurationException, SAXException, IOException {
		SinkEventAttributes tableAttr;
		sink.section1();
		sink.sectionTitle1();
		sink.text("Technical rules");
		sink.sectionTitle1_();

		sink.lineBreak();

		tableAttr = new SinkEventAttributeSet();
		tableAttr.addAttribute(SinkEventAttributes.ID, "results");
		tableAttr.addAttribute(SinkEventAttributes.CLASS, "bodyTable no-arrow rowstyle-alt colstyle-alt paginate-20 max-pages-7 paginationcallback-callbackTest-calculateTotalRating paginationcallback-callbackTest-displayTextInfo sortcompletecallback-callbackTest-calculateTotalRating");

		sink.table(tableAttr);
		sink.tableRow();
		sink.tableHeaderCell();
		sink.text("Name");
		sink.tableHeaderCell_();
		sink.tableRow_();

		for (File file : dir) {
			if (file.getName().endsWith(".drl")) {
				sink.tableRow();
				createRuleHtml(file);
				SinkEventAttributes linkAttr = new SinkEventAttributeSet();
				linkAttr.addAttribute("onclick", "javascript:showBusinessRules(\"" + BRLRULES + "/" + file.getName().replaceAll(".drl", ".html").replaceAll(" ", "") + "\"" + ")");
				linkAttr.addAttribute("href", "javascript:linkme();");
				sink.tableCell();
				sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, linkAttr);
				sink.text(file.getName().replaceAll(".drl", ""));
				sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);
				sink.tableCell_();
				sink.tableRow_();
			}

		}
		sink.table_();

		sink.lineBreak();
		sink.lineBreak();

		sink.section3();
		sink.sectionTitle3();
		sink.text("Rule Details");
		sink.sectionTitle3_();
		sink.lineBreak();
		SinkEventAttributeSet findigDivAttrs = new SinkEventAttributeSet();
		findigDivAttrs.addAttribute(SinkEventAttributes.CLASS, "businessrules");
		sink.unknown("div", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, findigDivAttrs);
		sink.text("Click findig on rule table to see the details.");
		sink.unknown("div", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);
		sink.section3_();

		sink.section1_();
	}

	private void createRuleHtml(File brlFile) {
		try {
			File rulesHtmlFolder = new File(outputDirectory + "/" + BRLRULES);
			rulesHtmlFolder.mkdirs();
			File file = new File(rulesHtmlFolder, brlFile.getName().replaceAll(".drl", ".html").replaceAll(" ", ""));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(brlFile), "UTF-8"));
			bw.write("<body>");
			while (br.ready()) {
				String line = br.readLine();
				if (line.contains("\\t")) {
					line = line.replaceAll("\\t", "<blockquote>");
					line = line + "</blockquote>";
				}
				bw.write(line);
				bw.write("<br/>");
			}
			bw.write("</body>");
			bw.close();
			br.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
		return "technical-rules-report";
	}

	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("technical-rules-report");
	}
}
