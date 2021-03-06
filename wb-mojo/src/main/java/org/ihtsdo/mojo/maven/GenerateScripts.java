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
package org.ihtsdo.mojo.maven;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Goal which generates shell scripts to start the dwfa bundle.
 * 
 * @goal generate-scripts
 */
public class GenerateScripts extends AbstractMojo {
    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     */
    private File outputDirectory;

    /**
     * Location to write the script files.
     * 
     * @parameter
     */
    private String scriptOutputDir;

    /**
     * Location of the jar files.
     * 
     * @parameter
     */
    private String jarDir;

    /**
     * Location of the libraries.
     * 
     * @parameter
     */
    private String libDir;

    /**
     * Location to write the script files.
     * 
     * @parameter
     */
    private String[] scriptNames;

    /**
     * The execution information for this commit operation.
     * 
     * @parameter expression="${mojoExecution}"
     */
    private MojoExecution execution;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    private static final String fileSep = System.getProperty("file.separator", "/");

    public void execute() throws MojoExecutionException {
        Log l = getLog();
        try {
            if (MojoUtil.alreadyRun(l, execution.getExecutionId(), this.getClass(), targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e1) {
            throw new MojoExecutionException(e1.getMessage(), e1);
        } catch (IOException e1) {
            throw new MojoExecutionException(e1.getMessage(), e1);
        }
        if (scriptOutputDir == null) {
            if (libDir == null) {
                l.info("Skipping generate scripts. scriptOutputDir and libDir are null");
                return;
            } else {
                l.info("Skipping generate scripts. scriptOutputDir is null");
                return;
            }
        }

        if (outputDirectory == null) {
            l.info("Skipping generate scripts. outputDirectory is null");
            return;
        }
        l.info("scriptOutputDir: " + scriptOutputDir);
        l.info("outputDirectory: " + outputDirectory);
        l.info("libDir: " + libDir);
        l.info("jarDir: " + libDir);
        if (scriptNames != null) {
            l.info("scriptNames: " + Arrays.asList(scriptNames));
        } else {
            l.info("scriptNames: " + scriptNames);
        }

        File[] jars;
        if (jarDir != null) {
            File jarDirectory = new File(outputDirectory + fileSep + jarDir);
            l.info("jarDirectory 1: " + jarDirectory);
            jars = jarDirectory.listFiles(new FileFilter() {

                public boolean accept(File pathname) {
                    return pathname.getPath().endsWith(".jar");
                }
            });
        } else {
            File jarDirectory = new File(outputDirectory + fileSep + scriptOutputDir + libDir);
            l.info("jarDirectory 2: " + jarDirectory);

            jars = jarDirectory.listFiles(new FileFilter() {

                public boolean accept(File pathname) {
                    return pathname.getPath().endsWith(".jar");
                }
            });
        }

        if (scriptNames == null) {

            startAllScript(jars, "startCore", "start-core.config", "500m", "500m", "128", "Workflow Bundle", false,
                false, false);
            startAllScript(jars, "startJehri", "start-jehri.config", "500m", "500m", "128", "Jehri Bundle", false,
                false, false);
            startAllScript(jars, "startJehriAuthoring", "start-jehri-authoring.config", "500m", "500m", "128",
                "Jehri Authoring Bundle", false, false, false);
            startAllScript(jars, "startJehriCentral", "start-jehri-central.config", "500m", "500m", "128",
                "Jehri Central Bundle", false, false, false);
            startAllScript(jars, "startJehriSatellite", "start-jehri-satellite.config", "500m", "500m", "128",
                "Jehri Satellite Bundle", false, false, false);
            startAllScript(jars, "startAce", "start-ace.config", "700m", "1400m", "128", "Ace Bundle", false, false,
                false);
            startAllScript(jars, "ace", "start-ace-local.config", "700m", "1400m", "128", "Ace Bundle", false, false,
                false);
            startAllScript(jars, "dAce", "start-ace-local.config", "700m", "1400m", "128", "Ace Bundle", false, true,
                false);
            startAllScript(jars, "pAce", "start-ace-local.config", "700m", "1400m", "128", "Ace Bundle", false, false,
                true);
            startAllScript(jars, "dStartJehri", "start-jehri.config", "500m", "500m", "128", "Jehri Bundle", false,
                true, false);
            startAllScript(jars, "pStartJehri", "start-jehri.config", "500m", "500m", "128", "Jehri Bundle", false,
                false, true);
        } else {
            for (String name : scriptNames) {
                l.info("generating script: " + name);
                if (name.equalsIgnoreCase("startCore")) {
                    startAllScript(jars, "startCore", "start-core.config", "500m", "500m", "128", "Workflow Bundle",
                        false, false, false);
                } else if (name.equalsIgnoreCase("startJehri")) {
                    startAllScript(jars, "startJehri", "start-jehri.config", "500m", "500m", "128", "Jehri Bundle",
                        false, false, false);
                } else if (name.equalsIgnoreCase("startAce")) {
                    startAllScript(jars, "startAce", "start-ace.config", "700m", "1400m", "128", "Ace Bundle", false,
                        false, false);
                    startAllScript(jars, "startAceNoNet", "start-ace-local.config", "700m", "1400m", "128", "Ace Bundle",
                        false, false, false);
                } else if (name.equalsIgnoreCase("amtViewer")) {
                    startAllScript(jars, "amtViewer", "start-ace.config", "700m", "1400m", "128", "AMT Viewer", true,
                        false, false);
                } else if (name.equalsIgnoreCase("dAce")) {
                    startAllScript(jars, "dAce", "start-ace-local.config", "700m", "1400m", "128", "Ace Bundle", false,
                        true, false);
                } else if (name.equalsIgnoreCase("pAce")) {
                    startAllScript(jars, "pAce", "start-ace-local.config", "700m", "1400m", "128", "Ace Bundle", false,
                        false, true);
                } else if (name.equalsIgnoreCase("dStartJehri")) {
                    startAllScript(jars, "dStartJehri", "start-jehri.config", "500m", "500m", "128", "Jehri Bundle",
                        false, true, false);
                } else if (name.equalsIgnoreCase("pStartJehri")) {
                    startAllScript(jars, "pStartJehri", "start-jehri.config", "500m", "500m", "128", "Jehri Bundle",
                        false, false, true);
                    startAllScript(jars, "pStartJehri", "start-jehri.config", "500m", "500m", "128", "Jehri Bundle",
                        false, false, true);
                }  else if (name.equalsIgnoreCase("wb")) {
                    startAllScript(jars, "wb", null, "1400m", "1400m", "128", "Workbench Bundle", false, false,
                        false);
                    startAllScript64(jars, "wb", null, "5g", "5g", "128", "Workbench Bundle", false, false,
                        false);
                }  else if (name.equalsIgnoreCase("dWb")) {
                    startAllScript(jars, "dWb", null, "1400m", "1400m", "128", "Workbench Bundle", false, true,
                        false);
                    startAllScript64(jars, "dWb", null, "5g", "5g", "128", "Workbench Bundle", false, true,
                        false);
                }  else if (name.equalsIgnoreCase("pWb")) {
                    startAllScript(jars, "pWb", null, "1400m", "1400m", "128", "Workbench Bundle", false, false,
                        true);
                    startAllScript64(jars, "pWb", null, "5g", "5g", "128", "Workbench Bundle", false, false,
                        true);
                }
            }
        }
    }

    private void startAllScript(File[] jars, String scriptName, String startFileName, String startHeap, String maxHeap, String maxPermGen,
            String xdockName, boolean bundledJre, boolean debug, boolean profile) throws MojoExecutionException {
        writeStartupScripts(jars, scriptName, startFileName, startHeap, maxHeap, maxPermGen,
            xdockName, bundledJre, debug, profile, false);
    }

    private void startAllScript64(File[] jars, String scriptName, String startFileName, String startHeap, String maxHeap, String maxPermGen,
            String xdockName, boolean bundledJre, boolean debug, boolean profile) throws MojoExecutionException {
        writeStartupScripts(jars, scriptName, startFileName, startHeap, maxHeap, maxPermGen,
            xdockName, bundledJre, debug, profile, true);
    }

    /**
     * @param jars
     * @throws MojoExecutionException
     */
    private void writeStartupScripts(File[] jars, String scriptName, String startFileName, String startHeap, String maxHeap, String maxPermGen,
            String xdockName, boolean bundledJre, boolean debug, boolean profile, boolean d64)
            throws MojoExecutionException {

        if (d64) {
            scriptName = scriptName + "64";
        }
        File windowScript = new File(outputDirectory + fileSep + scriptOutputDir + fileSep + scriptName + ".bat");
        File linuxScript = new File(outputDirectory + fileSep + scriptOutputDir + fileSep + scriptName + "Linux.sh");
        File unixScript = new File(outputDirectory + fileSep + scriptOutputDir + fileSep + scriptName + "OsX.sh");
 
        List<Scripter> scripters = new ArrayList<Scripter>();

        scripters.add(new WindowsScripter(windowScript, libDir));
        scripters.add(new PosixScripter(linuxScript, libDir));
        scripters.add(new OSXScripter(unixScript, libDir));

        for (Scripter scripter : scripters) {
            try {
                scripter.writeStartupScript(jars, startFileName, startHeap, maxHeap, maxPermGen, xdockName,
                    bundledJre, debug, profile, d64);
            } catch (IOException e) {
                throw new MojoExecutionException("Error creating script file.", e);
            }
        }

    }

    /**
     * Abstract class that writes startup scripts for the workbench
     * 
     * @author adrian
     * 
     */
    private static abstract class Scripter {

        File scriptFile;
        String libDir;

        protected int indent = 0;

        /**
         * Create a Scripter
         * 
         * @param scriptFile The file to write
         * @param libDir The library directory
         */
        public Scripter(File scriptFile, String libDir) {
            this.scriptFile = scriptFile;
            this.libDir = libDir;
        }

        /**
         * Write a startup script for the workbench
         * 
         * @param jars A list of JAR files to put on the classpath
         * @param startFileName The start file parameter to pass
         * @param startHeap Starting heap size (as per java property value)
         * @param maxHeap Maximum heap size (as per java property value)
         * @param xdockName Apple Dock name
         * @param jiniSecurity Use JINI security?
         * @param bundledJre Have we bundled a JRE?
         * @param debug Do we want to attach a debugger?
         * @param profile Do we want a profiler?
         * @throws IOException Thrown when there is trouble writing to the file
         */
        public void writeStartupScript(File[] jars, String startFileName, String startHeap, String maxHeap, String maxPermGen,
                String xdockName, boolean bundledJre, boolean debug, boolean profile, boolean d64)
                throws IOException {
            // make parent directories
            scriptFile.getParentFile().mkdirs();
            // create file writer
            FileWriter fw = new FileWriter(scriptFile);

            // write java path
            indent += 4;
            if (bundledJre) {
                fw.write("jre.bin.java".replace('.', fileSeparator()));
            } else {
                fw.write("java");
            }
            fw.write(lineContinuance());
            
            if (d64) {
                if (isOSX()) {
                    writeLine(fw, "-d64 -XX:+UseCompressedOops");
                } else {
                    writeLine(fw, "-XX:+UseCompressedOops");
                }
            }

            // write debug options
            if (debug) {
                writeLine(fw, "-ea");
                writeLine(fw, "-Xdebug");
                writeLine(fw, "-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n");
            }

            // write profiling options
            if (profile) {
            	writeFileLine(fw, "-Xbootclasspath/a:/Applications/jprofiler6/bin/agent.jar");
            	writeFileLine(fw, "-agentpath:/Applications/jprofiler6/bin/macos/libjprofilerti.jnilib");
            }

            // write heap size
            writeLine(fw, String.format("-Xms%1$s", startHeap));
            writeLine(fw, String.format("-Xmx%1$s", maxHeap));
            writeLine(fw, "-XX:MaxPermSize=" + maxPermGen + "m");

            // write OS X specific options
            if (isOSX()) {
                writeLine(fw, String.format("-Xdock:name=\"%1$s\"", xdockName));
                writeFileLine(fw, "-Xdock:icon=config/icons/app-icon-256.png");
                writeLine(fw, "-Dapple.laf.useScreenMenuBar=true");
            }

            // write classpath
            indent += 4;
            writeLine(fw, "-cp");
            for (File jar : Arrays.asList(jars)) {
                fw.write(libDir);
                fw.write(fileSeparator());
                fw.write(jar.getName());
                fw.write(pathSeparator());
            }
            writeLine(fw, "");

            // write start class
            indent += 4;
            writeLine(fw, "org.ihtsdo.db.runner.WorkbenchRunner");
            
            // Write config file, if specified.
            if (startFileName != null) {
                fw.write("config");
                fw.write(fileSeparator());
                fw.write(startFileName);
            }
            
            fw.close();

            // change file permissions
            setFilePermissions(scriptFile);
        }

        /**
         * Try to set the executable bit
         * 
         * @param scriptFile The file to set
         * @throws IOException
         */
        private void setFilePermissions(File scriptFile) throws IOException {
            if (System.getProperty("os.name").startsWith("Windows") == false) {
                try {
                    Runtime.getRuntime().exec("chmod a+x " + scriptFile.getPath());
                } catch (RuntimeException e) {
                    // Ignore, may be running on windows, and the permissions
                    // don't matter there...;
                }
            }
        }

        /**
         * Write a line to the file, followed by a line continuation
         * 
         * @param fw A FileWriter
         * @param line The line to write
         * @throws IOException
         */
        private void writeLine(FileWriter fw, String line) throws IOException {
            fw.write(line);
            fw.write(lineContinuance());
        }

        /**
         * Write a line to the file, replacing file separators for the target
         * environment
         * 
         * @param fw A FileWriter
         * @param line The line to write
         * @throws IOException
         */
        private void writeFileLine(FileWriter fw, String line) throws IOException {
            line = line.replace('/', fileSeparator());
            writeLine(fw, line);
        }

        /**
         * Return a string repeated a number of times
         * 
         * @param base The string to repeat
         * @param count The number of times to repeat it
         * @return The repeated string
         */
        protected static String repeat(String base, int count) {
            String rv = "";
            for (int ii = 0; ii < count; ii++) {
                rv += base;
            }
            return rv;
        }

        /**
         * Is the target OSX?
         * 
         * @return true if the target is OSX
         */
        protected boolean isOSX() {
            return false;
        }

        /**
         * Return the correct file separator for the target environment
         * 
         * @return
         */
        protected abstract char fileSeparator();

        /**
         * Return the correct PATH separator for the target environment
         * 
         * @return
         */
        protected abstract String pathSeparator();

        /**
         * Return a line continuance, for those shells that support it
         * or a space for those that don't
         * 
         * @return
         */
        protected abstract String lineContinuance();

    }

    /**
     * Class that writes a startup script for cmd.exe
     * 
     * @author adrian
     * 
     */
    private static class WindowsScripter extends Scripter {

        public WindowsScripter(File scriptFile, String libDir) {
            super(scriptFile, libDir);
        }

        @Override
        protected char fileSeparator() {
            return '\\';
        }

        @Override
        protected String pathSeparator() {
            return ";";
        }

        @Override
        protected String lineContinuance() {
            return " ";
        }

    }

    /**
     * Class that writes a startup script for POSIX shells
     * 
     * @author adrian
     * 
     */
    private static class PosixScripter extends Scripter {

        public PosixScripter(File scriptFile, String libDir) {
            super(scriptFile, libDir);
        }

        @Override
        protected char fileSeparator() {
            return '/';
        }

        @Override
        protected String pathSeparator() {
            return ":";
        }

        @Override
        protected String lineContinuance() {
            return " \\\n" + repeat(" ", indent);
        }
    }

    /**
     * Class that writes a startup script for POSIX shells
     * and also does Mac-y stuff
     * 
     * @author adrian
     * 
     */
    private static class OSXScripter extends PosixScripter {
        public OSXScripter(File scriptFile, String libDir) {
            super(scriptFile, libDir);
        }

        @Override
        protected boolean isOSX() {
            return true;
        }
    }

}
