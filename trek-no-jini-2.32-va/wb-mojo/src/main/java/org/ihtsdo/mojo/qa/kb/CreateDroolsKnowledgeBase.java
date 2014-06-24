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
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.TaskFailedException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.context.RulesDeploymentPackageReferenceHelper;

/**
 * The mojo creates a new knowledge base reference and lnks it to a context
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author ALO
 * @goal create-drools-kb
 * @phase process-resources
 */
public class CreateDroolsKnowledgeBase extends AbstractMojo {

	/**
	 * The uuid for the context.
	 * 
	 * @parameter
	 * @required
	 */
	private String[] context_uuid;

	/**
	 * delpoyment package reference name.
	 * 
	 * @parameter
	 */
	private String pkgName;

	/**
	 * delpoyment package reference url.
	 * 
	 * @parameter
	 */
	private String pkgUrl;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;


	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + pkgName, this.getClass(),
						targetDirectory)) {
					return;
				}
			} catch (NoSuchAlgorithmException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame activeConfig = tf.getActiveAceFrameConfig();
			if (activeConfig == null) {
				throw new TaskFailedException("Use the vodb-set-default-config and vodb-set-ace-edit-path goals prior to calling this goal.");
			}
			RulesContextHelper contextHelper = new RulesContextHelper(activeConfig);
			if ((pkgName != null && !pkgName.isEmpty()) && ((pkgUrl != null && !pkgUrl.isEmpty()))) {
				RulesDeploymentPackageReferenceHelper pkgHelper = new RulesDeploymentPackageReferenceHelper(activeConfig);
				RulesDeploymentPackageReference pkg = null;
				for (RulesDeploymentPackageReference loopPkg : pkgHelper.getAllRulesDeploymentPackages()) {
					if (loopPkg.getUrl().equals(pkgUrl) || loopPkg.getName().equals(pkgName)) {
						pkg = loopPkg;
					}
				}

				if (pkg == null) {
					pkg = pkgHelper.createNewRulesDeploymentPackage(pkgName, pkgUrl);
				}
				
				for (String loopUuid : context_uuid) {
					contextHelper.addPkgReferenceToContext(pkg, tf.getConcept(UUID.fromString(loopUuid)));
				}
			}
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}
}
