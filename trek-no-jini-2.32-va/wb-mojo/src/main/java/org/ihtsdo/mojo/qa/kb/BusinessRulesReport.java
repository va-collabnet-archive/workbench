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
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.module.xhtml.XhtmlSinkFactory;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributeSet;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.googlecode.sardine.util.SardineException;

/**
 * The mojo creates a new knowledge base reference and lnks it to a context
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Vahram
 * @goal rules-report
 * @phase process-resources
 */
public class BusinessRulesReport extends AbstractMavenReport {

	private static final String BRLRULES = "brlrules";

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

	private static Logger log = Logger.getLogger(BusinessRulesReport.class);

	private HashMap<String, String> map;

	private Document document;

	@Override
	protected void executeReport(Locale arg0) throws MavenReportException {
		try {
			generateReport();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}

	private void createRuleHtml(File brlFile, Document document) throws Exception {
		File rulesHtmlFolder = new File(outputDirectory + "/" + BRLRULES);
		rulesHtmlFolder.mkdirs();
		File file = new File(rulesHtmlFolder, brlFile.getName().replaceAll(".brl", ".html").replaceAll(" ", ""));
		Sink srs = new XhtmlSinkFactory().createSink(rulesHtmlFolder, brlFile.getName().replaceAll(".brl", ".html").replaceAll(" ", ""));

		srs.body();

		srs.section2();
		srs.sectionTitle2();
		srs.text(brlFile.getName().replaceAll(".brl", ""));
		srs.sectionTitle2_();

		createAttribtesTable(document, srs);

		createWhenTable(document, srs);

		createThenTable(document, srs);

		srs.section2_();

		srs.body_();

		srs.flush();
		srs.close();
	}

	@SuppressWarnings("unchecked")
	private void createAttribtesTable(Document document, Sink srs) {
		List<Node> attributes = document.selectNodes("//rule/attributes/attribute");
		List<Node> metadataList = document.selectNodes("//rule/metadataList/metadata");

		srs.table();
		srs.tableRow();
		srs.tableHeaderCell();
		srs.text("Attributes");
		srs.tableHeaderCell_();
		srs.tableRow_();

		for (Node attribute : attributes) {
			StringBuffer line = new StringBuffer();
			Node attributeName = attribute.selectSingleNode("attributeName");
			Node value = attribute.selectSingleNode("value");
			line.append(attributeName.getText() + " " + value.getText());
			srs.tableRow();
			srs.tableCell();
			srs.text(line.toString());
			srs.tableCell_();
			srs.tableRow_();
		}

		for (Node metadata : metadataList) {
			StringBuffer line = new StringBuffer();
			Node attributeName = metadata.selectSingleNode("attributeName");
			Node value = metadata.selectSingleNode("value");
			line.append("@" + attributeName.getText() + "(" + value.getText() + ")");
			srs.tableRow();
			srs.tableCell();
			srs.text(line.toString());
			srs.tableCell_();
			srs.tableRow_();
		}

		srs.table_();

	}

	@SuppressWarnings("unchecked")
	private void createThenTable(Document document, Sink srs) {
		Node factType = document.selectSingleNode("//rule/rhs/assert/factType");
		List<Node> asserts = document.selectNodes("//rule/rhs/assert");
		srs.table();
		srs.tableRow();
		srs.tableHeaderCell();
		srs.text("THEN");
		srs.tableHeaderCell_();
		srs.tableRow_();
		int i = 0;
		for (Node myAssert : asserts) {
			List<Node> fieldValues = myAssert.selectNodes("fieldValues/fieldValue");
			List<Node> freeForms = myAssert.selectNodes("freeForm");
			List<Node> freeFormTexts = myAssert.selectNodes("freeForm/text");
			if (freeForms.size() > 1) {
				log.warn("MORE THEN ONE FREE FORM IN " + document.getName());
			}
			StringBuffer thenLine = new StringBuffer();
			if (factType != null) {
				thenLine.append(factType.getText() + " fact" + i + " = new " + factType.getText() + "();");
			} else {
				log.info("NO fact Type in " + document.getName());
				thenLine.append("No fact type");
			}

			srs.tableRow();
			srs.tableCell();
			srs.text(thenLine.toString());
			srs.tableCell_();
			srs.tableRow_();

			for (Node node : fieldValues) {
				StringBuffer line = new StringBuffer();
				Node nodeType = node.selectSingleNode("type");
				Node field = node.selectSingleNode("field");
				Node value = node.selectSingleNode("value");
				if (nodeType.getText().equals("Numeric")) {
					line.append("fact" + i + ".set" + field.getText().substring(0, 1).toUpperCase());
					line.append(field.getText().substring(1, field.getText().length()));
					line.append("(");
					line.append(value.getText());
					line.append(");");
				} else if (nodeType.getText().equals("String")) {
					line.append("fact" + i + ".set" + field.getText().substring(0, 1).toUpperCase());
					line.append(field.getText().substring(1, field.getText().length()));
					line.append("(\"");
					line.append(value.getText());
					line.append("\");");
				} else {
					log.info("not numeric nor String, is: " + nodeType.getText());
					log.info(document.getName());
				}
				srs.tableRow();
				srs.tableCell();
				srs.text(line.toString());
				srs.tableCell_();
				srs.tableRow_();
			}
			StringBuffer line = new StringBuffer("insert(fact" + i + ");");
			srs.tableRow();
			srs.tableCell();
			srs.text(line.toString());
			srs.tableCell_();
			srs.tableRow_();

			for (Node node : freeFormTexts) {
				StringBuffer freeFormLine = new StringBuffer(node.getText());
				srs.tableRow();
				srs.tableCell();
				srs.text(freeFormLine.toString());
				srs.tableCell_();
				srs.tableRow_();
			}
			i++;
		}

		srs.table_();
	}

	@SuppressWarnings("unchecked")
	private void createWhenTable(Document document, Sink srs) throws Exception {
		List<Node> facts = document.selectNodes("//rule/lhs/fact");
		List<Node> fromCompositePattern = document.selectNodes("//rule/lhs/fromCompositePattern");

		List<Node> compositePattern = document.selectNodes("//rule/lhs/compositePattern");
		List<Node> freeFormTexts = document.selectNodes("//rule/lhs/freeForm/text");
		List<Node> dslNode = document.selectNodes("//rule/lhs/dslSentence");

		srs.table();
		srs.tableRow();
		srs.tableHeaderCell();
		srs.text("WHEN");
		srs.tableHeaderCell_();
		srs.tableRow_();

		processFacts(srs, facts, false);
		StringBuffer factLine = new StringBuffer();
		processformCompositePatterns(srs, fromCompositePattern, factLine, false);

		for (Node node : compositePattern) {
			Node type = node.selectSingleNode("type");
			StringBuffer compositeFactLine = new StringBuffer();
			String typeText = type.getText();
			compositeFactLine.append(typeText + " (");
			List<Node> factsList = node.selectNodes("patterns/fact");
			List<Node> fromCompositePatterns = node.selectNodes("patterns/fromCompositePattern");

			processFacts(srs, factsList, true);
			processformCompositePatterns(srs, fromCompositePatterns, compositeFactLine, true);

		}

		for (Node node : dslNode) {
			List<Node> dslSentence = node.selectNodes("sentence");
			for (Node object : dslSentence) {
				srs.tableRow();
				String line = object.getText();
				String[] parts = line.split("\\{");
				for (int k = 0; k < parts.length; k++) {
					if (parts[k].contains("ENUM")) {
						String target = parts[k].substring(0, parts[k].indexOf("}"));
						String part = target.split(":")[2].concat(target.split(":")[0]);
						if (map.containsKey(part)) {
							part = map.get(part);
							line = line.replace("{" + target + "}", part);
						}
					}
				}
				srs.tableCell();
				srs.text(line);
				srs.tableCell_();
				srs.tableRow_();
			}

		}

		for (Node node : freeFormTexts) {
			StringBuffer freeFormLine = new StringBuffer(node.getText());
			srs.tableRow();
			srs.tableCell();
			srs.text(freeFormLine.toString());
			srs.tableCell_();
			srs.tableRow_();
		}
		srs.table_();
	}

	@SuppressWarnings("unchecked")
	private void processformCompositePatterns(Sink srs, List<Node> formCompositePatterns, StringBuffer factLine, boolean compositePattern) throws Exception {
		for (Node fact : formCompositePatterns) {
			List<Node> fieldConstraints = fact.selectNodes("factPattern/constraintList/constraints/fieldConstraint");
			List<Node> compositeConstraints = fact.selectNodes("factPattern/constraintList/constraints/compositeConstraint");

			Node factType = fact.selectSingleNode("factPattern/factType");
			Node boundName = fact.selectSingleNode("factPattern/boundName");
			if (boundName != null) {
				factLine.append(boundName.getText() + " : ");
			}
			if (factType != null) {
				factLine.append(factType.getText() + "( ");
			}
			int i = 0;
			for (Node fieldConstraint : fieldConstraints) {
				processFieldConstraint(factLine, fieldConstraint);
				i++;
				if (i != fieldConstraints.size() || !compositeConstraints.isEmpty()) {
					factLine.append(", ");
				}
			}

			i = 0;
			for (Node node : compositeConstraints) {
				recurseveProcessCompositeConstraint(factLine, node);
				i++;
				if (i != compositeConstraints.size()) {
					factLine.append(", ");
				}
			}

			factLine.append(" )");

			List<Node> parts = fact.selectNodes("expression/parts");
			if (parts.size() > 1) {
				log.info("Part size > 1");
			}
			Node expPartVariableName = fact.selectSingleNode("expression/parts/variable/name");
			Node nextName = fact.selectSingleNode("expression/parts/variable/next/name");

			factLine.append(" from " + expPartVariableName.getText() + "." + nextName.getText());
			if (compositePattern) {
				factLine.append(" )");
			}
			srs.tableRow();
			srs.tableCell();
			srs.text(factLine.toString());
			srs.tableCell_();
			srs.tableRow_();
		}
	}

	@SuppressWarnings("unchecked")
	private void processFacts(Sink srs, List<Node> facts, boolean isComposite) throws Exception {
		for (Node fact : facts) {
			List<Node> fieldConstraints = fact.selectNodes("constraintList/constraints/fieldConstraint");
			List<Node> compositeConstraints = fact.selectNodes("constraintList/constraints/compositeConstraint");

			StringBuffer factLine = new StringBuffer();
			Node factType = fact.selectSingleNode("factType");
			Node boundName = fact.selectSingleNode("boundName");
			if (boundName != null) {
				factLine.append(boundName.getText() + " : ");
			}
			if (factType != null) {
				factLine.append(factType.getText() + "( ");
			}
			int i = 0;
			for (Node fieldConstraint : fieldConstraints) {
				processFieldConstraint(factLine, fieldConstraint);
				i++;
				if (i != fieldConstraints.size() || !compositeConstraints.isEmpty()) {
					factLine.append(", ");
				}
			}

			i = 0;
			for (Node node : compositeConstraints) {
				recurseveProcessCompositeConstraint(factLine, node);
				i++;
				if (i != compositeConstraints.size()) {
					factLine.append(", ");
				}
			}

			factLine.append(" )");

			if (isComposite) {
				factLine.append(" )");
			}
			srs.tableRow();
			srs.tableCell();
			srs.text(factLine.toString());
			srs.tableCell_();
			srs.tableRow_();
		}
	}

	@SuppressWarnings("unchecked")
	private void recurseveProcessCompositeConstraint(StringBuffer factLine, Node compositeConstraint) throws Exception {
		Node compositeJunctionTypes = compositeConstraint.selectSingleNode("compositeJunctionType");

		String junctionString = compositeJunctionTypes.getText();

		List<Node> fieldConstraints = compositeConstraint.selectNodes("constraints/fieldConstraint");
		List<Node> compositeConstraints = compositeConstraint.selectNodes("constraints/compositeConstraint");

		int i = 0;
		for (Node fieldConstraint : fieldConstraints) {
			processFieldConstraint(factLine, fieldConstraint);
			i++;
			if (i != fieldConstraints.size()) {
				factLine.append(" " + junctionString + " ");
			} else if (!compositeConstraints.isEmpty()) {
				factLine.append(", ");
			}
		}

		i = 0;
		for (Node node : compositeConstraints) {
			recurseveProcessCompositeConstraint(factLine, node);
			i++;
			if (i != compositeConstraints.size()) {
				factLine.append(" " + junctionString + " ");
			}
		}
	}

	private void processFieldConstraint(StringBuffer factLine, Node fieldConstraint) throws Exception {
		Node constraintValueType = fieldConstraint.selectSingleNode("constraintValueType");
		Node fieldName = null;
		Node operator = null;
		Node value = null;
		Node fieldType = null;
		if (constraintValueType.getText().equals("1") || constraintValueType.getText().equals("4") || constraintValueType.getText().equals("3") || constraintValueType.getText().equals("2")) {
			fieldName = fieldConstraint.selectSingleNode("fieldName");
			operator = fieldConstraint.selectSingleNode("operator");
			value = fieldConstraint.selectSingleNode("value");
			fieldType = fieldConstraint.selectSingleNode("fieldType");
		} else if (constraintValueType.getText().equals("6")) {
			fieldName = fieldConstraint.selectSingleNode("expression/parts/variable/name");
			operator = fieldConstraint.selectSingleNode("operator");
			value = fieldConstraint.selectSingleNode("value");
			fieldType = fieldConstraint.selectSingleNode("fieldType");

		} else if (constraintValueType.getText().equals("5")) {
			value = fieldConstraint.selectSingleNode("value");
			factLine.append("eval(" + value.getText() + ")");
			return;
		} else if (constraintValueType.getText().equals("0")) {
			Node fieldBinding = fieldConstraint.selectSingleNode("fieldBinding");
			fieldName = fieldConstraint.selectSingleNode("fieldName");
			factLine.append(fieldName.getText() + " : " + fieldBinding.getText());
			return;
		} else {
			log.warn("Unsuported constraint type: " + "\"" + constraintValueType.getText() + "\"" + " in " + document.getName());
		}
		factLine.append(fieldName != null ? fieldName.getText() : "-");
		factLine.append(operator != null ? " " + operator.getText() + " " : "-");

		if (fieldType != null && fieldType.getText().equals("String")) {
			factLine.append("\"");
		}

		factLine.append(value == null ? "-" : value.getText());
		if (fieldType != null && fieldType.getText().equals("String")) {
			factLine.append("\"");
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

			bussinessRules(dir, sink);

			sink.body_();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void bussinessRules(File[] dir, Sink sink) throws Exception {
		sink.section1();
		sink.sectionTitle1();
		sink.text("Busines rules");
		sink.sectionTitle1_();

		sink.lineBreak();

		SinkEventAttributes tableAttr = new SinkEventAttributeSet();
		tableAttr.addAttribute(SinkEventAttributes.ID, "results");
		tableAttr.addAttribute(SinkEventAttributes.CLASS, "bodyTable no-arrow rowstyle-alt colstyle-alt paginate-20 max-pages-7 paginationcallback-callbackTest-calculateTotalRating paginationcallback-callbackTest-displayTextInfo sortcompletecallback-callbackTest-calculateTotalRating");

		sink.table(tableAttr);
		sink.tableRow();
		sink.tableHeaderCell();
		sink.text("Name");
		sink.tableHeaderCell_();
		sink.tableRow_();

		for (File file : dir) {
			if (file.getName().endsWith(".brl")) {

				SAXReader reader = new SAXReader();
				Document document = reader.read(file);

				sink.tableRow();
				Node node = document.selectSingleNode("//rule/name");
				this.document = document;
				try {
					createRuleHtml(file, document);
				} catch (Exception e) {
					log.info(file.getName());
					throw e;
				}
				SinkEventAttributes linkAttr = new SinkEventAttributeSet();
				linkAttr.addAttribute("onclick", "javascript:showBusinessRules(\"" + BRLRULES + "/" + file.getName().replaceAll(".brl", ".html").replaceAll(" ", "") + "\"" + ")");
				linkAttr.addAttribute("href", "javascript:linkme();");
				sink.tableCell();
				sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, linkAttr);
				sink.text(node.getText());
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
		SinkEventAttributes findigDivAttrs = new SinkEventAttributeSet();
		findigDivAttrs.addAttribute(SinkEventAttributes.CLASS, "businessrules");
		sink.unknown("div", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, findigDivAttrs);
		sink.text("Click findig on rule table to see the details.");
		sink.unknown("div", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);
		sink.section3_();

		sink.section1_();
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
		return project.getArtifactId() + "rules-report";
	}

	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("rules-report");
	}
}
