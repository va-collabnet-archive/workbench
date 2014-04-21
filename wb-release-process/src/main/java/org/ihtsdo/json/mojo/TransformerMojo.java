package org.ihtsdo.json.mojo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.json.Transformer;
import org.ihtsdo.rf2.postexport.RF2FileRetrieve;
/**
 * The Class TransformerMojo.
 *
 * @author Alejandro Rodriguez
 * @goal transform-to-json
 * @requiresDependencyResolution compile
 */
public class TransformerMojo extends AbstractMojo {	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;
	/**
	 * rf2 files directory.
	 * 
	 * @parameter
	 * @required
	 */
	String rf2FilesDirectory;
	/**
	 * language code.
	 * 
	 * @parameter
	 * @required
	 */
	String languageCode;
	/**
	 * term type.
	 * 
	 * @parameter
	 * @required
	 */
	String termType;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Transformer transformer=new Transformer();
		transformer.setDefaultLangCode(languageCode);
		if (termType.toLowerCase().equals("fsn")){
			transformer.setDefaultTermType(transformer.fsnType);
		}else{
			transformer.setDefaultTermType(transformer.synType);
		}
		try {
			RF2FileRetrieve rf2Files=new RF2FileRetrieve(rf2FilesDirectory);
			transformer.loadConceptsFile(new File(rf2Files.getSnapshotConceptFile()));
			transformer.loadDescriptionsFile(new File(rf2Files.getSnapshotDescriptionFile()));
			transformer.loadRelationshipsFile(new File(rf2Files.getSnapshotRelationshipFile()));
			transformer.loadRelationshipsFile(new File(rf2Files.getSnapshotStatedRelationshipFile()));
			transformer.loadTextDefinitionFile(new File(rf2Files.getSnapshotTextDefinitionFile()));
			transformer.loadAssociationFile(new File(rf2Files.getSnapshotAssociationFile()));
			transformer.loadAttributeFile(new File(rf2Files.getSnapshotAttributeValueFile()));
			transformer.loadLanguageRefsetFile(new File(rf2Files.getSnapshotLanguageFile()));
			transformer.loadSimpleMapRefsetFile(new File(rf2Files.getSnapshotSimpleMapFile()));
			transformer.loadSimpleRefsetFile(new File(rf2Files.getSnapshotRefsetSimpleFile()));
			
			rf2Files=null;
			transformer.createConceptsJsonFile(targetDirectory.getName() +  "/json/concepts.json");
			transformer.createTextIndexFile(targetDirectory.getName() +  "/json/text-index.json");
			transformer=null;
			System.gc();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
