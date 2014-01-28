package org.ihtsdo.mojo.module.release.refset;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.rf2.module.refset.factory.RF2SimpleMapRefsetFactory;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;
import org.ihtsdo.rf2.module.util.FilterConfig;
import org.ihtsdo.rf2.module.util.I_amFilter;
import org.ihtsdo.rf2.module.util.JAXBUtil;
import org.ihtsdo.rf2.module.util.TestFilters;

/**
 * @author Varsha Parekh
 * 
 * @goal export-bymod-simple-map-refset
 * @requiresDependencyResolution compile
 */

public class RF2SimpleMapExporterMojo extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * release date.
	 * 
	 * @parameter
	 * @required
	 */
	private String releaseDate;

	/**
	 * previous release date.
	 * 
	 * @parameter
	 * @required
	 */
	private String previousReleaseDate;
	
	/**
	 * Location of the exportFoler.
	 * 
	 * @parameter
	 * @required
	 */
	private String exportFolder;
	
	// for accessing the web service
	/**
	 * endpointURL
	 * 
	 * @parameter
	 * 
	 */
	private String endpointURL;
	
	/**
	 * username
	 * 
	 * @parameter
	 * 
	 */
	private String username;
	
	/**
	 * password
	 * 
	 * @parameter
	 * 
	 */
	private String password;
	
	// for accessing the web service
	
	
	/**
	 * namespaceId
	 * 
	 * @parameter default-value="0"
	 * 
	 */
	private String namespaceId;
	
	/**
	 * partitionId
	 * 
	 * @parameter default-value="00"
	 * 
	 */
	private String partitionId;
	
	/**
	 * executionId
	 * 
	 * @parameter default-value="Daily-build"
	 * 
	 */
	private String executionId;
	
	/**
	 * releaseId
	 * 
	 * @parameter default-value="20110131"
	 * 
	 */
	private String releaseId;
	
	/**
	 * componentType
	 * 
	 * @parameter default-value="Concept"
	 * 
	 */
	private String componentType;

	/**
	 * Filter configurations
	 * 
	 * @parameter
	 * 
	 */
	private ArrayList<FilterConfig> filterConfigs;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		System.setProperty("java.awt.headless", "true");
		try {
			try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), this.getClass(), targetDirectory)) {
					return;
				}
			} catch (NoSuchAlgorithmException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}

			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/simpleMapRefset.xml");

			config.setExportFileName(config.getExportFileName().replace("Full", "Delta").replace("INT_",""));
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			config.setReleaseDate(releaseDate);
			config.setPreviousReleaseDate(previousReleaseDate);
			config.setFlushCount(10000);
			config.setFileExtension("txt");

			if (filterConfigs!=null){
				TestFilters testFilters= new TestFilters();

				for (FilterConfig filterConfig:filterConfigs){
					Class test=Class.forName(filterConfig.className);
					I_amFilter filter= (I_amFilter) test.newInstance();
					if (filterConfig.valuesToMatch!=null){
						filter.setValuesToMatch(filterConfig.valuesToMatch);
					}
					testFilters.addFilter(filter);
				}
				config.setTestFilters(testFilters);
			}			

			//Below Parameters are necessary for ID-Generation
			config.setNamespaceId(namespaceId);
			config.setPartitionId(partitionId);
			config.setExecutionId(executionId);
			config.setReleaseId(releaseId);
			config.setComponentType(componentType);			
			config.setUsername(username);
			config.setPassword(password);
			config.setEndPoint(endpointURL);
			
			// initialize ace framwork and meta hierarchy
			ExportUtil.init(config);
			//Exports snomedid , ctv3id and icdo map
			RF2SimpleMapRefsetFactory factory = new RF2SimpleMapRefsetFactory(config);
			factory.export();

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
}
