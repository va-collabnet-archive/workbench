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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.model.ReportPlugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.doxia.site.renderer.SiteRenderer;

/**
 * @goal jpgreport
 * @phase site
 */

public class MavenJpgReportGenerator extends AbstractMavenReport {

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
     * @parameter default-value="target/reports"
     * @required
     */
    private File opsDirectory;

    public List<File> findFiles() {
        List filelist = new ArrayList<File>();
        if (opsDirectory != null && opsDirectory.isDirectory()) {
            /*
             * Get directory listing, and find all extract language files
             * with .xtc extension
             */
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jpg");
                }
            };
            String[] files = opsDirectory.list(filter);
            for (String file : files) {
                File f = new File(opsDirectory + "/" + file);
                filelist.add(f);
            }

        }
        return filelist;
    }

    public void executeReport(Locale locale) {

        Sink sink = getSink();

        List<File> filelist = findFiles();

        sink.head();
        sink.title();
        sink.text("Graph Reports");
        sink.title_();
        sink.head_();

        sink.body();

        for (File f : filelist) {

            sink.section1();

            sink.sectionTitle1();
            sink.text(f.getName().substring(0, f.getName().lastIndexOf(".")));
            sink.sectionTitle1_();
            sink.lineBreak();
            sink.lineBreak();
            sink.figure();
            sink.figureGraphics(f.getName());
            sink.figure_();
            sink.lineBreak();
            File textfile = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + ".txt");
            try {
                fileCopy(f);

                BufferedReader textReader = new BufferedReader(new FileReader(textfile));
                String line = textReader.readLine();
                while (line != null) {
                    sink.text(line);
                    line = textReader.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            sink.section1_();

        }

        sink.body_();
        sink.flush();
        sink.close();

    }

    public void fileCopy(File f) throws Exception {
        FileInputStream fis = new FileInputStream(f);
        FileOutputStream fos = new FileOutputStream("target/site/" + f.getName());
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
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
        return project.getArtifactId() + "-JpgReport";
    }

    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("jpgreport", locale, this.getClass().getClassLoader());
    }
}
