package org.ihtsdo.mojo.qa.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributeSet;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
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
	private Renderer siteRenderer;

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

	private static final int EXECUTION_UUID = 0;
	private static final int EXECUTION_DATABASE_UUID = 1;
	private static final int EXECUTION_PATH_UUID = 2;
	private static final int EXECUTION_NAME = 3;
	private static final int EXECUTION_VIEW_POINT = 4;
	private static final int EXECUTION_START_TIME = 5;
	private static final int EXECUTION_END_TIME = 6;
	private static final int EXECUTION_CONTEXT = 7;
	private static final int EXECUTION_PATH_NAME = 8;

	private static final Logger log = Logger.getLogger(ReportBatchQACheck.class);

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
			sink.text("Last Batch QA Execution");
			sink.sectionTitle1_();

			sink.lineBreak();

			 String executionLine = br.readLine();
			 executionLine = br.readLine();
			 br.close();
			 String[] splitedLine = executionLine.split("\\t", -1);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			SimpleDateFormat sdf1 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
			String startTime = "";
			try {
				startTime = sdf1.format(sdf.parse(splitedLine[EXECUTION_START_TIME]));
			} catch (Exception e) {
				e.printStackTrace();
				startTime = "{Invalid date format in execution file}";
			}
			String endTime = "";
			try {
				endTime = sdf1.format(sdf.parse(splitedLine[EXECUTION_END_TIME]));
			} catch (Exception e) {
				e.printStackTrace();
				endTime = "{Invalid date format in execution file}";
			}
			sink.bold();
			sink.text("Start time: ");
			sink.bold_();
			sink.text(startTime);
			sink.lineBreak();
			

			sink.bold();
			sink.text("End time: ");
			sink.bold_();
			sink.text(endTime);
			sink.lineBreak();

			sink.bold();
			sink.text("Database Name: ");
			sink.bold_();

			sink.text(splitedLine[EXECUTION_NAME]);
			sink.lineBreak();

			sink.bold();
			sink.text("Database UUID: ");
			sink.bold_();

			sink.text(splitedLine[EXECUTION_DATABASE_UUID]);
			sink.lineBreak();

			sink.bold();
			sink.text("Path tested: ");
			sink.bold_();
			sink.text(splitedLine[EXECUTION_PATH_NAME] + " - " + splitedLine[EXECUTION_PATH_UUID]);
			sink.lineBreak();
			String vpt = "";
			try {
				Date viewPointTime = sdf.parse(splitedLine[EXECUTION_VIEW_POINT]);
				Date oneYearAfter = new Date((new Date().getTime()) + 30000000000l);
				if (viewPointTime.getTime() > oneYearAfter.getTime()) {
					vpt = "latest";
				} else {
					vpt = sdf1.format(viewPointTime);
				}
			} catch (Exception e) {
				e.printStackTrace();
				vpt = "{Invalid date format in execution file}";
			}
			sink.bold();
			sink.text("View point time: ");
			sink.bold_();
			sink.text(vpt);
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

				sink.text("This is the list of rules of the QA system, including the number of violation in each one. The list can be sorted by clicking on the headers.");
				
				sink.lineBreak();
				sink.lineBreak();

				SinkEventAttributes tableAttr = new SinkEventAttributeSet();
				tableAttr.addAttribute(SinkEventAttributes.ID, "results");
				tableAttr.addAttribute(SinkEventAttributes.CLASS,
						"bodyTable sortable-onload-3 no-arrow rowstyle-alt colstyle-alt paginate-20 max-pages-7 paginationcallback-callbackTest-calculateTotalRating paginationcallback-callbackTest-displayTextInfo sortcompletecallback-callbackTest-calculateTotalRating");

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
				while (ruleBr.ready()) {
					String ruleLine = ruleBr.readLine();
					String[] ruleLineSplit = ruleLine.split("\\t", -1);
					HashMap<String, Integer> findings = null;

					if (rulesWithFindings.contains(ruleLineSplit[0])) {
						findings = createFindingsReport(ruleLineSplit, sortedFindings, findingFolder);
					}
					SinkEventAttributes linkAttr = new SinkEventAttributeSet();
					if (findings != null) {
						Set<String> findingFiles = findings.keySet();
						for (String string : findingFiles) {
							Integer findingSize = findings.get(string);
							if (findings != null && findingSize == 0) {
								linkAttr.addAttribute("onclick", "javascript:showRuleDetails(this);");
							} else {
								linkAttr.addAttribute("onclick", "javascript:showRuleDetails(this);javascript:showFindings(\"findings/" + string + "\"" + ")");
							}
							linkAttr.addAttribute("href", "javascript:linkme();");
							sink.tableRow();
							for (int i = 0; i < ruleLineSplit.length; i++) {
								if (i >= 1 && i <= 3) {
									sink.tableCell();
									if (i == 3) {
										sink.text(resources.getString(ruleLineSplit[i]));
									} else if (i == 1) {
										sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, linkAttr);
										if (findings.size() > 1) {
											sink.text(ruleLineSplit[i] + " "+ string.replaceAll(ruleLineSplit[0], ""));
										} else {
											sink.text(ruleLineSplit[i]);
										}
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
							if (findingSize > 0) {
								SinkEventAttributes findigLinkAttrs = new SinkEventAttributeSet();
								findigLinkAttrs.addAttribute("onclick", "javascript:showRuleDetails(this);javascript:showFindings(\"findings/" + string + "\"" + ")");
								findigLinkAttrs.addAttribute("href", "javascript:linkme();");
								sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, findigLinkAttrs);
								sink.text("" + findings.get(string));
								sink.unknown("a", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);
							} else {
								sink.text("" + findings.get(string));
							}
							sink.tableRow_();
							sink.tableCell_();
						}
					} else {
						linkAttr.addAttribute("onclick", "javascript:showRuleDetails(this);");
						linkAttr.addAttribute("href", "javascript:linkme();");
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
								} else if (i == 2) {
									sink.text(ruleLineSplit[i].equals("null") ? "Technical rule" : ruleLineSplit[i]);
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
						sink.text("" + 0);
						sink.tableRow_();
						sink.tableCell_();
					}
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

	private HashMap<String, Integer> createFindingsReport(String[] ruleLineSplit, File sortedFindings, File findingFolder) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		try {
			FileInputStream sortedFis = new FileInputStream(sortedFindings);
			InputStreamReader sortedIsr = new InputStreamReader(sortedFis, "UTF-8");
			BufferedReader sortedBr = new BufferedReader(sortedIsr);

			int lines = 0;
			boolean keepmaking = true;
			while (keepmaking) {
				File findingFile = new File(findingFolder, ruleLineSplit[0]);
				BufferedWriter bw = new BufferedWriter(new FileWriter(findingFile));
				bw.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
				bw.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
				bw.write("<body>");

				bw.write("<table border=\"0\" id=\"findingTable\" class=\"bodyTable\">");

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
				while (sortedBr.ready() && splitedLine != null && splitedLine.length >= 7 && splitedLine[4].equals(ruleLineSplit[0])) {
					lines++;
					bw.write("<tr class=\"b\">");
					String name = "";
					String uuid = "";
					for (int i = 0; i < splitedLine.length; i++) {
						if (i == 5) {
							uuid = splitedLine[i];
						} else if (i == 7) {
							name = splitedLine[i];
						}
						bw.write("<input type=\"hidden\" name=\"" + headerSplited[i].replaceAll(" ", "").toLowerCase() + "\"" + " value=\"" + splitedLine[i] + "\"/>");
					}
					bw.write("<td>" + name + "</td>");
					bw.write("<td>" + uuid + "</td>");
					bw.write("</tr>");
					line = sortedBr.readLine();
					splitedLine = line.split("\\t", -1);
					if ((lines + 1) % 1000 == 0) {
						log.info("LINES + 1 = " + (lines + 1));
						break;
					}
				}

				if (line.trim() != "" && splitedLine.length >= 7 && splitedLine[4].equals(ruleLineSplit[0])) {
					lines++;
					log.info("PROCESSING LAST LINE");
					bw.write("<tr class=\"b\">");
					String name = "";
					String uuid = "";
					for (int i = 0; i < splitedLine.length; i++) {
						if (i == 5) {
							uuid = splitedLine[i];
						} else if (i == 7) {
							name = splitedLine[i];
						}
						bw.write("<input type=\"hidden\" name=\"" + headerSplited[i].replaceAll(" ", "").toLowerCase() + "\"" + " value=\"" + splitedLine[i] + "\"/>");
					}
					bw.write("<td>" + name + "</td>");
					bw.write("<td>" + uuid + "</td>");
					bw.write("</tr>");
				}
				bw.write("</table>");
				bw.write("</body>");
				bw.write("</html>");
				bw.flush();
				bw.close();
				if (lines % 1000 == 0) {
					log.info("1000 line file");
					int startline = lines - 1000;
					if (startline <= 0) {
						startline = 0;
					}
					findingFile.renameTo(new File(findingFolder, ruleLineSplit[0]+ "("+  startline + "-" + lines + ").html"));
					result.put(ruleLineSplit[0]+ "("+  startline + "-" + lines + ").html", lines - startline);
				} else {
					log.info("LAST FILE");
					int startline = lines - 1000;
					if (startline <= 0) {
						startline = 0;
					}
					findingFile.renameTo(new File(findingFolder, ruleLineSplit[0] + "("+ startline + "-" + lines + ").html"));
					result.put(ruleLineSplit[0] + "("+  startline + "-" + lines + ").html", lines - startline);
					keepmaking = false;
				}
			}
			sortedBr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
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
		return project.getArtifactId() + "-BatchQA-Report";
	}

	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("report-qa");
	}
}
