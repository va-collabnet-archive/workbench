package org.dwfa.mojo;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.maven.MojoUtil;
import org.dwfa.vodb.LoadSourcesFromJars;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal berkley-vodb
 * 
 * @phase generate-resources
 * @requiresDependencyResolution compile
 */
public class GenerateBerkeleyVodb extends AbstractMojo {
	private static final String fileSep = System.getProperty("file.separator",
			"/");

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}/classes/berkeley-db"
	 * @required
	 */
	File outputDirectory;

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
	private List<DependencySpec> dependenciesToProcess;

	/**
	 * @parameter
	 */
	private String dataLocationInJar;

	private String[] allowedGoals = new String[] { "install" };

	/**
	 * The maven session
	 * 
	 * @parameter expression="${session}"
	 * @required
	 */
	private MavenSession session;

	public void execute() throws MojoExecutionException {
		getLog().info(" data prefix: " + dataLocationInJar);
		if (MojoUtil.allowedGoal(getLog(), session.getGoals(), allowedGoals)) {
			if ((dataLocationInJar != null) && dependenciesToProcess.size() > 1) {
				throw new MojoExecutionException("Only one dependency is allowed with a data prefix specification");
			}
			String[] args = new String[3];
			int argIndex = 0;
			args[argIndex++] = outputDirectory.getAbsolutePath();
			outputDirectory.mkdirs();
			try {
				Set<String> processSet = new HashSet<String>(
						dependenciesToProcess.size());
				for (DependencySpec ds : dependenciesToProcess) {
					processSet.add(ds.getGroupId() + ds.getArtifactId()
							+ ds.getVersion());
				}
				getLog().info("Here are the dependencies...");

				for (Dependency d : dependencies) {
					getLog().info("Project Dependency: " + d);
					if (processSet.contains(d.getGroupId() + d.getArtifactId()
							+ d.getVersion())) {
						getLog().info("    Process this one: " + d);
						String path = dependencyToPath(d);
						args[argIndex++] = path;
						JarFile jf = new JarFile(path);

						Enumeration<JarEntry> jarEnum = jf.entries();
						while (jarEnum.hasMoreElements()) {
							JarEntry je = jarEnum.nextElement();
							getLog().info(je.toString());
						}
					}
				}

				if (dataLocationInJar == null) {
					LoadSourcesFromJars.main(args);
				} else {
					LoadSourcesFromJars.loadFromSingleJar(args[0], args[1], dataLocationInJar);
				}

			} catch (Exception ex) {
				throw new MojoExecutionException(
						"Error processing dependency. Reason: "
								+ ex.getMessage(), ex);
			}
		}
	}

	public String dependencyToPath(Dependency dep) {
		StringBuffer buff = new StringBuffer();
		buff.append(localRepository);
		buff.append(fileSep);
		buff.append(dep.getGroupId().replace('.', fileSep.charAt(0)));
		buff.append(fileSep);
		buff.append(dep.getArtifactId());
		buff.append(fileSep);
		buff.append(dep.getVersion());
		buff.append(fileSep);
		buff.append(dep.getArtifactId());
		buff.append("-");
		buff.append(dep.getVersion());
		buff.append(".jar");
		return buff.toString();
	}

	public String getDataLocationInJar() {
		return dataLocationInJar;
	}

	public void setDataLocationInJar(String dataLocationInJar) {
		this.dataLocationInJar = dataLocationInJar;
	}
}
