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
package org.dwfa.maven.reporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.doxia.sink.Sink;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.dwfa.maven.transform.IdentityTransformWithXMLCompliantMarkers;

/**
 * 
 * This class is currently generating html directly, it should be refactored to
 * generate
 * xdoc or something else instead
 * 
 * @goal report
 * @phase site
 */

public class MavenTxtReportGenerator extends AbstractMavenReport {

    private static final String XHTML_TAB = "&#160;&#160;&#160;&#160;&#160;";

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
     * @parameter default-value="target/reports"
     * @required
     */
    private File opsDirectory;

    private HashMap<String, List<String>> map = new HashMap<String, List<String>>();
    private HashMap<String, String> headerdata = new HashMap<String, String>();

    private String reportDate;

    public List<File> findFiles(File directory) {

        List filelist = new ArrayList<File>();
        if (directory != null && directory.isDirectory()) {
            /*
             * Get directory listing, and find all extract language files
             * with .xtc extension
             */
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (!name.endsWith("format.txt")) {
                        return true;
                    }
                    return false;
                }
            };
            String[] files = directory.list(filter);
            for (String file : files) {
                File f = new File(directory + "/" + file);
                if (f.getName().endsWith(".txt")) {
                    filelist.add(f);
                } else if (f.isDirectory()) {
                    filelist.addAll(findFiles(f));
                    filelist.add(f);
                }

            }

        }
        Collections.sort(filelist);
        return filelist;
    }

    public void createReportDirectory(File f) throws IOException {

        BufferedReader reader = null;
        int i = 0;
        String addition = "";
        String dirname = f.getAbsolutePath();
        /*
         * Create links to all the children of this directory
         */
        List<String> l = map.get(dirname);
        if (l != null) {
            Collections.sort(l);
            for (String s : l) {
                addition = addition + "<tr><td>";

                boolean bold = false;
                if (s.endsWith("d")) {
                    s = s.substring(0, s.length() - 1);
                    bold = true;
                    addition = addition + "<b>";
                }
                addition = addition + "<a href=\"" + s + "\">" + s.substring(0, s.lastIndexOf("."));
                if (!bold) {
                    if (new File(opsDirectory, s.replaceAll(".html", ".txt") + "format.txt").exists()) {
                        addition = addition + " (" + headerdata.get(s) + ")";
                    }
                }
                addition = addition + "</a>";

                if (bold) {
                    addition = addition + "</b>";
                }
                addition = addition + "</td>" + "</tr>";
            }
        }

        createXDoc(f.getName() + ".xml", f.getName(), addition);
    }

    public void createReportFile(File f) throws IOException {
        IdentityTransformWithXMLCompliantMarkers transform = new IdentityTransformWithXMLCompliantMarkers();

        String filename = f.getName().replaceAll(".txt", ".xml");

        BufferedReader reader = null;
        int i = -1;
        StringBuilder addition = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            boolean empty = (line == null);
            while (line != null) {
                String thisline = line;
                String[] fields = thisline.split("@");

                String header = null;
                line = reader.readLine();
                File format = new File(opsDirectory, f.getName() + "format.txt");
                if (format.exists()) {
                    BufferedReader formatreader = new BufferedReader(new FileReader(format));
                    String formatline = formatreader.readLine();
                    if (formatline != null && formatline.indexOf("header:") != -1) {
                        header = formatline.substring(formatline.indexOf("header: ") + 8);
                    }
                }
                addition.append("<tr>");

                if (line == null && format.exists()) {
                    if (header == null) {
                        header = thisline;
                    }
                    headerdata.put(f.getName().replaceAll(".txt", ".html"), header);
                } else {
                    for (String s : fields) {
                        addition.append("<td>");
                        try {
                            addition.append(transform.transform(s)
                                .replaceAll(":::", "<")
                                .replaceAll("::", ">")
                                .replaceAll("\t", XHTML_TAB));
                        } catch (Exception e) {
                            getLog().info("Cannot transform data to XML compliant text");
                        }
                        addition.append("</td>");
                    }
                }
                if (!empty)
                    addition.append("</tr>");
            }

            createXDoc(filename, f.getName().substring(0, f.getName().lastIndexOf(".")), addition.toString());
        } catch (IOException e) {
            System.out.println("no report file");
        }
    }

    public void executeReport(Locale locale) {

        Sink sink = getSink();

        List<File> filelist = findFiles(opsDirectory);

        reportDate = new Date().toString();

        sink.head();
        sink.title();
        sink.text(reportDate);
        sink.title_();
        sink.head_();
        sink.section1();
        sink.sectionTitle1();
        sink.text(reportDate);
        sink.sectionTitle1_();
        sink.section1_();
        sink.body();

        sink.table();

        File allreports = new File(opsDirectory, "All Reports");
        allreports.mkdirs();
        filelist.add(0, allreports);

        for (File f : filelist) {

            if (!f.isDirectory()) {
                try {
                    createReportFile(f);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String dirname = f.getParentFile().getAbsolutePath();

                List<String> children = map.get(dirname);
                if (children == null) {
                    children = new LinkedList<String>();
                    children.add(f.getName().replaceAll(".txt", ".html"));
                    map.put(dirname, children);
                } else {
                    children.add(f.getName().replaceAll(".txt", ".html"));
                }

                children = map.get(allreports.getAbsolutePath());
                if (children == null) {
                    children = new LinkedList<String>();
                    children.add(f.getName().replaceAll(".txt", ".html"));
                    map.put(allreports.getAbsolutePath(), children);
                } else {
                    children.add(f.getName().replaceAll(".txt", ".html"));
                }

            }
        }

        for (File f : filelist) {
            if (f.isDirectory()) {
                try {
                    createReportDirectory(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String dirname = f.getParentFile().getAbsolutePath();
                if (dirname.equals(opsDirectory.getAbsolutePath())) {
                    sink.tableRow();
                    sink.tableCell();
                    sink.link(f.getName() + ".html");
                    sink.bold();
                    sink.text(f.getName());
                    sink.bold_();
                    sink.link_();
                    sink.tableCell_();
                    sink.tableRow_();

                } else {
                    List<String> children = map.get(dirname);
                    if (children == null) {
                        children = new LinkedList<String>();
                        children.add(f.getName() + ".htmld");
                        map.put(dirname, children);
                    } else {
                        children.add(f.getName() + ".htmld");
                    }
                }

            }
        }

        for (File f : filelist) {
            if (!f.isDirectory()) {
                if (f.getParentFile().getAbsolutePath().equals(opsDirectory.getAbsolutePath())) {
                    sink.tableRow();
                    sink.tableCell();
                    sink.link(f.getName().replaceAll(".txt", ".html"));
                    String sinktext = f.getName().substring(0, f.getName().lastIndexOf("."));
                    if (new File(opsDirectory, f.getName() + "format.txt").exists()) {
                        sinktext = sinktext + "(" + headerdata.get(f.getName().replaceAll(".txt", ".html")) + ")";
                    }
                    sink.text(sinktext);
                    sink.link_();
                    sink.tableCell_();
                    sink.tableRow_();
                }
            }
        }

        sink.table_();

        sink.body_();
        sink.flush();
        sink.close();

    }

    protected MavenProject getProject() {
        return project;
    }

    protected String getOutputDirectory() {
        return outputDirectory;
    }

    protected SiteRenderer getSiteRenderer() {
        return siteRenderer;
    }

    public String getDescription(Locale locale) {
        return getBundle(locale).getString("report.description");
    }

    public String getName(Locale locale) {
        return getBundle(locale).getString("report.name");
    }

    public String getOutputName() {
        return project.getArtifactId() + "-Report";
    }

    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("report", locale, this.getClass().getClassLoader());
    }

    public void createXDoc(String fileName, String siteTitle, String table) {
        StringBuilder xdocXML = new StringBuilder();

        xdocXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xdocXML.append("<document>");
        xdocXML.append("<properties>");
        xdocXML.append("<author email=\"NEHTA.AT.NEHTA.DOT.com.DOT.au\">NEHTA</author>");
        xdocXML.append("<title>" + siteTitle + " - " + reportDate + "</title>");
        xdocXML.append("</properties>");
        xdocXML.append("<meta name=\"keyword\" content=\"jakarta, java\"/>");
        xdocXML.append("<body>");
        xdocXML.append("<section name=\"" + siteTitle + " - " + reportDate + "\">");
        xdocXML.append("<table>");

        xdocXML.append(getHeaderForFile(fileName));

        xdocXML.append(table);
        xdocXML.append("</table>");
        xdocXML.append("</section>");
        xdocXML.append("</body>");
        xdocXML.append("</document>");

        try {
            File file = new File(outputDirectory, fileName);
            System.out.println("created " + file.getAbsolutePath());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(xdocXML.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Customize this method when adding new filetypes which need headers.
     */
    protected String getHeaderForFile(String fileName) {
        return "";
    }
}
