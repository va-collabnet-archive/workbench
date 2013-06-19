/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.mojo.qa.kb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

/**
 * The mojo creates a new knowledge base reference and lnks it to a context
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author ALO
 * @goal download-package-snapshot
 * @phase process-resources
 */
public class DownloadPackageSnapshots extends AbstractMojo {

	/**
	 * delpoyment package reference url.
	 * 
	 * @parameter
	 */
	private String pkgUrl;

	/**
	 * delpoyment package reference name.
	 * 
	 * @parameter expression="inputfiles"
	 */
	private String brlOutputFolder;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	private static final Logger log = Logger.getLogger(DownloadPackageSnapshots.class);

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Sardine sardine = SardineFactory.begin("admin", "admin");
			List<DavResource> resources = sardine.getResources(pkgUrl);
			for (DavResource res : resources) {
				try {
					if (!res.getNameDecoded().trim().equals("") && !res.isDirectory() && !res.getNameDecoded().trim().equals("drools.package")) {
						log.info("Downloading from " + res.getAbsoluteUrl().replaceAll(" ", "%20"));
						InputStream is = sardine.getInputStream(res.getAbsoluteUrl().replaceAll(" ", "%20"));
						File inFolder = new File(brlOutputFolder);
						inFolder.mkdirs();
						File file = new File(inFolder, res.getName());
						file.createNewFile();
						OutputStream os = new FileOutputStream(file);
						int len;
						byte buf[] = new byte[1024];
						while ((len = is.read(buf)) > 0) {
							os.write(buf, 0, len);
						}
						os.close();
						is.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}
}
