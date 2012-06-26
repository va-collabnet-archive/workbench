package org.ihtsdo.mojo.qa.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributeSet;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.ihtsdo.rf2.file.delta.snapshot.tasks.FileSorter;

/**
 * The <codebatchQACheck</code> class iterates through the concepts from a
 * viewpoint and preforms QA
 * 
 * @author termmed
 * @goal report-qa
 * @phase site
 */
public class ReportBatchQACheck extends AbstractMavenReport {
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
	 * @component
	 * @required
	 * @readonly
	 */
	private SiteRenderer siteRenderer;

	/**
	 * Execution details csv/txt file.
	 * 
	 * @parameter
	 */
	private String executionDetailsOutputStr;

	/**
	 * Findings csv/txt file.
	 * 
	 * @parameter
	 */
	private String findingsOutputStr;

	/**
	 * Rules csv/txt file.
	 * 
	 * @parameter
	 */
	private String rulesOutputStr;

	@Override
	protected void executeReport(Locale arg0) throws MavenReportException {
		try {
			File findingFile = new File(findingsOutputStr);
			File outputDir = new File(outputDirectory);
			File sortedFindings = new File(outputDir, "sortedFindings.txt");
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}
			File tempFolder = new File(outputDir, "sortTemp");
			tempFolder.mkdirs();
			int[] sortColumns = new int[] { 4 };
			FileSorter fs = new FileSorter(findingFile, sortedFindings, tempFolder, sortColumns);
			fs.execute();

			FileInputStream sortedFis = new FileInputStream(sortedFindings);
			InputStreamReader sortedIsr = new InputStreamReader(sortedFis, "UTF-8");
			BufferedReader sortedBr = new BufferedReader(sortedIsr);
			Set<String> rulesWithFindings = new HashSet<String>();
			if (sortedBr.ready()) {
				sortedBr.readLine();
			}
			String fLine = "";
			while (sortedBr.ready()) {
				fLine = sortedBr.readLine();
				String[] splited = fLine.split("\\t", -1);
				if (splited.length >= 5) {
					rulesWithFindings.add(splited[4]);
				}
			}

			File findingFolder = new File(outputDir, "findings");
			if (rulesWithFindings != null && !rulesWithFindings.isEmpty()) {
				findingFolder.mkdirs();
			}

			sortedBr.close();
			ResourceBundle resources = ResourceBundle.getBundle("convertion");

			File executionFile = new File(executionDetailsOutputStr);
			FileInputStream fis = new FileInputStream(executionFile);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);

			Sink sink = getSink();

			sink.head();
			sink.title();
			sink.text("Quality Assurance Report");
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
			sink.section1();

			sink.sectionTitle1();
			sink.text("Execution");
			sink.sectionTitle1_();

			sink.lineBreak();
			sink.lineBreak();

			sink.table();
			sink.tableRow();
			String heading = br.readLine();
			String[] splited = heading.split("\\t", -1);
			for (String string : splited) {
				sink.tableHeaderCell();
				sink.text(string);
				sink.tableHeaderCell_();
			}
			sink.tableRow_();
			String executionLine = br.readLine();
			String[] splitedLine = executionLine.split("\\t", -1);
			sink.tableRow();
			for (String string : splitedLine) {
				sink.tableCell();
				sink.text(string);
				sink.tableCell_();
			}
			sink.tableRow_();
			sink.table_();
			sink.section1_();

			File rules = new File(rulesOutputStr);
			FileInputStream ruleFis = new FileInputStream(rules);
			InputStreamReader ruleIsr = new InputStreamReader(ruleFis, "UTF-8");
			BufferedReader ruleBr = new BufferedReader(ruleIsr);
			if (ruleBr.ready()) {
				String rulesHeader = ruleBr.readLine();

				// SECTION 2
				sink.section2();

				sink.sectionTitle2();
				sink.text("Rules");
				sink.sectionTitle2_();

				sink.lineBreak();
				sink.lineBreak();

				SinkEventAttributes tableAttr = new SinkEventAttributeSet();
				tableAttr.addAttribute(SinkEventAttributes.ID, "results");
				tableAttr.addAttribute(SinkEventAttributes.CLASS,
						"bodyTable sortable-onload-3 no-arrow rowstyle-alt colstyle-alt paginate-10 max-pages-7 paginationcallback-callbackTest-calculateTotalRating paginationcallback-callbackTest-displayTextInfo sortcompletecallback-callbackTest-calculateTotalRating");

				sink.table(tableAttr);
				sink.tableRow();
				String[] rulesHeaderSplited = rulesHeader.split("\\t", -1);
				SinkEventAttributes headerAttrs = new SinkEventAttributeSet();
				for (int i = 0; i < rulesHeaderSplited.length; i++) {
					if (i >= 1 && i <= 3) {
						headerAttrs.addAttribute(SinkEventAttributes.CLASS, "sortable-text fd-column-" + (i - 1));
						sink.tableHeaderCell(headerAttrs);
						sink.text(rulesHeaderSplited[i]);
						sink.tableHeaderCell_();
					}
				}
				headerAttrs.addAttribute(SinkEventAttributes.CLASS, "sortable-numeric fd-column-" + (3));
				sink.tableHeaderCell(headerAttrs);
				sink.text("Findings");
				sink.tableHeaderCell_();
				sink.tableRow_();
				SinkEventAttributes linkAttr = new SinkEventAttributeSet();
				linkAttr.addAttribute("onclick", "javascript:showRuleDetails(this);");
				linkAttr.addAttribute("href", "javascript:linkme();");
				while (ruleBr.ready()) {
					String ruleLine = ruleBr.readLine();
					String[] ruleLineSplit = ruleLine.split("\\t", -1);
					int findigSize = 0;
					if (rulesWithFindings.contains(ruleLineSplit[0])) {
						findigSize = createFindingsReport(ruleLineSplit, sortedFindings, findingFolder);
					}
					sink.tableRow();
					for (int i = 0; i < ruleLineSplit.length; i++) {
						if (i >= 1 && i <= 3) {
							sink.tableCell();
							if (i == 3) {
								sink.text(resources.getString(ruleLineSplit[i]));
							} else if (i == 1) {
								sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, linkAttr);
								sink.text(ruleLineSplit[i]);
								sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);
							} else {
								sink.text(ruleLineSplit[i]);
							}
						}
						sink.tableCell_();
						SinkEventAttributeSet hidden = new SinkEventAttributeSet();
						hidden.addAttribute(SinkEventAttributes.TYPE, "hidden");
						hidden.addAttribute(SinkEventAttributes.NAME, rulesHeaderSplited[i].replaceAll(" ", "").toLowerCase());
						hidden.addAttribute("value", ruleLineSplit[i]);
						sink.unknown("input", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, hidden);
						sink.unknown("input", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);
					}
					sink.tableCell();
					if (findigSize > 0) {
						SinkEventAttributes findigLinkAttrs = new SinkEventAttributeSet();
						findigLinkAttrs.addAttribute("onclick", "javascript:showRuleDetails(this);javascript:showFindings(\"findings/" + ruleLineSplit[0] + ".html\"" + ")");
						findigLinkAttrs.addAttribute("href", "javascript:nada();");
						sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, findigLinkAttrs);
						sink.text("" + findigSize);
						sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);
					} else {
						sink.text("" + findigSize);
					}
					sink.tableRow_();
					sink.tableCell_();
				}
				sink.table_();
			}

			sink.section3();
			sink.sectionTitle3();
			sink.text("Rule Details");
			sink.sectionTitle3_();
			sink.lineBreak();
			SinkEventAttributes divAttrs = new SinkEventAttributeSet();
			divAttrs.addAttribute(SinkEventAttributes.CLASS, "ruleDetails");
			sink.unknown("div", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, divAttrs);
			sink.text("Click on a rule to see the details.");
			sink.unknown("div", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);

			sink.section3_();

			sink.section3();
			sink.sectionTitle3();
			sink.text("Findings");
			sink.sectionTitle3_();
			sink.lineBreak();
			SinkEventAttributes findigDivAttrs = new SinkEventAttributeSet();
			findigDivAttrs.addAttribute(SinkEventAttributes.CLASS, "findings");
			sink.unknown("div", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, findigDivAttrs);
			sink.text("Click findig on rule table to see the details.");
			sink.unknown("div", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);
			sink.section3_();

			sink.section2_();
			sink.body_();
			sink.flush();
			sink.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int createFindingsReport(String[] ruleLineSplit, File sortedFindings, File findingFolder) {
		int result = 0;
		try {
			FileInputStream sortedFis = new FileInputStream(sortedFindings);
			InputStreamReader sortedIsr = new InputStreamReader(sortedFis, "UTF-8");
			BufferedReader sortedBr = new BufferedReader(sortedIsr);

			File findingFile = new File(findingFolder, ruleLineSplit[0] + ".html");
			BufferedWriter bw = new BufferedWriter(new FileWriter(findingFile));
			bw.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			bw.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
			bw.write("<body>");

			bw.write("<table border=\"0\" id=\"findingTable\" class=\"bodyTable sortable-onload-0 no-arrow rowstyle-alt colstyle-alt paginate-10 max-pages-7 paginationcallback-callbackTest-calculateTotalRating paginationcallback-callbackTest-displayTextInfo sortcompletecallback-callbackTest-calculateTotalRating\">");

			String header = sortedBr.readLine();
			String[] headerSplited = header.split("\\t", -1);

			bw.write("<tr class=\"a\">");
			bw.newLine();
			bw.write("<th>Component name</th>");
			bw.newLine();
			bw.write("<th>UUID</th>");
			bw.newLine();
			bw.write("</tr>");
			// Create tableheader in the report.
			String line = "";
			String[] splitedLine = null;
			while (sortedBr.ready()) {
				line = sortedBr.readLine();
				splitedLine = line.split("\\t", -1);
				if (splitedLine[4].equals(ruleLineSplit[0])) {
					break;
				}
			}

			while (sortedBr.ready() && splitedLine != null && splitedLine.length >= 5 && splitedLine[4].equals(ruleLineSplit[0])) {
				result++;
				bw.write("<tr class=\"b\">");
				for (int i = 0; i < splitedLine.length; i++) {
					if (i == 0|| i == 7) {
						bw.write("<td>" + splitedLine[i] + "</td>");
					}
					bw.write("<input type=\"hidden\" name=\"" + headerSplited[i].replaceAll(" ", "").toLowerCase() + "\"" + " value=\"" + splitedLine[i] + "\"/>");
				}
				bw.write("</tr>");
				line = sortedBr.readLine();
				splitedLine = line.split("\\t", -1);
			}

			if (line.trim() != "" && splitedLine.length >= 5 && splitedLine[4].equals(ruleLineSplit[0])) {
				result++;
				bw.write("<tr class=\"b\">");
				String name = "";
				String uuid = "";
				for (int i = 0; i < splitedLine.length; i++) {
					if (i == 1) {
						uuid = splitedLine[i];
					}else if(i == 7){
						name = splitedLine[i];
					}
					bw.write("<input type=\"hidden\" name=\"" + headerSplited[i].replaceAll(" ", "").toLowerCase() + "\"" + " value=\"" + splitedLine[i] + "\"/>");
				}
				bw.write("<td>"+name+"</td>");
				bw.write("<td>"+uuid+"</td>");
				bw.write("</tr>");
			}
			bw.write("</table>");
			bw.write("</body>");
			bw.write("</html>");
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private File generateHtmlReport() {
		BufferedWriter bw = null;
		File f = null;
		try {
			f = new File(outputDirectory, "file.html");
			bw = new BufferedWriter(new FileWriter(f));
			bw.write("HOLA");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return f;
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
	protected SiteRenderer getSiteRenderer() {
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
		return project.getArtifactId() + "-BatchQA-Report";
	}

	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("report-qa");
	}
}
