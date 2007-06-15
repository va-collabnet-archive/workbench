package org.dwfa.maven;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
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
	 * Location of the libraries.
	 * 
	 * @parameter
	 */
	private String libDir;

	private static final String fileSep = System.getProperty("file.separator","/");

	public void execute() throws MojoExecutionException {
		Log l = getLog();
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

		File[] jars = new File(outputDirectory + fileSep + scriptOutputDir
				+ libDir).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getPath().endsWith(".jar");
			}
		});
		/*
		 * if (jars != null) { for (File f: Arrays.asList(jars)) {
		 * System.out.println("lib: " + f); } }
		 */
        startAllScript(jars, "startCore", "start-core.config", "500m", "500m", "Workflow Bundle");
        startAllScript(jars, "startJehri", "start-jehri.config", "500m", "500m", "Jehri Bundle");
        startAllScript(jars, "startAce", "start-ace.config", "1400m", "1400m", "Ace Bundle");
		configureScript(jars);
	}

	/**
	 * @param jars
	 * @throws MojoExecutionException
	 */
	private void startAllScript(File[] jars, String scriptName, String startFileName,
			String startHeap, String maxHeap, String xdocName) throws MojoExecutionException {
		File windowScript = new File(outputDirectory + fileSep
				+ scriptOutputDir + fileSep + scriptName + ".bat");

		try {
			FileWriter fw = new FileWriter(windowScript);
			// fw.write("export
			// DYLD_LIBRARY_PATH=lib/osx:$DYLD_LIBRARY_PATH\n");
			fw.write("java ");
			fw.write("-Xms" + startHeap + " ");
			fw.write("-Xmx" + maxHeap + " ");
			fw.write("-Djava.security.manager=  ");
			fw.write("-Djava.util.logging.config.file=config\\logging.properties ");
			fw.write("-Djava.security.policy=config\\dwa.policy ");
			fw.write("-Djava.security.properties=config\\dynamic-policy.security-properties ");
			fw.write("-Djava.security.auth.login.config=config\\dwa.login ");
			fw.write("-Djavax.net.ssl.trustStore=prebuiltkeys\\truststore ");
			fw.write("-Djava.protocol.handler.pkgs=net.jini.url  ");
             fw.write("-Djava.library.path=lib ");
			fw.write("-cp ");
			for (File f : Arrays.asList(jars)) {
				fw.write(libDir + "\\" + f.getName() + ";");
			}
			fw.write(" ");
			fw.write("     com.sun.jini.start.ServiceStarter config\\" + startFileName + " ");
			fw.close();
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating script file.", e);
		}

		File unixScript = new File(outputDirectory + fileSep + scriptOutputDir
				+ fileSep + scriptName + "OsX.sh");

		try {
			FileWriter fw = new FileWriter(unixScript);
			// fw.write("export
			// DYLD_LIBRARY_PATH=lib/osx:$DYLD_LIBRARY_PATH\n");
			fw.write("java \\\n");
			fw.write("     -Xms" + startHeap + "  \\\n");
			fw.write("     -Xmx" + maxHeap + " \\\n");
			fw.write("     -Djava.security.manager=  \\\n");
			fw.write("     -Xdock:name=\"" + xdocName + "\"  \\\n");
             fw.write("     -Xdock:icon=config/icon/bundle.gif  \\\n");
			fw.write("     -Dapple.laf.useScreenMenuBar=true \\\n");
			fw.write("     -Djava.util.logging.config.file=config/logging.properties \\\n");
			fw.write("     -Djava.security.policy=config/dwa.policy \\\n");
			fw.write("     -Djava.security.properties=config/dynamic-policy.security-properties \\\n");
			fw.write("     -Djava.security.auth.login.config=config/dwa.login \\\n");
			fw.write("     -Djavax.net.ssl.trustStore=config/prebuiltkeys/truststore \\\n");
			fw.write("     -Djava.protocol.handler.pkgs=net.jini.url  \\\n");
			fw.write("     -cp ");
			for (File f : Arrays.asList(jars)) {
				fw.write(libDir + "/" + f.getName() + ":");
			}
			fw.write(" \\\n");
			fw
					.write("     com.sun.jini.start.ServiceStarter config/" + startFileName + " \n");
			fw.close();
			if (System.getProperty("os.name").startsWith("Windows") == false) {
				try {
					Runtime.getRuntime().exec(
							"chmod a+x " + unixScript.getPath());
				} catch (RuntimeException e) {
					// Ignore, may be running on windows, and the permissions
					// don't matter there...;
				}
			}
			File linuxScript = new File(outputDirectory + fileSep + scriptOutputDir
					+ fileSep + scriptName + "Linux.sh");

				fw = new FileWriter(linuxScript);
				// fw.write("export
				// DYLD_LIBRARY_PATH=lib/osx:$DYLD_LIBRARY_PATH\n");
				fw.write("java \\\n");
				fw.write("     -Xms" + startHeap + "  \\\n");
				fw.write("     -Xmx" + maxHeap + " \\\n");
				fw.write("     -Djava.security.manager=  \\\n");
				fw.write("     -Djava.util.logging.config.file=config/logging.properties \\\n");
				fw.write("     -Djava.security.policy=config/dwa.policy \\\n");
				fw.write("     -Djava.security.properties=config/dynamic-policy.security-properties \\\n");
				fw.write("     -Djava.security.auth.login.config=config/dwa.login \\\n");
				fw.write("     -Djavax.net.ssl.trustStore=config/prebuiltkeys/truststore \\\n");
				fw.write("     -Djava.protocol.handler.pkgs=net.jini.url  \\\n");
				fw.write("     -cp ");
				for (File f : Arrays.asList(jars)) {
					fw.write(libDir + "/" + f.getName() + ":");
				}
				fw.write(" \\\n");
				fw
						.write("     com.sun.jini.start.ServiceStarter config/" + startFileName + " \n");
				fw.close();
				if (System.getProperty("os.name").startsWith("Windows") == false) {
					try {
						Runtime.getRuntime().exec(
								"chmod a+x " + unixScript.getPath());
					} catch (RuntimeException e) {
						// Ignore, may be running on windows, and the permissions
						// don't matter there...;
					}
				}
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating script file.", e);
		}
	}

	/**
	 * @param jars
	 * @throws MojoExecutionException
	 */
	private void configureScript(File[] jars) throws MojoExecutionException {
		File unixScript = new File(outputDirectory + fileSep + scriptOutputDir
				+ fileSep + "configure.sh");
		try {
			FileWriter fw = new FileWriter(unixScript);
			// fw.write("export
			// DYLD_LIBRARY_PATH=lib/osx:$DYLD_LIBRARY_PATH\n");
			fw
					.write("/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home/bin/java -jar lib/");
			for (File f : jars) {
				if (f.getName().startsWith("configurator-")) {
					fw.write(f.getName());
					break;
				}
			}
			fw.close();
			if (System.getProperty("os.name").startsWith("Windows") == false) {
				try {
					Runtime.getRuntime().exec(
							"chmod a+x " + unixScript.getPath());
				} catch (RuntimeException e) {
					// Ignore, may be running on windows, and the permissions
					// don't matter there...;
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating script file.", e);
		}
	}
}
