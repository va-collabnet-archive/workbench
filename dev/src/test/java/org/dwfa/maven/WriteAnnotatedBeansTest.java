package org.dwfa.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ReactorManager;
import org.apache.maven.model.Dependency;
import org.apache.maven.monitor.event.EventDispatcher;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.PlexusContainer;
import org.dwfa.util.bean.BeanList;

public class WriteAnnotatedBeansTest extends TestCase {

	private File outputDirectory = new File("target/" + UUID.randomUUID().toString());
	private MavenSession session;
	private String localRepositoryStr;

	
	protected void setUp() throws Exception {
		super.setUp();
		PlexusContainer container = null;
		Settings settings = null;
		ArtifactRepository localRepository = null;
        EventDispatcher eventDispatcher = null;
		ReactorManager reactorManager = null;
		List goals = Arrays.asList(new String[] { "install", "write-annotated-beans" });
		String executionRootDir = null;
		Properties executionProperties = null;
		Date startTime = null;
		session = new MavenSession(container, settings, localRepository,
                eventDispatcher, reactorManager, goals,
                executionRootDir, executionProperties, startTime);
		String className = BeanList.class.getCanonicalName();
		className = className.replace('.', '/');
		className = "/" + className + ".class";
		System.out.println("BeanList.class: " + className);
		System.out.println("getClass().getResource(BeanList.class.getCanonicalName() + \".class\"): " + 
				getClass().getResource(className));
		localRepositoryStr = getClass().getResource(className).toExternalForm();
		System.out.println("localRepositoryStr: " + localRepositoryStr);
		localRepositoryStr = localRepositoryStr.substring("jar:file:".length());
		System.out.println("localRepositoryStr: " + localRepositoryStr);
		localRepositoryStr = localRepositoryStr.substring(0, localRepositoryStr.indexOf("org/dwfa/foundation"));
		System.out.println("localRepositoryStr: " + localRepositoryStr);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (outputDirectory.exists()) {
			if (outputDirectory.listFiles() != null) {
				for (File f: outputDirectory.listFiles()) {
					f.delete();
				}
			}
			outputDirectory.delete();
		}
	}
	public void testExecute() {
		outputDirectory.mkdirs();
		WriteAnnotatedBeans writer = new WriteAnnotatedBeans();
		Dependency d = new Dependency();
		d.setGroupId("org.dwfa");
		d.setArtifactId("foundation");
		d.setVersion("2.0.1-SNAPSHOT");
		d.setScope("compile");
		List dependencyList = new ArrayList();
		dependencyList.add(d);
		writer.setDependencies(dependencyList);
		writer.setSession(session);
		writer.setOutputDirectory(outputDirectory);
		writer.setLocalRepository(localRepositoryStr);
		try {
			writer.execute();
		} catch (MojoExecutionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (MojoFailureException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

}
