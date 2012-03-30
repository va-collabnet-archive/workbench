package org.ihtsdo.rf2.identifier.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.idgeneration.IdAssignmentImpl;
import org.ihtsdo.rf2.identifier.factory.RF2IdListGeneratorFactory;
import org.ihtsdo.rf2.mojo.ReleaseConfigMojo;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.JAXBUtil;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal id-generator-test-connection
 * 
 */
public class RF2TestConnectionIDCreatorMojo extends ReleaseConfigMojo {

	// for accessing the web service
	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String endpointURL;

	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String username;

	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String password;


	//This mojo needs to be used only for replacing sctid with existing uuid
	public void execute() throws MojoExecutionException {			
		final IdAssignmentImpl idGen = new IdAssignmentImpl(endpointURL, username, password);
		long sctId = 0L;

		try {
			sctId = idGen.getSCTID(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			if (sctId==138875005){
				getLog().info("***Web service test connection result: OK****");
			}else{
				getLog().info("***Web service test connection result: OK " + endpointURL + " ****");
				throw new MojoExecutionException("Wrong SCTID for root concept");

			}
		} catch (Exception e) {
			getLog().info("***Web service test connection result: NO connection established****");
			throw new MojoExecutionException("Cannot connect to web service " + endpointURL);
		}

	}


}