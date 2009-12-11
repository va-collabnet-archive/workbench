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
package org.dwfa.maven;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.dependency.AbstractDependencyFilterMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;

/**
 * 
 * @goal run-app
 * @requiresDependencyResolution compile
 */
public class ServiceStarter extends AbstractDependencyFilterMojo {

	/**
	 * @parameter
	 * @required
	 */
	private String[] args;

	/**
	 * POM
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 * @required
	 */
	protected MavenProject thisProj;

	/**
	 * @parameter expression="${settings.localRepository}"
	 * @required
	 */
	private String localRepository;

	/**
	 * The maven session
	 * 
	 * @parameter expression="${session}"
	 * @required
	 */
	private MavenSession session;
	
	private String[] allowedGoals = new String[] { "run-app", "org.dwfa:dwfa-mojo:run-app", "dwfa-mojo:run-app" };

	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (MojoUtil.allowedGoal(getLog(), session.getGoals(), allowedGoals)) {
			if (getProject() == null) {
				project = thisProj;
			}
			Set artifacts = getResolvedDependencies(true);

			if (artifacts == null || artifacts.isEmpty()) {
				getLog().info("No dependencies found.");
			}

			List artList = new ArrayList(artifacts);
			try {
				getLog().info(
						"Current classloader: " + this.getClass().getClassLoader());
				getLog().info(
						"Current classloader class: "
								+ this.getClass().getClassLoader().getClass()
										.getCanonicalName());

				final URLClassLoader classLoader = MojoUtil
						.getProjectClassLoader(artList);
				getLog().info("Made classloader: " + classLoader);
				
				
				Thread t = new Thread(new Runnable() {

					public void run() {
						try {

							getLog().info("Started independent thread");
							ClassLoader loader = Thread.currentThread()
									.getContextClassLoader();
							getLog().info("Using classloader: " + loader);
							if (loader != classLoader) {
								getLog().info(
										"Resetting classloader to: " + classLoader);
								Thread.currentThread().setContextClassLoader(
										classLoader);
								loader = Thread.currentThread()
										.getContextClassLoader();
							}
							getLog().info("Using classloader: " + loader);
							Class mvnUtilClass = loader
									.loadClass("org.dwfa.maven.MvnUtil");
							Method setLocalRepositoryMethod = mvnUtilClass.getMethod(
									"setLocalRepository", new Class[] { String.class });
							setLocalRepositoryMethod.invoke(null, new Object[] { localRepository });
							
							

							Class serviceStarterClass = Thread.currentThread()
									.getContextClassLoader().loadClass(
											"com.sun.jini.start.ServiceStarter");
							Method mainMethod = serviceStarterClass.getMethod(
									"main", new Class[] { String[].class });
							getLog().info(
									"ServiceStarter args: " + Arrays.asList(args));
							mainMethod.invoke(null, new Object[] { args });
						} catch (Exception e) {
							getLog().error(e.getMessage(), e);
						}
					}
				});
				t.setContextClassLoader(classLoader);
				getLog().info("Starting independent thread");
				t.run();

				Thread.sleep(Long.MAX_VALUE);
			} catch (IOException e) {
				// getLog().info(e);
				throw new MojoExecutionException(e.getMessage(), e);
			} catch (SecurityException e) {
				// getLog().info(e);
				throw new MojoExecutionException(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				// getLog().info(e);
				throw new MojoExecutionException(e.getMessage(), e);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	protected ArtifactsFilter getMarkedArtifactFilter() {
		// TODO Auto-generated method stub
		return null;
	}


}
