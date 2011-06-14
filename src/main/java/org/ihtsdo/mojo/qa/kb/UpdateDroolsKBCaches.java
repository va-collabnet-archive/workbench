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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
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
 * @goal update-drools-kb-cache
 * @phase process-resources
 */
public class UpdateDroolsKBCaches extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;
	
	/**
	 * Location of the build directory.
	 * 
	 * @parameter
	 * @required
	 */
	private File targetSubDir;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + "Update cache", this.getClass(),
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
			contextHelper.clearCache();

			RulesDeploymentPackageReferenceHelper pkgHelper = new RulesDeploymentPackageReferenceHelper(activeConfig);
			for ( RulesDeploymentPackageReference loopPkg : pkgHelper.getAllRulesDeploymentPackages()) {
				loopPkg.updateKnowledgeBase();
			}
			
			for (I_GetConceptData context : contextHelper.getAllContexts()) {
				contextHelper.getKnowledgeBaseForContext(context, activeConfig, true);
			}
			
		} catch (Exception e) {
			throw new MojoFailureException(e.getLocalizedMessage(), e);
		}
	}

	private void copyfile(String srFile, String dtFile){
		try{
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			//For Append the file.
			//  OutputStream out = new FileOutputStream(f2,true);

			//For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		}
		catch(FileNotFoundException ex){
			System.out.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		}
		catch(IOException e){
			System.out.println(e.getMessage());  
		}
	}
}
