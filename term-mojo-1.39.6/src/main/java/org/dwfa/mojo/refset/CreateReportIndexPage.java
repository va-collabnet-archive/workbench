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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.site.renderer.SiteRenderer;

/**
 * Generates a report which creates an index page for the reports in a particular directory.
 * The key feature here is it can handle multiple forms of the same report (eg. reportA.html and reportA.txt)
 * and will create one entry for reports with the same name with separate links to the different formats. 
 * 
 * Inclusion filtering is also provided if it is intended to only pick up reports with a certain prefix (eg. "conflicts-*")
 * This prefix can also be stripped off the name if required.
 *  
 * @phase site
 * @goal create-report-index-page
 */
public class CreateReportIndexPage extends AbstractMavenReport {

	/**
	 * The directory to source the report files to be indexed from.
	 * 
	 * @parameter
	 * @required
	 */
	private File reportDir;
	
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
	 * @parameter default-value="${project.build.directory}/generated-site"
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
	 * If specified, only files/reports that start with text will be included.
	 * 
	 * @parameter 
	 */
	private String prefixFilter;
	
	/**
	 * Used in conjunction with the prefixFilter parameter. If true, the prefix will be removed from the report name
	 * that is display in the index. 
	 * 
	 * @parameter default-value="false"
	 */
	private boolean removePrefix;  
	
	/**
	 * The name of the report file to be created 
	 * 
	 * @parameter
	 */
	private String outputName; 
	
	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		
		ReportIndex reportIdx = createIndex(findFiles(reportDir));
		String reportTitle = "All Conflict Reports - " + new Date().toString();
		
		Sink sink = getSink();
		sink.head();
		sink.title();
		sink.text(reportTitle);
		sink.title_();
		sink.head_();
		sink.section1();
		sink.sectionTitle1();
		sink.text(reportTitle);
		sink.sectionTitle1_();
		sink.section1_();
		sink.body();
		sink.table();
		
		// Create table row entry for report file we found
		for (String reportName : reportIdx.keySet()) {
			
			String linkText = reportName;
			if (prefixFilter != null && removePrefix && reportName.startsWith(prefixFilter)) { 
				linkText = reportName.substring(prefixFilter.length());
			}
			linkText = linkText.replaceAll("-", " ");
			
			sink.tableRow();
			sink.tableCell();
			sink.text(linkText);
			sink.tableCell_();
			List<Report> reportList = reportIdx.get(reportName);
			Collections.sort(reportList);
			for (Report report : reportList) {
				sink.tableCell();
				sink.link("./"  + reportName + "." + report.type);
				sink.bold();					
				sink.text(report.type);
				sink.bold_();
				sink.link_();
				sink.tableCell_();
			}
			sink.tableRow_();				
		}
		
		sink.table_();
		sink.body_();
		sink.flush();
		sink.close();		
	}

	
	private List<File> findFiles(File directory) {
		
		List<File> filelist = new ArrayList<File>();
		
		if (directory != null && directory.isDirectory()) {
			
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					boolean isAccepted = true;
					if (prefixFilter != null) {
						isAccepted &= name.startsWith(prefixFilter);
					}
					return isAccepted;
				}
			};

			
			String[] files = directory.list(filter);
			for (String file : files) {
				File f = new File(directory, file);
				if (f.isDirectory()) {
					filelist.addAll(findFiles(f));
				} else {
					filelist.add(f);
				}
			}
		}
		
		return  filelist;
	}

	private ReportIndex createIndex(List<File> files) {
		ReportIndex index = new ReportIndex();
		for (File f : files) {
			String filename = f.getName();
			String fileExt = filename.substring(filename.lastIndexOf(".") + 1);
			filename = filename.substring(0, filename.lastIndexOf("."));			
			Report newReport = new Report(f, fileExt);
			if (index.containsKey(filename)) {
				index.get(filename).add(newReport);
			} else {
				ArrayList<Report> reportList = new ArrayList<Report>();
				reportList.add(newReport);
				index.put(filename, reportList);
			}
		}
		return index;
	}
	
	@Override
	protected String getOutputDirectory() {
		return this.outputDirectory;
	}


	@Override
	protected MavenProject getProject() {
		return this.project;
	}


	@Override
	protected SiteRenderer getSiteRenderer() {
		return this.siteRenderer;
	}


	public String getDescription(Locale locale) {
		return getBundle(locale).getString("report.description");
	}


	public String getName(Locale locale) {
		return getBundle(locale).getString("report.name");
	}


	public String getOutputName() {
		if (outputName == null) {
			return project.getArtifactId() + "-report-index";	
		} else {
			return outputName;
		}
	}
	
	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle("report", locale, this.getClass()
				.getClassLoader());
	}
	
	private class ReportIndex extends TreeMap<String, List<Report>> {};
	
	private class Report implements Comparable<Report> {
		public File file;
		public String type;
		
		public Report(File file, String type) {
			this.file = file;
			this.type = type;
		}

		public int compareTo(Report o) {			
			return this.type.compareTo(o.type);
		}
	}
}
