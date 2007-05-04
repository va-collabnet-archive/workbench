package org.dwfa.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.dwfa.maven.ExtractAndProcessSpec.SubstutionSpec;

/**
 * Goal which writes configuration files to the output directory.
 * 
 * @goal process-config
 * @requiresDependencyResolution compile
 * 
 */

public class ExtractAndProcessFiles extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * @parameter expression="${project.dependencies}"
	 * @required
	 */
	private List<Dependency> dependencies;

	/**
	 * @parameter expression="${settings.localRepository}"
	 * @required
	 */
	private String localRepository;

	/**
	 * @parameter
	 * @required
	 */
	ExtractAndProcessSpec[] specs;

	public void execute() throws MojoExecutionException, MojoFailureException {
		Log l = getLog();
		UUID randomId = UUID.randomUUID();
		l.info("Now executing ExtractAndProcessFiles: " + Arrays.asList(specs));
		for (ExtractAndProcessSpec spec : specs) {
			Pattern filePattern = Pattern.compile(spec.getFilePatternStr());
			File rootDir = new File(outputDirectory, spec.getDestDir());
			List<RegexReplace> replacers = null;
			char[] cbuf = new char[1024];
			byte[] bbuf = new byte[1024];
			for (Dependency d : dependencies) {
				if (d.getScope().equals("provided")) {
					l.info("Skipping provided: " + d);
					continue;
				} 
				
				if (d.getScope().equals("runtime-directory")) {
					l.info("Skipping runtime-directory: " + d);
					continue;
				} 
				
				
				String dependencyPath = MojoUtil.dependencyToPath(
						localRepository, d);
				try {

					JarFile jf = new JarFile(dependencyPath);

					
					Enumeration<JarEntry> jarEnum = jf.entries();
					while (jarEnum.hasMoreElements()) {
						JarEntry je = jarEnum.nextElement();
						
						Matcher m = filePattern.matcher(je.getName());
						if (m.find()) {
							
							File destFile = new File(rootDir, je.getName().substring(je.getName().lastIndexOf('/') + 1));
							destFile.getParentFile().mkdirs();
							if (je.isDirectory()) {
								destFile.mkdirs();
							} else if (spec.getSubstitutions().length > 0) {
								
								if (replacers == null) {
									replacers = new ArrayList<RegexReplace>(spec.getSubstitutions().length);
									for (SubstutionSpec s: spec.getSubstitutions()) {
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
										replacers.add(new RegexReplace(s
												.getPatternStr(), replacementStr));
									}
								}
								InputStreamReader isr = new InputStreamReader(
										jf.getInputStream(je));
								StringBuffer buff = new StringBuffer();
								int read = isr.read(cbuf);
								while (read > 0) {
									buff.append(cbuf, 0, read);
									read = isr.read(cbuf);
								}
								isr.close();

								FileWriter fw = new FileWriter(destFile);
								String fileAsString = buff.toString();
								for (RegexReplace r: replacers) {
									fileAsString = r.execute(fileAsString);
								}
								
								Reader substuted = new StringReader(fileAsString);
								read = substuted.read(cbuf);
								while (read > 0) {
									fw.write(cbuf, 0, read);
									read = substuted.read(cbuf);
								}
								fw.close();
							} else {
								InputStream is = jf.getInputStream(je);
								destFile.getParentFile().mkdirs();
								OutputStream os = new FileOutputStream(destFile);
								int read = is.read(bbuf);
								while (read > 0) {
									os.write(bbuf, 0, read);
									read = is.read(bbuf);
								}
								is.close();
								os.close();
							}
						}
					}
				} catch (Exception e) {
					throw new MojoExecutionException(e.getMessage() + " path:" + dependencyPath + " spec" + spec, e);
				}
			}
		}
	}
}
