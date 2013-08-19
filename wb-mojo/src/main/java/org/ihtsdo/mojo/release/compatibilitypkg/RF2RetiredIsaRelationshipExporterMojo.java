package org.ihtsdo.mojo.release.compatibilitypkg;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.compatibilitypkg.factory.RF2RetiredIsaRelationshipFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.FilterConfig;
import org.ihtsdo.rf2.util.I_amFilter;
import org.ihtsdo.rf2.util.JAXBUtil;
import org.ihtsdo.rf2.util.TestFilters;

/**
 * @author Ale
 * 
 * @goal export-retired-isa-relationship
 * @requiresDependencyResolution compile
 */

public class RF2RetiredIsaRelationshipExporterMojo extends AbstractMojo {

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
	/**
	 * Location of the rF2Format.
	 * 
	 * @parameter
	 * @required
	 */
	private String rF2Format;

	/**
	 * Filter configurations
	 * 
	 * @parameter
	 * 
	 */
	private ArrayList<FilterConfig> filterConfigs;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Config config;
			
			if(rF2Format.equals("true"))
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/retiredisarelationship.xml");
			else
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/retiredisarelationshipqa.xml");
		
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);

			config.setReleaseDate(releaseDate);
			config.setRf2Format(rF2Format);
			config.setFlushCount(10000);
			config.setFileExtension("txt");	
			config.setUsername(username);
			config.setPassword(password);
			config.setEndPoint(endpointURL);
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

			// initialize meta hierarchy
			ExportUtil.init(config);


			RF2RetiredIsaRelationshipFactory factory = new RF2RetiredIsaRelationshipFactory(config);
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
