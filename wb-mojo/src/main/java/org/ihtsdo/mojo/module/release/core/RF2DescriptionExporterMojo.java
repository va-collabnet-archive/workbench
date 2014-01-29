package org.ihtsdo.mojo.module.release.core;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.module.core.factory.RF2DescriptionFactory;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;
import org.ihtsdo.rf2.module.util.FilterConfig;
import org.ihtsdo.rf2.module.util.I_amFilter;
import org.ihtsdo.rf2.module.util.JAXBUtil;
import org.ihtsdo.rf2.module.util.TestFilters;

// TODO: Auto-generated Javadoc
/**
 * The Class RF2DescriptionExporterMojo.
 *
 * @author Alejandro Rodriguez
 * @goal export-bymod-description
 * @requiresDependencyResolution compile
 */

public class RF2DescriptionExporterMojo extends AbstractMojo {

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

	/** Language Code for file names of release. @parameter */
	private String languageCode;
	
	/** Filter configurations. @parameter */
	private ArrayList<FilterConfig> filterConfigs;
	
	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			
			Config config;
			
			if(rF2Format.equals("true"))
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/description.xml");
			else
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/descriptionqa.xml");
			
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
			config.setLanguageCode(languageCode);
			
			// initialize meta hierarchy
			ExportUtil.init(config);

			RF2DescriptionFactory factory = new RF2DescriptionFactory(config);
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

	/**
	 * Gets the release date.
	 *
	 * @return the release date
	 */
	public String getReleaseDate() {
		return releaseDate;
	}

	/**
	 * Sets the release date.
	 *
	 * @param releaseDate the new release date
	 */
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	/**
	 * Gets the export folder.
	 *
	 * @return the export folder
	 */
	public String getExportFolder() {
		return exportFolder;
	}

	/**
	 * Sets the export folder.
	 *
	 * @param exportFolder the new export folder
	 */
	public void setExportFolder(String exportFolder) {
		this.exportFolder = exportFolder;
	}
}
