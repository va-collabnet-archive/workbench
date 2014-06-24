package org.ihtsdo.mojo.qa.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributeSet;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 * The <codebatchQACheck</code> class iterates through the concepts from a
 * viewpoint and preforms QA
 * 
 * @author termmed
 * @goal review-list-report
 * @phase site
 */
public class QaReviewListReport extends AbstractMavenReport {
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
	private String resourcesFolder;

	@Override
	protected void executeReport(Locale arg0) throws MavenReportException {
		try {
			Sink sink = getSink();
			sink.head();
			sink.title();
			sink.text("Quality Assurance Report");
			sink.title_();
			sink.head_();
			sink.body();

			File resources = new File(resourcesFolder);
			File[] files = resources.listFiles();
			sink.section1();
			sink.table();
			sink.tableRow();

			sink.tableHeaderCell();
			sink.text("List file name");
			sink.tableHeaderCell_();
			sink.tableRow_();

			for (File file : files) {

				FileInputStream sourceFis = new FileInputStream(file);
				InputStreamReader sourceIsr = new InputStreamReader(sourceFis, "UTF-8");
				BufferedReader sourceBr = new BufferedReader(sourceIsr);
				int count = 0;
				while (sourceBr.ready()) {
					sourceBr.readLine();
					count++;
				}
				sourceBr.close();

				createFileReport(file);
				sink.tableRow();
				sink.tableCell();
				sink.link(file.getName().replaceAll(".txt", ".html"));
				sink.text(file.getName().replaceAll(".txt", "") + " ( " + count + " )");
				sink.link_();
				sink.tableCell_();
				sink.tableRow_();
			}
			sink.table_();
			sink.section1_();
			sink.body_();
			closeReport();
			sink.flush();
			sink.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createFileReport(File sourceFile) throws IOException {
		try {

			Sink aSink = getSinkFactory().createSink(new File(outputDirectory), sourceFile.getName().replaceAll(".txt", ".html"));

			aSink.head();
			aSink.title();
			aSink.text("Quality Assurance Report");
			aSink.title_();

			SinkEventAttributeSet jsatts = new SinkEventAttributeSet();
			jsatts.addAttribute(SinkEventAttributes.TYPE, "text/javascript");
			jsatts.addAttribute(SinkEventAttributes.SRC, "js/jquery.js");
			aSink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, jsatts);
			aSink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);

			SinkEventAttributeSet pagerAttr = new SinkEventAttributeSet();
			pagerAttr.addAttribute(SinkEventAttributes.TYPE, "text/javascript");
			pagerAttr.addAttribute(SinkEventAttributes.SRC, "js/jquery.pajinate.js");
			aSink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, pagerAttr);
			aSink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);

			SinkEventAttributeSet sorterAttr = new SinkEventAttributeSet();
			sorterAttr.addAttribute(SinkEventAttributes.TYPE, "text/javascript");
			sorterAttr.addAttribute(SinkEventAttributes.SRC, "js/tablesort.js");
			aSink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, sorterAttr);
			aSink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);

			SinkEventAttributeSet atts = new SinkEventAttributeSet();
			atts.addAttribute(SinkEventAttributes.TYPE, "text/javascript");
			atts.addAttribute(SinkEventAttributes.SRC, "js/page.js");
			aSink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_START) }, atts);
			aSink.unknown("script", new Object[] { new Integer(HtmlMarkup.TAG_TYPE_END) }, null);

			aSink.head_();

			aSink.body();

			aSink.section2();
			aSink.sectionTitle2();

			crateTable(sourceFile, aSink);

			aSink.sectionTitle2_();
			aSink.section2_();

			aSink.body_();
			aSink.flush();
			aSink.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void crateTable(File sourceFile, Sink aSink) {
		BufferedReader sourceBr = null;
		try {

			SinkEventAttributes tableAttr = new SinkEventAttributeSet();
			tableAttr.addAttribute(SinkEventAttributes.ID, "results");
			tableAttr.addAttribute(SinkEventAttributes.CLASS,
					"bodyTable sortable-onload-3 no-arrow rowstyle-alt colstyle-alt paginate-20 max-pages-7 paginationcallback-callbackTest-calculateTotalRating paginationcallback-callbackTest-displayTextInfo sortcompletecallback-callbackTest-calculateTotalRating");

			FileInputStream sourceFis = new FileInputStream(sourceFile);
			InputStreamReader sourceIsr = new InputStreamReader(sourceFis, "UTF-8");
			sourceBr = new BufferedReader(sourceIsr);
			String firstLine = sourceBr.readLine();
			aSink.table(tableAttr);
			if (firstLine != null) {
				firstLine.split("\\t", -1);
				aSink.tableRow();
				String[] rulesHeaderSplited = firstLine.split("\\t", -1);
				SinkEventAttributes headerAttrs = new SinkEventAttributeSet();
				for (int i = 0; i < rulesHeaderSplited.length; i++) {
					headerAttrs.addAttribute(SinkEventAttributes.CLASS, "sortable-text fd-column-" + (i - 1));
					aSink.tableHeaderCell(headerAttrs);
					aSink.text("");
					aSink.tableHeaderCell_();
				}
				aSink.tableRow_();

				aSink.tableRow();
				for (String string : rulesHeaderSplited) {
					aSink.tableCell();
					aSink.text(string);
					aSink.tableCell_();
				}
				aSink.tableRow_();
				while (sourceBr.ready()) {
					String ruleLine = sourceBr.readLine();
					String[] ruleLineSplit = ruleLine.split("\\t", -1);

					aSink.tableRow();
					for (String string : ruleLineSplit) {
						aSink.tableCell();
						aSink.text(string);
						aSink.tableCell_();
					}
					aSink.tableRow_();
				}
			} else {
				aSink.tableRow();
				aSink.tableCell();
				aSink.text("No data found");
				aSink.tableCell_();
				aSink.tableRow_();
			}
			aSink.table_();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sourceBr != null) {
				try {
					sourceBr.close();
				} catch (IOException e) {
				}
			}
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

	protected org.apache.maven.doxia.siterenderer.Renderer getSiteRenderer() {
		return siteRenderer;
	}

	public String getDescription(Locale locale) {
		return getBundle(locale).getString("report.description");
	}

	public String getName(Locale locale) {
		return getBundle(locale).getString("report.name");
	}

	public String getOutputName() {
		return project.getArtifactId() + "-review-list-report";
	}

	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("review-list-report");
	}

	public boolean usePageLinkBar() {
		return true;
	}

}