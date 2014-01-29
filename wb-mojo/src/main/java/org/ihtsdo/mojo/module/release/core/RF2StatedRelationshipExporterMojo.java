package org.ihtsdo.mojo.module.release.core;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.rf2.module.core.factory.RF2StatedRelationshipFactory;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;
import org.ihtsdo.rf2.module.util.FilterConfig;
import org.ihtsdo.rf2.module.util.I_amFilter;
import org.ihtsdo.rf2.module.util.JAXBUtil;
import org.ihtsdo.rf2.module.util.TestFilters;

// TODO: Auto-generated Javadoc
/**
 * The Class RF2StatedRelationshipExporterMojo.
 *
 * @author Alejandro Rodriguez
 * @goal export-bymod-stated-relationship
 * @requiresDependencyResolution compile
 */

public class RF2StatedRelationshipExporterMojo extends AbstractMojo {

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
	
	/**
	 * Location of the rF2Format.
	 * 
	 * @parameter
	 * @required
	 */
	private String rF2Format;
	
	
	//Below Parameters are necessary for ID-Generation

	/** namespaceId. @parameter */
	private String namespaceId;
	
	/** partitionId. @parameter */
	private String partitionId;
	
	/** executionId. @parameter */
	private String executionId;
	
	/** moduleId. @parameter */
	private String releaseId;
	
	/** componentType. @parameter */
	private String componentType;
	
	// for accessing the web service
	/** endpointURL. @parameter */
	private String endpointURL;
	
	/** username. @parameter */
	private String username;
	
	/** password. @parameter */
	private String password;

	/** Filter configurations. @parameter */
	private ArrayList<FilterConfig> filterConfigs;
	
	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
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
			
			Config config;
			
			if(rF2Format.equals("true"))
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/statedrelationship.xml");
			else
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/statedrelationshipqa.xml");
		
			config.setExportFileName(config.getExportFileName().replace("Full", "Delta").replace("INT_",""));
			
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			config.setReleaseDate(releaseDate);
			config.setPreviousReleaseDate(previousReleaseDate);
			config.setRf2Format(rF2Format);
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

			RF2StatedRelationshipFactory factory = new RF2StatedRelationshipFactory(config);
			factory.export();

		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}

	/**
	 * Gets the target directory.
	 *
	 * @return the target directory
	 */
	public File getTargetDirectory() {
		return targetDirectory;
	}

	/**
	 * Sets the target directory.
	 *
	 * @param targetDirectory the new target directory
	 */
	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
}
