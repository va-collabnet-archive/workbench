package org.dwfa.maven;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.dwfa.maven.ExtractAndProcessSpec.SubstutionSpec;
import org.dwfa.util.io.FileIO;

/**
 * Goal which writes configuration files to the output directory.
 * 
 * @goal process-project-dirs
 * @requiresDependencyResolution compile
 * 
 */
public class ProcessProjectDirectories extends AbstractMojo {

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter
     * @required
     */
    ExtractAndProcessSpec[] specs;

    private void addFileMatches(File root, Pattern filePattern, List<File> matches) throws IOException {
        if (root.isDirectory() && (root.getName().equals("target") == false)
                && (root.getName().equals(".svn") == false)) {
            for (File child : root.listFiles()) {
                addFileMatches(child, filePattern, matches);
            }
        } else {
            Matcher m = filePattern.matcher(root.getCanonicalPath().replace('\\', '/'));
            if (m.find()) {
                matches.add(root);
            }
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Log l = getLog();
            l.info("Now executing ProcessProjectDirectories");
            UUID randomId = UUID.randomUUID();
            StringBuffer listbuffer = new StringBuffer();
            for (Object obj : specs) {
                listbuffer.append("\n");
                listbuffer.append(obj);
            }
            String input = listbuffer.toString();
            // l.info(input);
            // calculate the SHA-1 hashcode for this mojo based on input
            if (MojoUtil.alreadyRun(l, input)) {
                return;
            }

            for (ExtractAndProcessSpec spec : specs) {
                Pattern filePattern = Pattern.compile(spec.getFilePatternStr());
                File rootDir = new File(outputDirectory, spec.getDestDir());
                List<RegexReplace> replacers = null;
                List<File> matches = new ArrayList<File>();
                addFileMatches(new File("."), filePattern, matches);
                for (File f : matches) {
                    if (f.isHidden() || f.getName().startsWith(".")) {
                        // ignore hidden files...;
                    } else {
                        File destFile = new File(rootDir, f.getName());
                        if (spec.getRetainDirStructure()) {
                            String nameWithDirectories = f.getCanonicalPath().replaceAll(".*src.main.", "");
                            destFile = new File(rootDir, nameWithDirectories);
                        }
                        destFile.getParentFile().mkdirs();
                        if (spec.getSubstitutions().length > 0) {
                            char[] cbuf = new char[1024];

                            if (replacers == null) {
                                replacers = new ArrayList<RegexReplace>(spec.getSubstitutions().length);
                                for (SubstutionSpec s : spec.getSubstitutions()) {
                                    String replacementStr;
                                    switch (s.getNullAction()) {
                                    case MAKE_UUID:
                                        replacementStr = randomId.toString();
                                        break;
                                    case EMPTY_STRING:
                                        replacementStr = "";
                                        break;
                                    case PROMPT:
                                        throw new UnsupportedOperationException();
                                    case REPLACE_LITERAL:
                                        replacementStr = s.getReplacementStr();
                                        break;
                                    default:
                                        throw new UnsupportedOperationException();
                                    }
                                    if (s.getPropertyName() != null) {
                                        String propValue = System.getProperty(s.getPropertyName());
                                        if (propValue != null) {
                                            replacementStr = propValue;
                                        }
                                    }
                                    replacers.add(new RegexReplace(s.getPatternStr(), replacementStr));
                                }
                            }
                            InputStreamReader isr = new FileReader(f);
                            StringBuffer buff = new StringBuffer();
                            int read = isr.read(cbuf);
                            while (read > 0) {
                                buff.append(cbuf, 0, read);
                                read = isr.read(cbuf);
                            }
                            isr.close();

                            FileWriter fw = new FileWriter(destFile);
                            String fileAsString = buff.toString();
                            for (RegexReplace r : replacers) {
                                fileAsString = r.execute(fileAsString);
                            }

                            Reader substituted = new StringReader(fileAsString);
                            read = substituted.read(cbuf);
                            while (read > 0) {
                                fw.write(cbuf, 0, read);
                                read = substituted.read(cbuf);
                            }
                            fw.close();
                        } else {
                            if (f.equals(destFile) == false) {
                                FileIO.copyFile(f, destFile);
                            }
                        }
                        if (spec.isExecutable()) {
                            if (System.getProperty("os.name").startsWith("Windows") == false) {
                                try {
                                    Runtime.getRuntime().exec(
                                            "chmod a+x " + destFile.getPath());
                                } catch (RuntimeException e) {
                                    // Ignore, may be running on windows, and the permissions
                                    // don't matter there...;
                                }
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }


}